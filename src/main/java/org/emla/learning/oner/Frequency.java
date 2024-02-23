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
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.emla.learning.LearningSession;
import org.emla.learning.LearningUtils;

public class Frequency {

	String featureName;
	List<Pair<LearningUtils.Operator, Object>> operatorValue;   // i.e. (==, "sunny") or {(>=,30),(<50)}
	double coverage;
	Map<String, Integer> dataPointsByClass;	//	for each target value (i.e. play-golf {yes,no}), the number of instances that satisfy this condition
	String caseIDs;	// list of caseIDs supported by frequency
	String majorityTargetClass;
	double majorityTargetClassError;
	double bestFrequencyAssessment;

	public Frequency(String featureName, LearningUtils.Operator operator, Object value, String caseIDs) {
		this.featureName = featureName;
		this.operatorValue = new ArrayList<>();
		this.dataPointsByClass = new HashMap<>();
		this.addOperatorValue(operator,value);
		this.caseIDs = caseIDs;
	}

	public Frequency(String featureName){
		this.featureName = featureName;
		this.operatorValue = new ArrayList<>();
		this.dataPointsByClass = new HashMap<>();
	}

	public void addOperatorValue(LearningUtils.Operator operator, Object value){
		this.operatorValue.add(Pair.of(operator,value));
	}

	public List<String> getFrequencyConditions(){
		return this.operatorValue.stream()
				.map(operatorValue -> this.featureName + " " + operatorValue.getLeft().toString() + " " + operatorValue.getRight().toString())
				.collect(Collectors.toList());
	}

	public List<Pair<LearningUtils.Operator, Object>>  getOperatorValuePairs(){
		return this.operatorValue;
	}

	public String getFeatureName(){return this.featureName;}

	public String getMajorityTargetClass(){return this.majorityTargetClass;}

	public String getCaseIDs(){return this.caseIDs;}

	private void setCoverage(int allInstances) {
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
		//	desired properties : high coverage, low error (high accuracy)
		bestFrequencyAssessment =  this.coverage * (1 - majorityTargetClassError);
	}

	public double getBestFrequencyAssessment() {return this.bestFrequencyAssessment;}
	
	public String toString() {
		
		String str="\n>> " + this.getFrequencyConditions().stream().collect(Collectors.joining(", ")) //+ featureName + predictorValuesToString()
				+ " (target=" + this.majorityTargetClass + "), (coverage=" + LearningSession.df.format(coverage)
					+ ", accuracy=" + LearningSession.df.format(1-this.majorityTargetClassError)
					+ ", assessment=" + LearningSession.df.format(this.bestFrequencyAssessment) + ") "
					+ "(caseIDs= " + this.caseIDs + ")" + ":\n";

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

	/*  */
	public static class FrequencyCondition {

		LearningUtils.Operator operator;
		Object value;

		public FrequencyCondition(LearningUtils.Operator op, Object val){
			this.operator = op;
			this.value = val;
		}

		public Pair<LearningUtils.Operator, Object> pairOf(){
			return Pair.of(operator,value);
		}
	}
}
