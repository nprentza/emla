package org.emla.learning.oner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.emla.dbcomponent.Dataset;
import org.emla.dbcomponent.DbAccess;
import org.emla.learning.LearningSession;
import org.emla.learning.LearningUtils;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Table;

public class OneR {

	//	Sequential Covering
	public static List<Frequency> sequentialCovering(Dataset ds, String dataSplit){
		LearningSession emlaSession = new LearningSession(ds,"test");
		List<Frequency> rules = new ArrayList<>();
		List<Integer> allCaseIDs = ds.getCaseIDs(dataSplit);
		int run=1;

		//	first run
		List<FrequencyTable> frequencyTables = emlaSession.calculateFrequencyTables(ds, dataSplit,null);
		ds.getUniqueTargetValues().forEach(target -> rules.add(emlaSession.calculateFrequencyHighCoverageLowError(frequencyTables, target)));
		rules.removeAll(Collections.singleton(null));

		List<Integer> caseIDsCovered = Arrays.stream(rules.stream().map(rule -> rule.getCaseIDs())
						.collect(Collectors.joining(",")).split(","))
				.filter(id -> !id.equals("null"))
				.map(id -> Integer.valueOf(id)).collect(Collectors.toList());

		List<Integer> caseIDsNotCovered = allCaseIDs; caseIDsNotCovered.removeAll(caseIDsCovered);

		System.out.println("run " + run + "\ncaseIDs covered: " + caseIDsCovered.toString());
		System.out.println("caseIDs not covered: " + caseIDsNotCovered.toString());

		while (caseIDsNotCovered.size()>0){
			run++;
			List<FrequencyTable> frequencyTablesR = emlaSession.calculateFrequencyTables(ds, dataSplit,caseIDsNotCovered);
			ds.getUniqueTargetValues().forEach(target -> rules.add(emlaSession.calculateFrequencyHighCoverageLowError(frequencyTablesR, target)));
			rules.removeAll(Collections.singleton(null));
			caseIDsCovered = Arrays.stream(rules.stream().map(rule -> rule.getCaseIDs())
							.collect(Collectors.joining(",")).split(","))
					.filter(id -> !id.equals("null"))
					.map(id -> Integer.valueOf(id)).collect(Collectors.toList());
			List<Integer> caseIDsNotCovered_Before = caseIDsNotCovered;
			caseIDsNotCovered = allCaseIDs;	caseIDsNotCovered.removeAll(caseIDsCovered);
			System.out.println("run " + run + "\ncaseIDs covered: " + caseIDsCovered.toString());
			System.out.println("caseIDs not covered: " + caseIDsNotCovered.toString());
			//	if there is no improvemet on the  coverage, exit while look
			if (caseIDsNotCovered.equals(caseIDsNotCovered_Before)){break;}
		}

		return rules;
	}

	public static List<FrequencyTable> getFrequencyTables(Dataset ds, String dataSplit, List<Integer> caseIDs){
		List<FrequencyTable> freuencyTables = new ArrayList<>();

		for (String predictor : ds.getPredictors()) {
			//	if predictor is a string / text attribute then call DbAccess.getFrequencies
			if (ds.getDsTable().column(predictor).type()== ColumnType.STRING || ds.getDsTable().column(predictor).type()== ColumnType.TEXT){
				freuencyTables.add(frequencyTableCategoricalFeature(predictor,ds,dataSplit,caseIDs));
			}	// else calculate split-points and find frequencies for range of values
			else if (ds.getDsTable().column(predictor).type()== ColumnType.DOUBLE || ds.getDsTable().column(predictor).type()== ColumnType.INTEGER){
				freuencyTables.add(frequencyTableNumericalFeature(predictor,ds,dataSplit,caseIDs));
			}
		}
		return freuencyTables;
	}

	private static FrequencyTable frequencyTableCategoricalFeature(String featureName, Dataset ds, String dataSplit, List<Integer> caseIDs){
		FrequencyTable ft = DbAccess.getFrequencies(featureName, ds.getTargetFeature(), ds.getUniqueTargetValues(), ds.getDatasetName(), dataSplit, caseIDs);
		ft.updateFrequencies(caseIDs!=null ? caseIDs.size() : ds.getDataSplit(dataSplit).rowCount());

		return ft;
	}

