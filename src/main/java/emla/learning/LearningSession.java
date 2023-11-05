package emla.learning;

import java.text.DecimalFormat;
import java.util.List;

import emla.dbcomponent.DbAccess;
import emla.dbcomponent.Dataset;
import emla.learning.oner.Frequency;
import emla.learning.oner.FrequencyTable;
import emla.learning.oner.OneR;

public class LearningSession {

	private Dataset ds;
	private String sessionName;

	// session constants (parameters)
	public static DecimalFormat df;
		
	public LearningSession(Dataset ds, String sessionName) {
		this.ds = ds;
		this.sessionName = sessionName;
		initSession();
	}
	
	private void initSession() {
		
		DbAccess.initiateConnection();
		DbAccess.createDbTable(ds);
		df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		
	}
	
	public List<FrequencyTable> calculateFrequencyTables(Dataset ds, String dataSplit) {
		return OneR.getFrequencyTables(ds, dataSplit);
	}
	
	public Frequency calculateFrequencyHighCoverageLowError(Dataset ds, String dataSplit) {
		return OneR.getFrequencyHighCoverageLowError(ds, dataSplit);
		
	}

	public Dataset copyData(Dataset td, List<Integer> caseIDs) {
		Dataset copy = new Dataset(td,caseIDs);
		return copy;
	}
	
}
