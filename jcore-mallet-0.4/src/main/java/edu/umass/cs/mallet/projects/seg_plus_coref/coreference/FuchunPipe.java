package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import com.wcohen.secondstring.*;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.classify.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import edu.umass.cs.mallet.base.util.*;
import java.util.*;
import java.lang.*;
import java.io.*;


/*
	Various string-edit based features from Fuchun

*/

public class FuchunPipe extends Pipe
{
	double default_Max_Dist = 0;
	double default_Ignore_Dist = -1;

	StringDistance distMetric;
	static String[] startFields = new String[]
	{"authorsString", "title", "booktitle", "publisher", "journal","date", "location", "pages",
	 "note", "institution", "editor",  "volume", "tech"};

	static double[] tagWeight = new double[]{1.0, 1.2, 0.5, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};

	public FuchunPipe (StringDistance d) {
		this.distMetric = d;
	}

	public Instance pipe (Instance carrier) {

        assert(false); // todo: fix this pipe!

		NodePair pair = (NodePair)carrier.getData();
		Citation s1 = (Citation)pair.getObject1();
		Citation s2 = (Citation)pair.getObject2();

		double dist = computeObjDistance (s1, s2, 1);
		/*
		if (dist > 0.75)
		pair.setFeatureValue ("FuchunMetric", 1.0);*/
		pair.setFeatureValue ("FuchunMetric", dist);
		return carrier;
	}

	protected double computeObjDistance(Citation instance1, Citation instance2, int N)
	{

		// if using the SGML string, use the following method
//		return computeObjDistance((CharSequence)(instance1.getSource()), (CharSequence)(instance2.getSource()));// for taggged data

		//if two instances are of different types: conference paper (booktitle), journal paper (journal), tech report or thesis (tech)
		//they are considered different papers
		int sametype = 0;
		String type1 = "";
		if( (String)instance1.getField(Citation.booktitle) != "" ) type1 = "booktitle";
		else if( (String)instance1.getField(Citation.journal) != "") type1 = "journal";
		else if( (String)instance1.getField(Citation.tech) != "") type1 = "tech";

		String type2 = "";
		if( (String)instance2.getField(Citation.booktitle) != "" ) type2 = "booktitle";
		else if( (String)instance2.getField(Citation.journal) != "") type2 = "journal";
		else if( (String)instance2.getField(Citation.tech) != "") type2 = "tech";

		if(type2.equals(type1) && !type1.equals("") ) sametype = 1;
		else if( type1 == "" || type2 == "" ) sametype = 0;	
		else sametype = -1;

		// if using the field information stored in property list, use the
		// following method
		int numFields = startFields.length;
		

		int usedNumFields = 0;
		double dist = 0;
		double totalWeight = 0;
		for(int i=0; i<numFields; i++){
			String str1 = (String)instance1.getField(startFields[i]);
			String str2 = (String)instance2.getField(startFields[i]);

			if (str1 != null && str2 != null && !str1.equals("") && !str2.equals("")) {
			tagWeight[i] = Maths.sigmod(shortLength(str1, str2, true));
			double distTemp = tagWeight[i] * computeStringDistance(str1, str2, N);

			//System.out.println(startFields[i] + " : " + str1 + " : " + str2 + " : N=" + N + " dist=" + distTemp);

			if(distTemp >= 0){
				usedNumFields ++;
				dist += distTemp;
				totalWeight += tagWeight[i];
			}
			}
		}

		//add the type field
		if(sametype > 0){
			String str1 = (String)instance1.getField(type1);
			String str2 = (String)instance2.getField(type1);

			if (str1 != null && str2 != null && !str1.equals("") && !str2.equals("")) {

			double weight = Maths.sigmod(shortLength(str1, str2, true));//1;
			double distTemp = weight * computeStringDistance(str1, str2, N);

//			System.out.println(type1 + " " + type2 + " : " + str1 + " : " + str2 + " : " + distTemp);

			if(distTemp >= 0){
				usedNumFields ++;
				dist += distTemp;
				totalWeight += weight;
			}
		}
		else if(sametype < 0){
			dist += -1;
			usedNumFields ++;
			totalWeight += 1;
		}

		}


		// average the dist
//                dist /= usedNumFields;
		if (totalWeight > 0)
			dist /= totalWeight;
		else
			dist = 0.0;

//		System.out.println("dist = " + dist + " usedNumFields=" + usedNumFields + " sametype=" + sametype);
//		System.out.println("(" + type1 + ") : (" + type2 + ")" );

		return dist;
	}

