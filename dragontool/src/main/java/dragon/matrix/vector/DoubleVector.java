package dragon.matrix.vector;

import dragon.util.MathUtil;
/**
 * <p>Data structure for vector of double type</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class DoubleVector implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private double[] vector;

    public DoubleVector(int size) {
        vector=new double[size];
    }

    public DoubleVector(double[] vector){
        this.vector =vector;
    }

    public DoubleVector copy(){
        double[] newVector;

        newVector=new double[vector.length];
        System.arraycopy(vector,0,newVector,0,vector.length);
        return new DoubleVector(newVector);
    }

    public void assign(double initValue){
        for(int i=0;i<vector.length;i++)
            vector[i]=initValue;
    }

    public void assign(DoubleVector newVector){
        for(int i=0;i<vector.length;i++)
            vector[i]=newVector.get(i);
    }

    public void multiply(double rate){
        for(int i=0;i<vector.length;i++)
            vector[i]*=rate;
    }

    public void add(DoubleVector newVector){
        if(vector.length!=newVector.size())
            return;
        for(int i=0;i<vector.length;i++)
            vector[i]+=newVector.get(i);
    }

    public void add(int index, double inc){
        vector[index]+=inc;
    }

    public double get(int index){
        return vector[index];
    }

    public void set(int index, double value){
        vector[index]=value;
    }

    public int size(){
        return vector.length;
    }

    public double distance(){
        double sum;
        int i;

        sum=0;
        for(i=0;i<vector.length;i++){
            sum+=vector[i]*vector[i];
        }
        return Math.sqrt(sum);
    }

    public double distance(DoubleVector origin){
        double sum, a;
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

    public double getMaxValue(){
        return MathUtil.max(vector);
    }

    public double getMinValue(){
        return MathUtil.min(vector);
    }

    public double getAvgValue(){
        return MathUtil.average(vector);
    }

    public double getSummation(){
        return MathUtil.sumArray(vector);
    }

    public double dotProduct(DoubleVector newVector){
        int i;
        double product;

        product=0;
        for(i=0;i<vector.length;i++)
            product+=vector[i]*newVector.get(i);
        return product;
    }
}