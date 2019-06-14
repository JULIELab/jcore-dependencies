package dragon.ir.clustering.docdistance;

import dragon.ir.index.IRDoc;
import dragon.matrix.SparseMatrix;

/**
 * <p>Cosine document similarity/distance measure </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class CosineDocDistance extends AbstractDocDistance{

    public CosineDocDistance(SparseMatrix doctermMatrix){
        super(doctermMatrix);
    }

    public double getDistance(IRDoc first, IRDoc second){
        int firstIndexList[], secondIndexList[];
        double firstScoreList[], secondScoreList[];

         firstScoreList = matrix.getNonZeroDoubleScoresInRow(first.getIndex());
         secondScoreList=matrix.getNonZeroDoubleScoresInRow(second.getIndex());
         firstIndexList=matrix.getNonZeroColumnsInRow(first.getIndex());
         secondIndexList=matrix.getNonZeroColumnsInRow(second.getIndex());
         return 1 - cosine(firstIndexList,firstScoreList,secondIndexList,secondScoreList);
    }

    private double cosine(int[] arrXCol, double[] arrXScore, int[] arrYCol, double[] arrYScore){
        int xNum, yNum, x, y;
        double x2, y2, xy;

        if(arrXCol==null || arrYCol==null)
            return 0;
        xNum = arrXCol.length;
        yNum = arrYCol.length;
        if(xNum==0 || yNum==0)
            return 0;
        x = 0;
        y = 0;
        xy=0;
        x2=0;
        y2=0;

        while (x < xNum && y < yNum) {
            if (arrXCol[x] < arrYCol[y]) {
                if(featureFilter==null || featureFilter.map(arrXCol[x])>=0)
                    x2+= arrXScore[x] * arrXScore[x];
                x++;
            }
            else if (arrXCol[x] == arrYCol[y]) {
                if(featureFilter==null || featureFilter.map(arrXCol[x])>=0){
                    xy+=arrXScore[x] * arrYScore[y];
                    x2+=arrXScore[x] * arrXScore[x];
                    y2+=arrYScore[y] * arrYScore[y];
                }
                x++;
                y++;

            }
            else {
                if(featureFilter==null || featureFilter.map(arrYCol[y])>=0)
                    y2+=arrYScore[y] * arrYScore[y];
                y++;
            }
        }
        while(y<yNum)
        {
            if(featureFilter==null || featureFilter.map(arrYCol[y])>=0)
                y2+=arrYScore[y] * arrYScore[y];
            y++;
        }
        while(x<xNum)
        {
            if(featureFilter==null || featureFilter.map(arrXCol[x])>=0)
                x2+= arrXScore[x] * arrXScore[x];
            x++;
        }
        return xy/(java.lang.Math.sqrt(x2)*java.lang.Math.sqrt(y2));
    }
}
