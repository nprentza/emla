package emla.learning;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import emla.dbcomponent.Dataset;
import emla.learning.oner.Frequency;
import emla.learning.oner.FrequencyTable;

public class TestOneR {

	
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
}
