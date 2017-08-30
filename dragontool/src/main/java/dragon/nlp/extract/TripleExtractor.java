package dragon.nlp.extract;

import dragon.nlp.*;
import dragon.util.SortedArray;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * <p>Interface of triple extractors</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface TripleExtractor {
    /**
     * It is required to call this method before calling the extractFromDoc method.
     */
    public void initDocExtraction();

    /**
     * Extracts triples from a raw document.
     * @param doc the raw document for extraction
     * @return true if extracted successfully.
     */
    public boolean extractFromDoc(String doc);

    /**
     * Extracts triples from a parsed document.
     * @param doc the parsed document for extraction
     * @return true if extracted successfully.
     */
    public boolean extractFromDoc(Document doc);

    /**
     * Extracts triples from a sentence.
     * @param sent the sentence for extraction
     * @return a list of extracted triples.
     */
    public ArrayList extractFromSentence(Sentence sent);

    /**
     * Gets concept list
     * @return a list of concepts extracted before
     */
    public ArrayList getConceptList();

    /**
     * Gets triple list
     * @return a list of triples extracted before.
     */
    public ArrayList getTripleList();

    /**
     * Gets the concept extractor used for triple extraction
     * @return the concept extractor
     */
    public ConceptExtractor getConceptExtractor();

    /**
     * Merge duplicated triples.
     * @param mergedConceptList a merged and sorted concept list
     * @param tripleList a list of triples for merging
     * @return a list of merged and sorted triples
     */
    public SortedArray mergeTriples(SortedArray mergedConceptList, ArrayList tripleList);
    /**
     * Tests if the extractor applies concept filtering.
     * @return true if the extractor applies concept filtering
     */
    public boolean getFilteringOption();

    /**
     * Sets the option of concept filtering
     * @param option the option of concept filtering
     */
    public void setFilteringOption(boolean option);

    /**
     * Sets the concept filter for the concept extatractor
     * @param cf the concept filter
     */
    public void setConceptFilter(ConceptFilter cf);

    /**
     * Gets the concept filter used for this extractor.
     * @return the concept filter used.
     */
    public ConceptFilter getConceptFilter();

    public void setCoordinatingCheckOption(boolean option);
    public boolean getCoordinatingCheckOption();
    public void setCoReferenceOption(boolean option);
    public boolean getCoReferenceOption() ;

    /**
     * Gets the option of checking the semantic type of the relationship
     * @return true if the extractor checks the semantic type of the relationship
     */
    public boolean getSemanticCheckOption();

    /**
     * Sets the option of checking the semantic type of the relationship
     * @param option the option of checking the semantic type of the relationship
     */
    public void setSemanticCheckOption(boolean option) ;

    /**
     * Gets the option of checking the existance of the relationship between two semantic types.
     * @return true if the extractor checks the existance of the relationship between two semantic types.
     */
    public boolean getRelationCheckOption();

    /**
     * Sets the option of checking the existance of the relationship between two semantic types.
     * @param option the option of checking the existance of the relationship between two semantic types.
     */
    public void setRelationCheckOption(boolean option);

    /**
     * Gets the option of extracting triples within a clause window.
     * @return true if the extractor extracts triples within a clause window.
     */
    public boolean getClauseIdentifyOption();

    /**
     * Sets the option of extracting triples within a clause window.
     * @param option the option of extracting triples within a clause window.
     */
    public void setClauseIdentifyOption(boolean option);

    /**
     * Prints out extracted concepts and triples to the specified print writer.
     * @param out the print writer
     */
    public void print(PrintWriter out);

    /**
     * Prints out the specified concepts and triples to the print writer.
     * @param out the print writer
     * @param conceptList the concept list for print
     * @param tripleList the triple list for print
     */
    public void print(PrintWriter out,ArrayList conceptList,ArrayList tripleList);
}