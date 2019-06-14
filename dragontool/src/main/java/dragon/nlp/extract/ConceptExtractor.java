package dragon.nlp.extract;

import dragon.nlp.Document;
import dragon.nlp.DocumentParser;
import dragon.nlp.Sentence;
import dragon.nlp.tool.Lemmatiser;
import dragon.util.SortedArray;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * <p>Interface of Concept Extractors</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface ConceptExtractor {
    /**
     * It is required to call this method before one calls extractFromDoc method.
     */
    public void initDocExtraction();

    /**
     * The concepts with identical names will be merged.
     * @param conceptList a list of concepts
     * @return a list of merged and sorted unique concepts
     */
    public SortedArray mergeConceptByName(ArrayList conceptList);

    /**
     * The concepts with identical entry id will be merged.
     * @param conceptList a list of concepts
     * @return a list of merged and sorted unique concepts
     */
    public SortedArray mergeConceptByEntryID(ArrayList conceptList);

    /**
     * Extracts concepts from a raw document
     * @param doc the content of the document
     * @return a list of extacted concepts
     */
    public ArrayList extractFromDoc(String doc);

    /**
     * Extracts concepts from a parsed document
     * @param doc a parsed document
     * @return a list of extacted concepts
     */
    public ArrayList extractFromDoc(Document doc);

    /**
     * Extracts concepts from a sentence
     * @param sent the sentence for extraction
     * @return a list of extracted concepts
     */
    public ArrayList extractFromSentence(Sentence sent);

    /**
     * @return a list of extracted concepts
     */
    public ArrayList getConceptList();

    /**
     * Tests if the extracted concept has a name.
     * @return true or false
     */
    public boolean supportConceptName();

    /**
     * Tests if the extracted concept has an entry ID.
     * @return true or false
     */
    public boolean supportConceptEntry();
    public void setSubConceptOption(boolean option);
    public boolean getSubConceptOption();

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

    /**
     *Gets the lemmtiser used for this extractor.
     * @return the lemmatiser used
     */
    public Lemmatiser getLemmatiser();

    /**
     * Sets lemmatiser for this extractor.
     * @param lemmatiser the lemmatiser
     */
    public void setLemmatiser(Lemmatiser lemmatiser);

    /**
     * Sets the document parser for the concept extractor.
     * @param parser document parser
     */
    public void setDocumentParser(DocumentParser parser);

    /**
     * Gets document parser.
     * @return the document parser.
     */
    public DocumentParser getDocumentParser();

    /**
     * Print out the extract concepts to the speficid print writer.
     * @param out the print writer
     */
    public void print(PrintWriter out);

    /**
     * Print out the given list of concepts to the speficid print writer.
     * @param out the print writer
     * @param list a list concepts for output
     */
    public void print(PrintWriter out, ArrayList list);
}