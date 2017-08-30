package dragon.ml.seqmodel.feature;

import dragon.ml.seqmodel.data.*;

/**
 * <p>Window feature type </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


public class FeatureTypeWindow extends AbstractFeatureTypeWrapper {
    protected int currentWindow;
    protected int startPos;
    protected int endPos;
    protected transient DataSequence dataSeq;
    protected Window windows[];
    private int dataLen;

    public FeatureTypeWindow(Window windows[], FeatureType ftype) {
        super(ftype);
        this.windows = windows;
    }

    protected boolean advance(boolean firstCall) {
        int rightB, leftB;

        while (firstCall || !ftype.hasNext()) {
            currentWindow--;
            if (currentWindow < 0) {
                return false;
            }
            if ( (windows[currentWindow].getMaxLength() <endPos+1 - startPos) ||
                (windows[currentWindow].getMinLength() > endPos+1 - startPos))   continue;
            rightB = windows[currentWindow].rightBoundary(startPos, endPos);
            leftB = windows[currentWindow].leftBoundary(startPos,endPos);

            if ( (leftB < dataLen) && (rightB >= 0) && (leftB <= rightB)) {
                ftype.startScanFeaturesAt(dataSeq, Math.max(leftB, 0), Math.min(rightB, dataLen - 1));
                firstCall = false;
            }
        }
        return true;
    }

    public boolean startScanFeaturesAt(DataSequence data, int startPos, int endPos) {
        currentWindow = windows.length;
        dataSeq = data;
        dataLen = dataSeq.length();
        this.startPos = startPos;
        this.endPos =endPos;
        return advance(true);
    }

    public boolean hasNext() {
        return ftype.hasNext() && (currentWindow >= 0);
    }

    public Feature next() {
        FeatureIdentifier id;
        Feature f;

        f=ftype.next();
        id=f.getID();
        id.setName(id.getName() + ".W." + windows[currentWindow]);
        id.setId(id.getId()* windows.length + currentWindow);
        advance(false);
        return f;
    }
}
