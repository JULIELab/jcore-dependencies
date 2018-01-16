package de.julielab.evaluation.entities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import de.julielab.evaluation.entities.EvaluationDataEntry.ComparisonType;
import de.julielab.evaluation.entities.EvaluationDataEntry.OverlapType;
import de.julielab.evaluation.entities.format.EvaluationDataFormat;
import de.julielab.evaluation.entities.format.GeneNormalizationFormat;
import de.julielab.evaluation.entities.format.GeneNormalizationNoOffsetsFormat;

public class EntityEvaluator {

	public static final String PROP_ERROR_ANALYZER = "error-analyzer";
	public static final String PROP_ERROR_STATS = "error-statistics-class";
	public static final String PROP_COMPARISON_TYPE = "comparison-type";
	public static final String PROP_OVERLAP_TYPE = "overlap-type";
	public static final String PROP_OVERLAP_SIZE = "overlap-size";
	public static final String PROP_WITH_IDS = "with-ids";

	private ComparisonType comparisonType;
	private OverlapType overlapType;
	private int overlapSize;
	private boolean withIds;
	private Properties properties;

	private EvaluationDataFormat dataFormat;

	public static final Logger log = LoggerFactory.getLogger(EntityEvaluator.class);

	public EntityEvaluator() {
		comparisonType = EvaluationDataEntry.ComparisonType.EXACT;
		overlapType = EvaluationDataEntry.OverlapType.PERCENT;
		overlapSize = 100;
		withIds = true;
		dataFormat = new GeneNormalizationFormat();

		log.debug("ComparisonType: {}", comparisonType);
		log.debug("OverlapType: {}", overlapType);
		log.debug("OverlapSize: {}", overlapSize);
		log.debug("WithIds: {}", withIds);
	}

	public EntityEvaluator(File propertiesFile) throws FileNotFoundException, IOException {
		// this is just a very complicated way to say "load the properties and set them
		// to the constructor"
		this(new Supplier<Properties>() {
			@Override
			public Properties get() {
				Properties p = new Properties();
				try {
					p.load(new FileInputStream(propertiesFile));
				} catch (IOException e) {
					e.printStackTrace();
				}
				return p;
			}
		}.get());

	}

	public EntityEvaluator(Properties properties) throws FileNotFoundException, IOException {
		this.properties = properties;

		comparisonType = EvaluationDataEntry.ComparisonType.valueOf(properties
				.getProperty(PROP_COMPARISON_TYPE, EvaluationDataEntry.ComparisonType.EXACT.toString()).toUpperCase());
		overlapType = EvaluationDataEntry.OverlapType.valueOf(properties
				.getProperty(PROP_OVERLAP_TYPE, EvaluationDataEntry.OverlapType.PERCENT.toString()).toUpperCase());
		overlapSize = Integer.parseInt(properties.getProperty(PROP_OVERLAP_SIZE, "100"));
		withIds = Boolean.parseBoolean(properties.getProperty(PROP_WITH_IDS, "true"));

		String errorAnalyzerClass = properties.getProperty(PROP_ERROR_ANALYZER);
		String errorStatisticsClass = properties.getProperty(PROP_ERROR_STATS);

		log.debug("ComparisonType: {}", comparisonType);
		log.debug("OverlapType: {}", overlapType);
		log.debug("OverlapSize: {}", overlapSize);
		log.debug("Error analysis: {}", errorAnalyzerClass);
		log.debug("Print error statistics class: {}", errorStatisticsClass);
		log.debug("With IDs: {}", withIds);
	}

	public Properties getProperties() {
		return properties;
	}

