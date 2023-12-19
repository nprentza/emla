package org.emla.learning.oner;

import tech.tablesaw.api.ColumnType;

import java.util.ArrayList;
import java.util.List;

public class FrequencyTable {

	private String predictor;	// attribute / feature (i.e. outlook)
	private ColumnType featureType;
	List<String> targetValues;
	List<Frequency> frequencies;
	
	public FrequencyTable(String predictor, ColumnType featureType, List<String> targetValues) {
		this.predictor = predictor;
		this.featureType = featureType;
		this.targetValues = targetValues;
		this.frequencies = new ArrayList<>();
	}

	public String getPredictor(){return this.predictor;}

	public void setFeatureType(ColumnType featureType){this.featureType=featureType;}
	public ColumnType getFeatureType(){return this.featureType;}

	public void addFrequency(Frequency f) {
		this.frequencies.add(f);
	}
	public boolean isFrequenciesEmpty(){return this.frequencies.isEmpty();}
	
	public void updateFrequencies(int allInstances) {
		for (Frequency f : frequencies) {
			f.updateFrequency(allInstances);
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
		String str = "\n** Frequency Table for predictor '" + this.predictor + "':\n";
		for (Frequency f : frequencies) {
			str += f.toString();
		}
		return str;
	}
	
}
