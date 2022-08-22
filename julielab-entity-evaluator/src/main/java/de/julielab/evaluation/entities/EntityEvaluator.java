package de.julielab.evaluation.entities;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultiset;
import de.julielab.evaluation.entities.EvaluationDataEntry.ComparisonType;
import de.julielab.evaluation.entities.EvaluationDataEntry.OverlapType;
import de.julielab.evaluation.entities.format.EvaluationDataFormat;
import de.julielab.evaluation.entities.format.GeneNormalizationFormat;
import de.julielab.evaluation.entities.format.GeneNormalizationFormatWithEntityType;
import de.julielab.evaluation.entities.format.GeneNormalizationNoOffsetsFormat;
import de.julielab.java.utilities.spanutils.OffsetMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.julielab.evaluation.entities.EvaluationData.PROP_INPUT_FORMAT_CLASS;

public class EntityEvaluator {

    public static final String PROP_ERROR_ANALYZER = "error-analyzer";
    public static final String PROP_ERROR_STATS = "error-statistics-class";
    public static final String PROP_COMPARISON_TYPE = "comparison-type";
    public static final String PROP_OVERLAP_TYPE = "overlap-type";
    public static final String PROP_OVERLAP_SIZE = "overlap-size";
    public static final String PROP_WITH_IDS = "with-ids";
    public static final String PROP_GROUPING_TYPE = "grouping-type";
    public static final Logger log = LoggerFactory.getLogger(EntityEvaluator.class);
    private ComparisonType comparisonType;
    private OverlapType overlapType;
    private int overlapSize;
    private boolean withIds;
    private Properties properties;
    private EvaluationDataFormat dataFormat;

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

