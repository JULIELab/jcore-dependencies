package dragon.nlp.tool;

import dragon.nlp.Document;
import dragon.nlp.Sentence;

import java.util.ArrayList;

/**
 * <p>Interface of Named Entity Recognizer</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface NER {
    /**
     * The return format is the same as the one by the method extractFromSentence.
     * @param doc a parsed document
     * @return a list of extracted entities in their original order in the text.
     */
    public ArrayList extractFromDoc(Document doc);

    /**
     * Difering from the method extractEntites, this method will save an entity in a term and thus all information such as entity type
     * is kept. Moreover, duplicated entities will not be merged. Instead, all extracted entities will be returned in their orders in
     * the text.
     * @param sent the sentence for extraction
     * @return a list of extracted entities in their original order in the text.
     */
    public ArrayList extractFromSentence(Sentence sent);

    /**
     * Extract entities from a given text. Each extracted entity will be save in a Token. Duplicated entities will be merged. The frequency
     * of a token is actually the occurrence frequency of the entity in the text. The type of the entity will be ignored.
     * @param doc the text for processing
     * @return a list of unique entities with their frequency.
     */
    public ArrayList extractEntities(String doc);

    /**
     * The format of an annotated text is XML. Each entity in the text will be tagged by a XML node and the attribute of the node will be
     * the type of the entity.
     * @param doc the text for annotation
     * @return an annotated text
     */
    public String annotate(String doc);

    /**
     * Set the entity types of interest, for example, organization, location, person, etc. The interpretation of these types is subject to
     * the implemented named entity recognizers.
     * @param arrType a set of wanted entity types.
     */
    public void setAnnotationTypes(String[] arrType);
}