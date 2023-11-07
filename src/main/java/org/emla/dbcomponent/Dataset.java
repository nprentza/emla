package org.emla.dbcomponent;

import java.util.List;

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
}
