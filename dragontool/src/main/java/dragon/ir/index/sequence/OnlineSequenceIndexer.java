package dragon.ir.index.sequence;

import dragon.ir.index.IRCollection;
import dragon.ir.index.IRDocIndexList;
import dragon.ir.index.IRTermIndexList;
import dragon.nlp.SimpleElementList;
import dragon.nlp.extract.ConceptExtractor;
/**
 * <p>The online indexer for sequencial data </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class OnlineSequenceIndexer extends AbstractSequenceIndexer{
    public OnlineSequenceIndexer(ConceptExtractor ce) {
        super(ce);
        writer=new OnlineSequenceIndexWriter();
    }

    public IRTermIndexList getTermIndexList(){
        return ((OnlineSequenceIndexWriter)writer).getTermIndexList();
    }

    public IRDocIndexList getDocIndexList(){
        return ((OnlineSequenceIndexWriter)writer).getDocIndexList();
    }

    public SimpleElementList getDocKeyList(){
        return ((OnlineSequenceIndexWriter)writer).getDocKeyList();
    }

    public SimpleElementList getTermKeyList(){
        return ((OnlineSequenceIndexWriter)writer).getTermKeyList();
    }

    public SequenceReader getSequenceReader(){
        return ((OnlineSequenceIndexWriter)writer).getSequenceReader();
    }

    public IRCollection getIRCollection(){
        return ((OnlineSequenceIndexWriter)writer).getIRCollection();
    }


}