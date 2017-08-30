package dragon.ir.index;

import dragon.nlp.extract.*;
import dragon.onlinedb.*;
import java.util.ArrayList;

/**
 * <p>BasicIndexer, providing all basic functions for indexing, can be used to index documents directly </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicIndexer extends AbstractIndexer {
    private BasicIndexWriteController charWriter, cptWriter;
    private TripleExtractor te;
    private ConceptExtractor ce;
    private int minArticleSize;
    private boolean[] remainingSections;

    public BasicIndexer(TripleExtractor te, boolean useConcept, String indexFolder) {
        super(true);
        this.te = te;
        remainingSections=new boolean[4];
        minArticleSize=5;
        if(useConcept)
            cptWriter = new BasicIndexWriteController(indexFolder, relationSupported, true);
        else
            charWriter = new BasicIndexWriteController(indexFolder, relationSupported, false);
    }

    public BasicIndexer(TripleExtractor te, String charIndexFolder, String cptIndexFolder) {
        super(true);
        this.te = te;
        remainingSections=new boolean[4];
        minArticleSize=5;
        if(cptIndexFolder!=null)
            cptWriter = new BasicIndexWriteController(cptIndexFolder, relationSupported, true);
        if(charIndexFolder!=null)
            charWriter = new BasicIndexWriteController(charIndexFolder, relationSupported, false);
    }

    public BasicIndexer(ConceptExtractor ce, boolean useConcept, String indexFolder) {
        super(false);
        this.ce = ce;
        remainingSections=new boolean[4];
        if(useConcept)
            cptWriter = new BasicIndexWriteController(indexFolder, relationSupported, true);
        else
            charWriter = new BasicIndexWriteController(indexFolder, relationSupported, false);
    }

    public BasicIndexer(ConceptExtractor ce, String charIndexFolder, String cptIndexFolder) {
        super(false);
        this.ce = ce;
        remainingSections=new boolean[4];
        if(cptIndexFolder!=null)
            cptWriter = new BasicIndexWriteController(cptIndexFolder, relationSupported, true);
        if(charIndexFolder!=null)
            charWriter = new BasicIndexWriteController(charIndexFolder, relationSupported, false);
    }

    public void close() {
        initialized=false;
        if (charWriter != null) {
            charWriter.close();
            charWriter = null;
        }
        if (cptWriter != null) {
            cptWriter.close();
            cptWriter = null;
        }
    }

    public boolean indexed(String docKey) {
        if (charWriter != null) {
            return charWriter.indexed(docKey);
        }
        else if (cptWriter != null) {
            return cptWriter.indexed(docKey);
        }
        else {
            return true;
        }
    }

    public void setMinArticleSize(int minSize){
        this.minArticleSize=minSize;
    }

    public void setSectionIndexOption(boolean all, boolean title, boolean abt, boolean body, boolean meta) {
        if (all)
            addSection(new IRSection(IRSection.SEC_ALL));

        remainingSections[IRSection.SEC_TITLE-1]=!title;
        if (title)
            addSection(new IRSection(IRSection.SEC_TITLE));

        remainingSections[IRSection.SEC_ABSTRACT-1]=!abt;
        if (abt)
            addSection(new IRSection(IRSection.SEC_ABSTRACT));

        remainingSections[IRSection.SEC_BODY-1]=!body;
        if (body)
            addSection(new IRSection(IRSection.SEC_BODY));

        remainingSections[IRSection.SEC_META-1]=!meta;
        if (meta)
            addSection(new IRSection(IRSection.SEC_META));
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

    protected String getSection(Article paper, int sectionID) {
        switch (sectionID) {
            case IRSection.SEC_TITLE:
                return paper.getTitle();
            case IRSection.SEC_ABSTRACT:
                return paper.getAbstract();
            case IRSection.SEC_BODY:
                return paper.getBody();
            case IRSection.SEC_META:
                return paper.getMeta();
        }
        return null;
    }

    protected String getRemainingSections(Article paper) {
        StringBuffer sb;
        String section;
        int i;

        sb = new StringBuffer();
        for (i =0; i < remainingSections.length; i++) {
            if (remainingSections[i] && (section = getSection(paper, i+1)) != null && section.length()>=minArticleSize) {
                if (sb.length() > 0) {
                    sb.append("\n\n");
                }
                sb.append(section);
            }
        }
        return sb.toString();
    }

    protected void write(int sectionID, ArrayList conceptList){
        if(charWriter!=null)
            charWriter.write(sectionID,conceptList);
        if(cptWriter!=null)
            cptWriter.write(sectionID,conceptList);
    }

    protected void write(int sectionID, ArrayList conceptList, ArrayList relationList){
        if(charWriter!=null)
            charWriter.write(sectionID,conceptList, relationList);
        if(cptWriter!=null)
            cptWriter.write(sectionID,conceptList, relationList);
    }

    protected void initIndex(){
        if(charWriter!=null)
            charWriter.initialize();
        if(cptWriter!=null)
            cptWriter.initialize();
    }

    protected void initSectionWrite(IRSection section){
        if (charWriter != null)
            charWriter.addSection(section.copy());
        if (cptWriter != null)
            cptWriter.addSection(section.copy());
    }

    protected boolean setDoc(String docKey){
        boolean ret;

        ret=true;
        if(charWriter!=null)
            ret=charWriter.setDoc(docKey);
        if(ret && cptWriter!=null)
            ret=cptWriter.setDoc(docKey);
        return ret;
    }
}