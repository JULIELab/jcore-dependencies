package dragon.util;

import java.io.*;
import java.net.URL;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.cookie.CookiePolicy;

/**
 * <p>HTTP related utilities</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class HttpUtil {
    private HttpClient http;
    private String defaultCharSet;
    private String lastCharSet;
    private byte[] buf;
    private boolean autoRefresh;

    public static void main(String[] args){
        HttpUtil web;
        String content;

        web = new HttpUtil("www.google.com");
        web.setAutoRefresh(true);
        content=web.get("/search?q=killed+abraham+lincoln&hl=en&newwindow=1&rlz=1T4GZHY_enUS237US237&start=40&sa=N");
        FileUtil.saveTextFile("test.txt", content,"UTF-16LE");
        FileUtil.saveTextFile("test_notag.txt",(new HttpContent()).extractText(content),"UTF-16LE");
    }

    public HttpUtil(String host){
        this(host,80, null);
    }

    public HttpUtil(String host, String charSet){
        this(host,80, charSet);
    }

    public HttpUtil(String host, int port){
        this(host,port,null);
    }

    public HttpUtil(String host, int port, String charSet) {
        HostConfiguration hostConfig;

        buf=new byte[1024*1024];
        this.lastCharSet =null;
        this.autoRefresh =false;
        this.defaultCharSet =charSet;
        http=new HttpClient();
        hostConfig=new HostConfiguration();
        hostConfig.setHost(host,port);
        http.setHostConfiguration(hostConfig);
        setSocketTimeout(10000);
        setConnectionTimeout(10000);
    }

    public void setHost(String host){
        setHost(host,80, null);
    }

    public void setHost(String host, String charSet){
        setHost(host,80,charSet);
    }

    public void setHost(String host, int port){
        setHost(host,port,defaultCharSet);
    }

    public void setHost(String host, int port, String charSet){
        HostConfiguration hostConfig;

        this.defaultCharSet=charSet;
        hostConfig=http.getHostConfiguration();
        hostConfig.setHost(host,port);
        http.setHostConfiguration(hostConfig);
    }

    public void setAutoRefresh(boolean enable){
        this.autoRefresh =enable;
    }

    public boolean getAutoRefresh(){
        return autoRefresh;
    }

    public String getHost(){
        return http.getHostConfiguration().getHost();
    }

    public int getPort(){
        return http.getHostConfiguration().getPort();
    }

    public void setConnectionTimeout(int time){
        http.getHttpConnectionManager().getParams().setConnectionTimeout(time);
    }

    public int getConnectionTimeout(){
        return http.getHttpConnectionManager().getParams().getConnectionTimeout();
    }

    public void setSocketTimeout(int time){
        http.getParams().setParameter("http.socket.timeout", new Integer(time));
    }

    public int getSocketTimeout(){
        return ((Integer)(http.getParams().getParameter("http.socket.timeout"))).intValue();
    }

    /**
     * Gets the charset name of the last web page one access. One should call this function after calling get method.
     * @return charset name of the last web page
     */
    public String getCharSet(){
        return lastCharSet;
    }

    public String get(String url){
        return get(url,null);
    }

    public String get(String url, String charSet){
        String content;
        URL newUrl;

        content=internalGet(url,charSet);
        if(content==null || !autoRefresh)
            return content;
        newUrl=getDirectedURL(content);
        if(newUrl==null)
            return content;

        if(newUrl.getHost()!=""){
            if(newUrl.getPort()>0)
                setHost(newUrl.getHost(), newUrl.getPort());
            else
                setHost(newUrl.getHost());
        }
        url=newUrl.getFile();
        while(url.charAt(0)=='.')
            url=url.substring(1);
        if(url.charAt(0)!='/')
            url="/"+url;
        return internalGet(url,charSet);
    }

    private URL getDirectedURL(String message){
        URL url;
        int start, end;

        try{
            if(message==null || message.length()>=512)
                return null;
            message=message.toLowerCase();
            start=message.indexOf("http-equiv=\"refresh\"");
            if(start<0)
                start=message.indexOf("http-equiv='refresh'");
            if(start<0)
                return null;
            end=message.indexOf(">",start);
            if(end<0)
                return null;
            message=message.substring(start,end);
            start=message.indexOf("url=");
            if(start<0)
                return null;
            start=start+4;
            end=message.indexOf("\"",start);
            if(end<0)
                end=message.indexOf("\'",start);
            message=message.substring(start,end);
            if(!message.startsWith("http"))
                message="http:"+message;
            url=new URL(message);
            return url;
        }catch(Exception e){
            return null;
        }
    }

    private String internalGet(String url, String charSet){
        GetMethod method;
        String content, curCharSet;
        int len;

        method=null;
        try{
            method = new GetMethod(url);
            method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            http.executeMethod(method);
            if(method.getStatusCode()!=200){
                method.releaseConnection();
                return null;
            }

            len=read(method.getResponseBodyAsStream(),buf);
           	curCharSet=recognizeStreamEncode(buf);
            if(curCharSet==null)
            	curCharSet=charSet;
            if(curCharSet==null)
                curCharSet = method.getResponseCharSet();
            if(curCharSet!=null && curCharSet.equalsIgnoreCase("ISO-8859-1") )
                curCharSet=null;

            if(curCharSet!=null)
                content = new String(buf, 0, len, charsetNameConvert(curCharSet));
            else{
                if(defaultCharSet!=null){
                    curCharSet=defaultCharSet;
                    content = new String(buf, 0, len, charsetNameConvert(curCharSet));
                }
                else
                    content = new String(buf, 0, len);
                charSet=readCharSet(content);
                if(charSet!=null && !compatibleCharSet(curCharSet, charSet)){
                    curCharSet=charSet;
                    content = new String(buf, 0, len, charsetNameConvert(curCharSet));
                }
            }
            lastCharSet=curCharSet;
            method.releaseConnection();
            return content;
        }
        catch(Exception e){
            if(method!=null)
                method.releaseConnection();
            return null;
        }
    }

    private int read(InputStream in, byte[] buf){
        int len, offset;

        try{
            offset=0;
            len=in.read(buf);
            while(len>=0){
                offset=offset+len;
                if(offset==buf.length)
                    break;
                len=in.read(buf,offset,buf.length-offset);
            }
            if(offset==buf.length){
                System.out.println("Warning: The web page is too big and truncated!");
            }
            return offset;
        }
        catch(Exception e){
            return 0;
        }
    }

    private boolean compatibleCharSet(String charsetA, String charsetB){
        if(charsetA==null || charsetB==null)
            return false;
        if(charsetA.equalsIgnoreCase(charsetB))
            return true;
        else if(charsetA.equalsIgnoreCase("gbk") && charsetB.equalsIgnoreCase("gb2312"))
            return true;
        else
            return false;
    }

    private String charsetNameConvert(String charsetName){
        if(charsetName.equalsIgnoreCase("gb2312") )
            return "gbk";
        else
            return charsetName;
    }

    private String recognizeStreamEncode(byte[] buf){
        String hex;
        try{
            hex=ByteArrayConvert.toHexString(buf,0,3).toUpperCase();
            if(hex.startsWith("FFFE") || hex.startsWith("3C00"))
                return "UTF-16LE";
            else if(hex.startsWith("FEFF") || hex.startsWith("003C") )
                return "UTF-16BE";
            else if(hex.startsWith("EFBBBF"))
                return "UTF-8";
            else
                return null;
        }
        catch(Exception e){
            return null;
        }
    }

    private String readCharSet(String message){
        int start, end;

        try{
            end=Math.min(message.length(),2048);
            message=message.substring(0,end).toLowerCase();
            start=message.indexOf("http-equiv=");
            if(start<0)
                return null;
            end=message.indexOf(">",start);
            message=message.substring(start,end).trim();
            start=message.indexOf("charset=");
            if(start<0)
                return null;
            start=start+8;
            end=message.indexOf("\"",start);
            if(end<0)
                end=message.indexOf("'",start);
            if(end<0)
                return null;
            else
                return message.substring(start,end).trim();
        }
        catch(Exception e){
            return null;
        }
    }
}