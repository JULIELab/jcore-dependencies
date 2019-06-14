package dragon.onlinedb.trec;

import dragon.onlinedb.BasicArticle;
import dragon.util.FileUtil;

import java.io.BufferedReader;
import java.util.ArrayList;

/**
 * <p>Genomics 2005 topic reader</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class Genomics2005TopicReader extends AbstractTopicReader{
    public Genomics2005TopicReader(String topicFile) {
        super(topicFile);
    }

    protected ArrayList loadTopics(String topicFile){
        BasicArticle  cur;
        ArrayList query;
        BufferedReader br;
        String line, arrField[];
        int i, total;

        try {
            br=FileUtil.getTextReader(topicFile);
            total=Integer.parseInt(br.readLine());
            query=new ArrayList(total);
            for(i=0;i<total;i++){
                line=br.readLine();
                arrField=line.split("\t");
                cur=new BasicArticle();
                cur.setKey(arrField[0]);
                cur.setCategory(Integer.parseInt(arrField[0]));
                cur.setTitle(arrField[1]);
                query.add(cur);
            }
            return query;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}