package uk.ac.man.entitytagger.evaluate;

public class Result{
	int tp=0,fp=0,fn=0,fpfn=0;
	public Result(){}
	
	public double getRecall(){
		return ((double)tp)/((double)(tp+fn));
	}
	
	public double getPrecision(){
		return ((double)tp)/((double)(tp+fp));
	}
	
	public double getFScore(){
		double r = ((double)tp)/((double)(tp+fn));
		double p = ((double)tp)/((double)(tp+fp));
		return 2/(1/p+1/r);
	}
	
	public static String getHeader(){
		return "TP,FP,FN,FPFN,r,p,F";
	}
	
	public String toString() {
		double r = ((double)tp)/((double)(tp+fn));
		double p = ((double)tp)/((double)(tp+fp));
		double f = 2/(1/p+1/r);

		String res = tp + "," + fp + "," + fn + "," + fpfn + "," + r + "," + p + "," + f;
		return res;
	}
}
