package dragon.ir.index.sequence;

import dragon.ir.index.*;
import dragon.nlp.*;
import dragon.onlinedb.*;

/**
 * <p>The basic index reader for sequencial data </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicSequenceIndexReader extends AbstractSequenceIndexReader{
    private FileIndex fileIndex;

    public BasicSequenceIndexReader(String directory) {
       this(directory,null);
    }

    public BasicSequenceIndexReader(String directory, CollectionReader collectionReader) {
        this.collectionReader =collectionReader;
        fileIndex=new FileIndex(directory,false);
    }

    public void initialize(){
        if(initialized)
            return;
        collection = new IRCollection();
        collection.load(fileIndex.getCollectionFilename());
        termIndexList = new BasicIRTermIndexList(fileIndex.getTermIndexListFilename(),false);
        docIndexList = new BasicIRDocIndexList(fileIndex.getDocIndexListFilename(), false);
        termKeyList=new SimpleElementList(fileIndex.getTermKeyListFilename(),false);
        docKeyList=new SimpleElementList(fileIndex.getDocKeyListFilename(),false);
        doctermSeq = new SequenceFileReader(fileIndex.getDocTermSeqIndexFilename(), fileIndex.getDocTermSeqFilename());
        initialized=true;
    }
}