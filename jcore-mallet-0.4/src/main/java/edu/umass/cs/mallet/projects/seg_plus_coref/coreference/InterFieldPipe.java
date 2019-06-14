package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;

public class InterFieldPipe extends Pipe
{

	public InterFieldPipe () {}

	public Instance pipe (Instance carrier) {
		NodePair pair = (NodePair)carrier.getData();

		Citation s1 = (Citation)pair.getObject1();
		Citation s2 = (Citation)pair.getObject2();

		double authorVal = 0.0;
		double titleVal = 0.0;
		double paperVal = 0.0;
		double dateVal = 0.0;
		double venueVal = 0.0;
		double yearsNoMatch = 0.0;
		double lowAuthor = 0.0;
		authorVal = pair.getFeatureValue("AuthorOverlapLongHigh");
		lowAuthor = pair.getFeatureValue("AuthorOverlapLongNone");
		titleVal = pair.getFeatureValue("trigramTFIDF_titleHIGH");
		paperVal = pair.getFeatureValue("SamePaperType");
		dateVal = pair.getFeatureValue("YearsMatch");
		yearsNoMatch = pair.getFeatureValue("YearsDoNotMatch");
		venueVal = pair.getFeatureValue("VenueSimilarityHIGH");

		if (authorVal == 1 && titleVal == 1 && dateVal == 1.0 && venueVal == 1.0)
			pair.setFeatureValue("GeneralAgreement1", 1.0);
		else if (authorVal == 1 && titleVal == 1 && dateVal == 1.0)
			pair.setFeatureValue("TitleAndAuthorAndDateAgree", 1.0);
		else if (authorVal == 1 && titleVal == 1)
			pair.setFeatureValue("TitleAndAuthorAgree", 1.0);
		if (yearsNoMatch == 1 && titleVal == 1)
			pair.setFeatureValue("SimilarTitlesButDifferentDates", 1.0);
		if (lowAuthor==1 && titleVal==1)
			pair.setFeatureValue("SimilarTitlesButDifferentAuthors", 1.0);
		if (authorVal==0 && titleVal==0)
			pair.setFeatureValue("SimilarAuthorsButDifferentTitles", 1.0);

//		if (authorVal > 0.8 && dateVal > 0.8 && titleVal > 0.9 && paperVal > 0.9 && venueVal > 0.9)

		/*
		if (((!s1.getField(Citation.tech).equals("") && s2.getField(Citation.tech).equals("")) ||
				 (!s2.getField(Citation.tech).equals("") && s1.getField(Citation.tech).equals(""))) &&
				(pair.getFeatureValue("TitleSimilarity") > 0.9) )
		{
		
			System.out.println("TitleSimilarOnlyOneHasTech --> " +
			s1.getBaseString() + " :: " + s2.getBaseString());
			pair.setFeatureValue("TitleSimilarOnlyOneHasTech", 1.0);
		}

		if (((!s1.getField(Citation.booktitle).equals("") && s2.getField(Citation.booktitle).equals("")) ||
				(!s2.getField(Citation.booktitle).equals("") && s1.getField(Citation.booktitle).equals(""))) &&
				(pair.getFeatureValue("TitleSimilarity") > 0.9) )
		{
			pair.setFeatureValue("TitleSimilarOnlyOneHasBookTitle", 1.0);
		}
		
		if (((!s1.getField(Citation.journal).equals("") && s2.getField(Citation.journal).equals("")) ||
				 (!s2.getField(Citation.journal).equals("") && s1.getField(Citation.journal).equals(""))) &&
				(pair.getFeatureValue("TitleSimilarity") > 0.9) )
		{
			pair.setFeatureValue("TitleSimilarOnlyOneHasJournal", 1.0);
		}
		double v1 = ((double)pair.getFeatureValue("TitleSimilarity") *
								 (double)pair.getFeatureValue("AuthorOverlap"));
								 
		if (v1 > 0)
			pair.setFeatureValue("AuthorANDTitle", v1);

	*/
		return carrier;
	}
}
