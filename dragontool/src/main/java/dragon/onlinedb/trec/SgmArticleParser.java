package dragon.onlinedb.trec;

import dragon.nlp.Token;
import dragon.onlinedb.Article;
import dragon.onlinedb.ArticleParser;
import dragon.onlinedb.BasicArticle;
import dragon.util.SortedArray;

import java.util.Date;
/**
 * <p>sgm-styled news article parser </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SgmArticleParser implements ArticleParser{
    protected SortedArray tagList;

    public String assemble(Article article){
        return null;
    }

    public Article parse(String content){
        BasicArticle article;

        article=null;
        try{
            tagList=collectTagInformation(content);
            if(tagList==null || tagList.size()==0)
                return null;

            article=new BasicArticle();

            //get document no
            article.setKey(extractDocNo(content));

            //get title
            article.setTitle(extractTitle(content));

            //get abstract
            article.setAbstract(extractAbstract(content));

            //get meta
            article.setMeta(extractMeta(content));

            //get length information
            article.setLength(extractLength(content));

            //get date information
            article.setDate(extractDate(content));

            //get body
            article.setBody(extractBody(content));

            return article;
        }
        catch(Exception e){
            e.printStackTrace();
            if(article.getKey()!=null)
               return article;
            else
                return null;
        }
    }

    protected int extractLength(String rawText){
        //the extended classes can override this method
        return 0;
    }

    protected Date extractDate(String rawText){
        //the extended classes can override this method
        return null;
    }

    protected String extractDocNo(String rawText){
        Token tag;
        tag=getDocNoTag();
        if(tag==null)
            return null;
        else
            return getTagContent(rawText,tag,false).trim();
    }

    protected Token getDocNoTag(){
        int pos;

        pos=tagList.binarySearch(new Token("DOCNO"));
        if(pos<0)
            return null;
        else
            return (Token)tagList.get(pos);
    }


    protected String extractTitle(String rawText){
        StringBuffer out;
        Token tag;
        int start;

        tag=getTitleTag();
        if(tag==null)
            return null;

        out=new StringBuffer();
        getTagContent(rawText,tag.getName(),tag.getIndex(),out);
        if(tag.getName().equals("HL") && (start=out.indexOf("----"))>=0) // the case of WSJ
            out.delete(start,out.length());
        if(out.length()>=5){
            if(".!;?".indexOf(out.charAt(out.length()-1))<0)
                out.append('.');
            return out.toString();
        }
        else
            return null;
    }

    protected Token getTitleTag(){
        int pos;

        pos=tagList.binarySearch(new Token("HEAD"));
        if(pos<0)
            pos=tagList.binarySearch(new Token("HEADLINE"));
        if(pos<0)
            pos=tagList.binarySearch(new Token("HL"));
        if(pos<0)
            pos=tagList.binarySearch(new Token("TITLE"));
        if(pos<0)
            pos=tagList.binarySearch(new Token("TI"));
        if(pos<0)
            return null;
        else
            return (Token)tagList.get(pos);
    }

    protected String extractAbstract(String rawText){
        StringBuffer out;
        Token tag;
        tag=getAbstractTag();
        if(tag==null)
            return null;

        out=new StringBuffer();
        getTagContent(rawText,tag.getName(),tag.getIndex(),out);
        if(out.length()>=5){
            if(".!;?".indexOf(out.charAt(out.length()-1))<0)
                out.append('.');
            return out.toString();
        }
        else
            return null;
    }

    protected Token getAbstractTag(){
        int pos;

        pos=tagList.binarySearch(new Token("LP"));
        if(pos<0)
            pos=tagList.binarySearch(new Token("LEADPARA"));
        if(pos<0)
            return null;
        else
            return (Token)tagList.get(pos);
    }

    protected String extractMeta(String rawText){
        StringBuffer out;
        Token tag;
        tag=getMetaTag();
        if(tag==null)
            return null;

        out=new StringBuffer();
        getTagContent(rawText,tag.getName(),tag.getIndex(),out);
        if(out.length()>=1)
            return out.toString();
        else
            return null;
    }

    protected Token getMetaTag(){
        int pos;

        pos=tagList.binarySearch(new Token("DESCRIPT"));
        if(pos<0)
            pos=tagList.binarySearch(new Token("IN")); // the case of wall street journal
        if(pos<0)
            return null;
        else
            return (Token)tagList.get(pos);
    }

    protected String extractBody(String rawText){
        StringBuffer out;
        Token tag;
        int start, end;

        tag=getBodyTag();
        if(tag==null)
            return null;

        out=new StringBuffer();
        start=tag.getIndex();
        end=getTagContent(rawText,tag.getName(),start,out);
        while(end>start){
            start=end;
            end=getTagContent(rawText,tag.getName(),start,out);
        }
        if(out.length()>40)
            return out.toString();
        else
            return null;
    }

    protected Token getBodyTag(){
        int pos;

        pos=tagList.binarySearch(new Token("TEXT"));
        if(pos<0)
            return null;
        else
            return (Token)tagList.get(pos);
    }

    protected int  getTagContent(String content, String tag, int start, StringBuffer out){
        int end;

        start=content.indexOf("<"+tag+">",start);
        if(start<0)
            return start;
        start=start+2+tag.length();
        end=content.indexOf("</"+tag+">",start);
        if(end<0)
            return start;
        if(out.length()>0)
            out.append(' ');
        out.append(removeTag(content.substring(start,end)));
        return end+3+tag.length();
    }

    protected String getTagContent(String rawText, String tagName, boolean preprocess){
        Token tag;
        String tagContent;
        int pos, start, end;

        if((pos=tagList.binarySearch(new Token(tagName)))<0)
            return null;
        tag=(Token)tagList.get(pos);
        start=tag.getIndex()+2+tag.getName().length();
        end=rawText.indexOf("</"+tag+">",start);
        if(end<0)
            return null;
        tagContent=rawText.substring(start,end);
        if(preprocess)
            tagContent=removeTag(tagContent);
        return tagContent;
    }

    protected String getTagContent(String rawText, Token tag, boolean preprocess){
        String tagContent;
        int start, end;

        if(tag==null)
            return null;
        start=tag.getIndex()+2+tag.getName().length();
        end=rawText.indexOf("</"+tag+">",start);
        if(end<0)
            return null;
        tagContent=rawText.substring(start,end);
        if(preprocess)
            tagContent=removeTag(tagContent);
        return tagContent;
    }

    protected String removeTag(String content){
        StringBuffer sb;
        int start, lastPos;

        sb=new StringBuffer();
        start=0;
        lastPos=0;
        while(start>=0){
            //find next tag
            start=content.indexOf('<',start);
            if(start>=0){
                if(start>lastPos){
                    sb.append(processTagContent(content.substring(lastPos, start)));
                    sb.append(' ');
                }
                start=content.indexOf(">",start);
                if(start>=0)
                    lastPos=start+1;
            }
        }
        if(lastPos<content.length())
            sb.append(processTagContent(content.substring(lastPos).trim()));
        return sb.toString();
    }

    private String processTagContent(String content){
        if(content.length()<=10)
            return "";

        if(content.length()>=400){
            if(!containSentence(content))
                content=content.replaceAll("\n",". ");
        }
        content=replacement(content);
        if(content.length()>40 && ".!;?".indexOf(content.charAt(content.length()-1))<0)
            content=content+".";
        return content;
    }

    private String replacement(String content){
        content=content.replaceAll("&amp;","&");
        content=content.replaceAll("''","\"");
        content=content.replaceAll("``","\"");
        content=content.replace('\r', ' ');
        return content=content.replace('\n', ' ').trim();
    }

    private boolean containSentence(String content){
        int start;

        if(content==null) return false;

        start=content.indexOf(". ");
        if(start>=0 && start<=400)
            return true;

        start=content.indexOf(".\r");
        if(start>=0 && start<=400)
            return true;

        start=content.indexOf(".\n");
        if(start>=0 && start<=400)
            return true;
        else
            return false;
    }

    protected SortedArray collectTagInformation(String content){
        SortedArray tagList;
        Token curToken;
        int start, end;

        try{
            tagList = new SortedArray(30);
            start=content.indexOf('<');
            while(start>=0){
                if(content.charAt(start+1)!='/'){
                    end=content.indexOf('>',start);
                    curToken=new Token(content.substring(start+1,end),start,1);
                    if(!tagList.add(curToken)){
                        curToken=(Token)tagList.get(tagList.insertedPos());
                        curToken.addFrequency(1);
                    }
                    start=end+1;
                }
                else
                    start=start+1;
                start=content.indexOf('<',start);
            }
            return tagList;
        }
        catch(Exception e){
            System.out.println("Invalid SGM format!");
            return null;
        }
    }
}
