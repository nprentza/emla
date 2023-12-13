package org.emla.dbcomponent;

import org.emla.learning.LearningUtils;
import tech.tablesaw.api.Table;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Dataset extends DatasetAbstract {

	public Dataset(String dsFilepath, String dsName, String targetFeature) {
		super(dsFilepath, dsName, targetFeature);
	}

	public Dataset(String dsFilepath, String dsName, String targetFeature, double trainSplit, double testSplit) {
		super(dsFilepath, dsName, targetFeature, trainSplit, testSplit);
	}

	// create a dataset object with just the records in the caseIds
	public Dataset(Dataset dataFrom, List<Integer> caseIDs) {
		super(dataFrom, caseIDs);
	}

	public double calculateEntropy(Table dsTable, List<String> targetValues, String targetFeature){
		//List<String> targetValues = data.getUniqueTargetValues();
		HashMap<String, Integer> dataPointsPerClass = new HashMap<>();  // # of data-points per target value
		targetValues.forEach(value -> {
			dataPointsPerClass.put(value,
					dsTable.stream().filter(row -> row.getString(targetFeature).equals(value)).collect(Collectors.toList()).size());
		});
		//
		double entropy= dataPointsPerClass.values().stream().mapToDouble(value -> ((double) value / dsTable.rowCount()) * LearningUtils.log2((double) value / dsTable.rowCount())).sum();
		return (entropy * -1);

	}
}
