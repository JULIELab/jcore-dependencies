package dragon.nlp.extract;

import dragon.nlp.ontology.*;
import dragon.nlp.tool.*;

/**
 * <p>Interface of Ontological Term Extractors</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface TermExtractor extends ConceptExtractor{
    /**
     * Gets the part of speech tagger used for the term extractor
     * @return the part of speech tagger used
     */
    public Tagger getPOSTagger();

    /**
     * Gets the ontology used for the term extractor.
     * @return the ontology used
     */
    public Ontology getOntology();

    /**
     * Set the option of checking the coordinating terms
     * @param option the option of checking the coordinating terms
     */
    public void setCoordinatingCheckOption(boolean option);

    /**
     * Gets the option of checking the coordinating terms
     * @return true if the extractor checks the coordinating terms
     */
    public boolean getCoordinatingCheckOption();

    /**
     * Sets the option of checking terms in abbreviation.
     * @param option the option of checking terms in abbreviation.
     */
    public void setAbbreviationOption(boolean option);

    /**
     * Gets the option of checking terms in abbreviation.
     * @return true if the extractor checks terms in abbreviation.
     */
    public boolean getAbbreviationOption();

    public void setAttributeCheckOption(boolean option);
    public boolean getAttributeCheckOption();
    public boolean enableAttributeCheckOption(AttributeChecker checker);

    /**
     * Gets the option of checking the semantic type of the extracted term
     * @return true if the extractor checks the semantic type of the term
     */
    public boolean getSemanticCheckOption();

    /**
     * Sets the option of checking the semantic type of the extracted term
     * @param option the option of checking the semantic type of the extracted term
     */
    public void setSemanticCheckOption(boolean option);

    /**
     * Gets the option of predicting terms according to coordinating relationship. Predicated terms have no entry id.
     * @return true if the extractor predicts terms according to coordinating relationship.
     */
    public boolean getCoordinatingTermPredictOption();

    /**
     * Sets the option of predicting terms according to coordinating relationship.
     * @param option the option of predicting terms according to coordinating relationship.
     */
    public void setCoordinatingTermPredictOption(boolean option);

    /**
     * Gets the option of predicting compound terms. Predicted terms have no entry id. Componenets of predicted compound terms will be removed
     * from the extracted term list.
     * @return true if the extractor predicts compound terms
     */
    public boolean getCompoundTermPredictOption();

    /**
     * Sets the option of predicting compound terms.
     * @param option the option of predicting compound terms.
     */
    public void setCompoundTermPredictOption(boolean option);

    /**
     * Enable the option compound term prediction.
     * @param suffixFilename the name of the file containing a list of suffix for compound terms
     * @return true if no error occurs
     */
    public boolean enableCompoundTermPredictOption(String suffixFilename) ;
}