package dragon.ir.classification;

import dragon.ir.index.IRDoc;
import dragon.util.*;
import java.io.PrintWriter;
import java.text.DecimalFormat;

/**
 * <p>Evaluating classification results </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class ClassificationEva {
    private double[] arrPrecision, arrRecall, arrF1, arrPn;
    private double precision, recall, f1, mrr;

    public ClassificationEva() {
    }

    public boolean evaluate(int classNum, int[] human, int[][]machine){
    	DocClassSet humanSet, machineSet;
    	int[] rank;
    	int i, j, count;
    	
    	if(human.length!=machine.length)
    		return false;
    	
    	mrr=0;
    	count=0;
    	arrPn=new double[classNum];
    	humanSet=new DocClassSet(classNum);
    	machineSet=new DocClassSet(classNum);
    	
    	for(i=0;i<human.length;i++){
    		humanSet.addDoc(human[i], new IRDoc(i));
    		rank=machine[i];
    		if(rank==null)
    			continue;
    		
    		machineSet.addDoc(rank[0],new IRDoc(i));
    		count++;
    		for(j=0;j<classNum && rank[j]!=human[i];j++);
    		mrr+=1.0/(j+1);
    		for(;j<classNum;j++)
    			arrPn[j]++;
    	}
    	mrr=mrr/count;
    	for(i=0;i<classNum;i++)
    		arrPn[i]=arrPn[i]/count;
    	
    	return evaluate(humanSet,machineSet);
    }
    
    public boolean evaluate(DocClassSet human, DocClassSet machine){
        int[] arrResult;
        int totalCount, humanTotal, machineTotal;
        int i;

        if(human.getClassNum()!=machine.getClassNum()) return false;

        humanTotal=0;
        machineTotal=0;
        totalCount=0;
        arrPrecision=new double[human.getClassNum()];
        arrRecall=new double[human.getClassNum()];
        arrF1=new double[human.getClassNum()];

        for(i=0;i<human.getClassNum();i++){
            arrResult=matchDocs(human.getDocClass(i),machine.getDocClass(i));
            if(arrResult[1]==0)
                arrPrecision[i]=0;
            else
                arrPrecision[i]=arrResult[0]/(double)arrResult[1];
            if(arrResult[2]==0)
                arrRecall[i]=0;
            else
                arrRecall[i]=arrResult[0]/(double)arrResult[2];
            if(arrPrecision[i]==0 || arrRecall[i]==0)
                arrF1[i]=0;
            else
                arrF1[i]=2*arrPrecision[i]*arrRecall[i]/(arrPrecision[i]+arrRecall[i]);
            humanTotal+=arrResult[2];
            machineTotal+=arrResult[1];
            totalCount+=arrResult[0];
        }
        precision=totalCount/(double)machineTotal;
        recall=totalCount/(double)humanTotal;
        if(precision==0 || recall==0)
            f1=0;
        else
            f1=2*precision*recall/(precision+recall);

        return true;
    }

    public double getPrecision(int classID){
        return arrPrecision[classID];
    }

    public double getRecall(int classID){
        return arrRecall[classID];
    }

    public double getFscore(int classID){
        return arrF1[classID];
    }

    public double getMicroPrecision(){
        return precision;
    }

    public double getMicroRecall(){
        return recall;
    }

    public double getMicroFScore(){
        return f1;
    }

    public double getMacroPrecision(){
        return MathUtil.average(arrPrecision);
    }

    public double getMacroRecall(){
        return MathUtil.average(arrRecall);
    }

    public double getMacroFScore(){
        return MathUtil.average(arrF1);
    }

    public double getMRR(){
    	return mrr;
    }
    
    public double getPrecisionN(int top){
    	if(arrPn==null || top>=arrPn.length)
    		return 0;
    	else
    		return arrPn[top];
    }
    
    public void print(PrintWriter out){
        DecimalFormat df1,df2;
        int i;

        try{
            df1=FormatUtil.getNumericFormat(2,0);
            df2=FormatUtil.getNumericFormat(1,2);

            for(i=0;i<arrPrecision.length;i++){
                out.write("Class #"+df1.format(i)+": ");
                out.write(df2.format(arrPrecision[i]*100)+"%/"+df2.format(arrRecall[i]*100)+"%\n");
            }
            out.write("Overall: ");
            out.write(df2.format(precision*100)+"%/"+df2.format(recall*100)+"%\n");
            out.flush();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private int[] matchDocs(DocClass human, DocClass machine){
        int[] arrResult;
        int i, j, humanNum, machineNum;
        int humanIndex, machineIndex;
        int correct;
        humanNum=human.getDocNum();
        machineNum=machine.getDocNum();
        i=0;
        j=0;
        correct=0;
        while(i<humanNum && j<machineNum){
            humanIndex=human.getDoc(i).getIndex();
            machineIndex=machine.getDoc(j).getIndex();
            if(humanIndex==machineIndex){
                correct++;
                i++;
                j++;
            }
            else if(humanIndex<machineIndex){
                i++;
            }
            else{
                j++;
            }
        }
        arrResult=new int[3];
        arrResult[0]=correct;
        arrResult[1]=machineNum;
        arrResult[2]=humanNum;
        return arrResult;
    }
}