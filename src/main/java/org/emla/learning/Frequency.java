/*
 * Frequency class currently holds a condition that supports only the "equality" operator
 * 	
 */
package org.emla.learning;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

public class Frequency {
	
	Pair<String,Object> predictorValue;	//	i.e. (outlook, sunny)
	double predictorValueCoverage;
	Map<String, Integer> frequencies;	//	for each target value (i.e. play-golf {yes,no}), the number of instances that satisfy this condition
	String bestTargetValue;
	double bestTargetError;
	int allFrequencyIsntances;
	double bestFrequencyAssessment;


	public Frequency(String predictor, String value) {
		predictorValue = Pair.of(predictor,value);
		this.frequencies = new HashMap<>();
	}

	public Pair<String,Object> getPredictorValues(){
		return this.predictorValue;
	}

	public void setCoverage(int allInstances) {
		allFrequencyIsntances = frequencies.values().stream().mapToInt(d-> d).sum();
		this.predictorValueCoverage = (double) allFrequencyIsntances /(double) allInstances;
	}
	
	public double getCoverage() {return this.predictorValueCoverage;}
	
	public void addFrequency(String value, int instances) {
		this.frequencies.put(value, instances);
	}
	
	//	calculate "best" target value (bestFrequencyValue) and corresponding error
	public void updateFrequency() {
		//	set bestTargetValue
		int instances=0;
		for (String value : frequencies.keySet()) {
			if (frequencies.get(value)>instances) {
				bestTargetValue=value;
				instances=frequencies.get(value);}
		}
		//	set bestTargetError
		int allOtherInstances = frequencies.entrySet().stream().filter(entry -> !entry.getKey().equals(bestTargetValue)).mapToInt(entry -> entry.getValue()).sum();
		bestTargetError = (double) allOtherInstances / allFrequencyIsntances;
		bestFrequencyAssessment = this.predictorValueCoverage * (1-bestTargetError);
	}
	
	public Map<String, Integer> getFrequencies(){
		return this.frequencies;
	}

	public double getBestTargetError() {return this.bestTargetError;}
	public String getBestTargetValue() {return this.bestTargetValue;}
	
	public double getBestFrequencyAssessment() {return this.bestFrequencyAssessment;}
	
	public String toString() {
		
		String str="\n>> for " + predictorValuesToString() + " (Coverage= " + LearningSession.df.format(predictorValueCoverage)  + " ) "
		+ " (Best target value = " + this.bestTargetValue + ", with error=" +  LearningSession.df.format(this.bestTargetError) +  " ) " + " ) :\n";

		for (Entry<String, Integer> entry : frequencies.entrySet()) {
			str += "	[" + entry.getKey() + ", " + entry.getValue() + "]";
		}
		
		return str;
	}
	
	private String predictorValuesToString() {
		String condition="";
		
		condition += condition.length()==0 ?  predictorValue.getLeft() + "=" + predictorValue.getRight() :  "," + predictorValue.getLeft() + "=" + predictorValue.getRight();

		return condition;
			
	}
}
