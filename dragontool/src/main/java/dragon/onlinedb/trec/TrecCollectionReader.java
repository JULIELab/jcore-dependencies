package dragon.onlinedb.trec;

import dragon.onlinedb.*;
import java.io.*;

/**
 * <p>TREC collection reader</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class TrecCollectionReader implements CollectionReader{
    private CollectionReader curCollection;
    private BasicArticleIndex articleIndex;
    private RandomAccessFile curCacheFile;
    private int curCacheFileIndex;
    private String filename, collectionPath, indexFile;
    private byte[] buf;
    private File[] arrFile;
    private int curFileIndex;
    private int totalFileIndex;
    private ArticleParser parser;

    public TrecCollectionReader(ArticleParser parser) {
        curCollection=null;
        totalFileIndex=0;
        curFileIndex=0;
        arrFile=null;
        curCacheFileIndex=-1;
        curCacheFile=null;
        this.parser =parser;
    }

    private TrecCollectionReader(File folder, ArticleParser parser){
         this.parser=parser;
         curCacheFileIndex=-1;
         curCacheFile=null;
         loadCollection(folder,null);
     }

     public TrecCollectionReader(String collectionPath, ArticleParser parser) {
         this(new File(collectionPath),parser);
     }

    public TrecCollectionReader(String collectionPath, String indexFile, ArticleParser parser) {
        this.parser=parser;
        curCacheFileIndex=-1;
        curCacheFile=null;
        loadCollection(new File(collectionPath),indexFile);
    }

    public boolean loadCollection(String collectionPath){
        return loadCollection(collectionPath,null);
    }

    public boolean loadCollection(String collectionPath, String indexFile){
       return loadCollection(new File(collectionPath),indexFile);
    }

    private boolean loadCollection(File folder, String indexFile){
        close();
        this.indexFile =indexFile;
        if(indexFile==null)
            articleIndex=null;
        else if(!(new File(indexFile)).exists() || (new File(indexFile)).length()<=18)
            articleIndex=new BasicArticleIndex(indexFile,true);
        else
            articleIndex=new BasicArticleIndex(indexFile,false);

        if(!folder.isDirectory()){
            arrFile=new File[1];
            arrFile[0]=folder;
            totalFileIndex=1;
            filename=null;
        }
        else{
            filename=folder.getName();
            arrFile = folder.listFiles();
            totalFileIndex = arrFile.length;
        }
        collectionPath=folder.getPath();
        curFileIndex = 0;
        if(arrFile[0].isDirectory())
            curCollection= new TrecCollectionReader(arrFile[0],parser);
        else
            curCollection=new TrecFileReader(arrFile[0],parser);
        return true;
    }

    public ArticleParser getArticleParser(){
        return parser;
    }

    public void setArticleParser(ArticleParser parser) {
       this.parser =parser;
       if(curCollection!=null)
           curCollection.setArticleParser(parser);
    }

    public Article getArticleByKey(String key){
        BasicArticleKey articleKey;
        String curFilename;

        try{
            if (articleIndex == null || articleIndex.isWritingMode())
                return null;
            articleKey=articleIndex.search(key);
            if(articleKey==null)
                return null;
            if(articleKey.getFileIndex()!=curCacheFileIndex){
                if(curCacheFile!=null)
                    curCacheFile.close();
                curCacheFileIndex=articleKey.getFileIndex();
                curFilename=articleIndex.getFilename(curCacheFileIndex);
                if(curFilename!=null)
                    curFilename=collectionPath+"/"+curFilename;
                else
                    curFilename=collectionPath;
                curCacheFile=new RandomAccessFile(curFilename,"r");
            }
            curCacheFile.seek(articleKey.getOffset());
            if(buf==null || buf.length<articleKey.getLength())
                buf=new byte[articleKey.getLength()+10240];
            curCacheFile.read(buf,0,articleKey.getLength());
            return parser.parse(new String(buf,0,articleKey.getLength()));
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Article getNextArticle(){
        Article cur;
        String curArticleFilename;

        try{
            if (curCollection == null && parser==null)
                return null;

            cur = curCollection.getNextArticle();
            while (cur == null && curFileIndex<totalFileIndex-1){
                curCollection.close();
                curFileIndex++;
                if(arrFile[curFileIndex].isDirectory())
                    curCollection = new TrecCollectionReader(arrFile[curFileIndex],parser);
                else
                    curCollection = new TrecFileReader(arrFile[curFileIndex],parser);
                cur=curCollection.getNextArticle();
            }

            if(cur!=null && articleIndex!=null && articleIndex.isWritingMode()){
                //index the information of this article
                curArticleFilename=getArticleFilename();
                if(curArticleFilename.indexOf("/")>0){
                    curArticleFilename = curArticleFilename.substring(curArticleFilename.indexOf("/") + 1);
                    articleIndex.add(cur.getKey(),curArticleFilename,getArticleOffset(),getArticleLength());
                }
                else
                    articleIndex.add(cur.getKey(),getArticleOffset());
            }

            return cur;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public long getArticleOffset(){
        if(curCollection==null)
            return -1;
        else if(curCollection.getClass().getName().equals("dragon.onlinedb.trec.TrecFileReader")){
            return ((TrecFileReader)curCollection).getArticleOffset();
        }
        else{
            return ((TrecCollectionReader)curCollection).getArticleOffset();
        }
    }

    public int getArticleLength(){
        if(curCollection==null)
            return -1;
        else if(curCollection.getClass().getName().equals("dragon.onlinedb.trec.TrecFileReader")){
            return ((TrecFileReader)curCollection).getArticleLength();
        }
        else{
            return ((TrecCollectionReader)curCollection).getArticleLength();
        }
    }

    public String getArticleFilename(){
        String name;

        if(curCollection==null)
            return null;
        else{
            if(curCollection.getClass().getName().equals("dragon.onlinedb.trec.TrecFileReader"))
                name=( (TrecFileReader) curCollection).getArticleFilename();
            else
                name=( (TrecCollectionReader) curCollection).getArticleFilename();
            if(filename==null)
                return name;
            else
                return filename+"/"+name;
        }
    }

    public void close(){
        try{
            if (curCollection != null)
                curCollection.close();
            if (articleIndex != null)
                articleIndex.close();
            if (curCacheFileIndex >= 0){
                curCacheFileIndex=-1;
                curCacheFile.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean supportArticleKeyRetrieval(){
        return articleIndex!=null && !articleIndex.isWritingMode() ;
    }

    public void restart(){
        loadCollection(collectionPath,indexFile);
    }
    
    public int size(){
    	return -1;
    }
}