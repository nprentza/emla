/*
    assumptions: target feature (column) is categorical - of type "String"
 */
package org.emla.learning.trees;

import org.emla.dbcomponent.Dataset;
import org.emla.learning.LearningUtils;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.api.TextColumn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DecisionTreeBasics extends DecisionTreesAbstract {
    Dataset data;
    double initialEntropy;
    List<FeatureSplit> featureSplits;

    public DecisionTreeBasics(Dataset data){
        this.data = data;
        this.initialEntropy = LearningUtils.entropy(data.getDsTable(),data.getUniqueTargetValues(),data.getTargetFeature());
        this.featureSplits = new ArrayList<>();
    }

    public double getInitialEntropy(){return this.initialEntropy;}

    public List<FeatureSplit> candidateSplitsRootNode(){
        //  for each feature calculate a FeatureSplit object
        this.data.getPredictors().forEach(featureName ->{
            this.featureSplits.add(calculateFeatureSplit(featureName));
        });
        //  sort FeatureSplits by entropy, low entropy --> high Information Gain
        List<FeatureSplit> sortedFeatureSplits = this.featureSplits.stream()
                .sorted(Comparator.comparing(FeatureSplit::getWeightedEntropy))
                .collect(Collectors.toList());
        this.featureSplits = sortedFeatureSplits;
        return sortedFeatureSplits;
    }

    private FeatureSplit calculateFeatureSplit(String featureName){
        ColumnType columnType = data.getDsTable().column(featureName).type();
        if (ColumnType.INTEGER.equals(columnType) || ColumnType.DOUBLE.equals(columnType)){
            return featureSplitAsNumber(featureName, columnType);
        }
        else if (ColumnType.STRING.equals(columnType) || ColumnType.TEXT.equals(columnType)) {
            return featureSplitAsString(featureName, columnType);
        }else {return null;}
    }

    private FeatureSplit featureSplitAsNumber(String featureName, ColumnType featureType){
        FeatureSplit featureSplit = new FeatureSplit(featureName, featureType);
        List<Double> splitPoints = LearningUtils.numericalSplitPoints(data.getDsTable(),featureName,this.data.getTargetFeature());
        /*
            for each split point:
                calculate a FeatureValueSplit for feature < split-point and feature>= split-point
         */
        splitPoints.forEach(splitPoint ->{
            featureSplit.addFeatureValueSplit(calculateNumericalFeatureSplit(featureName, featureType, splitPoint));
                });

        featureSplit.setWeightedEntropy(data.getRowCout());
        return featureSplit;
    }

    private NumericalValueSplit calculateNumericalFeatureSplit(String featureName, ColumnType featureType, double splitPoint){
        Table tSelectionLessThan = null;
        Table tSelectionGreaterOrEqualThan = null;

        if (featureType==ColumnType.INTEGER){
            IntColumn intColumn = data.getDsTable().intColumn(featureName);
            tSelectionLessThan = data.getDsTable().where(intColumn.isLessThan(splitPoint));
            tSelectionGreaterOrEqualThan = data.getDsTable().where(intColumn.isGreaterThanOrEqualTo(splitPoint));
        }else if (featureType==ColumnType.DOUBLE) {
            DoubleColumn doubleColumn = data.getDsTable().doubleColumn(featureName);
            tSelectionLessThan = data.getDsTable().where(doubleColumn.isLessThan(splitPoint));
            tSelectionGreaterOrEqualThan = data.getDsTable().where(doubleColumn.isGreaterThanOrEqualTo(splitPoint));
        }

        Map<String, Integer> dataPointsByClassLessThan = getDataPointsByClass(tSelectionLessThan,data.getTargetFeature(),data.getUniqueTargetValues());;
        Map<String, Integer> dataPointsByClassGreaterOrEqualThan = getDataPointsByClass(tSelectionGreaterOrEqualThan,data.getTargetFeature(),data.getUniqueTargetValues());;

        FeatureValueSplit leftFeatureValueSplit = new FeatureValueSplit(featureName, FeatureSplit.Operator.LESS_THAN, splitPoint,dataPointsByClassLessThan,
                LearningUtils.entropy(tSelectionLessThan,data.getUniqueTargetValues(),data.getTargetFeature()));
        FeatureValueSplit rightFeatureValueSplit = new FeatureValueSplit(featureName, FeatureSplit.Operator.GREATER_OR_EQUAL, splitPoint,dataPointsByClassGreaterOrEqualThan,
                LearningUtils.entropy(tSelectionGreaterOrEqualThan,data.getUniqueTargetValues(),data.getTargetFeature()));

        return new NumericalValueSplit(featureName,splitPoint, leftFeatureValueSplit, rightFeatureValueSplit);
    }

    private FeatureSplit featureSplitAsString(String featureName, ColumnType featureType){
        FeatureSplit featureSplit = new FeatureSplit(featureName, featureType);
        List<String> distinctFeatureValues = this.getUniqueFeatureValues(data,featureName);
        if (featureType==ColumnType.STRING){
            StringColumn strColumn = data.getDsTable().stringColumn(featureName);
            for (String distinctFeatureValue : distinctFeatureValues) {
                Table tSelection = data.getDsTable().where(strColumn.isEqualTo(distinctFeatureValue));
                Map<String, Integer> dataPointsByClass = getDataPointsByClass(tSelection,data.getTargetFeature(),data.getUniqueTargetValues());
                double entropy = LearningUtils.entropy(tSelection,data.getUniqueTargetValues(),data.getTargetFeature());
                featureSplit.addFeatureValueSplit(calculateCategoricalFeatureSplit(featureName,distinctFeatureValue, dataPointsByClass, entropy));
            }
        }else if (featureType==ColumnType.DOUBLE){
            TextColumn txtColumn = data.getDsTable().textColumn(featureName);
            for (String distinctFeatureValue : distinctFeatureValues) {
                Table tSelection = data.getDsTable().where(txtColumn.isEqualTo(distinctFeatureValue));
                Map<String, Integer> dataPointsByClass = getDataPointsByClass(tSelection,data.getTargetFeature(),data.getUniqueTargetValues());
                double entropy = LearningUtils.entropy(tSelection,data.getUniqueTargetValues(),data.getTargetFeature());
                featureSplit.addFeatureValueSplit(calculateCategoricalFeatureSplit(featureName,distinctFeatureValue, dataPointsByClass, entropy));
            }
        }
        featureSplit.setWeightedEntropy(data.getRowCout());
        return featureSplit;
    }

    private CategoricalValueSplit calculateCategoricalFeatureSplit(String featureName, String value, Map<String, Integer> dataPointsByClass, double entropy ){
        CategoricalValueSplit categoricalValueSplit = new CategoricalValueSplit(featureName, FeatureSplit.Operator.EQUALS, value);
        categoricalValueSplit.setCoverage(dataPointsByClass.values().stream().mapToInt(v -> v).sum());
        categoricalValueSplit.setPrediction(LearningUtils.classMaxDatapoints(dataPointsByClass));
        categoricalValueSplit.setEntropy(entropy);

        return categoricalValueSplit;
    }

}
