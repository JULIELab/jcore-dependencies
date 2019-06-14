package dragon.onlinedb.dm;

import dragon.onlinedb.BasicArticle;
import dragon.util.FileUtil;

import java.io.BufferedReader;
import java.io.File;

/**
 * <p>20 news group article parser </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Drexel University</p>
 * @author Xiaodan Zhang
 * @version 1.0
 */

public class NewsGroupArticle extends BasicArticle
{
    public NewsGroupArticle(File file) {
        parse(file);
    }

    public boolean parse(File file) {
        BufferedReader br;
        StringBuffer sb;
        String line,header;
        int start, end;

        try{
            br = FileUtil.getTextReader(file);
            while ( (line = br.readLine()) != null) {
                if(line.length()==0) continue;
                start=line.indexOf(':');
                if(start<0)
                    break;
                else{
                    header=line.substring(0,start);
                    if(header.equalsIgnoreCase("subject"))
                        this.title=line.substring(start+1);
                    else if(header.equalsIgnoreCase("summary"))
                        this.abt=line.substring(start+1);
                    else if(header.equalsIgnoreCase("keywords"))
                        this.meta =line.substring(start+1);
                }
            }

            sb=new StringBuffer();
            end=0;
            if(line!=null){
                sb.append(line.trim());
                line=br.readLine();
            }
            while(line!=null){
                line=line.trim();
                if(line.length()>0){
                    sb.append(' ');
                    sb.append(line);
                }
                else{
                    end=sb.length();
                }
                line=br.readLine();
            }
            if(sb.length()-end<300) //remove the author's signature
                this.body =sb.substring(0,end);
            else
                this.body=sb.toString();
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
