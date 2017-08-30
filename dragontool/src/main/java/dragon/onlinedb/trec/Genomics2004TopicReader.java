package dragon.onlinedb.trec;

import dragon.onlinedb.*;
import java.util.ArrayList;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 * <p>Genomics 2005 topic reader</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class Genomics2004TopicReader extends AbstractTopicReader{
    public Genomics2004TopicReader(String topicFile) {
        super(topicFile);
    }

    protected ArrayList loadTopics(String topicFile){
        DocumentBuilderFactory factory;
        DocumentBuilder parser;
        org.w3c.dom.Document doc;
        NodeList topics, children;
        Node topic;
        BasicArticle cur;
        ArrayList query;
        int i;

        try {
            factory = DocumentBuilderFactory.newInstance();
            parser = factory.newDocumentBuilder();
            doc = parser.parse(topicFile);
            topics = doc.getElementsByTagName("TOPIC");
            query = new ArrayList(topics.getLength());
            for (i = 0; i < topics.getLength(); i++) {
                cur = new BasicArticle();
                cur.setKey(String.valueOf(i + 1));
                cur.setCategory(i + 1);
                topic = topics.item(i);
                children = topic.getChildNodes();
                cur.setTitle(children.item(3).getFirstChild().getNodeValue() + ".");
                cur.setAbstract(children.item(5).getFirstChild().getNodeValue());
                cur.setBody(children.item(7).getFirstChild().getNodeValue());
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
