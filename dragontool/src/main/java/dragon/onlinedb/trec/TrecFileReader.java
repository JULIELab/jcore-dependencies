package dragon.onlinedb.trec;

import dragon.onlinedb.Article;
import dragon.onlinedb.ArticleParser;
import dragon.onlinedb.CollectionReader;
import dragon.util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
/**
 * <p>TREC data file reader</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class TrecFileReader implements CollectionReader{
    private ArticleParser parser;
    private BufferedReader reader;
    private StringBuffer sb;
    private char[] buf;
    private String filename;
    private File colFile;
    private long curArticleOffset, deletedBytes;
    private int curArticleLength;
    private boolean done;

    public TrecFileReader(ArticleParser parser){
        this(null,parser);
    }

    public TrecFileReader(File colFile, ArticleParser parser) {
        this.parser =parser;
        buf = new char[10240];
        loadCollection(colFile);
    }

    public boolean loadFile(String colFile){
        return loadCollection(new File(colFile));
    }

    public boolean loadCollection(File colFile){
        try{
            deletedBytes=0;
            curArticleOffset=-1;
            curArticleLength=0;
            this.colFile =colFile;

            if(colFile==null || !testCollectionFile(colFile)){
                done=true;
                reader=null;
                sb=null;
                filename=null;
                return false;
            }
            else{
                filename=colFile.getName();
                reader = FileUtil.getTextReader(colFile);
                done = false;
                sb = new StringBuffer(10240);
                return true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            reader = null;
            done = true;
            return false;
        }
    }

    public ArticleParser getArticleParser(){
        return parser;
    }

    public void setArticleParser(ArticleParser parser){
        this.parser =parser;
    }

    public Article getArticleByKey(String key){
        return null;
    }

    public Article getNextArticle(){
        Article article;
        int len, start, end;

        try {
            if(reader==null || sb==null)
                return null;

            end=sb.indexOf("</DOC>");
            while(end<0 && !done){
                len = reader.read(buf);
                if(len<buf.length)
                    done=true;
                start=sb.length();
                sb.append(buf,0,len);
                end=sb.indexOf("</DOC>",start);
            }
            if(end<0) return null;

            end=end+6;
            start=sb.lastIndexOf("<DOC>",end);
            if(start<0) return null;

            curArticleOffset=deletedBytes+start;
            curArticleLength=end-start;
            article=parser.parse(sb.substring(start,end));
            sb.delete(0,end);
            deletedBytes+=end;
            return article;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public long getArticleOffset(){
        return curArticleOffset;
    }

    public int getArticleLength(){
        return curArticleLength;
    }

    public String getArticleFilename(){
        return filename;
    }

    public void close(){
        try{
            if (reader != null)
                reader.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private boolean testCollectionFile(File file){
        BufferedReader br;
        boolean ret;

        try{
            if(!file.exists() || file.isDirectory())
                return false;
            br=FileUtil.getTextReader(file);
            ret=br.readLine().trim().equalsIgnoreCase("<DOC>");
            br.close();
            return ret;
        }
        catch(Exception e){
            return false;
        }
    }

    public boolean supportArticleKeyRetrieval(){
        return false;
    }

    public void restart(){
        loadCollection(colFile);
    }
    
    public int size(){
    	return -1;
    }
}