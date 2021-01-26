package dragon.ir.query;

import dragon.ir.index.IRSection;
import dragon.nlp.extract.ConceptExtractor;
import dragon.nlp.extract.TripleExtractor;
import dragon.onlinedb.Article;

import java.util.ArrayList;
import java.util.Collections;

/**
 * <p>Basic Query Generator</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicQueryGenerator extends AbstractQueryGenerator{
    protected QueryWriter writer;
    protected IRSection[] arrSections;
    protected boolean relationSupported;
    protected boolean initialized;
    private ArrayList sectionConceptList, sectionRelationList;
    private TripleExtractor te;
    private ConceptExtractor ce;

    public BasicQueryGenerator(TripleExtractor te,  boolean indexConceptEntry) {
        this.te=te;
        relationSupported = true;
        writer=new QueryWriter(relationSupported, indexConceptEntry);
        initialized=false;
        sectionConceptList=new ArrayList();
        sectionRelationList=new ArrayList();
    }

    public BasicQueryGenerator(ConceptExtractor ce, boolean indexConceptEntry) {
        this.ce=ce;
        relationSupported = false;
        writer=new QueryWriter(relationSupported, indexConceptEntry);
        initialized=false;
        sectionConceptList=new ArrayList();
        sectionRelationList=new ArrayList();
    }

    public void initialize(double titleWeight, double abstractWeight, double bodyWeight, double metaWeight, double subtermWeight) {
        IRSection cur;
        ArrayList sectionList;
        int i;

        sectionList=new ArrayList();
        writer.setSubTermWeight(subtermWeight);

        if(titleWeight>0){
            cur=new IRSection(IRSection.SEC_TITLE);
            cur.setWeight(titleWeight);
            sectionList.add(cur);
        }
        if(abstractWeight>0){
            cur=new IRSection(IRSection.SEC_ABSTRACT);
            cur.setWeight(abstractWeight);
            sectionList.add(cur);
        }
        if(bodyWeight>0){
            cur=new IRSection(IRSection.SEC_BODY);
            cur.setWeight(bodyWeight);
            sectionList.add(cur);
        }
        if(metaWeight>0){
            cur=new IRSection(IRSection.SEC_META);
            cur.setWeight(metaWeight);
            sectionList.add(cur);
        }

        arrSections=new IRSection[sectionList.size()];
        Collections.sort(sectionList);
        for (i = 0; i < sectionList.size(); i++) {
            cur=(IRSection)sectionList.get(i);
            arrSections[i]=cur;
            writer.addSection(cur.copy());
        }
        initialized=true;
    }

    public IRQuery generate(Article article) {
        int i, sectionID;

        try {
            if(!initialized) return null;

            writer.initNewQuery();

            for(i=0;i<arrSections.length;i++){
                sectionConceptList.clear();
                sectionRelationList.clear();
                sectionID=arrSections[i].getSectionID();
                if(relationSupported){
                    extract(getSection(article, sectionID), sectionConceptList, sectionRelationList);
                    writer.write(sectionID,sectionConceptList, sectionRelationList);
                }
                else{
                    extract(getSection(article, sectionID), sectionConceptList);
                    writer.write(sectionID,sectionConceptList);
                }
            }
            return new RelSimpleQuery(writer.getQuery()) ;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean extract(String content, ArrayList conceptList, ArrayList relationList) {
        boolean ret;

        if (content == null || content.length() <= 5) {
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

    private boolean extract(String content, ArrayList conceptList) {
        if (content == null || content.length() <= 2) {
            return true;
        }
        if (ce.extractFromDoc(content) != null) {
            conceptList.addAll(ce.getConceptList());
            return true;
        }
        return false;
    }

    private String getSection(Article paper, int sectionID) {
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
}
