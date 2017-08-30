package dragon.util;

/**
 * <p>Math related utilities</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class MathUtil {
    public static double LOG0 = -1*Double.MAX_VALUE;
    public static double LOG2 = 0.69314718055;
    private static final double MINUS_LOG_EPSILON = 30; //-1*Math.log(Double.MIN_VALUE);

    public static void initArray(double[] array, double initVal){
        for(int i=0;i<array.length;i++)
            array[i]=initVal;
    }

    public static void initArray(int[] array, int initVal){
        for(int i=0;i<array.length;i++)
            array[i]=initVal;
    }

    public static void copyArray(double[] srcArray, double[] destArray){
        for(int i=0;i<srcArray.length;i++)
            destArray[i]=srcArray[i];
    }

    public static void multiArray(double[] array, double multiplier){
        for(int i=0;i<array.length;i++)
            array[i]*=multiplier;
    }

    public static double sumArray(double[] array){
        double sum;
        int i;

        sum=0;
        i=0;
        while(i<array.length){
            sum+=array[i];
            i++;
        }
        return sum;
    }

    public static int sumArray(int[] array){
        int sum;
        int i;

        sum=0;
        i=0;
        while(i<array.length){
            sum+=array[i];
            i++;
        }
        return sum;
    }

    public static void sumArray(double[] array, double[] incArray){
        for(int i=0;i<array.length;i++)
            array[i]+=incArray[i];
    }

    public static double max(double[] array){
        double max;
        int  i;

        max=array[0];
        i=1;
        while(i<array.length){
            if(array[i]>max)
                max=array[i];
            i++;
        }
        return max;
    }

    public static int max(int[] array){
        int max;
        int  i;

        max=array[0];
        i=1;
        while(i<array.length){
            if(array[i]>max)
                max=array[i];
            i++;
        }
        return max;
    }


    public static double min(double[] array){
        double min;
        int  i;

        min=array[0];
        i=1;
        while(i<array.length){
            if(array[i]<min)
                min=array[i];
            i++;
        }
        return min;
    }

    public static int min(int[] array){
        int min;
        int  i;

        min=array[0];
        i=1;
        while(i<array.length){
            if(array[i]<min)
                min=array[i];
            i++;
        }
        return min;
    }

    public static double average(double[] array){
        double sum;
        int  i;

        sum=0;
        for(i=0;i<array.length;i++)
            sum+=array[i];
        return sum/array.length;
    }

    public static double average(int[] array){
        double sum;
        int  i;

        sum=0;
        for(i=0;i<array.length;i++)
            sum+=array[i];
        return sum/array.length;
    }

    public static int maxElementInArray(double[] array){
        double max;
        int maxIndex, i;

        max=array[0];
        maxIndex=0;
        i=1;
        while(i<array.length){
            if(array[i]>max){
                max=array[i];
                maxIndex=i;
            }
            i++;
        }
        return maxIndex;
    }

    public static int maxElementInArray(int[] array){
        double max;
        int maxIndex, i;

        max=array[0];
        maxIndex=0;
        i=1;
        while(i<array.length){
            if(array[i]>max){
                max=array[i];
                maxIndex=i;
            }
            i++;
        }
        return maxIndex;
    }

    public static int[] rankElementInArray(double[] array, boolean desc){
        int[] rank;
        int i, j, count;

        rank=new int[array.length];
        initArray(rank,-1);
        for(i=0;i<array.length;i++){
            count=0;
            for(j=0;j<array.length;j++)
                if(array[j]>array[i])
                    count++;
            if(desc){
                while(rank[count]>=0) count++;
                rank[count]=i;
            }
            else{
                while(rank[array.length-1-count]>=0) count++;
                rank[array.length-1-count]=i;
            }
        }
        return rank;
    }
    
    public static int[] rankElementInArray(int[] array, boolean desc){
        int[] rank;
        int i, j, count;

        rank=new int[array.length];
        initArray(rank,-1);
        for(i=0;i<array.length;i++){
            count=0;
            for(j=0;j<array.length;j++)
                if(array[j]>array[i])
                    count++;
            if(desc){
                while(rank[count]>=0) count++;
                rank[count]=i;
            }
            else{
                while(rank[array.length-1-count]>=0) count++;
                rank[array.length-1-count]=i;
            }
        }
        return rank;
    }

    public static double exp(double d) {
        if (Double.isInfinite(d) || ((d < 0) && (Math.abs(d) > MINUS_LOG_EPSILON)))
            return 0;
        return Math.exp(d);
    }

    public static double log(double val) {
        return (Math.abs(val-1) < Double.MIN_VALUE)?0:Math.log(val);
    }

    public static double logSumExp(double v1, double v2) {
        double vmin, vmax;

        if (Math.abs(v1 - v2) < Double.MIN_VALUE)
            return v1 + LOG2;
        vmin = Math.min(v1, v2);
        vmax = Math.max(v1, v2);
        if (vmax > vmin + MINUS_LOG_EPSILON)
            return vmax;
        else
            return vmax + Math.log(Math.exp(vmin-vmax) + 1.0);
    }

    public static void logSumExp(double[] v1, double[] v2) {
        int i;

        for (i = 0; i < v1.length; i++) {
            v1[i]=logSumExp(v1[i], v2[i]);
        }
    }

    public static double logSumExp(double[] logArray) {
        double ret;
        int i;

        ret=logArray[0];
        for(i=1;i<logArray.length;i++)
            ret=logSumExp(ret,logArray[i]);
        return ret;
    }
}