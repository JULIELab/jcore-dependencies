package dragon.onlinedb;

import dragon.util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.RandomAccessFile;

/**
 * <p>Basic collection reader (supporting class for indexing) </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicCollectionReader implements CollectionReader{
    protected ArticleParser parser;
    protected BufferedReader br;
    protected BasicArticleIndex indexList;
    protected RandomAccessFile raf;
    protected String collectionFile, indexFile;

    public BasicCollectionReader(){
        parser=new BasicArticleParser();
        br=null;
    }

    public BasicCollectionReader(String collectionFile) {
        this(collectionFile,null);
    }

    public BasicCollectionReader(String collectionFile, String indexFile) {
        parser=new BasicArticleParser();
        loadCollection(collectionFile,indexFile);
    }

    public BasicCollectionReader(String collectionFile, String indexFile, ArticleParser parser) {
        this.parser =parser;
        loadCollection(collectionFile,indexFile);
    }

    public boolean loadCollection(String collectionFile, String indexFile){
        try{
            close();
            br = FileUtil.getTextReader(collectionFile);
            if(indexFile!=null && (new File(indexFile)).exists())
                indexList = new BasicArticleIndex(indexFile, false);
            else
                indexFile=null;
            raf = new RandomAccessFile(collectionFile, "r");
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public ArticleParser getArticleParser(){
        return parser;
    }

    public void setArticleParser(ArticleParser parser) {
        this.parser = parser;
    }

    public Article getArticleByKey(String key){
        BasicArticleKey curKey;
        String line;

        try{
            curKey = indexList.search(key);
            if (curKey == null)
                return null;
            else {
                raf.seek(curKey.getOffset());
                line=raf.readLine();
                return parser.parse(line);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Article getNextArticle(){
        String line;

        try {
            if(parser==null || br==null)
                return null;
            line = br.readLine();
            if (line == null || line.trim().length() == 0)
                return null;
            else
                return parser.parse(line);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void close(){
        try{
            if(indexList!=null)
                indexList.close();
            if(br!=null)
                br.close();
            if(raf!=null)
                raf.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean supportArticleKeyRetrieval(){
        return indexList!=null;
    }

    public void restart(){
        loadCollection(collectionFile,indexFile);
    }

    public int size(){
    	return -1;
    }
}