package org.emla.learning.trees;

public class NumericalValueSplit extends FeatureValueSplit {

    FeatureValueSplit leftSplit;
    FeatureValueSplit rightSplit;

    public NumericalValueSplit(String featureName, Double splitPoint, FeatureValueSplit leftSplit, FeatureValueSplit rightSplit){
        super(featureName, splitPoint);
        this.leftSplit = leftSplit;
        this.rightSplit = rightSplit;
        this.coverage = leftSplit.getCoverage() + rightSplit.getCoverage();
        this.calculateEntropy();
        this.prediction = (leftSplit.getEntropy() < rightSplit.getEntropy()) ? leftSplit.getPrediction() : rightSplit.getPrediction();
        this.operator = null;
    }

    public void calculateEntropy() {
        this.entropy=0;
        if (leftSplit!=null && rightSplit!=null){
            this.entropy = ( (double) leftSplit.getCoverage()/this.coverage) * leftSplit.getEntropy() +
                    ( (double) rightSplit.getCoverage()/this.coverage) * rightSplit.getEntropy();
        }
    }

    @Override
    public String toString(){
        return "IF "+ leftSplit.getFeatureName() + " " + leftSplit.getOperator() + " " + leftSplit.getValue() + " THEN " + leftSplit.getPrediction() +
                "\nIF " + rightSplit.getFeatureName() + " " + rightSplit.getOperator() + " " + rightSplit.getValue() + " THEN " + rightSplit.getPrediction();
    }
}
