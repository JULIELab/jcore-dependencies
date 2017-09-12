package de.julielab.evaluation.entities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import de.julielab.evaluation.entities.format.EvaluationDataColumn;
import de.julielab.evaluation.entities.format.EvaluationDataFormat;

public class EvaluationData extends ArrayList<EvaluationDataEntry> {

	private static final Logger log = LoggerFactory.getLogger(EvaluationData.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 7048898609514218622L;

	private boolean isMentionData;

	public static Comparator<EvaluationDataEntry> docIdComparator = new Comparator<EvaluationDataEntry>() {

		@Override
		public int compare(EvaluationDataEntry o1, EvaluationDataEntry o2) {
			return o1.getDocId().compareTo(o2.getDocId());
		}

	};

	public static Comparator<EvaluationDataEntry> offsetComparator = new Comparator<EvaluationDataEntry>() {

		@Override
		public int compare(EvaluationDataEntry o1, EvaluationDataEntry o2) {
			return Integer.compare(o1.getBegin(), o2.getBegin());
		}

	};

	private Multimap<String, EvaluationDataEntry> entriesByDocument;

	private EvaluationDataFormat dataFormat;

	public EvaluationData() {
		this(EntityEvaluator.DEFAULT_FORMAT);
	}
	
	public EvaluationData(EvaluationDataFormat dataFormat) {
		this.dataFormat = dataFormat;
	}

	public EvaluationData(List<String[]> records) {
		this(records, EntityEvaluator.DEFAULT_FORMAT);
	}

	public EvaluationData(List<String[]> records, EvaluationDataFormat format) {
		dataFormat = format;
		boolean hasOffsets = false;
		if (records.size() > 0) {
			add(records.get(0), format);
			EvaluationDataEntry lastEntry = get(size() - 1);
			hasOffsets = lastEntry.isMention();
			if (hasOffsets)
				log.debug("Got first line \"{}\", treating file as delivered with offsets.",
						new Object[] { records.get(0) });
			else
				log.debug("Got first line \"{}\", treating file as delivered without offsets.",
						new Object[] { records.get(0) });
		}
		for (int i = 1; i < records.size(); i++) {
			String[] record = records.get(i);
			add(record, format);
			EvaluationDataEntry lastEntry = get(size() - 1);
			if (hasOffsets && !lastEntry.isMention())
				throw new IllegalStateException("Input format error on line " + i
						+ ": Offset information expected, but not both begin and end offsets where found. The input format is <docId> <entityId> [<beginOffset> <endOffset>]. Line was: "
						+ Arrays.toString(record));
		}
	}

	public EvaluationData(String[]... data) {
		this(EntityEvaluator.DEFAULT_FORMAT, data);
	}

	public EvaluationData(EvaluationDataFormat format, String[]... data) {
		dataFormat = format;
		for (int i = 0; i < data.length; i++)
			add(data[i], format);
	}

	@Override
	public boolean add(EvaluationDataEntry entry) {
		checkMentionMode(entry);
		return super.add(entry);
	}

	protected void checkMentionMode(EvaluationDataEntry entry) {
		if (isEmpty()) {
			isMentionData = entry.isMention();
			if (entry.isMention())
				log.debug("Got first line \"{}\", treating file as delivered with offsets.", entry);
			else
				log.debug("Got first line \"{}\", treating file as delivered without offsets.", entry);
		} else if (isMentionData && !entry.isMention())
			throw new NoOffsetsException(
					"This data set is comprised of entity mentions with offset information. However, the new entry \""
							+ entry + "\" does not have valid offset information.");
	}

	protected void checkMentionMode() {
		if (size() > 0)
			isMentionData = get(0).isMention();
		for (EvaluationDataEntry entry : this) {
			if (isMentionData && !entry.isMention())
				throw new NoOffsetsException(
						"This data set is comprised of entity mentions with offset information. However, the new entry \""
								+ entry + "\" does not have valid offset information.");
		}
	}

	public boolean add(String[] dataRecord, EvaluationDataFormat format) {
		EvaluationDataEntry e = new EvaluationDataEntry();
		for (EvaluationDataColumn col : format.getColumns()) {
			col.set(e, format, dataRecord);
		}
		add(e);
		return true;
	}

	/**
	 * Minimal format is
	 * 
	 * <pre>
	 * docId &lt; tab &gt; entityId
	 * </pre>
	 * 
	 * Full format is
	 * 
	 * <pre>
	 * docId &lt; tab &gt; entityId &lt; tab &gt; begin &lt; tab &gt; end &lt; tab &gt; text &lt; tab &gt; systemId
	 * </pre>
	 * 
	 * Any subset of columns between the two may be given as long as the order
	 * is always correct.
	 * 
	 * @param dataRecord
	 * @return
	 */
	public boolean add(String[] dataRecord) {
		return add(dataRecord, dataFormat);
		// if (dataRecord.length < 2)
		// throw new IllegalArgumentException("Given data record \"" +
		// Arrays.toString(dataRecord)
		// + "\" has less than two columns. The expected format is at least two
		// columns where the first column is a document ID and the second is an
		// entity ID to allow for the document-level evaluation of entity
		// mention findings.");
		// if (dataRecord.length < 3) {
		// EvaluationDataEntry evalDataEntry = new
		// EvaluationDataEntry(dataRecord[0], dataRecord[1]);
		// return add(evalDataEntry);
		// } else if (dataRecord.length > 2) {
		// String docId = dataRecord[0];
		// String entityId = dataRecord[1];
		// int begin;
		// int end;
		// String entityString = null;
		// String recognitionSystem = null;
		// String confidence = null;
		// try {
		// begin = Integer.parseInt(dataRecord[2]);
		// } catch (NumberFormatException e) {
		// EvaluationDataEntry evalDataEntry = new
		// EvaluationDataEntry(dataRecord[0], dataRecord[1]);
		// entityString = dataRecord[2];
		// if (dataRecord.length > 3)
		// recognitionSystem = dataRecord[3];
		// evalDataEntry.setEntityString(entityString);
		// evalDataEntry.setRecognitionSystem(recognitionSystem);
		// return add(evalDataEntry);
		// }
		// try {
		// end = Integer.parseInt(dataRecord[3]);
		// } catch (NumberFormatException e) {
		// EvaluationDataEntry evalDataEntry = new
		// EvaluationDataEntry(dataRecord[0], dataRecord[1]);
		// entityString = dataRecord[2];
		// if (dataRecord.length > 3)
		// recognitionSystem = dataRecord[3];
		// evalDataEntry.setEntityString(entityString);
		// evalDataEntry.setRecognitionSystem(recognitionSystem);
		// return add(evalDataEntry);
		// }
		// EvaluationDataEntry evalDataEntry = new EvaluationDataEntry(docId,
		// entityId, begin, end);
		// if (dataRecord.length > 4)
		// entityString = dataRecord[4];
		// if (dataRecord.length > 5)
		// recognitionSystem = dataRecord[5];
		// if (dataRecord.length > 6)
		// confidence = dataRecord[6];
		// evalDataEntry.setEntityString(entityString);
		// evalDataEntry.setRecognitionSystem(recognitionSystem);
		// evalDataEntry.setConfidence(confidence);
		// return add(evalDataEntry);
		// }
		// return false;
	}

	@Override
	public boolean addAll(Collection<? extends EvaluationDataEntry> c) {
		return super.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends EvaluationDataEntry> c) {
		return super.addAll(index, c);
	}

	@Override
	public void add(int index, EvaluationDataEntry element) {
		checkMentionMode(element);
		super.add(index, element);
	}

	public Multimap<String, EvaluationDataEntry> organizeByDocument() {
		entriesByDocument = ArrayListMultimap.create();
		for (EvaluationDataEntry entry : this)
			entriesByDocument.put(entry.getDocId(), entry);
		return entriesByDocument;
	}

	public boolean isMentionData() {
		return isMentionData;
	}

	public Multimap<String, EvaluationDataEntry> getEntriesByDocument() {
		return entriesByDocument;
	}

	/**
	 * Reads the given data file with the
	 * {@link EntityEvaluator#DEFAULT_FORMAT}.
	 * 
	 * @param dataFile
	 * @return
	 */
	public static EvaluationData readDataFile(File dataFile) {
		return readDataFile(dataFile, EntityEvaluator.DEFAULT_FORMAT);
	}

	/**
	 * Reads a tab separated file and returns its contents
	 * <tt>EvaluationData</tt>.
	 * 
	 * @param dataFile
	 * @param format
	 * @return
	 */
	public static EvaluationData readDataFile(File dataFile, EvaluationDataFormat format) {
		EvaluationData data = new EvaluationData();
		int i = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				i++;
				String[] splitLine = line.split("\t");
				data.add(splitLine, format);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoOffsetsException e) {
			log.error("Error while reading file \"{}\" on line {}: ",
					new Object[] { dataFile.getAbsolutePath(), i, e });
		}
		return data;
	}
}
