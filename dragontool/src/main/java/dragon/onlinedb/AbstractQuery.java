package dragon.onlinedb;

/**
 * <p>Abstract class for querying articles from a data source</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractQuery implements ArticleQuery{
    protected int curPageNo, curArticleNo;
    protected int pageNum, articleNum;
    protected int pageWidth, curPageWidth;
    protected Article curArticle;
    protected ArticleParser parser;

    abstract protected Article getArticle(int articleNo);

    public AbstractQuery(int pageWidth){
        this.pageWidth=pageWidth;
        curPageNo=-1;
        curArticle=null;
        articleNum=-1;
    }

    public int getCurPageNo(){
        return curPageNo;
    }

    public boolean moveToNextPage(){
        return moveToPage(curPageNo+1);
    }

    public int getTotalArticleNum(){
        return articleNum;
    }

    public int getPageNum(){
        return pageNum;
    }

    public int getPageWidth(){
        return pageWidth;
    }

    public int getCurPageWidth(){
        return curPageWidth;
    }

    public boolean moveToNextArticle(){
        if(curArticleNo>=curPageWidth-1 || curPageNo<0){
            if(!moveToNextPage())
                return false;
        }
        else
        {
            curArticleNo++;
            curArticle=getArticle(curArticleNo);
        }
        return true;
    }

    public boolean moveToArticle(int paperNo){
        if(paperNo<0 || paperNo>curPageWidth)
            return false;
        else
            curArticleNo=paperNo;
        curArticle=getArticle(curArticleNo);
        return true;
    }

    public Article getArticle(){
        return curArticle;
    }

    public String getArticleKey(){
        return curArticle.getKey();
    }

    public boolean loadCollection(String collectionPath, String collectionName){
        return false;
    }

    public ArticleParser getArticleParser(){
        return parser;
    }

    public void setArticleParser(ArticleParser parser) {
        this.parser = parser;
    }

    public Article getNextArticle(){
        if(moveToNextArticle())
            return curArticle;
        else
            return null;
    }

    public void close(){
        curPageNo=-1;
        curArticle=null;
    }

    public void restart(){
        close();
        initQuery();
    }
    
    public int size(){
    	return articleNum;
    }
}