package dragon.onlinedb.pubmed;

import dragon.onlinedb.AbstractQuery;
import dragon.onlinedb.Article;
import dragon.util.HttpUtil;
/**
 * <p>PubMed article query </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class PubMedQuery extends AbstractQuery{
    protected HttpUtil http;
    protected String webEnv,queryKey;
    protected String database, term;
    protected String eSearchUrl, eFetchUrl, server;
    protected String startDate, endDate;
    protected String[] arrPaper;

    public static void main(String[] args) {
        PubMedQuery query;
        String term;
        int i,top;

        query=new PubMedQuery(1);
        term="hypertension[TIAB] AND diabetes[TIAB]";
        term="";
        query.setSearchTerm(term);
        query.setDateRange("1995/01/01","2003/12/31");
        query.initQuery();

        top=100000;
        for(i=0;i<top && query.moveToNextArticle();i++){
            System.out.println(query.getArticle().getKey());
        }
    }

    public PubMedQuery(String term, int pageWidth){
        super(pageWidth);
        arrPaper=new String[pageWidth];
        if(term!=null)
            this.term=term.replace(' ','+');
        eSearchUrl="/entrez/eutils/esearch.fcgi?";
        eFetchUrl="/entrez/eutils/efetch.fcgi?";
        server="eutils.ncbi.nlm.nih.gov";
        http=new HttpUtil(server);
        startDate=null;
        endDate=null;
    }

    public PubMedQuery(int pageWidth){
        this(null,pageWidth);
    }

    public boolean supportArticleKeyRetrieval(){
        return true;
    }

    public void setSearchTerm(String term){
        this.term=term.replace(' ','+');
    }

    public void setDateRange(String start, String end){
        startDate=start;
        endDate=end;
    }

    public  boolean initQuery(){
        String curUrl;
        String content;
        int start, end;

        curPageNo=-1;
        curArticle=null;
        curPageWidth=0;

        curUrl = eSearchUrl+"usehistory=y&db=pubmed&term="+term+"&retmax=1";
        if(startDate!=null && endDate!=null)
            curUrl=curUrl+"&mindate="+startDate+"&maxdate="+endDate;
        content=http.get(curUrl);
        if(content==null) return false;

        //get Search Result
        start=content.indexOf("<Count>")+7;
        end=content.indexOf("</Count>",start);
        articleNum=Integer.parseInt(content.substring(start,end));
        start=content.indexOf("<QueryKey>",start)+10;
        end=content.indexOf("</QueryKey>",start);
        queryKey=content.substring(start,end);
        start=content.indexOf("<WebEnv>",start)+8;
        end=content.indexOf("</WebEnv>",start);
        webEnv=content.substring(start,end);
        if(articleNum==0)
            pageNum=0;
        else
            pageNum=(articleNum-1)/pageWidth+1;
        return true;
    }

    public boolean moveToPage(int pageNo){
        String curUrl;
        String content;
        int start, end, count;

        if (pageNo >= pageNum || pageNum==0)
            return false;
        if(pageNo==curPageNo) return true;

        curUrl = eFetchUrl + "db=PubMed&WebEnv=" + webEnv +
            "&retmode=text&query_key=" + queryKey;
        curUrl = curUrl + "&retmax=" + pageWidth + "&retstart=" +
            (pageNo * pageWidth);
        content=http.get(curUrl);
        if (content == null)
            return false;

        count = 0;
        start = content.indexOf("Pubmed-entry");
        while (start >= 0) {
            end = content.indexOf("Pubmed-entry", start + 12);
            if (end >= 0) {
                arrPaper[count]=content.substring(start,end);
            }
            else {
                arrPaper[count] = content.substring(start);
            }
            count = count + 1;
            start = end;
        }
        curPageNo=pageNo;
        curPageWidth = count;
        curArticleNo = 0;
        if(arrPaper[curArticleNo]==null)
            return false;
        curArticle=new PubMedArticle(arrPaper[curArticleNo]);
        return true;
    }

    public Article getArticleByKey(String PMID){
        String curUrl;
        String content;
        int start;

        curUrl = eFetchUrl + "db=PubMed&retmode=text&id="+PMID;
        content=http.get(curUrl);
        if (content == null)
            return null;

        start = content.indexOf("Pubmed-entry");
        if(start >= 0) {
            return new PubMedArticle(content);
        }
        else
            return null;
    }

    protected Article getArticle(int articleNo){
        return new PubMedArticle(arrPaper[articleNo]);
    }
}