	protected double computeStringDistance(String str1, String str2, int N)
	{
		return computeStringDistance(str1, str2, false, N);
	}

	protected double computeStringDistance(String str1, String str2)
	{
		return computeStringDistance(str1, str2, false);
	}	
	
	protected double computeStringDistance(String str1, String str2, boolean caseSensitive)
	{
		return computeStringDistance(str1, str2, caseSensitive, 1);
	}

	protected double computeStringDistance(String str1, String str2, boolean caseSensitive, int N)
	{

		if(str1 ==null || str2 == null){
			return default_Ignore_Dist;
		}

		if(!caseSensitive){
			str1 = str1.toLowerCase();
			str2 = str2.toLowerCase();
		}

		if(str1.length() > 0 && str2.length() > 0 ){

			if(N==1){
				return distMetric.score(str1, str2);		

				/*
					double dist_w = nw.score(str1, str2);
					// do not use character similiarity for years
					if(yearRegex.matcher (str1).matches() || yearRegex.matcher (str2).matches()){
					return dist_w;
					}

					// if word level similarity is 0, check if there is a typo there.
					if(dist_w < 0.01){
					double dist_c = nc.score(str1, str2);
					if(dist_c > 0.8)
					return dist_c;
					else
					return dist_w;
					}
					else return dist_w;
				*/
        	
				//              double lamda = 0.9;
				//              return lamda*nw.score(str1, str2) + (1-lamda)*nc.score(str1,str2);

			}
			else 
				return commonPhraseRatio(str1, str2, N);

//			double lamda = 0.9;
//			return lamda*nw.score(str1, str2) + (1-lamda)*nc.score(str1,str2);
		}
		else if(str1.length() == 0 && str2.length() == 0){
			return default_Ignore_Dist;
		}
		else{
			return default_Max_Dist; // this makes 97.1%
//			return default_Ignore_Dist;
		}

		


	}

	//the ratio of the intersection of two strings to the union 
	// N is the length of phrases
	// not very efficient in current implementation
	public static double commonPhraseRatio(String str1, String str2, int N)
	{
		ArrayList list1= new ArrayList();
		String[] words1 = str1.split("\\s+");
		String phrase = "";
		for(int i=words1.length-1; i>=N-1; i--){
			phrase = "";
			for(int j=i-N+1; j<=i; j++){
				phrase += words1[j];
				if(j<i)
					phrase += " ";
			}
			
			if(!list1.contains(phrase) && phrase != "") {
				list1.add(phrase);
//				System.out.println("(" + phrase + ")");
			}
		}

//		System.out.println("=======\n");

		ArrayList list2= new ArrayList();
		String[] words2 = str2.split("\\s+");
		for(int i=words2.length-1; i>=N-1; i--){
			phrase = "";
			for(int j=i-N+1; j<=i; j++){
				phrase += words2[j];
				if(j<i)
					phrase += " ";
			}

			if(!list2.contains(phrase) && phrase != ""){
				list2.add(phrase);
//				System.out.println("(" + phrase + ")");
			}
		}

		//union
		ArrayList union = (ArrayList)list1.clone();
		ArrayList intersect = new ArrayList();
		for(int i=0; i< list2.size(); i++){
			String ele = (String)list2.get(i);

			if(!list1.contains(ele)){
				union.add(ele);	
			}
			else{
				intersect.add(ele);
			}
		}

		
		/*	System.out.println("\n union:\n");
				for(int i=0; i<union.size(); i++){
				System.out.println("(" + union.get(i) + ")" );		
				}

				System.out.println("\n intersect: ");
				for(int i=0; i<intersect.size(); i++){
				System.out.println("(" + intersect.get(i) + ")");
				}
		*/	

		if(union.size() ==0 ) return 0;

		double ratio = (double)intersect.size()/union.size();

		return ratio;
	}
													

	public static int shortLength(String str1, String str2, boolean smaller)
	{
		String[] words1 = str1.split("\\s+");
		String[] words2 = str2.split("\\s+");

		int size1 = words1.length;
		int size2 = words2.length;
		if(smaller) return Math.min(size1,size2);
		else return Math.max(size1, size2);
	}
	

}
