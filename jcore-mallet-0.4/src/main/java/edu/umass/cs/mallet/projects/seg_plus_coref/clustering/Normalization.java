/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */




package edu.umass.cs.mallet.projects.seg_plus_coref.clustering;

import java.util.ArrayList;


public class Normalization
{

	//remove whitespace at the ends
	//remove punctuaions at the ends
	//lowercase
	//replace - with whitespace
	//conf. -> conference
	//proc. -> proceedings
	public static String generalNormalization(String field)
	{
//		System.out.println("before: " + field);
		if(field.length() == 0) return field;

		field = field.toLowerCase();
		
		while(true){
			String field1 = field.replaceAll("^\\p{Punct}+", "");
			field1 = field1.replaceAll("^\\s+", "");
			field1 = field1.replaceAll("\\p{Punct}+$", "");
			field1 = field1.replaceAll("\\s+$", "");

			if(field.equals(field1)) break;

			field = field1;
		}
	
//		field = field.replaceAll("\\s*$|\\p{Punct}*$", "");
//		field = field.replaceAll("\\p{Punct}+$", "");
//		field = field.replaceAll("\\s+$", "");

		field = field.replaceAll("-+", " ");
		field = field.replaceAll("conf\\. |conf ", "conference ");
		field = field.replaceAll("conf\\.$|conf$", "conference");
		field = field.replaceAll("proc\\. |proc ", "proceedings ");
		field = field.replaceAll("int'l|int\\.|intl\\.","international");
		field = field.replaceAll("trans\\. |trans ", "transactions ");
		field = field.replaceAll("addisonwesley", "addison wesley");
		field = field.replaceAll("\\s+", " ");

//		System.out.println("after: " + field);

		return field;
	}

	//author normalization
	//pick first name as abbrevation
	//pick last name
	public static String authorNormalization(String field)
	{
		if(field.length() == 0) return field;
	
//		System.out.println("before: " + field);

		ArrayList lastnames = LastName(field);
//		assert(lastnames.size() == 1);
		
		field = (String)lastnames.get(0);

//		System.out.println("after: " + field);

		return field;
	}

	// replace AAAI with "national conference on artificial intelligence"
	public static String booktitleNormalization(String field)
	{
		if(field.length() == 0) return field;
	
//		System.out.println("before: " + field);

		// mlc -> machine learning conference
		// ml-97 -> machine learning 97
		// ml97  -> machine learning 97
		// ml -> machine learning
//		if(field.indexOf(" mlc| ml-| ml\\d+| ml ") != -1){
		if(field.indexOf(" mlc") != -1){
			if(field.indexOf("machine learning") == -1){
				field = field.replaceAll(" mlc", " international machine learning conference ");
			}
		}

                if(field.indexOf(" ml ") != -1){
                        if(field.indexOf("machine learning") == -1){
                                field = field.replaceAll(" ml ", " international machine learning ");
                        }
                }

	
		if(field.indexOf("aaai") != -1){
			if( field.indexOf("artificial intelligence") == -1)
				field = field.replaceAll("aaai", "the national conference on artificial intelligence ");
			else
				field = field.replaceAll("aaai[^\\s]*\\s", " ");
		}


                if(field.indexOf("ijcai") != -1){
                        if( field.indexOf("artificial intelligence") == -1)
                                field = field.replaceAll("ijcai", "international joint conference on artificial intelligence ");
                        else// has both ijcai and full conference name
                                field = field.replaceAll("ijcai[^\\s]*\\s", " ");
                }

                if(field.indexOf("sigir") != -1){
                        if( field.indexOf("information retrieval") == -1)
                                field = field.replaceAll("sigir", "international conference on research and development in information retrieval ");
                        else// has both ijcai and full conference name
                                field = field.replaceAll("sigir[^\\s]*\\s", " ");
                }

                if(field.indexOf("nips") != -1){
                        if( field.indexOf("neural information") == -1)
                                field = field.replaceAll("nips", "advances in neural information processing systems ");
                        else// has both ijcai and full conference name
                                field = field.replaceAll("nips[^\\s]*\\s", " ");
                }

                if(field.indexOf("jair") != -1){
                       field = field.replaceAll("jair", "journal of artificial intelligence research ");
                }

                if(field.indexOf("jmlr") != -1){
                       field = field.replaceAll("jmlr", "journal of machine learning research ");
                }


                if(field.indexOf("iros") != -1){
                       field = field.replaceAll("iros", "international conference on intelligent robots and systems ");
                }

                if(field.indexOf("eccv") != -1){
                       field = field.replaceAll("eccv", "european conference on computer vision");
                }

		field = field.replaceAll("^in |^in: ", "");
		field = field.replaceAll("proceedings of the|proceedings of|proceedings", "");

//		System.out.println("after: " + field);

		return field;
	}

        public static String publisherNormalization(String field)
        {
                if(field.length() == 0) return field;

                field = field.replaceAll("addisonwesley", "addison-wesley");

                return field;
        }

	public static String institutionNormalization(String field)
	{
		if(field.length() == 0) return field;

		field = field.replaceAll("inst\\.", "institution");
		field = field.replaceAll("univ\\.", "university");

		return field;
	}

	public static String dateNormalization(String field)
	{
		if(field.length() == 0) return field;

//		System.out.println("before: " + field);

		String[] words = field.split("\\s|,");
		field = words[words.length-1];

		field = field.replaceAll("\\D", "");

//		System.out.println("after: " + field);

		return field;
	}

        public static ArrayList LastName(String ss)
        {
//              System.out.println("ss=" + ss);
                                                                                                                                                             
                ss = ss.replaceAll(" \\w\\.", "");
                ss = ss.replaceAll("\\s\\w\\s\\.", "");
                                                                                                                                                             
                ss = ss.replaceAll("^\\w\\.", "");
                ss = ss.replaceAll("^\\w\\s\\.", "");

                ss = ss.replaceAll(" and", " ,");
                ss = ss.replaceAll("\\.$", "");
 
 
//              System.out.println(ss);
 
                ArrayList names = new ArrayList();
 
                String[] authors = ss.split(",");
                String last_name;
                for(int i=0; i<authors.length; i++){
                        String author = authors[i];
                        author = author.replaceAll("^\\s+|\\s+$", "");
                        String[] first_last_name = author.split(" ");
                        if(first_last_name.length == 2){
				if(first_last_name[1].length() <= 2)//"Maes P" , "Wolberg WH" case, irregular format
					last_name = first_last_name[0];
				else
	                                last_name = first_last_name[1];
                        }
                        else if(first_last_name.length == 1){
                                last_name = first_last_name[0];
                        }
                        else {
//                              System.out.println(ss);
//                              throw new UnsupportedOperationException(author);
                                last_name = first_last_name[first_last_name.length-1];
                        }
                                         
                        if(!last_name.equals("")){
//                              System.out.println(i+": \""+last_name+"\"");
                                names.add(last_name);
                        }
                }
                 
                return names;
        }
 
}
