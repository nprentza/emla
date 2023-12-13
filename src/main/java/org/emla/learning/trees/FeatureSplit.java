package org.emla.learning.trees;

import tech.tablesaw.api.ColumnType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FeatureSplit {
    String featureName;
    ColumnType featureType;
    List<FeatureValueSplit> featureValueSplits;
    double weightedEntropy;

    public enum Operator {
        EQUALS("=="),
        GREATER_THAN(">"),
        LESS_THAN("<"),
        GREATER_OR_EQUAL(">="),
        LESS_OR_EQUAL("<=");

        private String value;

        Operator(final String value){
            this.value=value;
        }

        public String getValue(){
            return value;
        }

        @Override
        public String toString(){
            return this.getValue();
        }
    }

    public FeatureSplit(String featureName,ColumnType featureType) {
        this.featureName = featureName;
        this.featureType = featureType;
        this.featureValueSplits = new ArrayList<>();
    }

    public void addFeatureValueSplit(FeatureValueSplit featureValueSplit){
        this.featureValueSplits.add(featureValueSplit);
    }

    public void setWeightedEntropy(int dataSize) {
        if (featureType == ColumnType.TEXT || featureType == ColumnType.STRING){
            weightedEntropy = featureValueSplits.stream().mapToDouble(e -> (((double) e.getCoverage() / dataSize)) * e.getEntropy()).sum();
        }else if (featureType == ColumnType.INTEGER || featureType == ColumnType.DOUBLE){
            weightedEntropy = featureValueSplits.stream().mapToDouble(fvs -> fvs.getEntropy()).min().orElse(-1);
        }
    }

    public double getWeightedEntropy() {
        return this.weightedEntropy;
    }

    public String getFeatureName() {
        return this.featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public String toString() {
        return "Feature " + this.featureName + ", weighted entropy=" + this.weightedEntropy;
    }

    public FeatureValueSplit featureValueSplitMinEntropy(){
        List<FeatureValueSplit> sortedFeatureValueSplits = this.featureValueSplits.stream()
                .sorted(Comparator.comparing(FeatureValueSplit::getEntropy))
                .collect(Collectors.toList());
        return sortedFeatureValueSplits.get(0);
    }
}
