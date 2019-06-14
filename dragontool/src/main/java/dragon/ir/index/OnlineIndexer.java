package dragon.ir.index;

import dragon.matrix.IntSparseMatrix;
import dragon.nlp.SimpleElementList;
import dragon.nlp.SimplePairList;
import dragon.nlp.extract.ConceptExtractor;
import dragon.nlp.extract.TripleExtractor;
import dragon.onlinedb.Article;

import java.util.ArrayList;

/**
 * <p>The class is designed for indexing in computer memory especially for small document set when writing to disk is not necessary. </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class OnlineIndexer extends AbstractIndexer{
    private TripleExtractor te;
    private ConceptExtractor ce;
    private OnlineIndexWriteController writer;
    private int minArticleSize;
    private boolean useTitle, useAbstract, useBody, useMeta;

    public OnlineIndexer(TripleExtractor te, boolean useConcept) {
        super(true);
        this.te = te;
        minArticleSize=3;
        useTitle=true;
        useAbstract=true;
        useBody=true;
        useMeta=true;
        addSection(new IRSection(IRSection.SEC_ALL));
        writer = new OnlineIndexWriteController(relationSupported, useConcept);
    }

    public OnlineIndexer(ConceptExtractor ce, boolean useConcept) {
        super(false);
        this.ce = ce;
        minArticleSize=3;
        useTitle=true;
        useAbstract=true;
        useBody=true;
        useMeta=true;
        addSection(new IRSection(IRSection.SEC_ALL));
        writer = new OnlineIndexWriteController(relationSupported, useConcept);
    }

    public void close() {
        initialized=false;
        writer.close();
    }

    public boolean isRelationSupported(){
        return relationSupported;
    }

    public boolean indexed(String docKey) {
        return writer.indexed(docKey);
    }

    public void setMinArticleSize(int minSize){
        this.minArticleSize=minSize;
    }

    public boolean screenArticleContent(boolean useTitle, boolean useAbstract, boolean useBody, boolean useMeta){
        if(initialized)
            return false;
        this.useTitle =useTitle;
        this.useAbstract =useAbstract;
        this.useBody =useBody;
        this.useMeta =useMeta;
        return true;
    }

    public IRTermIndexList getTermIndexList(){
        return writer.getTermIndexList();
    }

    public IRRelationIndexList getRelationIndexList(){
        return writer.getRelationIndexList();
    }

    public IRDocIndexList getDocIndexList(){
        return writer.getDocIndexList();
    }

    public SimpleElementList getDocKeyList(){
        return writer.getDocKeyList();
    }

    public SimpleElementList getTermKeyList(){
        return writer.getTermKeyList();
    }

    public SimplePairList getRelationKeyList(){
        return writer.getRelationKeyList();
    }

    public IntSparseMatrix getDocTermMatrix(){
        return writer.getDocTermMatrix();
    }

    public IntSparseMatrix getDocRelationMatrix(){
        return writer.getDocRelationMatrix();
    }

    public IRCollection getIRCollection(){
        return writer.getIRCollection();
    }

    protected void initDocIndexing(){
        if(te!=null)
            te.initDocExtraction();
        if(ce!=null)
            ce.initDocExtraction();
    }

    protected boolean extract(String content, ArrayList conceptList, ArrayList relationList) {
        boolean ret;

        try{
            if (content == null || content.length() <minArticleSize) {
                return true;
            }
            ret = te.extractFromDoc(content);
            if (ret) {
                if (te.getConceptList() != null) {
                    conceptList.addAll(te.getConceptList());
                }
                if (te.getTripleList() != null) {
                    relationList.addAll(te.getTripleList());
                }
            }
            return ret;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    protected boolean extract(String content, ArrayList conceptList) {
        try{
            if (content == null || content.length() <minArticleSize) {
                return true;
            }
            if (ce.extractFromDoc(content) != null) {
                conceptList.addAll(ce.getConceptList());
                return true;
            }
            else
                return false;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    protected String getRemainingSections(Article paper) {
        StringBuffer sb;
        String section;
        sb = new StringBuffer();
        if (useTitle && (section =paper.getTitle()) != null && section.length()>=minArticleSize) {
            if (sb.length() > 0)
                sb.append("\n\n");
            sb.append(section);
        }
        if (useAbstract && (section =paper.getAbstract()) != null && section.length()>=minArticleSize) {
            if (sb.length() > 0)
                sb.append("\n\n");
            sb.append(section);
        }
        if (useBody && (section =paper.getBody()) != null && section.length()>=minArticleSize) {
            if (sb.length() > 0)
                sb.append("\n\n");
            sb.append(section);
        }
        if (useMeta && (section =paper.getMeta()) != null && section.length()>=minArticleSize) {
            if (sb.length() > 0)
                sb.append("\n\n");
            sb.append(section);
        }
        return sb.toString();
    }

    protected void write(int sectionID, ArrayList conceptList){
        writer.write(conceptList);
    }

    protected void write(int sectionID, ArrayList conceptList, ArrayList relationList){
        writer.write(conceptList, relationList);
    }

    protected void initSectionWrite(IRSection section){
        //do nothing;
    }

    protected void initIndex(){
        writer.initialize();
    }

    protected boolean setDoc(String docKey){
        return writer.setDoc(docKey);
    }
}