    public EntityEvaluator(File propertiesFile) {
        // this is just a very complicated way to say "load the properties and set them
        // to the constructor"
        this(((Supplier<Properties>) () -> {
            Properties p = new Properties();
            try {
                p.load(new FileInputStream(propertiesFile));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return p;
        }).get());
    }

    public EntityEvaluator(Properties properties) {
        this.properties = properties;

        comparisonType = EvaluationDataEntry.ComparisonType.valueOf(properties
                .getProperty(PROP_COMPARISON_TYPE, EvaluationDataEntry.ComparisonType.EXACT.toString()).toUpperCase());
        overlapType = EvaluationDataEntry.OverlapType.valueOf(properties
                .getProperty(PROP_OVERLAP_TYPE, EvaluationDataEntry.OverlapType.PERCENT.toString()).toUpperCase());
        overlapSize = Integer.parseInt(properties.getProperty(PROP_OVERLAP_SIZE, "100"));
        withIds = Boolean.parseBoolean(properties.getProperty(PROP_WITH_IDS, "true"));
        if (properties.containsKey(PROP_INPUT_FORMAT_CLASS))
            this.dataFormat = getDataFormatFromConfiguration(properties);

        String errorAnalyzerClass = properties.getProperty(PROP_ERROR_ANALYZER);
        String errorStatisticsClass = properties.getProperty(PROP_ERROR_STATS);

        log.debug("ComparisonType: {}", comparisonType);
        log.debug("OverlapType: {}", overlapType);
        log.debug("OverlapSize: {}", overlapSize);
        log.debug("Error analysis: {}", errorAnalyzerClass);
        log.debug("Print error statistics class: {}", errorStatisticsClass);
        log.debug("With IDs: {}", withIds);
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println(
                    "Usage: " + EntityEvaluator.class.getSimpleName() + " -g <gold data> [-a <alternative gold data>] -p <pred data> [-c configration file]");
            System.exit(1);
        }

        File goldFile = null;
        File goldAltFile = null;
        File predFile = null;
        File configFile = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                if (i >= args.length) {
                    System.err.println("Missing value for parameter " + arg);
                    System.exit(1);
                }
                String value = args[i + 1];
                switch (arg) {
                    case "-g":
                        goldFile = new File(value);
                        break;
                    case "-a":
                        goldAltFile = new File(value);
                        break;
                    case "-p":
                        predFile = new File(value);
                        break;
                    case "-c":
                        configFile = new File(value);
                        break;
                    default:
                        System.err.println("Unknown parameter " + arg);
                        System.exit(2);
                }
            }
        }

        if (goldFile == null || predFile == null) {
            System.err.println("Missing gold or prediction file.");
            System.err.println(
                    "Usage: " + EntityEvaluator.class.getSimpleName() + " -g <gold data> [-a <alternative gold data>] -p <pred data> [-c configration file]");
            System.exit(3);
        }


        EntityEvaluator evaluator = configFile == null ? new EntityEvaluator()
                : new EntityEvaluator(configFile);

        EvaluationData goldData = null;
        EvaluationData goldAltData = null;
        EvaluationData predData = null;
        if (configFile == null
                || evaluator.getProperties().getProperty(PROP_INPUT_FORMAT_CLASS) == null) {
            System.out.println("No configuration file given, trying to guess data format.");

            List<EvaluationDataFormat> formats = Arrays.asList(new GeneNormalizationFormat(), new GeneNormalizationFormatWithEntityType(),
                    new GeneNormalizationNoOffsetsFormat());
            EvaluationDataFormat foundFormat = null;
            int i = 0;
            while (foundFormat == null && i < formats.size()) {
                EvaluationDataFormat format = formats.get(i);
                try {
                    goldData = EvaluationData.readDataFile(goldFile, format);
                    goldAltData = goldAltFile != null ? EvaluationData.readDataFile(goldAltFile, format) : new EvaluationData();
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
            goldAltData = goldAltFile != null ? EvaluationData.readDataFile(goldAltFile, evaluator.dataFormat) : new EvaluationData();
            predData = EvaluationData.readDataFile(predFile, evaluator.dataFormat);
        }

        EntityEvaluationResults results = evaluator.evaluate(goldData, goldAltData, predData);
        try (FileOutputStream fos = new FileOutputStream("EvaluationReport.txt")) {
            for (EntityEvaluationResult result : results.values()) {
                System.out.println(result.getEvaluationReportShort());
                IOUtils.write(result.getEvaluationReportLong(), fos);
            }
        }

    }

    public static EvaluationDataFormat getDataFormatFromConfiguration(Properties properties) {
        String dataFormatClassName = properties != null
                ? properties.getProperty(PROP_INPUT_FORMAT_CLASS, GeneNormalizationFormat.class.getCanonicalName())
                : GeneNormalizationFormat.class.getCanonicalName();
        EvaluationDataFormat dataFormat;
        try {
            dataFormat = (EvaluationDataFormat) Class.forName(dataFormatClassName).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new IllegalArgumentException(
                    "Evaluation data format class " + dataFormatClassName + " could not be loaded.", e);
        }
        return dataFormat;
    }

    public Properties getProperties() {
        return properties;
    }

    public EntityEvaluationResults evaluate(EvaluationData gold, EvaluationData predicted) {
        return evaluate(gold, new EvaluationData(), predicted);
    }

    public EntityEvaluationResults evaluate(EvaluationData gold, EvaluationData alternative, EvaluationData predicted) {
        if (comparisonType != EvaluationDataEntry.ComparisonType.EXACT || !withIds) {
            log.debug(
                    "Converting gold and predicted entity entries to the following comparison method: ComparisonType - {}; OverlapType - {}; OverlapSize - {}",
                    comparisonType, overlapType, overlapSize);
            Consumer<Stream<EvaluationDataEntry>> entryConfigurator = s -> {
                if (!withIds)
                    log.debug("Converting gold and predicted entity entries for evaluation without IDs.");
                s.forEach(e -> {
                    e.setComparisonType(comparisonType);
                    e.setOverlapType(overlapType);
                    e.setOverlapSize(overlapSize);
                    if (!withIds)
                        e.setEntityId("0");
                });
            };
            entryConfigurator.accept(gold.stream());
            entryConfigurator.accept(alternative.stream());
            entryConfigurator.accept(predicted.stream());
        }

        EvaluationData.GroupingType groupingType = properties != null ? EvaluationData.GroupingType.valueOf(properties.getProperty(PROP_GROUPING_TYPE, EvaluationData.GroupingType.ENTITYTYPE.name()).toUpperCase()) : EvaluationData.GroupingType.ENTITYTYPE;

        Function<EvaluationData, Map<String, EvaluationData>> toLabelGroups =
                groupingType == EvaluationData.GroupingType.ENTITYTYPE
                ?
                data -> {
                    final Map<String, EvaluationData> map = data.groupByEntityTypes();
                    if (map.size() > 1)
                        map.put(EntityEvaluationResults.OVERALL, data);
                    return map;
                }
                :
                data -> {
                    final Map<String, EvaluationData> map = data.groupByEntityLabel();
                    if (map.size() > 1)
                        map.put(EntityEvaluationResults.OVERALL, data);
                    return map;
                };
        Map<String, EvaluationData> goldByEntityTypes = toLabelGroups.apply(gold);
        Map<String, EvaluationData> goldAltByEntityTypes = toLabelGroups.apply(alternative);
        Map<String, EvaluationData> predictedByEntityTypes = toLabelGroups.apply(predicted);

        EntityEvaluationResults results = new EntityEvaluationResults();

        for (String entityType : Sets.union(goldByEntityTypes.keySet(), predictedByEntityTypes.keySet())) {

            Multimap<String, EvaluationDataEntry> goldByDoc = goldByEntityTypes
                    .getOrDefault(entityType, new EvaluationData(gold.isMentionData())).organizeByDocument();
            Multimap<String, EvaluationDataEntry> goldAltByDoc = goldAltByEntityTypes
                    .getOrDefault(entityType, new EvaluationData(predicted.isMentionData())).organizeByDocument();
            Multimap<String, EvaluationDataEntry> predByDoc = predictedByEntityTypes
                    .getOrDefault(entityType, new EvaluationData(predicted.isMentionData())).organizeByDocument();

            EntityEvaluationResult evalResult = new EntityEvaluationResult();
            evalResult.setEntityType(entityType);

            for (String docId : Sets.union(goldByDoc.keySet(), predByDoc.keySet())) {
                Collection<EvaluationDataEntry> goldEntries = goldByDoc.get(docId);
                Collection<EvaluationDataEntry> goldAltEntries = goldAltByDoc.get(docId);
                Collection<EvaluationDataEntry> predEntries = predByDoc.get(docId);
                if (gold.isMentionData() && predicted.isMentionData()) {
                    computeEvalStatisticsMentionWise(goldEntries, goldAltEntries, predEntries, docId, evalResult);
                }
                computeEvalStatisticsDocWise(goldEntries, predEntries, docId, evalResult);
            }

            results.put(entityType, evalResult);
        }
        return results;
    }

    public EntityEvaluationResults evaluate(List<String[]> gold, List<String[]> predicted) {
        return evaluate(gold, predicted, dataFormat);
    }

    public EntityEvaluationResults evaluate(List<String[]> gold, List<String[]> predicted,
                                            EvaluationDataFormat format) {
        EvaluationData goldData = new EvaluationData(gold, format);
        EvaluationData predData = new EvaluationData(predicted, format);
        return evaluate(goldData, predData);
    }

    private void computeEvalStatisticsMentionWise(Collection<EvaluationDataEntry> goldEntries, Collection<EvaluationDataEntry> goldAltEntries,
                                                  Collection<EvaluationDataEntry> predEntries, String docId, EntityEvaluationResult evalResult) {
        // We must use TreeSets for overlap-comparisons because then the hash
        // values won't work for HashMap.
        Multiset<EvaluationDataEntry> goldSet = TreeMultiset.create(goldEntries);
        Multiset<EvaluationDataEntry> goldAltSet = TreeMultiset.create(goldAltEntries);
        Multiset<EvaluationDataEntry> predSet = TreeMultiset.create(predEntries);
        Runnable resetGoldSet = () -> {
            goldSet.clear();
            goldSet.addAll(goldEntries);
        };
        final OffsetMap<EvaluationDataEntry> goldOffsetMap = goldSet.stream().collect(Collectors.toMap(EvaluationDataEntry::getOffsetRange, Function.identity(), (e1, e2) -> e1, OffsetMap::new));

        Multiset<EvaluationDataEntry> tpGoldSet = predSet.stream().filter(goldSet::remove).collect(Collectors.toCollection(TreeMultiset::create));
        resetGoldSet.run();
        Set<EvaluationDataEntry> tpAltSet = new HashSet<>();
        // Find those gold entries that have been hit through an alternative. Note that in the end, the gold entries
        // and not the alternative entries will be collected. This is because one alternative might actually indicate
        // multiple gold hits (if the alternative spans multiple gold entities) and if we would only add the alternative
        // we would not calculate enough TPs.
        for (EvaluationDataEntry e : (Iterable<EvaluationDataEntry>) () -> predSet.stream().filter(goldAltEntries::contains).iterator()) {
            final Range<Integer> range = Range.between(e.getBegin(), e.getEnd());
            final Multiset<EvaluationDataEntry> overlappingGold = goldOffsetMap.getOverlapping(range).values().stream().filter(gold -> gold.getEntityId().equals(e.getEntityId())).collect(Collectors.toCollection(TreeMultiset::create));
            tpAltSet.addAll(overlappingGold);
        }
        final Multiset<EvaluationDataEntry> tpSet = Stream.concat(tpGoldSet.stream(), tpAltSet.stream()).collect(Collectors.toCollection(TreeMultiset::create));

        Multiset<EvaluationDataEntry> fpSet = predSet.stream().filter(Predicate.not(goldSet::remove)).filter(Predicate.not(goldAltSet::remove)).collect(Collectors.toCollection(TreeMultiset::create));

        // Now compute the false negatives. We will start with the complete gold set and remove found gold items. The leftovers are the fns.
        Multiset<EvaluationDataEntry> fnSet = TreeMultiset.create(goldEntries);
        // Remove the found elements from the gold set
        tpGoldSet.forEach(fnSet::remove);
        // Remove the gold elements whose correspondence in the alternative set has been found. Since we collected
        // the original gold mentions of the found alternatives (see comment above), we don't need to handle
        // alternatives here.
        tpAltSet.forEach(fnSet::remove);

        evalResult.addStatisticsByDocument(docId, tpSet, fpSet, fnSet, EvaluationMode.MENTION);
    }

    private void computeEvalStatisticsDocWise(Collection<EvaluationDataEntry> goldEntries,
                                              Collection<EvaluationDataEntry> predEntries, String docId, EntityEvaluationResult evalResult) {
        // We must use TreeSets for overlap-comparisons because then the hash
        // values won't work for HashMap.
        TreeSet<EvaluationDataEntry> goldSet = new TreeSet<>();
        Runnable resetGoldSet = () -> {
            for (EvaluationDataEntry entry : goldEntries)
                goldSet.add(entry.toDocWiseEntry());
        };
        resetGoldSet.run();
        TreeSet<EvaluationDataEntry> predSet = new TreeSet<>();
        for (EvaluationDataEntry entry : predEntries)
            predSet.add(entry.toDocWiseEntry());

        Multiset<EvaluationDataEntry> tpSet = predSet.stream().filter(goldSet::contains).collect(Collectors.toCollection(TreeMultiset::create));
        Multiset<EvaluationDataEntry> fpSet = predSet.stream().filter(Predicate.not(goldSet::remove)).collect(Collectors.toCollection(TreeMultiset::create));
        resetGoldSet.run();
        Multiset<EvaluationDataEntry> fnSet = goldSet.stream().filter(Predicate.not(predSet::remove)).collect(Collectors.toCollection(TreeMultiset::create));

        evalResult.addStatisticsByDocument(docId, tpSet, fpSet, fnSet, EvaluationMode.DOCUMENT);

    }

    public EvaluationDataFormat getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(EvaluationDataFormat dataFormat) {
        this.dataFormat = dataFormat;
    }

}
