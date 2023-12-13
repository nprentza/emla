package org.emla.learning.oner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.emla.dbcomponent.Dataset;
import org.emla.dbcomponent.DbAccess;
import org.emla.learning.LearningUtils;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Table;

public class OneR {

	public static List<FrequencyTable> getFrequencyTables(Dataset ds, String dataSplit, List<Integer> caseIDs){
		List<FrequencyTable> freuencyTables = new ArrayList<>();

		for (String predictor : ds.getPredictors()) {
			//	if predictor is a string / text attribute then call DbAccess.getFrequencies
			if (ds.getDsTable().column(predictor).type()== ColumnType.STRING || ds.getDsTable().column(predictor).type()== ColumnType.TEXT){
				freuencyTables.add(frequencyTableCategoricalFeature(predictor,ds,dataSplit,caseIDs));
			}	// else calculate split-points and find frequencies for range of values
			else if (ds.getDsTable().column(predictor).type()== ColumnType.DOUBLE || ds.getDsTable().column(predictor).type()== ColumnType.INTEGER){
				freuencyTables.add(frequencyTableNumericalFeature(predictor,ds,dataSplit,caseIDs));
			}
			/*FrequencyTable ft = DbAccess.getFrequencies(predictor, ds.getTargetFeature(), ds.getUniqueTargetValues(), ds.getDatasetName(), dataSplit, caseIDs);
			ft.updateFrequencies(caseIDs!=null ? caseIDs.size() : ds.getDataSplit(dataSplit).rowCount());
			freuencyTables.add(ft);*/
		}
		return freuencyTables;
	}

	private static FrequencyTable frequencyTableCategoricalFeature(String featureName, Dataset ds, String dataSplit, List<Integer> caseIDs){
		FrequencyTable ft = DbAccess.getFrequencies(featureName, ds.getTargetFeature(), ds.getUniqueTargetValues(), ds.getDatasetName(), dataSplit, caseIDs);
		ft.updateFrequencies(caseIDs!=null ? caseIDs.size() : ds.getDataSplit(dataSplit).rowCount());

		return ft;
	}

	private static FrequencyTable frequencyTableNumericalFeature(String featureName, Dataset ds, String dataSplit, List<Integer> caseIDs){
		Dataset dsFiltered = (caseIDs == null) ? ds : new Dataset(ds,caseIDs) ;
		Table data = dataSplit!=null ? dsFiltered.getDataSplit(dataSplit) : dsFiltered.getDsTable();
		data = data.sortAscendingOn(featureName);
		List<Double> splitPoints = LearningUtils.numericalSplitPoints(data,featureName,ds.getTargetFeature());
		FrequencyTable freqTable = new FrequencyTable(featureName, ds.getUniqueTargetValues());
		 /*
            for each split point i:
                calculate frequencies for the range [split-point(i-1), split-point(i))
         */
		double lowValue, highValue;
		Table dataSelection = null;
		if (splitPoints.size()==0){	// all datapoints of the same class
			lowValue = data.row(0).getNumber(featureName);
			highValue = data.row(data.rowCount()).getNumber(featureName);
			dataSelection  = filterSelection(data, featureName, lowValue, highValue,true,true);
			freqTable.addFrequency(numericalFrequency(featureName,
					LearningUtils.getDataPointsByClass(dataSelection,ds.getTargetFeature(),ds.getUniqueTargetValues()),
					lowValue, highValue,LearningUtils.Operator.GREATER_OR_EQUAL, LearningUtils.Operator.LESS_OR_EQUAL,data.rowCount()));
		}else {
			// calculate frequency for ranges
			// 		[splitPoint(0),splitPoint(1)), [splitPoint(1),splitPoint(2)), .., [splitPoint(N-2),splitPoint(N-1)]
			lowValue = data.row(0).getNumber(featureName);
			for (int i=0; i<splitPoints.size();i++){
				highValue = splitPoints.get(i);
				dataSelection  = filterSelection(data, featureName, lowValue, highValue,
						true, false);
				freqTable.addFrequency(numericalFrequency(featureName,
						LearningUtils.getDataPointsByClass(dataSelection,ds.getTargetFeature(),ds.getUniqueTargetValues()),
						lowValue, highValue, LearningUtils.Operator.GREATER_OR_EQUAL, LearningUtils.Operator.LESS_THAN, data.rowCount()));
				lowValue = splitPoints.get(i);
			}
			//	calculate frequency for last split
			highValue = data.row(data.rowCount()-1).getNumber(featureName);
			dataSelection  = filterSelection(data, featureName, lowValue, highValue, true,true);
			freqTable.addFrequency(numericalFrequency(featureName,
					LearningUtils.getDataPointsByClass(dataSelection,ds.getTargetFeature(),ds.getUniqueTargetValues()),
					lowValue, highValue, LearningUtils.Operator.GREATER_OR_EQUAL, LearningUtils.Operator.LESS_OR_EQUAL,data.rowCount()));
		}
		return freqTable;
	}

