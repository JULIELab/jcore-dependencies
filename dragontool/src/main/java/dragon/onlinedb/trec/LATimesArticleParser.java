package dragon.onlinedb.trec;

import dragon.util.Conversion;

import java.util.Date;

/**
 * <p>LA Times article parser</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class LATimesArticleParser extends SgmArticleParser{
    protected Date extractDate(String rawText){
        String content;
        int start;

        try{
            content=processParagraph(getTagContent(rawText,"DATE",false));
            start=content.indexOf(",")+1;
            start=content.indexOf(",",start);
            if(start<0)
                return null;
            content=content.substring(0,start);
            return Conversion.engDate(content);
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    protected String extractMeta(String rawText){
        String content, last;
        int start;

        try{
            content=processParagraph(getTagContent(rawText,"SECTION",false));
            start=content.lastIndexOf(";");
            last=content.substring(start+1).trim();
            if(!last.endsWith("Desk"))
                return null;
            return last.substring(0,last.length()-4).trim();
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    protected int extractLength(String rawText){
        String content;
        int start;

        try{
            content=processParagraph(getTagContent(rawText,"LENGTH",false));
            if(content==null)
                return -1;
            start=content.indexOf(" ");
            content=content.substring(0,start);
            return Integer.parseInt(content);
        }
        catch(Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    private String processParagraph(String paragraph){
        String content;
        int start, end;

        if(paragraph==null)
            return null;
        start=paragraph.indexOf("<P>");
        if(start<0)
            start=0;
        else
            start=start+3;
        end=paragraph.indexOf("</P>",start);
        if(end>0)
            content=paragraph.substring(start,end);
        else
            content=paragraph.substring(start);
        content=content.replace('\r',' ');
        content=content.replace('\n',' ');
        return content.trim();
    }
}