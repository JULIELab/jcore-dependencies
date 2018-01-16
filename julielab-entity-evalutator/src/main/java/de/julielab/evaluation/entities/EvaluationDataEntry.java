package de.julielab.evaluation.entities;

import java.util.Comparator;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;

public class EvaluationDataEntry implements Comparable<EvaluationDataEntry> {

	public enum ComparisonType {
		EXACT, OVERLAP
	}

	public enum OverlapType {
		CHARS, PERCENT
	}

	public static Comparator<EvaluationDataEntry> beginComparator = new Comparator<EvaluationDataEntry>() {

		@Override
		public int compare(EvaluationDataEntry o1, EvaluationDataEntry o2) {
			return o1.getBegin() - o2.getBegin();
		}

	};

	private String docId;

	private String entityId;
	
	private String entityType;

	// public int begin;
	// public int end;
	private String entityString;

	private String recognitionSystem;

	private Range<Integer> offsetRange;

	private ComparisonType comparisonType;

	private OverlapType overlapType;

	private int overlapSize;

	private String confidence;

	private Object referenceObject;

	/**
	 * Constructs a document-wise evaluation data entry. It is just known that
	 * the entity with ID <tt>entityId</tt> occurs in the document with ID
	 * <tt>docId</tt>. The exact location of the entity within the document text
	 * is unknown.
	 * 
	 * @param docId
	 * @param entityId
	 */
	public EvaluationDataEntry(String docId, String entityId) {
		this(docId, entityId, -1, -1);
	}

	public EvaluationDataEntry(String docId, String entityId, int begin, int end) {
		this.docId = docId;
		this.entityId = entityId;
		offsetRange = Range.between(begin, end);
		comparisonType = ComparisonType.EXACT;
		overlapType = OverlapType.PERCENT;
		overlapSize = 100;
		entityType = "entity";
	}
	
	public EvaluationDataEntry(String docId, String entityId, int begin, int end, String entityString, String recognitionSystem, String entityType) {
		this(docId, entityId, begin, end);
		this.entityString = entityString;
		this.recognitionSystem = recognitionSystem;
		this.entityType = entityType;
	}

	public EvaluationDataEntry(String docId, String entityId, String entityString, String recognitionSystem) {
		this(docId, entityId, -1, -1);
		this.entityString = entityString;
		this.recognitionSystem = recognitionSystem;
	}
	
	public EvaluationDataEntry(String docId, String entityId, String entityString, String recognitionSystem, String entityType) {
		this(docId, entityId, -1, -1);
		this.entityString = entityString;
		this.recognitionSystem = recognitionSystem;
		this.entityType = entityType;
	}
	
	

	public EvaluationDataEntry() {
		this(null, null, -1, -1);
	}

	@Override
	public int compareTo(EvaluationDataEntry o) {
		boolean equal = this.equals(o);
		if (equal)
			return 0;
		return this.makeComparisonString().compareTo(o.makeComparisonString());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EvaluationDataEntry other = (EvaluationDataEntry) obj;
		if (docId == null) {
			if (other.docId != null)
				return false;
		} else if (!docId.equals(other.docId))
			return false;
		if (entityId == null) {
			if (other.entityId != null)
				return false;
		} else if (!entityId.equals(other.entityId))
			return false;
		if (!hasComparableOffset(other))
			return false;
		return true;
	}

	public int getBegin() {
		return offsetRange.getMinimum();
	}

	public ComparisonType getComparisonType() {
		return comparisonType;
	}

	public String getConfidence() {
		return confidence;
	}

	public String getDocId() {
		return docId;
	}

	public int getEnd() {
		return offsetRange.getMaximum();
	}

	public String getEntityId() {
		return entityId;
	}

	public String getEntityString() {
		return entityString;
	}

	public Range<Integer> getOffsetRange() {
		return offsetRange;
	}

	public int getOverlapSize() {
		return overlapSize;
	}

	public OverlapType getOverlapType() {
		return overlapType;
	}

	public String getRecognitionSystem() {
		return recognitionSystem;
	}

	/**
	 * Returns true, if this and the <tt>otherEntry</tt> are both mentions and
	 * have comparable offsets. What 'comparable' exactly is, is subject to
	 * definition and may range from exact matching to loose overlapping.
	 * 
	 * @param otherEntry
	 * @return
	 */
	public boolean hasComparableOffset(EvaluationDataEntry other) {
		if (isMention() && other.isMention()) {
			if (!offsetRange.isOverlappedBy(other.offsetRange))
				return false;
			if (comparisonType == ComparisonType.EXACT) {
				if (!offsetRange.equals(other.offsetRange))
					return false;
				// if (offsetRange.getMinimum() !=
				// other.offsetRange.getMinimum())
				// return false;
				// if (offsetRange.getMaximum() !=
				// other.offsetRange.getMaximum())
				// return false;
			} else if (comparisonType == ComparisonType.OVERLAP) {
				Range<Integer> intersection = offsetRange.intersectionWith(other.offsetRange);
				int intersectionLength = intersection.getMaximum() - intersection.getMinimum();
				if (overlapType == OverlapType.CHARS && intersectionLength < overlapSize)
					return false;
				if (overlapType == OverlapType.PERCENT) {
					int thisMentionLength = offsetRange.getMaximum() - offsetRange.getMinimum();
					int otherMentionLength = other.offsetRange.getMaximum() - other.offsetRange.getMinimum();
					int thisOverlapPercent = (int) Math.ceil((double) intersectionLength / thisMentionLength * 100d);
					int otherOverlapPercent = (int) Math.ceil((double) intersectionLength / otherMentionLength * 100d);
					if (thisOverlapPercent < overlapSize || otherOverlapPercent < overlapSize)
						return false;
				}
			}
		}
		return true;
		// if (!isMention())
		// return false;
		// if (!otherEntry.isMention())
		// return false;
		// if (begin == otherEntry.begin && end == otherEntry.end)
		// return true;
		// return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + offsetRange.getMinimum();
		result = prime * result + ((docId == null) ? 0 : docId.hashCode());
		result = prime * result + offsetRange.getMaximum();
		result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
		return result;
	}

