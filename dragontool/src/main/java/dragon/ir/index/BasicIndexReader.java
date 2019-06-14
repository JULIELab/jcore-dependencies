package dragon.ir.index;

import dragon.matrix.IntGiantSparseMatrix;
import dragon.matrix.IntSuperSparseMatrix;
import dragon.nlp.SimpleElementList;
import dragon.onlinedb.CollectionReader;

import java.io.File;

/**
 * <p>BasicIndexReader is used to read all the indexing information into memory for a indexed collection </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicIndexReader  extends AbstractIndexReader {
    private FileIndex fileIndex;

    public BasicIndexReader(String directory, boolean relationSupported) {
        this(directory,relationSupported,null);
    }

    public BasicIndexReader(String directory, boolean relationSupported, CollectionReader collectionReader) {
        super(relationSupported);
        this.relationSupported = relationSupported;
        this.collectionReader =collectionReader;
        fileIndex=new FileIndex(directory,relationSupported);
    }

    public void initialize() {
        collection = new IRCollection();
        collection.load(fileIndex.getCollectionFilename());
        termIndexList = new BasicIRTermIndexList(fileIndex.getTermIndexListFilename(),false);
        docIndexList = new BasicIRDocIndexList(fileIndex.getDocIndexListFilename(), false);
        termdocMatrix = new IntSuperSparseMatrix(fileIndex.getTermDocIndexFilename(), fileIndex.getTermDocFilename());
        doctermMatrix = new IntGiantSparseMatrix(fileIndex.getDocTermIndexFilename(), fileIndex.getDocTermFilename());
        if((new File(fileIndex.getDocKeyListFilename())).exists())
            docKeyList=new SimpleElementList(fileIndex.getDocKeyListFilename(),false);
        if((new File(fileIndex.getTermKeyListFilename())).exists())
            termKeyList=new SimpleElementList(fileIndex.getTermKeyListFilename(),false);
        if (relationSupported) {
            relationIndexList = new BasicIRRelationIndexList(fileIndex.getRelationIndexListFilename(), false);
            relationdocMatrix = new IntGiantSparseMatrix(fileIndex.getRelationDocIndexFilename(), fileIndex.getRelationDocFilename());
            docrelationMatrix = new IntGiantSparseMatrix(fileIndex.getDocRelationIndexFilename(), fileIndex.getDocRelationFilename());
        }
    }
}
