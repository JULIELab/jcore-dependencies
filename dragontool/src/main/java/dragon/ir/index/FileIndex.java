package dragon.ir.index;

import java.io.*;

/**
 * <p>The class is used to read according files storing indexing information.</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class FileIndex {
    protected String directory;
    protected boolean relationSupported;

    public FileIndex(String directory, boolean relationSupported) {
        File file;

        if(directory.endsWith("\\") || directory.endsWith("/"))
            this.directory =directory.substring(0,directory.length()-1);
        else
            this.directory =directory;
        this.relationSupported =relationSupported;
        file=new File(directory);
        if(!file.exists()) file.mkdirs();
    }

    public String getDirectory(){
        return directory;
    }

    public boolean isRelationSupported(){
        return relationSupported;
    }

    public String getCollectionFilename(){
        return directory+"/collection.stat";
    }

    public String getTermDocFilename(){
        return directory+"/termdoc.matrix";
    }

    public String getTermDocIndexFilename(){
        return directory+"/termdoc.index";
    }

    public String getRelationDocFilename(){
        return directory+"/relationdoc.matrix";
    }

    public String getRelationDocIndexFilename(){
        return directory+"/relationdoc.index";
    }

    public String getDocRelationFilename(){
        return directory+"/docrelation.matrix";
    }

    public String getDocRelationIndexFilename(){
        return directory+"/docrelation.index";
    }

    public String getDocTermFilename(){
        return directory+"/docterm.matrix";
    }

    public String getDocTermIndexFilename(){
        return directory + "/docterm.index";
    }

    public String getDocTermSeqFilename(){
        return directory+"/doctermseq.matrix";
    }

    public String getDocTermSeqIndexFilename(){
        return directory + "/doctermseq.index";
    }

    public String getTermIndexListFilename(){
        return directory+"/termindex.list";
    }

    public String getTermKeyListFilename(){
        return directory+"/termkey.list";
    }

    public String getRelationIndexListFilename(){
        return directory+"/relationindex.list";
    }

    public String getRelationKeyListFilename(){
        return directory+"/relationkey.list";
    }

    public String getDocIndexListFilename(){
        return directory+"/docindex.list";
    }

    public String getDocKeyListFilename(){
       return directory+"/dockey.list";
   }

   public String getRawSentenceCollectionFilename(){
        return directory+"/rawsentence.collection";
   }

   public String getRawSentenceIndexFilename(){
        return directory+"/rawsentence.index";
   }

   public static String getSentenceCollectionName(){
       return "rawsentence";
   }
}