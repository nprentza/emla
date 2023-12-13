/*
 * Frequency class currently holds a condition that supports only the "equality" operator
 * 	
 */
package org.emla.learning.oner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.emla.learning.LearningSession;
import org.emla.learning.LearningUtils;

public class Frequency {

	String featureName;
	List<Pair<LearningUtils.Operator, Object>> operatorValue;   // i.e. (==, "sunny") or {(>=,30),(<50)}

	//Pair<String,Object> predictorValue;	//	i.e. (outlook, sunny)
	double coverage;
	Map<String, Integer> dataPointsByClass;	//	for each target value (i.e. play-golf {yes,no}), the number of instances that satisfy this condition
	String majorityTargetClass;
	double majorityTargetClassError;
	double bestFrequencyAssessment;

	// TODO remove this
	public Frequency(String featureName, LearningUtils.Operator operator, String value) {
		//predictorValue = Pair.of(predictor,value);
		this.featureName = featureName;
		this.operatorValue = new ArrayList<>();
		this.dataPointsByClass = new HashMap<>();
		this.addOperatorValue(operator,value);
	}

	public Frequency(String featureName){
		this.featureName = featureName;
		this.operatorValue = new ArrayList<>();
		this.dataPointsByClass = new HashMap<>();
	}

	public void addOperatorValue(LearningUtils.Operator operator, Object value){
		this.operatorValue.add(Pair.of(operator,value));
	}

	/*public Pair<String,Object> getPredictorValues(){
		return this.predictorValue;
	}*/

	public void setCoverage(int allInstances) {
		this.coverage = (double) dataPointsByClass.values().stream().mapToInt(d-> d).sum() /(double) allInstances;
	}
	
	public double getCoverage() {return this.coverage;}
	
	public void addFrequency(String value, int instances) {
		this.dataPointsByClass.put(value, instances);
	}

	public void setFrequencies(Map<String, Integer> dataPointsByClass){this.dataPointsByClass=dataPointsByClass;}
	
	//	calculate majority target class (majorityTargetClass) and corresponding error
	public void updateFrequency(int allInstances) {
		this.setCoverage(allInstances);
		//	set bestTargetValue
		int instances=0;
		for (String value : dataPointsByClass.keySet()) {
			if (dataPointsByClass.get(value)>instances) {
				majorityTargetClass=value;
				instances=dataPointsByClass.get(value);}
		}
		//	set bestTargetError
		int allOtherInstances = dataPointsByClass.entrySet().stream().filter(entry -> !entry.getKey().equals(majorityTargetClass)).mapToInt(entry -> entry.getValue()).sum();
		majorityTargetClassError = (double) allOtherInstances / dataPointsByClass.values().stream().mapToInt(d-> d).sum();
		bestFrequencyAssessment = this.coverage * (1-majorityTargetClassError);
	}

	public double getBestFrequencyAssessment() {return this.bestFrequencyAssessment;}
	
	public String toString() {
		
		String str="\n>> for " + predictorValuesToString() + " (Coverage= " + LearningSession.df.format(coverage)  + " ) "
		+ " (Best target value = " + this.majorityTargetClass + ", with error=" +  LearningSession.df.format(this.majorityTargetClassError) +  " ) " + " ) :\n";

		for (Entry<String, Integer> entry : dataPointsByClass.entrySet()) {
			str += "	[" + entry.getKey() + ", " + entry.getValue() + "]";
		}
		
		return str;
	}
	
	private String predictorValuesToString() {
		String condition="";

		// List<Pair<LearningUtils.Operator, Object>> operatorValue

		for (Pair<LearningUtils.Operator, Object> opValue : operatorValue){
			condition += condition.length()==0 ? opValue.getLeft().toString() + opValue.getRight().toString() : " AND "
					+ opValue.getLeft().toString() + opValue.getRight().toString();
		}
		// condition += condition.length()==0 ?  predictorValue.getLeft() + "=" + predictorValue.getRight() :  "," + predictorValue.getLeft() + "=" + predictorValue.getRight();

		return condition;
			
	}
}
