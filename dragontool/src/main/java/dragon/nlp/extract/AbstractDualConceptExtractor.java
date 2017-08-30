package dragon.nlp.extract;

import dragon.nlp.*;
import dragon.onlinedb.*;
import java.util.ArrayList;

/**
 * <p>Dual concept extractor for dual indexing </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractDualConceptExtractor implements DualConceptExtractor{
    protected ArrayList firstConceptList, secondConceptList;
    protected DocumentParser parser;

    public AbstractDualConceptExtractor() {
        this.parser=new EngDocumentParser();
    }

    public boolean extractFromDoc(Article article){
        return extractFromDoc(getArticleContent(article));
    }

    public boolean extractFromDoc(String doc){
        return extractFromDoc(parser.parse(doc));
    }

    public ArrayList getFirstConceptList(){
        return firstConceptList;
    }

    public ArrayList getSecondConceptList(){
        return secondConceptList;
    }

    public boolean isExtractionMerged(){
        return false;
    }

    public boolean supportConceptName(){
        return true;
    }

    public boolean supportConceptEntry(){
        return false;
    }

    public void initDocExtraction(){
        firstConceptList=new ArrayList(100);
        secondConceptList=new ArrayList(100);
    }

    protected String getArticleContent(Article article) {
        StringBuffer sb;
        sb = new StringBuffer();

        if (article.getTitle()!=null && article.getTitle().length()>=5) {
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            sb.append(article.getTitle());
        }
        if (article.getMeta()!=null && article.getMeta().length()>=5) {
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            sb.append(article.getMeta());
        }
        if (article.getAbstract()!=null && article.getAbstract().length()>=5) {
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            sb.append(article.getAbstract());
        }
        if (article.getBody()!=null && article.getBody().length()>=5) {
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            sb.append(article.getBody());
        }
        return sb.toString();
    }

    public DocumentParser getDocumentParser(){
        return parser;
    }

    public void setDocumentParser(DocumentParser parser){
        this.parser = parser;
    }
}