package martin.common;

import java.io.File;


public class ChiTester {

	private double[][] vals;
	
	public ChiTester(File file){
		double[][] csvvals = Misc.loadCSV(file);
		double[][] vals = new double[csvvals.length][2]; 
		
		for (int i = 0; i < csvvals.length; i++){
			//int k = (int) vals[i][0];
			double X2 = csvvals[i][1];
			double p = csvvals[i][2];
			
			vals[i][0] = X2;
			vals[i][1] = p;
		}
		
		this.vals = vals;
	}

	public String doTest(double X2){
		for (int i = 0; i < vals.length-1; i++){
			double X2_1 = vals[i][0];
			double X2_2 = vals[i+1][0];
			
			if ((X2 >= X2_1) && (X2 < X2_2)){
				if (Math.abs(X2-X2_1) < Math.abs(X2 - X2_2))
					return "X2: " + Misc.round(X2,4) + "; best match: " + X2_1 + " => " + Misc.round(vals[i][1],4);
				else
					return "X2: " + Misc.round(X2,4) + "; best match: " + X2_2 + " => " + Misc.round(vals[i+1][1],4);
			}
		}
		
		return "X2: " + Misc.round(X2,4) + ", best match: " + vals[vals.length-1][0] + " => " + vals[vals.length-1][1];
	}
	
	public String doTest(double o1, double e1, double o2, double e2){
		double X2 = (o1 - e1) * (o1 - e1) / e1 + (o2 - e2) * (o2 - e2) / e2;
		return doTest(X2);
	}
	
	public String doTest(double observed, double total, double expectedRatio){
		double o1 = observed;
		double e1 = total * expectedRatio;
		double o2 = total - observed;
		double e2 = total - e1;
		
		return doTest(o1,e1,o2,e2);
	}
		
	public void printValues(){
		for (int i = 0; i < vals.length; i++){
			for (int j = 0; j < vals[0].length; j++)
				System.out.print(vals[i][j] + "\t");
			
			System.out.println();
		}
		
	}
	

}
