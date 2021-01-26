/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */




package edu.umass.cs.mallet.projects.seg_plus_coref.clustering;

import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.types.Token;
import edu.umass.cs.mallet.base.types.TokenSequence;
import edu.umass.cs.mallet.base.util.CharSequenceLexer;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.SGMLStringOperation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import edu.umass.cs.mallet.users.fuchun.coref.SGMLStringOperation;
//cas: changed above line to: I hope that doesn't introduce a bug

public class SGML2FieldsPipe extends Pipe implements Serializable
{
	String sgmlRegex = "</?([^>]*)>";
	Pattern sgmlPattern = Pattern.compile(sgmlRegex);

	CharSequenceLexer lexer;
	String lexerRegex;
	String backgroundTag;

	String refNoMeta;
	String clusterNoMeta;
	String clusterNoMeta_true = "true_id=";

        String[] startTags;
        String[] endTags;
        double[] tagWeight;

	String Source = "SOURCE";
	String TEXT_NO_TAG = "TEXT_NO_TAG";

	public SGML2FieldsPipe (CharSequenceLexer lexer, String backgroundTag,
		String refNoMeta, String clusterNoMeta, String[] startTags, String[] endTags, double[] tagWeight)
	{
		this.lexer = lexer;
		this.backgroundTag = backgroundTag;
		this.lexerRegex = lexer.getPattern();

		this.refNoMeta = refNoMeta;
		this.clusterNoMeta = clusterNoMeta;

		this.startTags = startTags;
		this.endTags = endTags;
		this.tagWeight = tagWeight;

	}

	public SGML2FieldsPipe (String regex, String backgroundTag,
		String refNoMeta, String clusterNoMeta, String[] startTags, String[] endTags, double[] tagWeight)
	{
		this.lexer = new CharSequenceLexer (regex);
		this.backgroundTag = backgroundTag;

		this.refNoMeta = refNoMeta;
		this.clusterNoMeta = clusterNoMeta;

		this.startTags = startTags;
		this.endTags = endTags;
		this.tagWeight = tagWeight;

	}

	public SGML2FieldsPipe (String refNoMeta, String clusterNoMeta, String[] startTags, String[] endTags, double[] tagWeight)
	{
		this (new CharSequenceLexer(), "O", 
			refNoMeta, clusterNoMeta, startTags, endTags, tagWeight);
	}

	public Instance pipe (Instance carrier)
	{
		TokenSequence dataTokens = new TokenSequence ();
		TokenSequence targetTokens = new TokenSequence ();
		CharSequence string = (CharSequence) carrier.getData();
		String tag = backgroundTag;
		String nextTag = backgroundTag;
		Matcher m = sgmlPattern.matcher (string);
		
		int textStart = 0;
		int textEnd = 0;
		int nextStart = 0;
		boolean done = false;

//		System.out.println(sgmlPattern.pattern());
//		System.out.println(lexer.getPattern());
//		System.out.println(backgroundTag);
//		System.out.println(string);

		String text_no_tag = "";

		while (!done) {

			done = !(m.find());
			if (done)
				textEnd = string.length()-1;
			else {
				String sgml = m.group();
//				System.out.println ("SGML = "+sgml);

				int groupCount = m.groupCount();
//				System.out.println(groupCount);

				if (sgml.charAt(1) == '/')
					nextTag = backgroundTag;
				else{
					nextTag = m.group(1).intern();
				}
//				System.out.println("nextTag: " + nextTag);

				nextStart = m.end();
				textEnd = m.start();
//				System.out.println ("Text start/end "+textStart+" "+textEnd);
			}

			if (textEnd - textStart > 0) {
//				System.out.println ("Tag = "+tag);
//				System.out.println ("Target = "+string.subSequence (textStart, textEnd));

				dataTokens.add ( new Token ((String)string.subSequence (textStart, textEnd)) );
				targetTokens.add (new Token (tag));
				text_no_tag += (String)string.subSequence (textStart, textEnd) + " ";
			}
			textStart = nextStart;
			tag = nextTag;
		}

		carrier.setData(dataTokens);
		carrier.setTarget(targetTokens);
		carrier.setSource(dataTokens);

		carrier.setProperty(Source,string);//put the original text in SOURCE property
		carrier.setProperty(TEXT_NO_TAG, text_no_tag);

		// set the property list
		String name = (String) carrier.getName();
//		System.out.println("name: " + name);

		String[] refNo_clusterNo = name.split(":");		
		assert(refNo_clusterNo.length == 2 || refNo_clusterNo.length == 3);
		String referenceNo = refNo_clusterNo[0];
		String clusterNo = refNo_clusterNo[1];

		carrier.setProperty(refNoMeta, referenceNo);
		carrier.setProperty(clusterNoMeta, clusterNo);

		if(refNo_clusterNo.length == 3){
			String clusterNo_true = refNo_clusterNo[2];
			carrier.setProperty(clusterNoMeta_true, clusterNo_true);
		}


		for(int i=0; i<startTags.length; i++){
			String field = SGMLStringOperation.locateField(startTags[i], endTags[i], (String)string);
			carrier.setProperty(startTags[i], field);
//			System.out.println(startTags[i] + " : " +  carrier.getProperty(startTags[i]));
		}


		return carrier;
	}

	public static void main (String[] args)
	{
/*		try {
			Pipe p = new SerialPipes (new Pipe[] {
				new Input2CharSequence (),
				new SGML2FieldsPipe(new CharSequenceLexer (Pattern.compile ("\\+[A-Z]+\\+|\\p{Alpha}+|\\p{Digit}+|\\p{Punct}")), "O")
//				new SGML2FieldsPipe (new CharSequenceLexer (Pattern.compile (".")), "O")
				});
			
			for (int i = 0; i < args.length; i++) {
				Instance carrier = new Instance (new File(args[i]), null, null, null, p);
				TokenSequence data = (TokenSequence) carrier.getData();
				TokenSequence target = (TokenSequence) carrier.getTarget();
				System.out.println ("===");
				System.out.println (args[i]);
				for (int j = 0; j < data.size(); j++)
					System.out.println (target.getToken(j).getText()+" "+data.getToken(j).getText());
			}
		} catch (Exception e) {
			System.out.println (e);
			e.printStackTrace();
		}
*/
	}

	// Serialization 
	
	private static final long serialVersionUID = 1;
	private static final int CURRENT_SERIAL_VERSION = 0;
	
	private void writeObject (ObjectOutputStream out) throws IOException {
		out.writeInt(CURRENT_SERIAL_VERSION);

//		out.writeObject(sgmlPattern);
		out.writeObject(sgmlRegex);

		out.writeObject(backgroundTag);

	//	out.writeObject(lexer);
		out.writeObject(lexerRegex);

//		System.out.println("lexRegex:" + lexerRegex);

	}
	
	private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
		int version = in.readInt ();
//		sgmlPattern = (Pattern) in.readObject();
		sgmlRegex = (String)in.readObject();
		sgmlPattern = Pattern.compile(sgmlRegex);

		backgroundTag = (String) in.readObject();

		lexerRegex = (String)in.readObject();
//		System.out.println("lexRegex:" + lexerRegex);

		lexer = new CharSequenceLexer(lexerRegex);	

	}
}
