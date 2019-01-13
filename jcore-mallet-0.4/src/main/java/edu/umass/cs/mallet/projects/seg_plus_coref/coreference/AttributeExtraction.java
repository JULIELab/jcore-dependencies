package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import java.util.*;
import java.lang.*;
import java.io.*;


public abstract class AttributeExtraction
{
	ArrayList fileList;
	String attributeStartTag;
	String attributeEndTag;
	boolean includeSingleton = true;

	static String[] SEPERATOR = new String[] {"<NEW_HEADER>", "<NEWREFERENCE>"};
	
	public AttributeExtraction(ArrayList fileList, 
		String attributeStartTag, String attributeEndTag, 
		boolean includeSingleton)
	{
		this.fileList = fileList;
		this.attributeStartTag = attributeStartTag;
		this.attributeEndTag = attributeEndTag;
		this.includeSingleton = includeSingleton;
	}	
	
	public abstract int processOnePaper(File file);

	public double attributeExtraction()
	{
		int count = 0;
		for(int i=0; i<fileList.size(); i++){
			File file = (File)fileList.get(i);

			int index = processOnePaper(file);
			if(index == 0)  count++;
			else{
	//			if(index != -1)
	//			System.out.println("ERRORS:" + file.toString() + ":" + index);
			}
		}

		double accuracy = (double)count/fileList.size();

		return accuracy;

	}

        public String toString()
        {
                return this.getClass().getName();
        }

}