	public EntityEvaluationResults evaluate(EvaluationData gold, EvaluationData predicted) {
		if (comparisonType != EvaluationDataEntry.ComparisonType.EXACT) {
			log.debug(
					"Converting gold and predicted entity entries to the following comparison method: ComparisonType - {}; OverlapType - {}; OverlapSize - {}",
					comparisonType, overlapType, overlapSize);
			for (EvaluationDataEntry entry : gold) {
				entry.setComparisonType(comparisonType);
				entry.setOverlapType(overlapType);
				entry.setOverlapSize(overlapSize);
			}
			for (EvaluationDataEntry entry : predicted) {
				entry.setComparisonType(comparisonType);
				entry.setOverlapType(overlapType);
				entry.setOverlapSize(overlapSize);
			}
		}
		if (!withIds) {
			log.debug("Converting gold and predicted entity entries for evaluation without IDs.");
			for (EvaluationDataEntry entry : gold) {
				entry.setEntityId("0");
			}
			for (EvaluationDataEntry entry : predicted) {
				entry.setEntityId("0");
			}
		}

		Map<String, EvaluationData> goldByEntityTypes = gold.sliceIntoEntityTypes();
		if (goldByEntityTypes.size() > 1)
			goldByEntityTypes.put(EntityEvaluationResults.OVERALL, gold);
		Map<String, EvaluationData> predictedByEntityTypes = predicted.sliceIntoEntityTypes();
		if (predictedByEntityTypes.size() > 1)
			predictedByEntityTypes.put(EntityEvaluationResults.OVERALL, predicted);

		EntityEvaluationResults results = new EntityEvaluationResults();

		for (String entityType : Sets.union(goldByEntityTypes.keySet(), predictedByEntityTypes.keySet())) {

			Multimap<String, EvaluationDataEntry> goldByDoc = goldByEntityTypes
					.getOrDefault(entityType, new EvaluationData(gold.isMentionData())).organizeByDocument();
			Multimap<String, EvaluationDataEntry> predByDoc = predictedByEntityTypes
					.getOrDefault(entityType, new EvaluationData(predicted.isMentionData())).organizeByDocument();

			EntityEvaluationResult evalResult = new EntityEvaluationResult();
			evalResult.setEntityType(entityType);

			for (String docId : Sets.union(goldByDoc.keySet(), predByDoc.keySet())) {
				Collection<EvaluationDataEntry> goldEntries = goldByDoc.get(docId);
				Collection<EvaluationDataEntry> predEntries = predByDoc.get(docId);
				if (gold.isMentionData() && predicted.isMentionData()) {
					computeEvalStatisticsMentionWise(goldEntries, predEntries, docId, evalResult, gold.isMentionData());
				}
				computeEvalStatisticsDocWise(goldEntries, predEntries, docId, evalResult, !gold.isMentionData());
			}

			results.put(entityType, evalResult);
		}
		return results;
	}

	public EntityEvaluationResults evaluate(List<String[]> gold, List<String[]> predicted) {
		return evaluate(gold, predicted, EvaluationData.getDataFormatFromConfiguration(properties));
	}

	public EntityEvaluationResults evaluate(List<String[]> gold, List<String[]> predicted,
			EvaluationDataFormat format) {
		EvaluationData goldData = new EvaluationData(gold, format);
		EvaluationData predData = new EvaluationData(predicted, format);
		return evaluate(goldData, predData);
	}

	private void computeEvalStatisticsMentionWise(Collection<EvaluationDataEntry> goldEntries,
			Collection<EvaluationDataEntry> predEntries, String docId, EntityEvaluationResult evalResult,
			boolean doErrorAnalysis) {
		// We must use TreeSets for overlap-comparisons because then the hash
		// values won't work for HashMap.
		TreeSet<EvaluationDataEntry> goldSet = new TreeSet<>(goldEntries);
		TreeSet<EvaluationDataEntry> predSet = new TreeSet<>(predEntries);

		SetView<EvaluationDataEntry> tpSet = Sets.intersection(predSet, goldSet);
		SetView<EvaluationDataEntry> fpSet = Sets.difference(predSet, goldSet);
		SetView<EvaluationDataEntry> fnSet = Sets.difference(goldSet, predSet);

		evalResult.addStatisticsByDocument(docId, tpSet, fpSet, fnSet, EvaluationMode.MENTION);
	}

