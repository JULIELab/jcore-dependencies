package dragon.onlinedb.searchengine;

import dragon.onlinedb.AbstractQuery;
import dragon.onlinedb.Article;
import dragon.onlinedb.BasicArticle;
import dragon.util.HttpContent;
import dragon.util.HttpUtil;

/**
 * <p>Abstract Search Engine Query</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractSearchEngine extends AbstractQuery{
	private HttpUtil webServer;
    protected WebLink[] arrUrl;
    protected String term;
    protected HttpContent parser;
    protected String site, defaultEncoding;
    private boolean summaryOnly;
    private boolean removeTag;
    
	public AbstractSearchEngine(int pageWidth) {
		super(pageWidth);
		arrUrl=new WebLink[this.pageWidth];
		removeTag=true;
		parser=new HttpContent();
		webServer=new HttpUtil("www.google.com");
		webServer.setConnectionTimeout(10000);
		webServer.setSocketTimeout(10000);
		site=null;
		defaultEncoding=null;
		summaryOnly=false;
	}
	
	public void setSiteRestriction(String site){
    	if(site!=null)
    		site=site.trim();
    	this.site=site;
    }
    
    public String getSiteRestriction(){
    	return site;
    }
    
    public void setDefaultEncoding(String encoding){
    	this.defaultEncoding=encoding;
    }
    
    public String getDefaultEncoding(){
    	return defaultEncoding;
    }
    
    public void setAutoRefresh(boolean enable){
        webServer.setAutoRefresh(enable);
    }

    public boolean getAutoRefresh(){
        return webServer.getAutoRefresh();
    }

	public void setSummaryOnlyOption(boolean option){
		this.summaryOnly=option;
	}
	
	public boolean getSummaryOnlyOption(){
		return this.summaryOnly;
	}
	
	public void setRemoveTagOption(boolean option){
		this.removeTag=option;
	}
	
	public boolean getRemoveTagOption(){
		return removeTag;
	}
	
    public void setSearchTerm(String term){
        this.term=term;
    }
	
    public boolean supportArticleKeyRetrieval(){
        return true;
    }
	
	public Article getArticleByKey(String id){
		WebLink link;
		
		link=new WebLink(id);
		return getArticle(link,true);
    }

    protected Article getArticle(int articleNo){
        return getArticle(arrUrl[articleNo],false);
    }
    
    protected Article getArticle(WebLink link, boolean useKey){
    	Article article;
        String content;
        int start, end;
        
    	article=new BasicArticle();
    	article.setKey(link.toString());
    	article.setTitle(link.getTitle());
    	if(summaryOnly && !useKey){
    		article.setBody(link.getSummary());
    		return article;
    	}
    	
        try{
        	if(!webServer.getHost().equalsIgnoreCase(link.getHost())|| webServer.getPort()!=link.getPort())
                webServer.setHost(link.getHost(),link.getPort(),defaultEncoding);
            content = webServer.get(link.getPath());
            if (content == null)
                return article;
            start = content.indexOf("<html");
            if (start < 0)
                start = content.indexOf("<HTML");
            if(start<0)
                return article;
            if(removeTag){
            	//get title
            	start=content.indexOf("<title>",start);
            	if(start<0)
            		start=content.indexOf("<TITLE>");
            	if(start>=0){
            		start=start+7;
            		end=content.indexOf("<",start);
            		article.setTitle(content.substring(start,end));
            	}
            	else
            		start=0;
            	
            	//get body
            	start=content.indexOf("<body ",start);
            	if(start<0)
            		start=content.indexOf("<BODY ");
            	if(start>0)
            		content=content.substring(start);
            	article.setBody(parser.extractText(content));
            }
            else
            	article.setBody(content);
            return article;
        }
        catch(Exception e){
            return article;
        }
    }
    
    public static void sleepOneSecond() {
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sleepManySeconds(long s) {
        try {
            Thread.sleep(s * 1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
