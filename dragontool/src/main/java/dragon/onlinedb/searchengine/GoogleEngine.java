package dragon.onlinedb.searchengine;

import dragon.util.HttpUtil;
import org.apache.commons.httpclient.util.URIUtil;

/**
 * <p>Google Search Engine</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class GoogleEngine extends AbstractSearchEngine{
    private HttpUtil http;
    
    public GoogleEngine(){
        super(10);
        http=new HttpUtil("www.google.com","UTF-8");
        http.setConnectionTimeout(10000);
		http.setSocketTimeout(10000);
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
        String url;
        String content;
        String query;
        int count;

        try{
	        if (pageNo >= pageNum || pageNum==0)
	            return false;
	        if(pageNo==curPageNo) return true;
	
	        query=term;
	        query=query.replaceAll("  ", " ");
	        query=query.replaceAll("  ", " ");
	        query=query.replace(' ', '+');
	        query=URIUtil.encodeAll(query,"UTF-8");
	        query=query.replaceAll("%2B","+");
	        
	        count=0;
	        while(pageNo<pageNum && count==0){
		        if(pageNo==0)
		        	url="/search?hl=en&newwindow=1&rlz=1T4GZHY_enUS237US237&q="+query+"&btnG=Search";
		        else
		        	url="/search?hl=en&newwindow=1&rlz=1T4GZHY_enUS237US237&q="+query+"&start="+(pageNo*pageWidth);
		        if(site!=null && site.length()>0)
		        	url=url+"&sitesearch="+site;
		        content=http.get(url,"UTF-8");
		        if (content == null)
		            return false;
		        count=processPage(pageNo,content);
		        if(count<0)
		        	return false;
		        else if(count>0)
		        	return true;
		        else
		        	pageNo++;
	        }
	        return true;
        }
        catch(Exception e){
        	return false;
        }
    }

    private int processPage(int pageNo, String content){
        String url, word, summary, title;
        int pos, startPos, endPos, end, count;
        boolean hasDoc;

        try{
        	count=0;
        	if(getSummaryOnlyOption())
        		word=">Similar pages<";
        	else
        		word=">Cached<";
            startPos=content.indexOf(word);
            if(startPos>=0)
            	hasDoc=true;
            else
            	hasDoc=false;
            
            while(startPos>0){
                pos=startPos;
                
                //get summary
                endPos=content.lastIndexOf("<span ",startPos-5);
                startPos=content.lastIndexOf("<table ",endPos);
                summary=content.substring(startPos,endPos);
                //remove the file format part if any
                end=summary.indexOf("<span ");
                if(end>=0){
                	end=summary.indexOf("<br>",end+5);
                	summary=summary.substring(end+4);
                }
                //remove the html tag of the summary
                summary=parser.extractText(summary);
                if(summary==null || summary.length()==0){
                	//skip this entry
                	startPos=content.indexOf(word,pos+10);
                	continue;
                }
                
                //get the url
                end=startPos;
                startPos=content.lastIndexOf("href=",startPos-5);
                //skip the translation url
                if(content.substring(startPos,end).indexOf("translate.google")>=0){
                	startPos=content.lastIndexOf("href=",startPos-5);
                }
                endPos=content.indexOf("\"",startPos+6);
                url=content.substring(startPos+6,endPos);
               
                
                //get title
                startPos=content.indexOf(">",endPos);
                endPos=content.indexOf("</a>",startPos+1);
                title=parser.extractText(content.substring(startPos+1,endPos));
                
                arrUrl[count]=new WebLink(url);
                arrUrl[count].setSummary(processSnippet(summary));
                arrUrl[count].setTitle(processSnippet(title));
                
                //get next entry
                count++;
                startPos=content.indexOf(word,pos+10);
            }
                
	        curPageNo=pageNo;
	        curPageWidth = count;
	        curArticleNo = 0;
	        if(curPageWidth==0 && !hasDoc)
	            return -1;
	        if(count>0)
	        	curArticle=getArticle(0);
	
	        //adjust page number
	        end=content.indexOf(">Next<");
	        if(end>=0)
	            pageNum=pageNo+2;
	        return count;
        }
        catch(Exception e){
        	e.printStackTrace();
        	return -1;
        }
    }
    
    private String processSnippet(String content){
    	if(content==null || content.length()==0)
    		return content;
    	
    	content=content.replaceAll("&#39;", "'");
    	content=content.replaceAll("&quot;","\"");
    	content=content.replaceAll("&lt;","<");
    	content=content.replaceAll("&gt;",">");
    	return content;
    }
}
