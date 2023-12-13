package org.emla.learning;

import org.emla.dbcomponent.Dataset;
import org.emla.learning.trees.DecisionTreeBasics;
import org.emla.learning.trees.FeatureSplit;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestDecisionTreeBasics {

    @Test
    public void testRootNode() {
        Dataset ds = new Dataset("./src/test/resources/agentRequests3.csv", "agents", "access", 1, 0);
        DecisionTreeBasics dt = new DecisionTreeBasics(ds);
        System.out.println("Initial entropy: " + dt.getInitialEntropy());
        List<FeatureSplit> sortedFeatureSplits = dt.candidateSplitsRootNode();
        System.out.println("== candidate feature splits ==");
        sortedFeatureSplits.forEach(featureSplit -> {System.out.println(" >> " + featureSplit.toString());});
        System.out.println("Feature that gives highest Information Gain is " + sortedFeatureSplits.get(0).getFeatureName() +
                ":\n " + sortedFeatureSplits.get(0).featureValueSplitMinEntropy().toString());
    }
}
