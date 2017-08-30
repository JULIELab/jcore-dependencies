package dragon.nlp.extract;

import dragon.nlp.*;
import dragon.onlinedb.Article;
import java.util.ArrayList;

/**
 * <p>Interface of Dual Concept Extractor</p>
 * <p>This type of extractors extract two sets of concepts from a documetn or a sentence simultaneously</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface DualConceptExtractor {
    /**
     * Extracts concepts from a raw document
     * @param doc a raw document for extraction
     * @return true if extracted successfully
     */
    public boolean extractFromDoc(String doc);

    /**
     * Extracts concepts from a parsed document
     * @param doc a parsed  document for extraction
     * @return true if extracted successfully
     */
    public boolean extractFromDoc(Document doc);

    /**
     * Extracts concepts from a semi-structured article
     * @param doc a semi-structured article for extraction
     * @return true if extracted successfully
     */
    public boolean extractFromDoc(Article doc);

    /**
     * Extracts concept from a sentence
     * @param sent a sentence for extraction
     * @return true if extracted successfully
     */
    public boolean extractFromSentence(Sentence sent);

    /**
     * Gets the first set of concepts extracted before
     * @return the first set of concepts extracted before
     */
    public ArrayList getFirstConceptList();

    /**
     * Gets the second set of concepts extracted before
     * @return the second set of concepts extracted before
     */
    public ArrayList getSecondConceptList();

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

    /**
     * It is required to call this method before one calls extractFromDoc method.
     */
    public void initDocExtraction();

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
}
