package dragon.config;

import dragon.matrix.*;

/**
 * <p>Sparse matrix configuration</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SparseMatrixConfig extends ConfigUtil{
    public SparseMatrixConfig() {
       super();
    }

    public SparseMatrixConfig(ConfigureNode root){
       super(root);
    }

    public SparseMatrixConfig(String configFile){
        super(configFile);
    }

    public IntSparseMatrix getIntSparseMatrix(int matrixID){
        return getIntSparseMatrix(root,matrixID);
    }

    public IntSparseMatrix getIntSparseMatrix(ConfigureNode node, int matrixID){
        return loadIntSparseMatrix(node,matrixID);
    }

    private IntSparseMatrix loadIntSparseMatrix(ConfigureNode node, int matrixID){
        ConfigureNode matrixNode;
        String matrixName;
        matrixNode=getConfigureNode(node,"intsparsematrix",matrixID);
        if(matrixNode==null)
            return null;
        matrixName=matrixNode.getNodeName();
        return loadIntSparseMatrix(matrixName,matrixNode);
    }

    protected IntSparseMatrix loadIntSparseMatrix(String matrixName, ConfigureNode matrixNode){
         if(matrixName.equalsIgnoreCase("IntSuperSparseMatrix"))
            return loadIntSuperSparseMatrix(matrixNode);
        else if(matrixName.equalsIgnoreCase("IntGiantSparseMatrix"))
            return loadIntGiantSparseMatrix(matrixNode);
        else if(matrixName.equalsIgnoreCase("IntFlatSparseMatrix"))
            return loadIntFlatSparseMatrix(matrixNode);
        else
            return (IntSparseMatrix)loadResource(matrixNode);
    }

    private IntSparseMatrix loadIntFlatSparseMatrix(ConfigureNode node){
        boolean binaryMode, mergeMode, miniMode;
        String filename;

        filename=node.getString("filename",null);
        binaryMode=node.getBoolean("binaryfile",false);
        mergeMode=node.getBoolean("mergemode",false);
        miniMode=node.getBoolean("minimode",false);
        if(filename!=null)
            return new IntFlatSparseMatrix(filename,binaryMode);
        else
            return new IntFlatSparseMatrix(mergeMode,miniMode);
    }

    private IntSparseMatrix loadIntSuperSparseMatrix(ConfigureNode node){
        String matrixFile, indexFile;
        String path, key;

        path=node.getString("matrixpath");
        key=node.getString("matrixkey");
        matrixFile=path+"/"+key+".matrix";
        indexFile=path+"/"+key+".index";
        if(node.exist("mergemode"))
            return new IntSuperSparseMatrix(indexFile,matrixFile,node.getBoolean("mergemode",false),node.getBoolean("minimode",false));
        else
            return new IntSuperSparseMatrix(indexFile,matrixFile);
    }

    private IntSparseMatrix loadIntGiantSparseMatrix(ConfigureNode node){
        String matrixFile, indexFile;
        String path, key;

        path=node.getString("matrixpath");
        key=node.getString("matrixkey");
        matrixFile=path+"/"+key+".matrix";
        indexFile=path+"/"+key+".index";
        if(node.exist("mergemode"))
            return new IntGiantSparseMatrix(indexFile,matrixFile,node.getBoolean("mergemode",false),node.getBoolean("minimode",false));
        else
            return new IntGiantSparseMatrix(indexFile,matrixFile);
    }

    public DoubleSparseMatrix getDoubleSparseMatrix(int matrixID){
        return getDoubleSparseMatrix(root,matrixID);
    }

    public DoubleSparseMatrix getDoubleSparseMatrix(ConfigureNode node, int matrixID){
        return loadDoubleSparseMatrix(node,matrixID);
    }

    private DoubleSparseMatrix loadDoubleSparseMatrix(ConfigureNode node, int matrixID){
        ConfigureNode matrixNode;
        String matrixName;
        matrixNode=getConfigureNode(node,"doublesparsematrix",matrixID);
        if(matrixNode==null)
            return null;
        matrixName=matrixNode.getNodeName();
        return loadDoubleSparseMatrix(matrixName,matrixNode);
    }

    protected DoubleSparseMatrix loadDoubleSparseMatrix(String matrixName, ConfigureNode matrixNode){
        if(matrixName.equalsIgnoreCase("DoubleSuperSparseMatrix"))
            return loadDoubleSuperSparseMatrix(matrixNode);
        else if(matrixName.equalsIgnoreCase("DoubleGiantSparseMatrix"))
            return loadDoubleGiantSparseMatrix(matrixNode);
        else if(matrixName.equalsIgnoreCase("DoubleFlatSparseMatrix"))
            return loadDoubleFlatSparseMatrix(matrixNode);
        else
            return (DoubleSparseMatrix)loadResource(matrixNode);
    }

    private DoubleSparseMatrix loadDoubleFlatSparseMatrix(ConfigureNode node){
        boolean binaryMode, mergeMode, miniMode;
        String filename;

        filename=node.getString("filename",null);
        binaryMode=node.getBoolean("binaryfile",false);
        mergeMode=node.getBoolean("mergemode",false);
        miniMode=node.getBoolean("minimode",false);
        if(filename!=null)
            return new DoubleFlatSparseMatrix(filename,binaryMode);
        else
            return new DoubleFlatSparseMatrix(mergeMode,miniMode);
    }

    private DoubleSparseMatrix loadDoubleSuperSparseMatrix(ConfigureNode node){
        String matrixFile, indexFile;
        String path, key;

        path=node.getString("matrixpath");
        key=node.getString("matrixkey");
        matrixFile=path+"/"+key+".matrix";
        indexFile=path+"/"+key+".index";
        if(node.exist("mergemode"))
            return new DoubleSuperSparseMatrix(indexFile,matrixFile,node.getBoolean("mergemode",false),node.getBoolean("minimode",false));
        else
            return new DoubleSuperSparseMatrix(indexFile,matrixFile);
    }

    private DoubleSparseMatrix loadDoubleGiantSparseMatrix(ConfigureNode node){
        String matrixFile, indexFile;
        String path, key;

        path=node.getString("matrixpath");
        key=node.getString("matrixkey");
        matrixFile=path+"/"+key+".matrix";
        indexFile=path+"/"+key+".index";
        if(node.exist("mergemode"))
            return new DoubleGiantSparseMatrix(indexFile,matrixFile,node.getBoolean("mergemode",false),node.getBoolean("minimode",false));
        else
            return new DoubleGiantSparseMatrix(indexFile,matrixFile);
    }
}
