package dragon.ml.seqmodel.data;

/**
 * <p>Interface of Label Converter </p>
 * <p>The internal labels for sequence models such as CRF and HMM are integer-based and start from zero. However, human-annotated lables are
 * usually string-based. Thus, it is necessary to convert.</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface LabelConverter {
    /**
     * Converts external integer-based labels to internal integer-based labels
     * @param externalLabel the external label
     * @return the label for internal use
     */
    public int getInternalLabel(int externalLabel);

    /**
     * Converts external string-based labels to internal integer-based labels
     * @param externalLabel the external label
     * @return the label for internal use
     */
    public int getInternalLabel(String externalLabel);

    /**
     * Converts internal integer-based labels to external integer-based labels
     * @param internalLabel the internal label
     * @return the label for external use
     */
    public int getExternalLabelID(int internalLabel);

    /**
     * Converts internal integer-based labels to external string-based labels
     * @param internalLabel the internal label
     * @return the label for external use
     */
    public String getExternalLabelString(int internalLabel);
}