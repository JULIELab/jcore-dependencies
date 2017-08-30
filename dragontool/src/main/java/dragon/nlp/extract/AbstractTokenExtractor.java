package dragon.nlp.extract;

import dragon.nlp.tool.Lemmatiser;

/**
 * <p>Abstract class for token extraction </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractTokenExtractor extends AbstractConceptExtractor implements TokenExtractor {
    protected Lemmatiser lemmatiser;

    public AbstractTokenExtractor(Lemmatiser lemmatiser) {
        this.lemmatiser=lemmatiser;
        cf=null;
        conceptFilter_enabled=false;
    }

    public boolean supportConceptName(){
        return true;
    }

    public boolean supportConceptEntry(){
        return false;
    }

    public Lemmatiser getLemmatiser(){
        return lemmatiser;
    }

    public void setLemmatiser(Lemmatiser lemmatiser){
        this.lemmatiser =lemmatiser;
    }

    public void initDocExtraction(){
    }
}