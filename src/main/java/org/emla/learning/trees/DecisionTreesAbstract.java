package org.emla.learning.trees;

import org.emla.dbcomponent.Dataset;
import org.emla.learning.LearningUtils;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class DecisionTreesAbstract {

    //  only for text / string column types
    protected List<String> getUniqueFeatureValues(Dataset data, String columnName) {
        if (data.getDsTable().column(columnName).type()== ColumnType.STRING ||
                            data.getDsTable().column(columnName).type()== ColumnType.TEXT){
            return (List<String>) data.getDsTable().column(columnName).unique().asList();
        }else {
            return null;
        }
    }

    protected Map<String, Integer> getDataPointsByClass(Table data, String featureName, List<String> targetValues){
        Map<String, Integer> dataByClass = new HashMap<>();

        targetValues.forEach(value -> {dataByClass.put(value,
                data.stream().filter(row -> row.getString(featureName).equals(value)).collect(Collectors.toList()).size());
        });

        return dataByClass;
    }

    /*
        entropy functions
        TODO keep just one version
     */

    protected double calculateEntropy(HashMap<String, Integer> dataPointsByClass, int dataSize ){

        double entropy = dataPointsByClass.values().stream().mapToDouble(value -> - ((double) value / dataSize * LearningUtils.log2((double) value / dataSize)) ).sum();
        return (entropy * -1);
    }

    protected double weightedEntropy(){
        return 0;
    }

    public double calculateEntropy(Dataset data){
        List<String> targetValues = data.getUniqueTargetValues();
        HashMap<String, Integer> dataPointsPerClass = new HashMap<>();  // # of data-points per target value
        targetValues.forEach(value -> {
            dataPointsPerClass.put(value,
                   data.getDsTable().stream().filter(row -> row.getString(data.getTargetFeature()).equals(value)).collect(Collectors.toList()).size());
        });
        double entropy= dataPointsPerClass.values().stream().mapToDouble(value -> ((double) value / data.getRowCout()) * LearningUtils.log2((double) value / data.getRowCout())).sum();
        return (entropy * -1);
    }


}
