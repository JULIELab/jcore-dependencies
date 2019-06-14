package dragon.ir.index.sequence;

import dragon.ir.index.BasicIRDocIndexList;
import dragon.ir.index.BasicIRTermIndexList;
import dragon.ir.index.FileIndex;
import dragon.ir.index.IRCollection;
import dragon.nlp.SimpleElementList;
import dragon.util.SortedArray;

import java.io.File;

/**
 * <p>The basic index writer for sequencial data </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicSequenceIndexWriter extends AbstractSequenceIndexWriter{
    private FileIndex fileIndex;

    public BasicSequenceIndexWriter(String directory) {
        fileIndex=new FileIndex(directory,false);
    }

    public void initialize(){
        if(initialized)
            return;
        collection=new IRCollection();
        collection.load(fileIndex.getCollectionFilename());

        termCache=new SortedArray(500);
        docKeyList=new SimpleElementList(fileIndex.getDocKeyListFilename(),true);
        termKeyList=new SimpleElementList(fileIndex.getTermKeyListFilename(),true);
        docIndexList=new BasicIRDocIndexList(fileIndex.getDocIndexListFilename(),true);
        termIndexList=new BasicIRTermIndexList(fileIndex.getTermIndexListFilename(),true);
        doctermMatrix=new SequenceFileWriter(fileIndex.getDocTermSeqIndexFilename(),fileIndex.getDocTermSeqFilename());
        initialized=true;
    }

    public void close(){
        flush();
        collection.save(fileIndex.getCollectionFilename());
        docIndexList.close();
        termIndexList.close();
        doctermMatrix.close();
        docKeyList.close();
        termKeyList.close();
        initialized=false;
    }

    public void clean(){
        File file;

        file=new File(fileIndex.getDirectory());
        file.delete();
        file.mkdir();
    }
}
