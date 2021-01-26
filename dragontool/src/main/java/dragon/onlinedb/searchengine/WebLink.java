package dragon.onlinedb.searchengine;

/**
 * <p>Data structure to store a web link</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>@author Davis Zhou</p>
 * @version 1.0
 *
 */

public class WebLink implements Comparable{
	private String url;
    private String host;
    private String path;
    private String summary;
    private String title;
    private int port;

    public WebLink(String url){
    	int start, end;
    	
    	this.url=url;
    	start=url.indexOf("//");
        end=url.indexOf("/",start+2);
        host=url.substring(start+2,end);

        //get path
        path=url.substring(end);

        //get port
        start=host.indexOf(":");
        if(start>0){
            port=Integer.parseInt(host.substring(start+1));
            host=host.substring(0,start).trim();
        }
        else
            port=80;
    }
    
    public WebLink(String host, String path){
        this(host,80,path,null);
    }

    public WebLink(String host, String path, String key){
        this(host,80,path,key);
    }

    public WebLink(String host, int port, String path, String key) {
        this.host =host;
        this.path =path;
        this.port =port;
    }

    public String getHost(){
        return host;
    }

    public String getPath(){
        return path;
    }

    public String getTitle(){
    	return title;
    }
    
    public void setTitle(String title){
    	this.title =title;
    }
    
    public String getSummary(){
    	return summary;
    }
    
    public void setSummary(String summary){
    	this.summary=summary;
    }

    public int getPort(){
        return port;
    }

    public int compareTo(Object obj){
        int result;

        result=host.compareToIgnoreCase(((WebLink)obj).getHost());
        if(result!=0)
            return result;
        else
            return path.compareToIgnoreCase(((WebLink)obj).getPath());
    }
    
    public String toString(){
    	return url;
    }

}