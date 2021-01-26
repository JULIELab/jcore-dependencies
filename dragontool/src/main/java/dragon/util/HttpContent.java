package dragon.util;

/**
 * <p>Extract Text from Web Pages </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class HttpContent {
    private static String formatTags="<b> <font> <u> <i> <strong> <small> <big>";

    public HttpContent() {
    }
    
    public String extractText(String rawContent){
        StringBuffer sb;
        String content,str, tag, tagLine;
        int start, end, lastPos;

        try{
        	content=rawContent.toLowerCase();
            sb = new StringBuffer();
            start =0;
            lastPos = 0;
            
            while (start >= 0) {
                //find the next opening tag
                start = content.indexOf('<', start);

                if (start >= 0) {
                    if (start > lastPos) {
                        sb.append(rawContent.substring(lastPos, start));
                    }
                    end = content.indexOf(">", start);
                    if(end<0) break;

                    tagLine=content.substring(start + 1, end).trim();
                    tag = getTagName(tagLine);
                    if(tag.equals("style") || tag.equals("script")){
                        //remove style or script
                        if(!tagLine.endsWith("/") && !tagLine.startsWith("/")){
                            end=content.indexOf("</"+tag,end+1);
                            end=content.indexOf(">",end+1);
                        }
                    }

                    if (sb.length()>0 && sb.charAt(sb.length()-1)!=' ' && needSpace(tag))
                        sb.append(' ');

                    lastPos =end + 1;
                    if(end<start) //error occurs
                    {
                        System.out.println("Error occur!!!");
                        break;
                    }
                    else
                        start=end;
                }
            }
            if (lastPos < content.length())
                sb.append(content.substring(lastPos).trim());
            str = sb.toString();
            str=str.replaceAll("(&nbsp;)+", " ");
            return str.replaceAll(" +", " ").trim();
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private boolean needSpace(String tag){
        if(formatTags.indexOf("<"+tag.toLowerCase()+">")>=0)
            return false;
        else
            return true;
    }

    private String getTagName(String fragment){
        if(fragment.charAt(0)=='/')
            return fragment.substring(1).trim();
        else{
            int start=fragment.indexOf(' ');
            if(start<0)
                return fragment;
            else
                return fragment.substring(0,start);
        }
    }
}