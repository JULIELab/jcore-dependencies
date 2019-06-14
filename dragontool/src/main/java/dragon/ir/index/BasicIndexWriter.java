package dragon.ir.index;

import dragon.matrix.IntSuperSparseMatrix;

import java.io.File;

/**
 * <p>The class is used to initialize and write index to disk </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicIndexWriter extends AbstractIndexWriter{
    private FileIndex fileIndex;

    public BasicIndexWriter(String directory, boolean relationSupported) {
        super(relationSupported);
        fileIndex=new FileIndex(directory,relationSupported);
    }

    public void initialize(){
        if(initialized)
            return;
        doc_in_cache=0;
        collection=new IRCollection();
        collection.load(fileIndex.getCollectionFilename());

        docIndexList=new BasicIRDocIndexList(fileIndex.getDocIndexListFilename(),true);
        termIndexList=new BasicIRTermIndexList(fileIndex.getTermIndexListFilename(),true);
        doctermMatrix=new IntSuperSparseMatrix(fileIndex.getDocTermIndexFilename(),fileIndex.getDocTermFilename(),false,false);
        ((IntSuperSparseMatrix)doctermMatrix).setFlushInterval(Integer.MAX_VALUE);

        if(relationSupported){
            relationIndexList = new BasicIRRelationIndexList(fileIndex.getRelationIndexListFilename(), true);
            docrelationMatrix=new IntSuperSparseMatrix(fileIndex.getDocRelationIndexFilename(),fileIndex.getDocRelationFilename(),false,false);
            ((IntSuperSparseMatrix)docrelationMatrix).setFlushInterval(Integer.MAX_VALUE);
        }
        initialized=true;
    }

    public void flush(){
        doc_in_cache = 0;
        collection.setDocNum(docIndexList.size());
        collection.setTermNum(termIndexList.size());
        ((IntSuperSparseMatrix)doctermMatrix).flush();

        if (relationSupported) {
            collection.setRelationNum(relationIndexList.size());
            ((IntSuperSparseMatrix) docrelationMatrix).flush();
        }
    }


    public void close(){
        TransposeIRMatrix trans;

        flush();
        collection.save(fileIndex.getCollectionFilename());
        docIndexList.close();
        termIndexList.close();
        doctermMatrix.finalizeData();
        doctermMatrix.close();

        if(relationSupported){
            relationIndexList.close();
            docrelationMatrix.finalizeData();
            docrelationMatrix.close();
        }

        trans=new TransposeIRMatrix();
        trans.genTermDocMatrix(fileIndex.getDirectory());
        if(relationSupported)
            trans.genRelationDocMatrix(fileIndex.getDirectory());
    }

    public void clean(){
        File file;

        file=new File(fileIndex.getDirectory());
        file.delete();
        file.mkdir();
    }
}
