package de.julielab.xml.binary;

import org.apache.commons.lang3.Range;

/**
 * <p>This is the base class for {@link Element} and {@link JeDISAttribute} which represent parts of an XMI document.</p>
 */
public class DataRange {
    private Range<Integer> range;
    private boolean toBeOmitted;

    public boolean isToBeOmitted() {
        return toBeOmitted;
    }

    public void setToBeOmitted(boolean toBeOmitted) {
        this.toBeOmitted = toBeOmitted;
    }

    public Range<Integer> getRange() {
        return range;
    }

    public void setRange(Range<Integer> range) {
        this.range = range;
    }

    public void setRange(int begin, int end) {
        this.range = Range.between(begin, end);
    }

    public int getLength() {
        return range.getMaximum() - range.getMinimum();
    }

    public int getBegin() {
        return range.getMinimum();
    }

    public int getEnd() {
        return range.getMaximum();
    }

    @Override
    public String toString() {
        return range.toString();
    }
}
