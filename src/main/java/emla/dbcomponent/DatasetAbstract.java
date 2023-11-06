package emla.dbcomponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.selection.Selection;

public abstract class DatasetAbstract {
	
	private static final Logger logger = LogManager.getLogger(DatasetAbstract.class);
	
	
	Table dsTable;
	String datasetName;
	String dsFilepath;
	String targetFeature;
	double trainSplit=0.8;		// default
	double testSplit=0.2;		// default
	List<String> initializationFeatures;
	String[] notFeatures = {"caseID", "datasplit"};	// exclude from learning
	private HashMap<Integer, String> dataArgs;		// caseID to {argNo} association
	private HashMap<String, List<Object>> uniqueValuesPerFeature;	// we need this to calculate "OneR" rules
	

	/*
	 * constructors
	 */
	
	public DatasetAbstract(DatasetAbstract that, List<Integer> caseIDs) {
		trainSplit = that.trainSplit;
		testSplit = that.testSplit;
		initDataset(that.dsFilepath, that.datasetName, that.targetFeature);
		
		// restrict data to record in caseIDs
		List<Row> caseIDRows = new ArrayList<>();
		caseIDs.forEach(caseId ->{
			Selection s = ((IntColumn) dsTable.column("caseID")).isEqualTo(caseId);
			if (dsTable.where(s).rowCount()==1) {caseIDRows.add(dsTable.where(s).row(0));}
		});
		this.dsTable = this.dsTable.dropRange(dsTable.rowCount());
		caseIDRows.forEach(row -> dsTable.append(row));
	}
	
	public DatasetAbstract(String dsFilepath, String dsName, String targetFeature) {
		initDataset(dsFilepath, dsName, targetFeature);
	}
	
	public DatasetAbstract(String dsFilepath, String dsName, String targetFeature, List<String> initializationFeatures) {
		this.initializationFeatures=initializationFeatures;
		initDataset(dsFilepath, dsName, targetFeature);
	}
	
	public DatasetAbstract(String dsFilepath, String dsName, String targetFeature, double trainPercSplit, double testPercSplit) {
		trainSplit = trainPercSplit;
		testSplit = testPercSplit;
		initDataset(dsFilepath, dsName, targetFeature);
	}
	
	public DatasetAbstract(String dsFilepath, String dsName, String targetFeature, double trainPercSplit, double testPercSplit, List<String> initializationFeatures) {
		trainSplit = trainPercSplit;
		testSplit = testPercSplit;
		this.initializationFeatures=initializationFeatures;
		initDataset(dsFilepath, dsName, targetFeature);
	}


	private void initDataset(String dsFilepath, String dsName, String targetFeature) {
		this.dsFilepath = dsFilepath;
		this.datasetName = dsName;
		this.targetFeature = targetFeature;
		this.loadDataset();
		this.dataArgs = new HashMap<>();
	}
	
	/*
	 * 	getters / setters
	 */
	public Table getDsTable() {
		return dsTable;
	}

	public void setDsTable(Table dsTable) {
		this.dsTable = dsTable;
	}

