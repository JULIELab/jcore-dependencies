package dragon.onlinedb.citeulike;

import dragon.onlinedb.AbstractQuery;
import dragon.onlinedb.Article;
import dragon.onlinedb.ArticleParser;
import dragon.util.HttpUtil;
/**
 * <p>CiteULike Tag Query</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class CiteULikeTagQuery extends AbstractQuery{
    protected HttpUtil http;
    protected String term;
    protected String[] arrPaper;
    private ArticleParser parser;

    public static void main(String[] args) {
        CiteULikeTagQuery query;
        Article article;
        int i,top;

        query=new CiteULikeTagQuery("network");
        query.initQuery();

        top=10;
        for(i=0;i<top && query.moveToNextArticle();i++){
            article=query.getArticle();
            System.out.println(query.getArticle().getKey()+" "+article.getTitle());
        }
    }

    public CiteULikeTagQuery(){
        this(null);
    }

    public CiteULikeTagQuery(String term){
        super(50);
        parser=new CiteULikeArticleParser();
        arrPaper=new String[pageWidth];
        this.term=term;
        http=new HttpUtil("www.citeulike.org");
    }

    public boolean supportArticleKeyRetrieval(){
        return true;
    }

    public void setSearchTerm(String term){
        this.term=term;
    }

    public  boolean initQuery(){
        curPageNo=-1;
        curArticle=null;
        curPageWidth=0;
        pageNum=1;
        return true;
    }

    public boolean moveToPage(int pageNo){
        String curUrl;
        String content;

        if (pageNo >= pageNum || pageNum==0)
            return false;
        if(pageNo==curPageNo) return true;

        curUrl ="/search/all?f=tag&q="+term;
        if(pageNo>0)
            curUrl=curUrl+"&page="+(pageNo+1);
        content=http.get(curUrl);
        if (content == null)
            return false;
        return processPage(pageNo,content);
    }

    private boolean processPage(int pageNo, String content){
        int start, end, count;

        count = 0;
        start = content.indexOf("class=\"title\"");
        while (start >= 0) {
            start=content.indexOf("article",start);
            if(start<0)
                break;
            start=start+8;
            end=content.indexOf('\"',start);
            arrPaper[count] = content.substring(start,end);
            count = count + 1;
            start = content.indexOf("class=\"title\"",end);
        }
        curPageNo=pageNo;
        curPageWidth = count;
        curArticleNo = 0;
        if(curPageWidth==0)
            return false;
        curArticle=getArticleByKey(arrPaper[curArticleNo]);

        //adjust page number
        end=content.indexOf(">Next<");
        if(end<0)
            pageNum=pageNo+1;
        else{
            end=content.lastIndexOf("</a>",end);
            start=content.lastIndexOf('>',end);
            pageNum=Integer.parseInt(content.substring(start+1,end));
        }
        return true;
    }

    public Article getArticleByKey(String id){
        String curUrl;
        String content;
        try{
            curUrl = "/article/" + id;
            content = http.get(curUrl);
            if (content == null)
                return null;
            return parser.parse(content);
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    protected Article getArticle(int articleNo){
        return getArticleByKey(arrPaper[articleNo]);
    }
}
