package dragon.config;

import dragon.ir.index.IndexConverter;

/**
 * <p>Index conversion application configuration </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class IndexConvertAppConfig {
    public IndexConvertAppConfig() {
    }

    public static void main(String[] args) {
        IndexConvertAppConfig indexApp;
        ConfigureNode root,indexAppNode;
        ConfigUtil util;

        if(args.length!=2){
            System.out.println("Please input two parameters: configuration xml file and indexing applicaiton id");
            return;
        }

        root=new BasicConfigureNode(args[0]);
        util=new ConfigUtil();
        indexAppNode=util.getConfigureNode(root,"indexconvertapp",Integer.parseInt(args[1]));
        if(indexAppNode==null)
            return;
        indexApp=new IndexConvertAppConfig();
        indexApp.convert(indexAppNode);
    }

    public void convert(ConfigureNode node){
        IndexConverter converter;
        String task, indexFolder, doctermFile, doclinkFile;
        boolean outputTransposedMatrix;

        task = node.getString("task");
        indexFolder=node.getString("indexfolder");
        if(task==null || indexFolder==null)
            return;
        doctermFile=node.getString("doctermfile");
        doclinkFile=node.getString("doclinkagefile");
        outputTransposedMatrix=node.getBoolean("gentransposedmatrix",false);
        converter=new IndexConverter();
        if(task.equalsIgnoreCase("import")){
            if(doctermFile!=null && doctermFile.trim().length()>0)
                converter.importIndex(indexFolder,doctermFile);
            if(doclinkFile!=null && doclinkFile.trim().length()>0)
                converter.importDocLinkage(indexFolder,doclinkFile,outputTransposedMatrix);
        }
        else if(task.equalsIgnoreCase("export")){
            if(doctermFile!=null && doctermFile.trim().length()>0)
                converter.exportIndex(indexFolder,doctermFile);
            if(doclinkFile!=null && doclinkFile.trim().length()>0)
                converter.exportDocLinkage(indexFolder,doclinkFile);
        }
    }
}
