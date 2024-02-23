package org.emla.learning.oner;

import tech.tablesaw.api.ColumnType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

		// sort frequencies by assessment, highest first
		if (frequencies.size()>0){
			frequencies.sort(Comparator.comparing(Frequency::getBestFrequencyAssessment).reversed());
			return frequencies.get(0);
		}else{
			return null;
		}
	}

	//	select Frequency with high coverage and low error for a particular target-class
	public Frequency selectFrequencyHighCoverageLowError(String targetClass) {

		List<Frequency> freqsForTargetClass = frequencies.stream().filter(fr -> fr.getMajorityTargetClass().equals(targetClass)).collect(Collectors.toList());
		if (freqsForTargetClass.isEmpty()){
			return null;
		}else{
			freqsForTargetClass.sort(Comparator.comparing(Frequency::getBestFrequencyAssessment).reversed());
			return freqsForTargetClass.get(0);
		}
	}
	
	public String toString() {
		String str = "\n** Frequency Table for predictor '" + this.predictor + "':\n";
		frequencies.sort(Comparator.comparing(Frequency::getBestFrequencyAssessment).reversed());
		for (Frequency f : frequencies) {
			str += f.toString();
		}
		return str;
	}
	
}