	public String getDatasetName() {
		return datasetName;
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	public String getTargetFeature() {
		return targetFeature;
	}

	public void setTargetFeature(String targetFeature) {
		this.targetFeature = targetFeature;
	}

	public List<String> getInitializationFeatures() {
		return initializationFeatures;
	}

	public void setInitializationFeatures(List<String> initializationFeatures) {
		this.initializationFeatures = initializationFeatures;
	}

	public String[] getNotFeatures() {
		return notFeatures;
	}

	public void setNotFeatures(String[] notFeatures) {
		this.notFeatures = notFeatures;
	}

	public HashMap<Integer, String> getDataArgs() {
		return dataArgs;
	}

	public void setDataArgs(HashMap<Integer, String> dataArgs) {
		this.dataArgs = dataArgs;
	}
	
	public List<String> getPredictors(){
		List<String> predictors = this.dsTable.columnNames();
		for (String attribute : this.notFeatures) {
			predictors.remove(attribute);
		}
		// remove targetColumn
		if (predictors.contains(this.getTargetFeature())){predictors.remove(this.getTargetFeature());}
		return predictors;
	}
	
	public List<String> getPredictos(boolean initTheory){
		
		if (initTheory) {	// return the special list (i.e. most important features) for theory initialization
			return this.initializationFeatures;
		}else {
			return getPredictors();
		}
		
	}
	
	public List<String> getColumnNames(){
		return dsTable.columnNames();
	}
	
	public int getRowCout() {return this.dsTable.rowCount();}

	
	public Table getDataSplit(String dataSplit) {
		Selection s = ((StringColumn) dsTable.column("datasplit")).isEqualTo(dataSplit);
		return dsTable.where(s);
	}
	
	public String dataRowToCommaSeparatedString(int rowIndex) {
		String str="";
		for (int c=0; c<this.dsTable.columnCount(); c++) {
			if (this.dsTable.column(c).type()==ColumnType.STRING || this.dsTable.column(c).type()==ColumnType.TEXT) {
				str += "'" + this.dsTable.get(rowIndex, c) + "',";
			}else {str += this.dsTable.get(rowIndex, c) + ",";}
		}
		return str.substring(0,str.length()-1);
	}
	
	/*
	 * 	public methods
	 */
	public void associateRuleWithData(List<Integer> caseIDs, String ruleName) {
		for (Integer caseId : caseIDs) {
			if (this.dataArgs.get(caseId)!=null) {
				// update
				String existingRules = this.dataArgs.get(caseId);
				this.dataArgs.replace(caseId, existingRules + "," + ruleName);
			} else {
				// new entry
				this.dataArgs.put(caseId, ruleName);
			}
		}
	}
	
	/*
	 * 	protected methods
	 */
	protected void loadDataset() {
		
		Table temp = Table.read().csv(dsFilepath);
		this.dsTable = temp.dropRowsWithMissingValues(); 
		this.preprocessing();
		
		if (!this.dsTable.containsColumn("datasplit")) {
			// create column and split data into train and test
			StringColumn cDatasplit = StringColumn.create("datasplit");
			dsTable.addColumns(cDatasplit);
			
			// filter rows by value, split in train and test
			Table uTable = dsTable.emptyCopy(0).dropRowsWithMissingValues();
			
			this.getUniqueTargetValues().forEach(targetValue ->{
				Selection s = ((StringColumn) dsTable.column(targetFeature)).isEqualTo((String) targetValue);
				int trainRowsCount = (int) (dsTable.where(s).rowCount() * trainSplit);
				Table[] splitResults = dsTable.where(s).sampleSplit((double)trainRowsCount/dsTable.where(s).rowCount());
				Table trainSample = splitResults[0];
				Table testSample = splitResults[1];

				trainSample.forEach(row ->{
					row.setString("datasplit", "train"); uTable.append(row);
				});
				testSample.forEach(row ->{
					row.setString("datasplit", "test"); uTable.append(row);
				}); 
			});
			
			this.dsTable = uTable;
		}
		
		logger.debug("Dataset.loadDataset(), rowCount()=" + dsTable.rowCount());
	}
	
	

	@SuppressWarnings("unchecked")
	public List<String> getUniqueTargetValues(){
		return (List<String>) dsTable.column(targetFeature).asList().
				stream().distinct().collect(Collectors.toList());
	}
	
	// add caseID
	private void preprocessing() {
		// caseID
		if (!this.dsTable.containsColumn("caseID")) {
			IntColumn cCaseID = IntColumn.create("caseID");
			this.dsTable.addColumns(cCaseID);
			logger.debug("Dataset.preprocessing():" + dsTable.rowCount());
			for (int i=0; i<dsTable.rowCount(); i++) {
			dsTable.row(i).setInt("caseID", i+1);
			}
		}else { // make sure caseID is of type int
			if (this.dsTable.column("caseID").type() != ColumnType.INTEGER) {
				this.dsTable.column("caseID").setName("caseID_");
				IntColumn cCaseID = IntColumn.create("caseID");
				this.dsTable.addColumns(cCaseID);
				for (int i=0; i<dsTable.rowCount(); i++) {
					dsTable.row(i).setInt("caseID", Integer.parseInt(dsTable.getString(i, "caseID_")));
				}
				this.dsTable.removeColumns("caseID_");
			}
		}
		//
		this.tragetColumnProcessing() ;
		this.featureColumnsProcessing();
	}
	
	// target feature values should be lower case string	(Prolog requirement)
	private void tragetColumnProcessing() {
		for (int i=0; i<this.dsTable.rowCount(); i++) {
			String classLabel = this.dsTable.row(i).getString(targetFeature);
			this.dsTable.row(i).setString(targetFeature, classLabel.toLowerCase());
		}
	}
	
	// lowercase all feature columns (exclude caseID, datasplit)
	private void featureColumnsProcessing() {
		String colName;
		for (int i=0; i<this.dsTable.columnCount(); i++) {
			if (!this.dsTable.column(i).name().trim().equals("caseID")) {
				colName = this.dsTable.column(i).name();
				this.dsTable.column(i).setName(colName.toLowerCase());
			}
		}
	}
	
	//
	public void printTable() {
		System.out.println(this.dsTable.print());
	}
		
}