	private static FrequencyTable frequencyTableNumericalFeature(String featureName, Dataset ds, String dataSplit, List<Integer> caseIDs){
		Dataset dsFiltered = (caseIDs == null) ? ds : new Dataset(ds,caseIDs) ;
		Table data = dataSplit!=null ? dsFiltered.getDataSplit(dataSplit) : dsFiltered.getDsTable();
		data = data.sortAscendingOn(featureName);
		List<Double> splitPoints = LearningUtils.numericalSplitPoints(data,featureName,ds.getTargetFeature());
		List<Pair<Frequency.FrequencyCondition, Frequency.FrequencyCondition>> splitPointsCondtions = LearningUtils.splitPointsToConditions(splitPoints);
		FrequencyTable freqTable = new FrequencyTable(featureName, ds.getDsTable().column(featureName).type(), ds.getUniqueTargetValues());

		Table dataSelection = null;
		for (Pair<Frequency.FrequencyCondition, Frequency.FrequencyCondition> conditionPair : splitPointsCondtions){
			dataSelection = filterSelection(data,featureName,conditionPair);
			if (dataSelection.rowCount()>0) {
				freqTable.addFrequency(numericalFrequency(featureName,
						LearningUtils.getDataPointsByClass(dataSelection, ds.getTargetFeature(), ds.getUniqueTargetValues()),
						conditionPair, data.rowCount()));
			}
		}
		return freqTable;
	}

	private static Table filterSelection(Table data, String featureName, Double lowValue, Double highValue, boolean leftValueIncluded, boolean rightValueIncluded){
		Table dataFiltered=null;
		// leftValueIncluded always true
		if (!rightValueIncluded) {highValue++;}
		if (data.column(featureName).type()==ColumnType.INTEGER){
			IntColumn intColumn = data.intColumn(featureName);
			dataFiltered = data.where(intColumn.isBetweenInclusive(lowValue,highValue));
		} else if (data.column(featureName).type()==ColumnType.DOUBLE) {
			DoubleColumn doubleColumn = data.doubleColumn(featureName);
			dataFiltered = data.where(doubleColumn.isBetweenInclusive(lowValue,highValue));
		}
		return dataFiltered;
	}

	private static Table filterSelection(Table data, String featureName, Pair<Frequency.FrequencyCondition,Frequency.FrequencyCondition> condition){
		Table datafiltered = null;

		if (condition.getLeft()!=null){
			datafiltered = singleFilterSelection(data,featureName, condition.getLeft());
		}

		if (condition.getRight()!=null){
			datafiltered = singleFilterSelection(datafiltered==null? data : datafiltered,
					featureName, condition.getRight());
		}

		return datafiltered;
	}

	//	assumption: condition.operator is either LESS_THAN or GREATER_OR_EQUAL_THAN
	private static Table singleFilterSelection(Table data, String featureName, Frequency.FrequencyCondition condition){
		if (data.column(featureName).type()==ColumnType.INTEGER){
			IntColumn intColumn = data.intColumn(featureName);
			return condition.operator== LearningUtils.Operator.LESS_THAN ? data.where(intColumn.isLessThan((double)condition.value))
					: data.where(intColumn.isGreaterThanOrEqualTo((double)condition.value));
		}else if (data.column(featureName).type()==ColumnType.DOUBLE){
			DoubleColumn doubleColumn = data.doubleColumn(featureName);
			return condition.operator== LearningUtils.Operator.LESS_THAN ? data.where(doubleColumn.isLessThan((double)condition.value))
					: data.where(doubleColumn.isGreaterThanOrEqualTo((double)condition.value));
		}else{
			return null;	// column type is not supported
		}
	}

