package org.emla.learning;

import org.emla.dbcomponent.Dataset;
import org.emla.dbcomponent.DbAccess;
import org.emla.learning.oner.Frequency;
import org.emla.learning.oner.FrequencyTable;
import org.emla.learning.oner.OneR;
import org.emla.learning.trees.DecisionTreeBasics;
import org.emla.learning.trees.FeatureSplit;

import java.text.DecimalFormat;
import java.util.List;

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

	/*
		learning methods for OneR algorithm
	 */
	public List<FrequencyTable> calculateFrequencyTables(Dataset ds, String dataSplit, List<Integer> caseIDs) {
		return OneR.getFrequencyTables(ds, dataSplit,caseIDs);
	}
	
	public Frequency calculateFrequencyHighCoverageLowError(Dataset ds, String dataSplit) {
		return OneR.getFrequencyHighCoverageLowError(ds, dataSplit);
	}

	public Frequency calculateFrequencyHighCoverageLowError(List<FrequencyTable> frequencyTables) {
		return OneR.getFrequencyHighCoverageLowError(frequencyTables);
	}

	/*
		learning methods for Trees algorithms
	 */

	public List<FeatureSplit> calculateSplitPoints(Dataset ds, String dataSplit, List<Integer> caseIDs){
		Dataset data = new Dataset(ds, caseIDs);
		DecisionTreeBasics dt = new DecisionTreeBasics(ds);
		List<FeatureSplit> sortedFeatureSplits = dt.candidateSplitsRootNode();
		return sortedFeatureSplits;
	}

}
