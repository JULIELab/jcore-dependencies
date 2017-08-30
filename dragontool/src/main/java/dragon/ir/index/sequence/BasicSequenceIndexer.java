package dragon.ir.index.sequence;

import dragon.nlp.extract.ConceptExtractor;

/**
 * <p>The basic indexer for sequencial data</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicSequenceIndexer extends AbstractSequenceIndexer{

    public BasicSequenceIndexer(ConceptExtractor ce, String indexFolder) {
        super(ce);
        writer=new BasicSequenceIndexWriter(indexFolder);
    }
}