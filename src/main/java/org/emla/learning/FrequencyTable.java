package org.emla.learning;

import java.util.ArrayList;
import java.util.List;

public class FrequencyTable {

	private String predictor;	// attribute / feature (i.e. outlook)
	List<String> targetValues;
	List<Frequency> frequencies;
	
	public FrequencyTable(String predictor, List<String> targetValues) {
		this.predictor = predictor;
		this.targetValues = targetValues;
		this.frequencies = new ArrayList<>();
	}

	public String getPredictor(){return this.predictor;}

	public void addFrequency(Frequency f) {
		this.frequencies.add(f);
	}
	public boolean isFrequenciesEmpty(){return this.frequencies.isEmpty();}
	
	public void updateFrequencies(int allInstances) {
		for (Frequency f : frequencies) {
			f.setCoverage(allInstances); f.updateFrequency();
		}
	}

	//	select Frequency with high coverage and low error
	public Frequency selectFrequencyHighCoverageLowError() {
		
		Frequency f = frequencies.get(0);
		
		for (int i=1; i < frequencies.size(); i++) {
			if (frequencies.get(i).getBestFrequencyAssessment() > f.getBestFrequencyAssessment()) {
				f= frequencies.get(i);
			}
		}
		return f;
		
	}
	
	public String toString() {
		String str = "\nFrequency Table for predictor '" + this.predictor + "':\n";
		for (Frequency f : frequencies) {
			str += f.toString();
		}
		return str;
	}
	
}
