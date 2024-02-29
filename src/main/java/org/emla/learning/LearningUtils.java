package org.emla.learning;

import org.apache.commons.lang3.tuple.Pair;
import org.emla.learning.oner.Frequency;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LearningUtils {

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

    public static double log2(double N) {

        // calculate log2 N indirectly
        // using log() method
       return (Math.log(N) / Math.log(2));

    }

    public static double entropy(Table dsTable, List<String> targetValues, String targetFeature){
        HashMap<String, Integer> dataPointsPerClass = new HashMap<>();  // # of data-points per target value
        targetValues.forEach(value -> {
            dataPointsPerClass.put(value,
                    dsTable.stream().filter(row -> row.getString(targetFeature).equals(value)).collect(Collectors.toList()).size());
        });
        // if all datapoints belong to the same class then return 0
        if (dataPointsSingleClass(dataPointsPerClass)){
            return 0;
        }else {
            double entropy= dataPointsPerClass.values().stream().mapToDouble(value -> ((double) value / dsTable.rowCount())
                * LearningUtils.log2((double) value / dsTable.rowCount())).sum();
            return (entropy * -1);
        }
    }

    public static HashMap<String, Integer> dataPointsByClass(Table data, String featureName, List<String> targetValues){
        HashMap<String, Integer> dataByClass = new HashMap<>();

        targetValues.forEach(value -> {dataByClass.put(value,
                data.stream().filter(row -> row.getString(featureName).equals(value)).collect(Collectors.toList()).size());
        });

        return dataByClass;
    }

    private static boolean dataPointsSingleClass(Map<String, Integer> dataPointsPerClass){
        return (dataPointsPerClass.values().stream().filter( v -> v==0).collect(Collectors.toList()).size()==1);
    }

    public static String classMaxDatapoints(Map<String, Integer> dataPointsByClass){
        return dataPointsByClass.entrySet()
                .stream()
                .max(Comparator.comparing(Map.Entry::getValue)).get().getKey();
    }

    public static List<Double> numericalSplitPoints(Table data, String featureName, String targetFeatureName){
        Table tableSorted = data.sortAscendingOn(featureName);
        ColumnType cType = data.column(featureName).type();
        List<Double> splits = new ArrayList<>();

        for (int i=1; i<tableSorted.rowCount(); i++){
            if (!tableSorted.row(i).getString(targetFeatureName).equals(tableSorted.row(i-1).getString(targetFeatureName))){
                // calculate a split
                if (cType == ColumnType.DOUBLE){
                    splits.add((tableSorted.row(i).getDouble(featureName) + tableSorted.row(i-1).getDouble(featureName)) /2 );
                }else if (cType == ColumnType.INTEGER){
                    splits.add( ((double) (tableSorted.row(i).getInt(featureName) + tableSorted.row(i-1).getInt(featureName))/2));
                }
            }
        }
        return splits.stream().distinct().collect(Collectors.toList());
    }

    public static List<Pair<Frequency.FrequencyCondition, Frequency.FrequencyCondition>> splitPointsToConditions(List<Double> splitPoints){
        List<Pair<Frequency.FrequencyCondition, Frequency.FrequencyCondition>> conditions = new ArrayList<>();
        Arrays.sort(splitPoints.toArray());

        if (splitPoints.size()>0){
            conditions.add(Pair.of(null,new Frequency.FrequencyCondition(Operator.LESS_OR_EQUAL,splitPoints.get(0))));
            if (splitPoints.size()>1){
                for (int i=1; i<splitPoints.size(); i++){
                    Frequency.FrequencyCondition left = new Frequency.FrequencyCondition(Operator.GREATER_THAN,splitPoints.get(i-1));
                    Frequency.FrequencyCondition right = new Frequency.FrequencyCondition(Operator.LESS_OR_EQUAL,splitPoints.get(i));
                    conditions.add(Pair.of(left,right));
                }
                conditions.add(Pair.of(new Frequency.FrequencyCondition(Operator.GREATER_THAN,splitPoints.get(splitPoints.size()-1)),null));
            }
        }

        return conditions;
    }

    public static List<Pair<Frequency.FrequencyCondition, Frequency.FrequencyCondition>> samePointsToConditions(Double low, Double high){
        List<Pair<Frequency.FrequencyCondition, Frequency.FrequencyCondition>> conditions = new ArrayList<>();
        if (low==high){
            conditions.add(Pair.of(null,new Frequency.FrequencyCondition(Operator.EQUALS,low)));
        }else{
            conditions.add(Pair.of(new Frequency.FrequencyCondition(Operator.GREATER_OR_EQUAL,low),
                    new Frequency.FrequencyCondition(Operator.LESS_OR_EQUAL,high)));
        }
        return conditions;
    }

    public static Map<String, Integer> getDataPointsByClass(Table data, String targetFeatureName, List<String> targetValues){
        Map<String, Integer> dataByClass = new HashMap<>();

        targetValues.forEach(value -> {dataByClass.put(value,
                data.stream().filter(row -> row.getString(targetFeatureName).equals(value)).collect(Collectors.toList()).size());
        });

        return dataByClass;
    }
}
