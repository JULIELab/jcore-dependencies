package dragon.onlinedb.amazon;

import dragon.onlinedb.AbstractQuery;
import dragon.onlinedb.Article;
import dragon.onlinedb.BasicArticle;
import dragon.util.Conversion;
import dragon.util.HttpUtil;


/**
 * <p>Amazon reveiw query </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class AmazonReviewQuery extends AbstractQuery{
    protected String webEnv,queryKey;
    protected String term;
    protected String reviewUrl;
    protected Article[] arrArticle;
    protected HttpUtil http;

    public AmazonReviewQuery(String productCode){
        super(10);
        arrArticle=new Article[pageWidth];
        this.term=productCode;
        reviewUrl="/gp/product/customer-reviews/";
        http=new HttpUtil("www.amazon.com");
    }

    public AmazonReviewQuery(){
        super(10);
        arrArticle=new Article[pageWidth];
        reviewUrl="/gp/product/customer-reviews/";
        http=new HttpUtil("www.amazon.com");
    }

    public static void main(String[] args) {
        AmazonReviewQuery query;
        query=new AmazonReviewQuery("B000AYGDIO");
        query.initQuery();
    }


    public boolean supportArticleKeyRetrieval(){
        return false;
    }

    public void setSearchTerm(String productCode){
        this.term=productCode;
    }

    public  boolean initQuery(){
        String curUrl;
        String content;
        curPageNo=-1;
        curArticle=null;
        curPageWidth=0;

        curUrl = reviewUrl+term;
        content=http.get(curUrl);
        if(content==null) return false;

        //get Search Result
        articleNum=getReviewNum(content);
        if(articleNum==0)
            pageNum=0;
        else
            pageNum=(articleNum-1)/pageWidth+1;
        if(articleNum>0){
            curPageWidth=readReviewsFromWebPage(content);
        }
        return true;
    }

    public boolean moveToPage(int pageNo){
        String curUrl;
        String content;
        if (pageNo >= pageNum || pageNum==0)
            return false;

        if(pageNo==curPageNo) return true;

        if(pageNo==0 && curPageNo==-1){
            curPageNo=pageNo;
            curArticleNo = 0;
            if (curPageWidth <= 0)  return false;
            curArticle = arrArticle[curArticleNo];
            return true;
        }

        curUrl = reviewUrl + term+"?_encoding=UTF8&customer-reviews.sort_by=-SubmissionDate&customer-reviews.start="+(pageNo*pageWidth+1);
        content=http.get(curUrl);
        if (content == null)
            return false;

        curPageWidth = readReviewsFromWebPage(content);
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

    private int getReviewNum(String content){
        int start, end;

        try{
            start=content.indexOf("customer-reviews.sort_by");
            if(start<0) return 0;
            start=content.indexOf("</form>",start);
            if(start<0) return 0;
            end=start+7;
            while(!Character.isDigit(content.charAt(end))) end++;
            if(end-start>=30) return 0;

            start=content.indexOf("of ",end)+3;
            end=content.indexOf("\n",start);
            return Integer.parseInt(content.substring(start,end));
        }
        catch(Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    private int readReviewsFromWebPage(String content){
        int start, end;
        int count;

        try{
            start=content.indexOf("customer-reviews.sort_by");
            if(start<0) return 0;
            start=content.indexOf("</form>",start);
            if(start<0) return 0;

            count=0;
            start=content.indexOf("<!-- BOUNDARY -->",start);
            while(start>0){
                end=content.indexOf("<!-- BOUNDARY -->",start+20);
                if(end>0){
                    arrArticle[count] = readReview(content.substring(start, end));
                    count++;
                    start = end;
                }
                else{
                    //last review in the current page
                    end=content.indexOf("<hr ",start);
                    arrArticle[count] = readReview(content.substring(start, end));
                    count++;
                    start=-1;
                }
            }
            return count;
        }
        catch(Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    private Article readReview(String content){
        BasicArticle article;
        int start, end;

        article=new BasicArticle();
        start=content.indexOf("name=\"")+7;
        end=content.indexOf("\"",start);
        article.setKey(content.substring(start,end));

        end=content.indexOf("width",end);
        start=content.lastIndexOf("/",end);
        article.setCategory(Integer.parseInt(content.substring(start+7,start+8)));

        start=content.indexOf("<b>",end)+3;
        end=content.indexOf("</b>",start);
        article.setTitle(content.substring(start,end));

        start=end+5;
        end=content.indexOf("<br />",start);
        article.setDate(Conversion.engDate(content.substring(start,end).trim()));

        start=content.indexOf("</b>",end);
        if(start>0)
            start+=4; //A special case: Kid's Review
        else
            start=content.indexOf("</table>",end)+8;
        end=content.indexOf("<nobr>",start);
        end=content.lastIndexOf("<br /><br />",end);
        article.setAbstract(processReviewContent(content.substring(start,end)));

        return article;
    }

    private String processReviewContent(String raw){
        String[] arrPara;
        StringBuffer sb;
        String line;
        int i;

        raw=raw.replaceAll("\r","");
        raw=raw.replaceAll("\n","");
        raw=raw.replaceAll("<br />","<br>");
        raw=raw.replaceAll("<p>","<br>");
        arrPara=raw.split("<br>");
        sb=new StringBuffer();
        for(i=0;i<arrPara.length;i++){
            if(arrPara[i]==null || (line=arrPara[i].trim()).length()==0) continue;
            if(sb.length()>0) sb.append(" ");
            sb.append(line);
            if(".?!".indexOf(line.charAt(line.length()-1))<0)
                sb.append('.');
        }
        return sb.toString();
    }
}
