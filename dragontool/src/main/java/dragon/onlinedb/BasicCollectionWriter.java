package dragon.onlinedb;

import dragon.util.EnvVariable;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * <p>Writing collection to disk </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicCollectionWriter implements CollectionWriter{
    protected RandomAccessFile rafCollection;
    protected BasicArticleIndex articleIndex;
    protected ArticleParser parser;
    protected String charSet;

    public BasicCollectionWriter(String collectionFile, String indexFile, boolean append) {
        File file;

        try{
            charSet=EnvVariable.getCharSet();
            parser=new BasicArticleParser();
            if(!append){
                file=new File(collectionFile);
                if(file.exists())
                    file.delete();
                file=new File(indexFile);
                if(file.exists())
                    file.delete();
            }
            rafCollection = new RandomAccessFile(collectionFile, "rw");
            if(rafCollection.length()>0) rafCollection.seek(rafCollection.length());
            articleIndex = new BasicArticleIndex(indexFile, true);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public ArticleParser getArticleParser(){
        return parser;
    }

    public void setArticleParser(ArticleParser parser){
        this.parser =parser;
    }

    public boolean add(Article article){
        String line;

        try{
            if (article == null || article.getKey() == null)
                return false;
            if (!articleIndex.add(article.getKey(),rafCollection.getFilePointer()))
                return false;
            line=parser.assemble(article);
            //support different character set
            if(charSet!=null)
                rafCollection.write(line.getBytes(charSet));
            else
                rafCollection.write(line.getBytes());
            if(line.charAt(line.length()-1)!='\n')
                rafCollection.writeByte('\n');
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
     }

    public void close(){
        try{
            articleIndex.setCollectionFileSize(rafCollection.getFilePointer());
            rafCollection.close();
            articleIndex.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}