	private static Table filterSelection(Table data, String featureName, Double lowValue, Double highValue, boolean leftValueIncluded, boolean rightValueIncluded){
		Table dataFiltered=null;
		if (data.column(featureName).type()==ColumnType.INTEGER){
			IntColumn intColumn = data.intColumn(featureName);
			dataFiltered = data.
					where(leftValueIncluded ? intColumn.isGreaterThanOrEqualTo(lowValue) : intColumn.isGreaterThan(lowValue)).
					where(intColumn.isLessThan(highValue)); 	// where(rightValueIncluded ? intColumn.isLessThanOrEqualTo(highValue) : intColumn.isLessThan(highValue));
		} else if (data.column(featureName).type()==ColumnType.DOUBLE) {
			DoubleColumn doubleColumn = data.doubleColumn(featureName);
			dataFiltered = data.
					where(leftValueIncluded ? doubleColumn.isGreaterThanOrEqualTo(lowValue) : doubleColumn.isGreaterThan(lowValue)).
					where(doubleColumn.isLessThan(highValue)); // where(rightValueIncluded ? doubleColumn.isLessThanOrEqualTo(highValue) : doubleColumn.isLessThan(highValue));
		}
		return dataFiltered;
	}

	private static Frequency numericalFrequency(String featureName, Map<String, Integer> dataPointsByClass, double splitPointLow, double splitPointHigh,
												LearningUtils.Operator leftOperator, LearningUtils.Operator rightOperator, int dataSize){
		Frequency f = new Frequency(featureName);
		f.addOperatorValue(leftOperator,splitPointLow);
		f.addOperatorValue(rightOperator,splitPointHigh);
		dataPointsByClass.entrySet().forEach( e -> f.addFrequency(e.getKey(),e.getValue()));
		f.updateFrequency(dataSize);
		return f;
	}
	
	public static Frequency getFrequencyHighCoverageLowError(Dataset ds, String dataSplit) {
		
		List<FrequencyTable> freuencyTables = getFrequencyTables(ds, dataSplit, null);
		
		Frequency f = freuencyTables.get(0).selectFrequencyHighCoverageLowError();
		
		for (int i=1; i<freuencyTables.size(); i++) {
			if (freuencyTables.get(i).selectFrequencyHighCoverageLowError().getBestFrequencyAssessment() > f.getBestFrequencyAssessment()) {
				f = freuencyTables.get(i).selectFrequencyHighCoverageLowError();
			}
		}
		
		return f;
	}

	public static Frequency getFrequencyHighCoverageLowError(List<FrequencyTable> freuencyTables) {

		Frequency f = freuencyTables.get(0).selectFrequencyHighCoverageLowError();

		for (int i=1; i<freuencyTables.size(); i++) {
			if (freuencyTables.get(i).selectFrequencyHighCoverageLowError().getBestFrequencyAssessment() > f.getBestFrequencyAssessment()) {
				f = freuencyTables.get(i).selectFrequencyHighCoverageLowError();
			}
		}
		return f;
	}


}
