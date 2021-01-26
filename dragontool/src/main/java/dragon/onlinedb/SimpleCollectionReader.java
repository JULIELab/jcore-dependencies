package dragon.onlinedb;

import dragon.util.FileUtil;

import java.io.File;

/**
 * <p>A light collection reader</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SimpleCollectionReader implements CollectionReader{
    private File[] arrFile;
    private int curPos;
    private ArticleParser parser;
    private String root;

    public SimpleCollectionReader(String folder){
    	this(folder,new SimpleArticleParser());
    }
    
    public SimpleCollectionReader(String folder, ArticleParser parser) {
        File file;

        root=folder;
        file=new File(folder);
        if(file.isDirectory())
            arrFile = file.listFiles();
        else
            arrFile = null;
        curPos=0;
        this.parser =parser;
    }

    public SimpleCollectionReader(ArticleParser parser){
        root=null;
        arrFile=null;
        this.parser=parser;
        curPos=0;
    }

    public boolean loadCollection(String collectionPath){
        File file;

        root=collectionPath;
        file=new File(collectionPath);
        if(file.isDirectory()){
            arrFile = file.listFiles();
            if(parser==null)
                parser=new SimpleArticleParser();
        }
        else{
            arrFile = null;
        }
        curPos=0;
        return arrFile!=null;
    }

    public ArticleParser getArticleParser(){
        return parser;
    }

    public void setArticleParser(ArticleParser parser) {
        this.parser = parser;
    }

    public Article getNextArticle(){
        Article article;

        if(parser==null || arrFile==null)
            return null;
        while(curPos<arrFile.length){
            if(arrFile[curPos].isFile()){
                article=parser.parse(FileUtil.readTextFile(arrFile[curPos]));
                if(article.getKey()==null)
                    article.setKey(arrFile[curPos].getName());
                curPos++;
                return article;
            }
            else
                curPos++;
        }
        return null;
    }

    public Article getArticleByKey(String key){
        File file;
        Article article;

        file=new File(root,key);
        if(file.exists() && file.isFile()){
            article=parser.parse(FileUtil.readTextFile(file));
            article.setKey(key);
            return article;
        }
        else
            return null;
    }

    public void close(){
        parser=null;
    }

    public boolean supportArticleKeyRetrieval(){
        return true;
    }

    public void restart(){
        curPos=0;
    }
    
    public int size(){
    	return -1;
    }
}