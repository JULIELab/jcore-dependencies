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

public class HeuristicPipe extends Pipe {

	String[] fields;
	
	public HeuristicPipe (String[] fields) {
		this.fields = fields;
	}

	public boolean isConferencePaper (Citation c1) {
		String booktitle = c1.getField(Citation.conference);
		if (booktitle.length() > 0)
			return true;		
		booktitle = c1.getField(Citation.booktitle);
		if (booktitle.matches(".*proc\\..*") ||
				booktitle.matches(".*proceedings.*") ||
				booktitle.matches(".*workshop.*") ||
				booktitle.matches(".*conference.*"))
			return true;
		return false;
	}

	public boolean isJournalPaper (Citation c1) {
		String journal = c1.getField(Citation.journal);
		if (journal.length() > 0)
			return true;
		String volume = c1.getField(Citation.volume);
		if (volume.length() > 0)
			return true;
		return false;
	}

	public boolean isTechPaper (Citation c1) {
		String journal = c1.getField(Citation.journal);
		if (journal.length() > 0)
			return false;
		String volume = c1.getField(Citation.volume);
		if (volume.length() > 0)
			return false;
		String tech = c1.getField(Citation.tech);
		if (tech.length() > 0)
			return true;
		return false;
	}

	public String getPaperType (Citation c1) {
		if (isJournalPaper(c1))
			return "journal";
		else if (isConferencePaper(c1))
			return "conference";
		else if (isTechPaper(c1))
			return "tech";
		else
			return "none";
	}

	public Instance pipe (Instance carrier) {
		NodePair pair = (NodePair)carrier.getData();
		Citation c1 = (Citation)pair.getObject1();
		Citation c2 = (Citation)pair.getObject2();
		String paperType1 = getPaperType(c1);
		String paperType2 = getPaperType(c2);
		double authorVal = 0.0;
		double titleVal = 0.0;
		authorVal = pair.getFeatureValue("trigramTFIDF_author");
		titleVal = pair.getFeatureValue("trigramTFIDF_title");
		

		// only set if they're fairly close
//		if ( authorVal > 0.2 && titleVal > 0.1) {
		//System.out.println("Pair has high venue score!!!");
		if (paperType1.equals(paperType2))
			pair.setFeatureValue("SamePaperType",1.0);
		else
			pair.setFeatureValue("DiffPaperType",1.0);
		if ((paperType1.equals("conference") && paperType2.equals("journal")) ||
				(paperType2.equals("conference") && paperType1.equals("journal")))
			pair.setFeatureValue("OneJournalOneConference", 1.0);
		else if ((paperType1.equals("conference") && paperType2.equals("tech")) ||
						 (paperType2.equals("conference") && paperType1.equals("tech")))
			pair.setFeatureValue("OneConferenceOneTech", 1.0);
		else if ((paperType1.equals("journal") && paperType2.equals("tech")) ||
						 (paperType2.equals("journal") && paperType1.equals("tech")))
			pair.setFeatureValue("OneJournalOneTech", 1.0);
		/*
			else if (paperType1.equals("conference") && paperType2.equals("conference"))
			pair.setFeatureValue("BothConferencePapers", 1.0);
			else if (paperType1.equals("journal") && paperType2.equals("journal"))
			pair.setFeatureValue("BothJournalPapers", 1.0);
			else if (paperType1.equals("tech") && paperType2.equals("tech"))
			pair.setFeatureValue("BothTechPapers", 1.0);
		*/
		
		return carrier;
	}
	
}
