package dragon.ml.seqmodel.feature;

/**
 * <p>Data structor for a window</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public  class Window {
    private int start;
    private boolean startRelativeToLeft;
    private int end;
    private boolean endRelativeToLeft;
    private String winName = null;
    private int maxLength = Integer.MAX_VALUE;
    private int minLength = 1;

    public Window(int start, boolean startRelativeToLeft, int end, boolean endRelativeToLeft) {
        this(start, startRelativeToLeft, end, endRelativeToLeft, null);
        String startB = startRelativeToLeft ? "L" : "R";
        String endB = endRelativeToLeft ? "L" : "R";
        winName = startB + start + endB + end;
    }

    public Window(int start, boolean startRelativeToLeft, int end, boolean endRelativeToLeft, String winName) {
        this.start = start;
        this.startRelativeToLeft = startRelativeToLeft;
        this.end = end;
        this.endRelativeToLeft = endRelativeToLeft;
        this.winName = winName;
    }

    public Window(int start, boolean startRelativeToLeft, int end, boolean endRelativeToLeft, String winName, int minWinLength, int maxWinLength) {
        this(start, startRelativeToLeft, end, endRelativeToLeft, winName);
        this.maxLength = maxWinLength;
        this.minLength = minWinLength;
    }

    public int leftBoundary(int segStart, int segEnd) {
        if (startRelativeToLeft) {
            return boundary(segStart, start);
        }
        return boundary(segEnd, start);
    }

    public int rightBoundary(int segStart, int segEnd) {
        if (endRelativeToLeft) {
            return boundary(segStart, end);
        }
        return boundary(segEnd, end);
    }

    private int boundary(int boundary, int offset) {
        return boundary + offset;
    }

    public int getMinLength(){
        return minLength;
    }

    public int getMaxLength(){
        return maxLength;
    }

    public String toString() {
        return winName;
    }
}