	private static Frequency numericalFrequency(String featureName, Map<String, Integer> dataPointsByClass, double splitPointLow, double splitPointHigh,
												LearningUtils.Operator leftOperator, LearningUtils.Operator rightOperator, int dataSize){
		Frequency f = new Frequency(featureName);
		f.addOperatorValue(leftOperator,splitPointLow);
		f.addOperatorValue(rightOperator,splitPointHigh);
		dataPointsByClass.entrySet().forEach( e -> f.addFrequency(e.getKey(),e.getValue()));
		f.updateFrequency(dataSize);
		return f;
	}

	private static Frequency numericalFrequency(String featureName, Map<String, Integer> dataPointsByClass,
												Pair<Frequency.FrequencyCondition, Frequency.FrequencyCondition> condition, int dataSize){
		Frequency f = new Frequency(featureName);
		if (condition.getLeft()!=null) {
			f.addOperatorValue(condition.getLeft().operator, condition.getLeft().value);
		}
		if (condition.getRight()!=null) {
			f.addOperatorValue(condition.getRight().operator, condition.getRight().value);
		}
		dataPointsByClass.entrySet().forEach( e -> f.addFrequency(e.getKey(),e.getValue()));
		f.updateFrequency(dataSize);
		return f;
	}

	public static Frequency getFrequencyHighCoverageLowError(Dataset ds, String dataSplit) {
		
		List<FrequencyTable> freuencyTables = getFrequencyTables(ds, dataSplit, null);
		
		Frequency f = freuencyTables.get(0).selectFrequencyHighCoverageLowError();
		
		for (int i=1; i<freuencyTables.size(); i++) {
			if (freuencyTables.get(i).selectFrequencyHighCoverageLowError().getBestFrequencyAssessment() > f.getBestFrequencyAssessment()) {
				f = freuencyTables.get(i).selectFrequencyHighCoverageLowError();
			}
		}
		
		return f;
	}

	public static Frequency getFrequencyHighCoverageLowError(List<FrequencyTable> frequencyTables) {

		Frequency f = frequencyTables.get(0).selectFrequencyHighCoverageLowError();

		for (int i=1; i<frequencyTables.size(); i++) {
			if (frequencyTables.get(i).selectFrequencyHighCoverageLowError().getBestFrequencyAssessment() > f.getBestFrequencyAssessment()) {
				f = frequencyTables.get(i).selectFrequencyHighCoverageLowError();
			}
		}
		return f;
	}

	public static Frequency getFrequencyHighCoverageLowError(List<FrequencyTable> frequencyTables, String targetClass) {

		List<Frequency> freqsTargetClass = new ArrayList<>();

		frequencyTables.forEach( ft -> {
			Frequency f = ft.selectFrequencyHighCoverageLowError(targetClass);
			if (f!=null){freqsTargetClass.add(f);}
		});

		if (!freqsTargetClass.isEmpty()){
			return freqsTargetClass.stream().sorted(Comparator.comparingDouble(Frequency::getBestFrequencyAssessment)).collect(Collectors.toList()).get(0);
		}else {
			return null;
		}
	}

	public static Frequency getFrequencyHighCoverageLowError(List<FrequencyTable> frequencyTables, ColumnType featureType) {

		List<FrequencyTable> frequencyTablesByType= frequencyTables.stream().filter(ft -> ft.getFeatureType().equals(featureType)).collect(Collectors.toList());

		if (!frequencyTablesByType.isEmpty()){
			Frequency f = frequencyTablesByType.get(0).selectFrequencyHighCoverageLowError();
			for (int i=1; i<frequencyTablesByType.size(); i++) {
				if (frequencyTablesByType.get(i).selectFrequencyHighCoverageLowError().getBestFrequencyAssessment() > f.getBestFrequencyAssessment()) {
					f = frequencyTablesByType.get(i).selectFrequencyHighCoverageLowError();
				}
			}
			return f;
		}else {return null;}
	}

	public static Frequency getFrequencyHighCoverageLowError(List<FrequencyTable> frequencyTables, ColumnType featureType, String targetClass) {

		List<FrequencyTable> frequencyTablesByType= frequencyTables.stream().filter(ft -> ft.getFeatureType().equals(featureType)).collect(Collectors.toList());
		return getFrequencyHighCoverageLowError(frequencyTablesByType,targetClass);

	}

}
