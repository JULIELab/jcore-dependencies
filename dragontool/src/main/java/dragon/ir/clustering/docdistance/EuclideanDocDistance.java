package dragon.ir.clustering.docdistance;
import dragon.ir.index.*;
import dragon.matrix.*;

/**
 * <p>Document similarity or distance based on Euclidean distance </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Drexel University</p>
 * @author Xiaodan Zhang, Davis Zhou
 * @version 1.0
 */

public class EuclideanDocDistance extends AbstractDocDistance{

     public EuclideanDocDistance(SparseMatrix doctermMatrix) {
         super(doctermMatrix);
     }

     public double getDistance(IRDoc first, IRDoc second){
         int firstNum,secondNum,x,y, firstIndexList[], secondIndexList[];
         double sum;
         double firstScoreList[], secondScoreList[];

         firstScoreList = matrix.getNonZeroDoubleScoresInRow(first.getIndex());
         secondScoreList=matrix.getNonZeroDoubleScoresInRow(second.getIndex());
         firstIndexList=matrix.getNonZeroColumnsInRow(first.getIndex());
         secondIndexList=matrix.getNonZeroColumnsInRow(second.getIndex());
         firstNum = firstScoreList.length;
         secondNum = secondScoreList.length;

         x=0;
         y=0;
         sum=0;

         while (x < firstNum && y < secondNum) {
             if (firstIndexList[x] < secondIndexList[y]){
                 if(featureFilter==null || featureFilter.map(firstIndexList[x])>=0)
                     sum+=firstScoreList[x]*firstScoreList[x];
                 x++;
             }
             else if (firstIndexList[x]==secondIndexList[y]) {
                 if(featureFilter==null || featureFilter.map(firstIndexList[x])>=0)
                     sum+=(firstScoreList[x]-secondScoreList[y])*(firstScoreList[x]-secondScoreList[y]);
                 x++;
                 y++;
             }
             else{
                 if(featureFilter==null || featureFilter.map(secondIndexList[y])>=0)
                     sum=sum+secondScoreList[y]*secondScoreList[y];
                 y++;
             }
         }
         while(x<firstNum){
             if(featureFilter==null || featureFilter.map(firstIndexList[x])>=0)
                 sum+=firstScoreList[x]*firstScoreList[x];
             x++;
         }
         while(y<secondNum){
             if(featureFilter==null || featureFilter.map(secondIndexList[y])>=0)
                 sum+=secondScoreList[y]*secondScoreList[y];
             y++;
         }
         return Math.sqrt(sum);
     }
}