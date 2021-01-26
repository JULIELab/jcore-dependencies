/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */




package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;


public class NormalizationPipe extends Pipe implements Serializable
{

/*        String[] startTags = new String[]
        {"<author>", "<title>", "<booktitle>", "<journal>","<publisher>", "<date>", "<location>", "<pages>",
        "<note>", "<institution>", "<editor>",  "<volume>", "<tech>"};

        String[] endTags = new String[]
        {"</author>", "</title>", "</booktitle>", "</journal>","</publisher>",  "</date>", "</location>", "</pages>",
        "</note>", "</institution>", "</editor>",  "</volume>", "</tech>"};
*/

	String[] startTags;
	String[] endTags;
	
	public NormalizationPipe (String[] startTags, String[] endTags)
	{
		this.startTags = startTags;
		this.endTags = endTags;
	}

	public Instance pipe (Instance carrier)
	{
		// set the property list
		for(int i=0; i<startTags.length; i++){
			String field = (String)carrier.getProperty(startTags[i]);
			
			field = generalNormalization(field);
			if(startTags[i].equals("<author>")){
				field = authorNormalization(field);		
			}
			else if(startTags[i].equals("<booktitle>")){
				field = booktitleNormalization(field);
			}
			else if(startTags[i].equals("<date>")){
				field = dateNormalization(field);
			}

			carrier.setProperty(startTags[i], field);

//			System.out.println(startTags[i] + " : " +  carrier.getProperty(startTags[i]));
		}


		return carrier;
	}

	//remove whitespace at the ends
	//remove punctuaions at the ends
	//lowercase
	//replace - with whitespace
	//conf. -> conference
	//proc. -> proceedings
	protected String generalNormalization(String field)
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
	
		field = field.replaceAll("\\s+", " ");

//		System.out.println("after: " + field);

		return field;
	}

	//author normalization
	//pick first name as abbrevation
	//pick last name
	protected String authorNormalization(String field)
	{
		if(field.length() == 0) return field;
	
//		System.out.println("before: " + field);

		ArrayList lastnames = LastName(field);
		assert(lastnames.size() == 1);
		
		field = (String)lastnames.get(0);

//		System.out.println("after: " + field);

		return field;
	}

	// replace AAAI with "national conference on artificial intelligence"
	protected String booktitleNormalization(String field)
	{
		if(field.length() == 0) return field;
	
		System.out.println("before: " + field);

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


		field = field.replaceAll("^in |^in: ", "");
		field = field.replaceAll("proceedings of the|proceedings of|proceedings", "");

		System.out.println("after: " + field);

		return field;
	}


	protected String dateNormalization(String field)
	{
		if(field.length() == 0) return field;

		System.out.println("before: " + field);

		String[] words = field.split("\\s|,");
		field = words[words.length-1];

		field = field.replaceAll("\\D", "");

		System.out.println("after: " + field);

		return field;
	}

        protected ArrayList LastName(String ss)
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
 


	public static void main (String[] args)
	{
	}

	// Serialization 
	
	private static final long serialVersionUID = 1;
	private static final int CURRENT_SERIAL_VERSION = 0;
	
	private void writeObject (ObjectOutputStream out) throws IOException {
		out.writeInt(CURRENT_SERIAL_VERSION);

	}
	
	private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
		int version = in.readInt ();

	}
}
