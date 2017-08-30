package dragon.ml.seqmodel.crf;

/**
 * <p>Edge seperator</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

/**
 if the original label set is A (0), B (1), C (2), D (3) and markov order is 2, the new label set will be:
 AA, BA, CA, DA, AB, BB, CB, DB, AC, BC, CC, DC, DA, DB, DC, DD
  0   1   2   3   4   5   6   7   8   9  10  11  12  13  14  15
 */

public class EdgeGenerator {
    private int offset;
    private int numOrigY;
    private int markovOrder;

    public EdgeGenerator(int originalLabelNum) {
        this(1,originalLabelNum);
    }

    public EdgeGenerator(int markovOrder, int originalLabelNum) {
        offset = 1;
        for (int i = 0; i < markovOrder - 1; i++)
            offset *= originalLabelNum;
        this.numOrigY = originalLabelNum;
        this.markovOrder = markovOrder;
    }

    // to get the first label (or state) which can transfer to the specified label (state) by @destY
    public int first(int destY) {
        return destY/ numOrigY;
    }

    // to get the label (or state) which is after currentSrcY and can transfer to the specified label (state) by @destY
    public int next(int destY, int currentSrcY) {
        return currentSrcY + offset;
    }

    // to get the first possible label for the current position
    public int firstLabel(int pos) {
        return 0;
    }

    // to get the label after currentLabel for the current position
    public int nextLabel(int currentLabel, int pos) {
        if ( (pos >= markovOrder - 1) || (currentLabel < numOrigY - 1))
            return currentLabel + 1;
        if (currentLabel >= Math.pow(numOrigY, (pos + 1)))
            return numOrigY * offset;
        else
            return currentLabel + 1;
    }
};
