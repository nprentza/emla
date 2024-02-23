package org.emla.learning;

import org.emla.dbcomponent.Dataset;
import org.emla.learning.oner.Frequency;
import org.emla.learning.oner.FrequencyTable;
import org.emla.learning.oner.OneR;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.ColumnType;

import java.util.Arrays;
import java.util.List;

public class TestOneR {

	@Test
	public void testSequentialCovering(){
		Dataset ds = new Dataset("./src/test/resources/dataAccessPolicy.csv", "dataacess", "grantaccess", 1, 0);
		List<Frequency> rules = OneR.sequentialCovering(ds,"train");

		System.out.println("\n\n List of frequencies (rules) selected:");
		rules.forEach(r -> System.out.println(r.toString()));
	}

	@Test
	public void testLoadDataset() {
		
		Dataset ds = new Dataset("./src/test/resources/playtennis.csv", "playtennis", "play", 1, 0);
		LearningSession emlaSession = new LearningSession(ds,"test");
		List<FrequencyTable> frequencyTables = emlaSession.calculateFrequencyTables(ds, "train",null);

		frequencyTables.forEach(f -> System.out.println(f.toString()));

		System.out.println("\n\n ** BEST FREQUENCY ** ");
		Frequency f = emlaSession.calculateFrequencyHighCoverageLowError(ds, "train");
		System.out.println(f.toString() + "\n*************************************************************************************");
		
		/*
		 * 	simulate a refinement of data and re-calculation of frequency tables
		 */
		List<Integer> caseIDs = Arrays.asList(1,4,8,12,10);
		List<FrequencyTable> frequencyTables_R = emlaSession.calculateFrequencyTables(ds, "train", caseIDs);
		
		frequencyTables_R.forEach(f_R -> System.out.println(f_R.toString()));
		
		System.out.println("\n\n ** BEST FREQUENCY ** ");
		Frequency f_R = emlaSession.calculateFrequencyHighCoverageLowError(frequencyTables_R);
		System.out.println(f_R.toString()+ "\n*************************************************************************************");
	}

	@Test
	public void testNumericalFeatures() {
		// Dataset ds = new Dataset("./src/test/resources/agentRequests.csv", "agents", "access", 1, 0);
		// LearningSession emlaSession = new LearningSession(ds,"agents");
		Dataset ds = new Dataset("./src/test/resources/medical_access_1.csv", "medical", "access", 1, 0);
		LearningSession emlaSession = new LearningSession(ds,"medical");
		List<FrequencyTable> frequencyTables = emlaSession.calculateFrequencyTables(ds, "train",null);
		frequencyTables.forEach(ft -> System.out.println(ft.toString()));

		Frequency fHighCovLowError_Numeric = emlaSession.calculateFrequencyHighCoverageLowError(frequencyTables, ColumnType.INTEGER);
		if (fHighCovLowError_Numeric!=null){
			System.out.println("** fHighCovLowError_Numeric **\n    " + fHighCovLowError_Numeric.toString());
		}
	}
}
