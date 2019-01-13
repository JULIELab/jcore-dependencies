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

public class AuthorLastNameEqual extends Pipe
{

//	AuthorLastNameEqual(){}

	public Instance pipe (Instance carrier) 
	{
		NodePair pair = (NodePair)carrier.getData();
		String s1 = (String)((Node)pair.getObject1()).getString(); // assume nodes are strings
		String s2 = (String)((Node)pair.getObject2()).getString();

		String start_tag = "<author>";
		String end_tag   = "</author>";

		String ss1 = SGMLStringOperation.locateField(start_tag, end_tag, s1);
		String ss2 = SGMLStringOperation.locateField(start_tag, end_tag, s2);
	
		if(!ss1.equals("") && !ss2.equals("")){
				
			ArrayList author_lastnames1 = LastName(ss1);
			ArrayList author_lastnames2 = LastName(ss2);

			int size1 = author_lastnames1.size();
			int size2 = author_lastnames2.size();

			if(size1 == size2){
				pair.setFeatureValue("sameAuthorNumber", 1.0);
			}

			int size = size1<size2? size1:size2;
			for(int i=0; i<size; i++){
				String author1 = (String)author_lastnames1.get(i);
				String author2 = (String)author_lastnames2.get(i);
				
				if(author1.equals(author2)){
					pair.setFeatureValue("LastnameEqual_"+i, 1.0);
				}
			}
		}

		return carrier;
	}

	protected ArrayList LastName(String ss)
	{
//		System.out.println("ss=" + ss);

		ss = ss.replaceAll(" \\w\\.", "");
		ss = ss.replaceAll("\\s\\w\\s\\.", "");

		ss = ss.replaceAll("^\\w\\.", "");
		ss = ss.replaceAll("^\\w\\s\\.", "");

		ss = ss.replaceAll(" and", " ,");

		ss = ss.replaceAll("\\.$", "");


//		System.out.println(ss);

		ArrayList names = new ArrayList();

		String[] authors = ss.split(",");
		String last_name;
		for(int i=0; i<authors.length; i++){
			String author = authors[i];
			author = author.replaceAll("^\\s+|\\s+$", "");
			String[] first_last_name = author.split(" ");
			if(first_last_name.length == 2){
				last_name = first_last_name[1];
			}
			else if(first_last_name.length == 1){
				last_name = first_last_name[0];
			}
			else {
//				System.out.println(ss);
//				throw new UnsupportedOperationException(author);
				last_name = first_last_name[first_last_name.length-1];
			}
					
			if(!last_name.equals("")){
//				System.out.println(i+": \""+last_name+"\"");
				names.add(last_name);
			}
		}
		
		return names;
	}


	protected ArrayList LastName2(String ss)
	{
		System.out.println("ss=" + ss);

/*		ss = ss.replaceAll(" \\w\\.", "");
		ss = ss.replaceAll("\\s\\w\\s\\.", "");

		ss = ss.replaceAll("^\\w\\.", "");
		ss = ss.replaceAll("^\\w\\s\\.", "");

//		ss= ss.replaceAll("[, ]+", ", ");
//		ss = ss.replaceAll(" and", "");

*/
		System.out.println(ss);

		ArrayList names = new ArrayList();

		//determine the type
		//there are four types of name conventions
		// 0: ,.     like helzerman , r . a . , and harper , m . p .    
		// 1: .,     like v . dumortier , g . janssens , and m . bruynooghe .
		// 2: only . like p . prosser .
		// 3: only , like roberto bagnara, roberto giacobazzi, giorgio levi 
		// 4: no punctuations like Fuchun Peng and Dale Schuurmans
		
		int indexPeriod = ss.indexOf(".");
		int indexComma  = ss.indexOf(",");
		int type = -1;

		if(indexPeriod == -1 && indexComma == -1){//no punctuations
			type = 4;
		}
		else if(indexPeriod == -1){// only ,	
			type = 3;
		}
		else if(indexComma == -1){// only .
			type = 2;
		}
		else if(indexPeriod < indexComma){//.,
			type = 1;
		}
		else if(indexComma < indexPeriod){//,.
			type = 0;
		}
		else{
			throw new UnsupportedOperationException();
		}

		System.out.println("	type=" + type);

		switch(type){
			case 0:
				// in this case, last names are the word before ,
				while(true){
					indexComma = ss.indexOf(",");
					if(indexComma == -1){
						break;
					}
				
					int indexBlank = ss.lastIndexOf(" ", indexComma - 2);

					System.out.println(indexComma + "/" + indexBlank);

					String name = ss.substring(indexBlank+1,indexComma-1);
					System.out.println("lastname = \"" + name + "\"");

					ss = ss.substring(indexComma+1);
				}
			case 1:
			case 2:
			case 3:
			case 4:
		}
		

		return names;
	}


}