	private void computeEvalStatisticsDocWise(Collection<EvaluationDataEntry> goldEntries,
			Collection<EvaluationDataEntry> predEntries, String docId, EntityEvaluationResult evalResult,
			boolean doErrorAnalysis) {
		// We must use TreeSets for overlap-comparisons because then the hash
		// values won't work for HashMap.
		TreeSet<EvaluationDataEntry> goldSet = new TreeSet<>();
		for (EvaluationDataEntry entry : goldEntries)
			goldSet.add(entry.toDocWiseEntry());
		TreeSet<EvaluationDataEntry> predSet = new TreeSet<>();
		for (EvaluationDataEntry entry : predEntries)
			predSet.add(entry.toDocWiseEntry());

		SetView<EvaluationDataEntry> tpSet = Sets.intersection(predSet, goldSet);
		SetView<EvaluationDataEntry> fpSet = Sets.difference(predSet, goldSet);
		SetView<EvaluationDataEntry> fnSet = Sets.difference(goldSet, predSet);

		evalResult.addStatisticsByDocument(docId, tpSet, fpSet, fnSet, EvaluationMode.DOCUMENT);

	}

	public static void main(String[] args) throws IOException {
		if (args.length < 2 || args.length > 3) {
			System.err.println(
					"Usage: " + EntityEvaluator.class.getSimpleName() + " <gold data> <pred data> [properties file]");
			System.exit(1);
		}

		File goldFile = new File(args[0]);
		File predFile = new File(args[1]);
		File propertiesFile = null;
		if (args.length > 2)
			propertiesFile = new File(args[2]);

		EntityEvaluator evaluator = propertiesFile == null ? new EntityEvaluator()
				: new EntityEvaluator(propertiesFile);

		EvaluationData goldData = null;
		EvaluationData predData = null;
		if (propertiesFile == null
				|| evaluator.getProperties().getProperty(EvaluationData.PROP_INPUT_FORMAT_CLASS) == null) {
			System.out.println("No properties file given, trying to guess data format.");

			List<EvaluationDataFormat> formats = Arrays.asList(new GeneNormalizationFormat(),
					new GeneNormalizationNoOffsetsFormat());
			EvaluationDataFormat foundFormat = null;
			int i = 0;
			while (foundFormat == null && i < formats.size()) {
				EvaluationDataFormat format = formats.get(i);
				try {
					goldData = EvaluationData.readDataFile(goldFile, format);
					predData = EvaluationData.readDataFile(predFile, format);
					foundFormat = format;
				} catch (NumberFormatException e) {
					System.out.println(format.getClass().getSimpleName() + " did cause exception, trying the next.");
				}
				++i;
			}
			if (foundFormat == null) {
				System.out.println("All input data format specifications failed: "
						+ formats.stream().map(f -> f.getClass().getSimpleName()).collect(Collectors.joining(", ")));
				System.out.println("Please use a configuration file and specify the correct format class.");
				System.exit(1);
			}
			evaluator.setDataFormat(foundFormat);
		} else {
			goldData = EvaluationData.readDataFile(goldFile, evaluator.dataFormat);
			predData = EvaluationData.readDataFile(predFile, evaluator.dataFormat);
		}

		EntityEvaluationResults results = evaluator.evaluate(goldData, predData);
		try (FileOutputStream fos = new FileOutputStream("EvaluationReport.txt")) {
			for (EntityEvaluationResult result : results.values()) {
				System.out.println(result.getEvaluationReportShort());
				IOUtils.write(result.getEvaluationReportLong(), fos);
			}
		}

	}

	public EvaluationDataFormat getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(EvaluationDataFormat dataFormat) {
		this.dataFormat = dataFormat;
	}

}
