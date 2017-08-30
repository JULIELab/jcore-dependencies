package dragon.ir.topicmodel;

import dragon.ir.index.IndexReader;
import dragon.matrix.vector.DoubleVector;
import java.io.PrintWriter;

/**
 * <p>Abstract model implements basic functions for topic model which can be inherited by other classes</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class AbstractModel {
    protected int iterations;
    protected int seed;
    protected PrintWriter statusOut;

    public AbstractModel() {
        seed=-1;
        iterations=100;
    }

    public int getIterationNum(){
        return iterations;
    }

    public void setIterationNum(int num) {
        this.iterations = num;
    }

    public void setRandomSeed(int seed) {
        this.seed = seed;
    }

    public void setStatusOut(PrintWriter out){
        this.statusOut = out;
    }

    protected void printStatus(String line){
        try{
            System.out.println(line);
            if(statusOut!=null){
                statusOut.write(line + "\n");
                statusOut.flush();
            }
        }
        catch(Exception e){
            e.printStackTrace() ;
        }
    }

    protected DoubleVector getBkgModel(IndexReader indexReader){
        DoubleVector bkgModel;
        int i;

        bkgModel=new DoubleVector(indexReader.getCollection().getTermNum());
        for(i=0;i<bkgModel.size();i++)
            bkgModel.set(i,indexReader.getIRTerm(i).getFrequency());
        bkgModel.multiply(1.0/indexReader.getCollection().getTermCount());
        return bkgModel;
    }
}