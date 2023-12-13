package org.emla.learning.trees;

public class CategoricalValueSplit extends FeatureValueSplit{

    public CategoricalValueSplit(String featureName, FeatureSplit.Operator op, String value) {
        super(featureName, op, value);
    }
}
