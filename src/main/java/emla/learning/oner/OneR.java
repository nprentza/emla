package emla.learning.oner;

import java.util.ArrayList;
import java.util.List;

import emla.dbcomponent.DbAccess;
import emla.dbcomponent.DatasetAbstract;

public class OneR {

	public static List<FrequencyTable> getFrequencyTables(DatasetAbstract ds, String dataSplit, List<Integer> caseIDs){
		List<FrequencyTable> freuencyTables = new ArrayList<>();

		for (String predictor : ds.getPredictors()) {
			FrequencyTable ft = DbAccess.getFrequencies(predictor, ds.getTargetFeature(), ds.getUniqueTargetValues(), ds.getDatasetName(), dataSplit, caseIDs);
			ft.updateFrequencies(caseIDs!=null ? caseIDs.size() : ds.getDataSplit(dataSplit).rowCount());
			freuencyTables.add(ft);
		}

		return freuencyTables;
	}
	
	public static Frequency getFrequencyHighCoverageLowError(DatasetAbstract ds, String dataSplit) {
		
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
