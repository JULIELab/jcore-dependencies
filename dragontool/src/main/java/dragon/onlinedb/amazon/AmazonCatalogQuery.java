package dragon.onlinedb.amazon;

import dragon.onlinedb.AbstractQuery;
import dragon.onlinedb.Article;
import dragon.onlinedb.BasicArticle;
import dragon.util.FileUtil;
import dragon.util.HttpUtil;
import dragon.util.SortedArray;

import java.io.BufferedWriter;

/**
 * <p>Amazon catalog query</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class AmazonCatalogQuery extends AbstractQuery{
    protected String queryKey;
    protected String term;
    protected String searchUrl, browseUrl;
    protected Article[] arrArticle;
    protected HttpUtil http;

    public AmazonCatalogQuery(String productCatalog){
        super(24);
        arrArticle=new Article[pageWidth];
        this.term=productCatalog;
        searchUrl="/gp/search";
        browseUrl="/gp/browse.html";
        http=new HttpUtil("www.amazon.com");
    }

    public AmazonCatalogQuery(){
        super(24);
        arrArticle=new Article[pageWidth];
        searchUrl="/gp/search";
        browseUrl="/gp/browse.html";
        http=new HttpUtil("www.amazon.com");
    }

    public static void main(String[] args) {
        //query=new AmazonCatalogQuery("281052");
        //query.initQuery();
        getProductList("565108","indexreview/laptop.query","indexreview/laptop.desc");
    }

    public static void getProductList(String catalogNode, String codeListFile, String descriptionListFile){
        AmazonCatalogQuery query;
        BufferedWriter bwCode, bwDesc;
        SortedArray codeList;
        Article article;
        int i,count;

        try{
            query = new AmazonCatalogQuery(catalogNode);
            if (!query.initQuery() || query.size()<=0)
                return;

            count=0;
            codeList=new SortedArray(query.size());
            bwDesc=FileUtil.getTextWriter(descriptionListFile);
            while(query.moveToNextArticle()){
                article=query.getArticle();
                if(article.getKey()!=null){
                    System.out.println(count+" "+article.getKey());
                    bwDesc.write(article.getKey() + "\t" + article.getTitle() + "\n");
                    bwDesc.flush();
                    codeList.add(article.getKey()+"");
                    count++;
                }
            }
            bwDesc.close();

            bwCode=FileUtil.getTextWriter(codeListFile);
            bwCode.write(codeList.size()+"\n");
            for(i=0;i<codeList.size();i++){
                bwCode.write((String)codeList.get(i)+"\n");
                bwCode.flush();
            }
            bwCode.flush();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean supportArticleKeyRetrieval(){
        return false;
    }

    public void setSearchTerm(String productCatalog){
        this.term=productCatalog;
    }

    public  boolean initQuery(){
        String curUrl;
        String content;
        curPageNo=-1;
        curArticle=null;
        curPageWidth=0;

        curUrl =browseUrl+"?_encoding=UTF8&node="+term;
        content=http.get(curUrl);
        if(content==null) return false;

        //get Search Result
        articleNum=getProductNum(content);
        if(articleNum==0)
            pageNum=0;
        else
            pageNum=(articleNum-1)/pageWidth+1;
        if(articleNum>0){
            curPageWidth=readProductFromWebPage(content);
        }
        return true;
    }

    public boolean moveToPage(int pageNo){
        String curUrl;
        String content;
        if (pageNo >= pageNum || pageNum==0)
            return false;

        if(pageNo==0 && curPageNo==-1){
            curPageNo=pageNo;
            curArticleNo = 0;
            if (curPageWidth <= 0)  return false;
            curArticle = arrArticle[curArticleNo];
            return true;
        }

        curUrl = searchUrl+"?_encoding=UTF8&rh="+queryKey+"&page="+(pageNo+1);
        content=http.get(curUrl);
        if (content == null)
            return false;

        curPageWidth = readProductFromWebPage(content);
        if(curPageWidth<=0)  return false;
        curPageNo=pageNo;
        curArticleNo = 0;
        curArticle=arrArticle[curArticleNo];
        return true;
    }

    public Article getArticleByKey(String PMID){
        return null;
    }

    protected Article getArticle(int articleNo){
        return arrArticle[articleNo];
    }

    private int getProductNum(String content){
        int start, end;
        int num;

        try{
            start=content.indexOf("\"ladderCount\"");
            if(start<0) return 0;
            start=content.indexOf("of ",start)+3;
            end=content.indexOf(" result",start);
            num=Integer.parseInt(content.substring(start,end));

            //read query key
            start=content.indexOf("name=\"rh\"",end)+9;
            start=content.indexOf("\"",start)+1;
            end=content.indexOf("\"",start);
            queryKey=content.substring(start,end);
            return num;
        }
        catch(Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    private int readProductFromWebPage(String content){
        int start, end;
        int count;

        try{
            start=content.indexOf("<div id=\"Results\">");
            if(start<0) return 0;
            end=content.indexOf("</div>",start);
            if(start<0) return 0;
            content=content.substring(start,end);

            count=0;
            start=content.indexOf("id=\"Td:");
            while(start>0){
                end=content.indexOf("id=\"Td:",start+10);
                if(end>0){
                    arrArticle[count] = readProduct(content.substring(start, end));
                    count++;
                    start = end;
                }
                else{
                    arrArticle[count] = readProduct(content.substring(start));
                    count++;
                    start = end;
                }
                if(arrArticle[count-1]==null){
                    return 0;
                }
            }
            return count;
        }
        catch(Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    private Article readProduct(String content){
        BasicArticle article;
        String key;
        int start, end;

        article=new BasicArticle();
        start=content.indexOf("<a ")+3;
        start=content.indexOf("<a ",start);
        start=content.indexOf("product/",start);
        if(start<0){
            //it is not a product
            return article;
        }
        else
            start=start+8;
        end=content.indexOf("/",start);
        key=content.substring(start,end).trim();
        if(key.length()>10){
            return null;
        }

        article.setKey(content.substring(start,end));

        start=content.indexOf(">",end)+1;
        end=content.indexOf("<",start);
        article.setTitle(content.substring(start,end));

        return article;
    }
}
