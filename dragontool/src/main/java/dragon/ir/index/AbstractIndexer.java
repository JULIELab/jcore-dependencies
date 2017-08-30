package dragon.ir.index;

import dragon.onlinedb.*;
import dragon.util.*;
import java.io.*;
import java.util.*;

/**
 * <p>AbstractIndexer implements basic functions of interface Indexer
 * such as building index for an article and options for section index
 * and relation index
 *  </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractIndexer  implements Indexer {
    private PrintWriter log;
    protected IRSection[] arrSections;
    protected boolean relationSupported;
    protected boolean initialized;
    protected boolean enable_AllSection;
    private ArrayList sectionList;
    private ArrayList conceptList, relationList;
    private ArrayList sectionConceptList, sectionRelationList;

    public AbstractIndexer(boolean relationSupported) {
        log = null;
        this.relationSupported = relationSupported;
        initialized=false;
        sectionList=new ArrayList();
        conceptList=new ArrayList();
        relationList=new ArrayList();
        sectionConceptList=new ArrayList();
        sectionRelationList=new ArrayList();
    }

    abstract protected void initDocIndexing();
    abstract protected String getRemainingSections(Article article);
    abstract protected boolean extract(String text, ArrayList conceptList);
    abstract protected boolean extract(String text, ArrayList conceptList, ArrayList relationList);
    abstract protected boolean setDoc(String docKey);
    abstract protected void initSectionWrite(IRSection section);
    abstract protected void initIndex();
    abstract protected void write(int sectionID, ArrayList conceptList);
    abstract protected void write(int sectionID, ArrayList conceptList, ArrayList relationList);

    public void setLog(String logFile) {
        log = FileUtil.getPrintWriter(logFile);
    }

    protected boolean addSection(IRSection section){
        IRSection cur;
        int i;

        if(initialized) return false;

        for (i = 0; i < sectionList.size(); i++) {
            cur=(IRSection)sectionList.get(i);
            if(cur.getSectionID() ==section.getSectionID())
                return false;
            if(cur.getSectionName().equalsIgnoreCase(section.getSectionName()))
                return false;
        }
        sectionList.add(section);
        return true;
    }

    public void initialize() {
        IRSection cur;
        int i;

        if(initialized) return;

        arrSections=new IRSection[sectionList.size()];
        Collections.sort(sectionList);
        for (i = 0; i < sectionList.size(); i++) {
            cur=(IRSection)sectionList.get(i);
            arrSections[i]=cur;
            if(cur.getSectionID()==IRSection.SEC_ALL)
                enable_AllSection=true;
            initSectionWrite(cur);
        }
        initIndex();
        initialized=true;
    }

    public boolean index(Article article) {
        int i, sectionID;
        boolean ret;

        try {
            if(!initialized) return false;

            writeLog(new java.util.Date().toString());
            writeLog("Indexing article #" + article.getKey()+": ");

            conceptList.clear();
            relationList.clear();
            initDocIndexing();
            if(!setDoc(article.getKey())){
                writeLog("indexed\n");
                return false;
            }

            if(enable_AllSection)
                i=1;
            else
                i=0;
            ret=true;
            for(;i<arrSections.length;i++){
                sectionConceptList.clear();
                sectionRelationList.clear();
                sectionID=arrSections[i].getSectionID();
                if(relationSupported){
                    ret = extract(getSection(article, sectionID), sectionConceptList, sectionRelationList);
                    write(sectionID,sectionConceptList, sectionRelationList);
                    if(enable_AllSection){
                        conceptList.addAll(sectionConceptList);
                        relationList.addAll(sectionRelationList);
                    }
                }
                else{
                    ret = extract(getSection(article, sectionID), sectionConceptList);
                    write(sectionID,sectionConceptList);
                    if(enable_AllSection){
                        conceptList.addAll(sectionConceptList);
                    }
                }
                if(!ret) break;
            }

            if(enable_AllSection && ret){
                sectionConceptList.clear();
                sectionRelationList.clear();
                if(relationSupported){
                    ret = extract(getRemainingSections(article), sectionConceptList, sectionRelationList);
                    conceptList.addAll(sectionConceptList);
                    relationList.addAll(sectionRelationList);
                    write(IRSection.SEC_ALL,conceptList, relationList);
                }
                else{
                    ret = extract(getRemainingSections(article), sectionConceptList);
                    conceptList.addAll(sectionConceptList);
                    write(IRSection.SEC_ALL,conceptList);
                }
            }
            if(ret){
                writeLog("succeeded\r\n");
                return true;
            }
            else{
                writeLog("failed\r\n");
                return false;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            writeLog("failed\r\n");
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

    protected void writeLog(String content) {
        if (log != null) {
            log.write(content);
            log.flush();
        }
    }
}
