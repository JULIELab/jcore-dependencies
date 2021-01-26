package dragon.matrix.vector;

import dragon.util.MathUtil;
/**
 * <p>Data structure for vector of integer type</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class IntVector implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private int[] vector;

    public IntVector(int size) {
        vector=new int[size];
    }

    public IntVector(int[] vector){
        this.vector =vector;
    }

    public IntVector copy(){
        int[] newVector;

        newVector=new int[vector.length];
        System.arraycopy(vector,0,newVector,0,vector.length);
        return new IntVector(newVector);
    }

    public void assign(int initValue){
        for(int i=0;i<vector.length;i++)
            vector[i]=initValue;
    }

    public void assign(IntVector newVector){
        for(int i=0;i<vector.length;i++)
            vector[i]=newVector.get(i);
    }

    public void multiply(int rate){
        for(int i=0;i<vector.length;i++)
            vector[i]*=rate;
    }

    public void add(IntVector newVector){
        if(vector.length!=newVector.size())
            return;
        for(int i=0;i<vector.length;i++)
            vector[i]+=newVector.get(i);
    }

    public void add(int index, int inc){
        vector[index]+=inc;
    }

    public int get(int index){
        return vector[index];
    }

    public void set(int index, int value){
        vector[index]=value;
    }

    public int size(){
        return vector.length;
    }

    public double distance(){
        int sum;
        int i;

        sum=0;
        for(i=0;i<vector.length;i++){
            sum+=vector[i]*vector[i];
        }
        return Math.sqrt(sum);
    }

    public double distance(IntVector origin){
        int sum, a;
        int i;

        if(vector.length!=origin.size())
            return -1;

        sum=0;
        for(i=0;i<vector.length;i++){
            a=vector[i]-origin.get(i);
            sum+=a*a;
        }
        return Math.sqrt(sum);
    }

    public int[] rank(boolean desc){
    	return MathUtil.rankElementInArray(vector, desc);
    }
    
    public int getDimWithMaxValue(){
        return MathUtil.maxElementInArray(vector);
    }

    public int getMaxValue(){
        return MathUtil.max(vector);
    }

    public int getMinValue(){
        return MathUtil.min(vector);
    }

    public double getAvgValue(){
        return MathUtil.average(vector);
    }

    public int getSummation(){
        return MathUtil.sumArray(vector);
    }

    public int dotProduct(IntVector newVector){
        int i;
        int product;

        product=0;
        for(i=0;i<vector.length;i++)
            product+=vector[i]*newVector.get(i);
        return product;
    }
}
