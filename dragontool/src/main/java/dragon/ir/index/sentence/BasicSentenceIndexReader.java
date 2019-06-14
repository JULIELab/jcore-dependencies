package dragon.ir.index.sentence;

import dragon.ir.index.BasicIndexReader;
import dragon.ir.index.FileIndex;
import dragon.onlinedb.BasicCollectionReader;

import java.io.File;
/**
 * <p>Basic index reader for reading sentence indexing information </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicSentenceIndexReader extends BasicIndexReader{
    private FileIndex fileIndex;

    public BasicSentenceIndexReader(String directory, boolean relationSupported) {
        super(directory,relationSupported);
        fileIndex=new FileIndex(directory,relationSupported);
    }

    public void initialize(){

        super.initialize();
        if(new File(fileIndex.getRawSentenceCollectionFilename()).exists() && new File(fileIndex.getRawSentenceIndexFilename()).exists())
            collectionReader=new BasicCollectionReader(fileIndex.getDirectory() ,FileIndex.getSentenceCollectionName());
    }

    public void close(){
        collectionReader.close();
        super.close();
    }
}