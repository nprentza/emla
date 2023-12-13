package org.emla.learning.trees;

import org.emla.learning.LearningUtils;
import java.util.Map;

public class FeatureValueSplit<T>{
    String featureName;
    FeatureSplit.Operator operator;
    T value;
    double entropy;
    int coverage;
    String prediction;

    public FeatureValueSplit(String featureName, T value){
        this.featureName = featureName;
        this.value = value;
    }

    public FeatureValueSplit(String featureName, FeatureSplit.Operator op, T value){
        this.featureName = featureName;
        this.operator = op;
        this.value = value;
    }

    public FeatureValueSplit(String featureName, FeatureSplit.Operator op, T value, Map<String, Integer> dataPointsByClass, double entropy){
        this.featureName = featureName;
        this.operator = op;
        this.value = value;
        this.entropy = entropy;
        this.coverage = dataPointsByClass.values().stream().mapToInt(v->v).sum();
        this.prediction = LearningUtils.classMaxDatapoints(dataPointsByClass);
    }

    public void setFeatureName(String featureName) {this.featureName=featureName;}
    public String getFeatureName() {return this.featureName;}

    public void setValue(T value){this.value=value;}
    public T getValue() {return this.value;}

    public void setOperator(FeatureSplit.Operator operator){this.operator=operator;}
    public FeatureSplit.Operator getOperator(){return this.operator;}

    public void setEntropy(double entropy){this.entropy=entropy;}
    public double getEntropy(){return entropy;}

    public void setCoverage(int coverage){this.coverage=coverage;}
    public int getCoverage(){return this.coverage;}

    public void setPrediction(String prediction){this.prediction=prediction;}
    public String getPrediction(){return this.prediction;}

    public String toString(){
        return this.featureName + " " + this.operator + " " + this.value;
    }
}