	/**
	 * Tests, whether this entry describes a specific entity mention in text. If
	 * <tt>false</tt>, that means that this entry is document-wide index term
	 * with no - or unknown - location within the document.
	 * 
	 * @return <tt>true</tt>, iff <tt>begin</tt> and <tt>end</tt> are greater or
	 *         equal to zero and this offset describes a non-empty span, i.e.
	 *         <tt>begin</tt> &lt; <tt>end</tt>.
	 */
	public boolean isMention() {
		return offsetRange.getMinimum() >= 0 && offsetRange.getMaximum() >= 0
				&& offsetRange.getMinimum() <= offsetRange.getMaximum();
	}

	private String makeComparisonString() {
		StringBuilder sb = new StringBuilder();
		sb.append(docId);
		sb.append(offsetRange);
		sb.append(entityId);
		return sb.toString();
	}

	public boolean overlaps(EvaluationDataEntry other) {
		return offsetRange.isOverlappedBy(other.offsetRange);
	}

	public void setBegin(int begin) {
		Integer end = offsetRange != null ? offsetRange.getMaximum() : begin;
		if (end < begin)
			end = begin;
		offsetRange = Range.between(begin, end);
	}

	public void setComparisonType(ComparisonType comparisonType) {
		this.comparisonType = comparisonType;
	}

	public void setConfidence(String confidence) {
		this.confidence = confidence;

	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	public void setEnd(int end) {
		Integer begin = offsetRange != null ? offsetRange.getMinimum() : end;
		if (begin > end)
			begin = end;
		offsetRange = Range.between(begin, end);
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public void setEntityString(String entityString) {
		this.entityString = entityString;
	}

	public void setOverlapSize(int overlapSize) {
		this.overlapSize = overlapSize;
	}

	public void setOverlapType(OverlapType overlapType) {
		this.overlapType = overlapType;
	}

	public void setRecognitionSystem(String recognitionSystem) {
		this.recognitionSystem = recognitionSystem;
	}

	/**
	 * Returns an <tt>EvaluationDataEntry</tt> without offset information (all
	 * offsets set to 0). This way, the returned data entry can be used for
	 * evaluations on the document level where the concrete placing within a
	 * document is ignored.
	 * <p>
	 * The returned data entry is a new object if this entry is a mention, i.e.
	 * has non-0 offsets. Otherwise, this instance is returned.
	 * </p>
	 * 
	 * @return
	 */
	public EvaluationDataEntry toDocWiseEntry() {
		return isMention() ? new EvaluationDataEntry(docId, entityId, entityString, recognitionSystem) : this;
	}

	public String toShortString() {
		String entityId = StringUtils.isBlank(this.entityId) ? "0" : this.entityId;
		String ret = docId + " " + entityId + " " + entityString + " " + offsetRange.getMinimum() + "-"
				+ offsetRange.getMaximum();
		if (null != confidence)
			ret += "\t(" + confidence + ")";
		if (null != recognitionSystem)
			ret += "\t(" + recognitionSystem + ")";
		return ret;

	}

	@Override
	public String toString() {
		return "EvaluationDataEntry [docId=" + docId + ", entityId=" + entityId + ", begin=" + offsetRange.getMinimum()
				+ ", end=" + offsetRange.getMaximum() + ", entityString=" + entityString + ", recognitionSystem="
				+ recognitionSystem + "]";
	}

	/**
	 * The reference object to this evaluation data entry. May be null.
	 * 
	 * @return The original classified object to which this evaluation data
	 *         entry corresponds.
	 */
	public Object getReferenceObject() {
		return referenceObject;
	}

	/**
	 * The referenceObject field allows evaluation data entries to be associated
	 * with their original classified object. It is completely ignored during
	 * the evaluation and just provides a means to easily retrieve the
	 * classified object for the evaluation data after it has been determined
	 * for each entry whether it is correct or not.
	 * 
	 * @param referenceObject
	 *            The original classification object.
	 */
	public void setReferenceObject(Object referenceObject) {
		this.referenceObject = referenceObject;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

}
