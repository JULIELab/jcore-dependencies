package dragon.ir.query;

import dragon.ir.index.*;
import dragon.nlp.*;
import dragon.util.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * <p>Query writer which supports query related document sets indexing</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class QueryWriter {
    private static final int MAX_SEC = 10;
    private boolean indexConceptEntry;
    private double subtermWeight;
    private SortedArray conceptList, relationList;
    private IRSection[] arrSections;

    public QueryWriter(boolean relationSupported, boolean indexConceptEntry) {
        this.indexConceptEntry = indexConceptEntry;
        subtermWeight = 1;
        arrSections = new IRSection[MAX_SEC];
        conceptList = new SortedArray();
        relationList = new SortedArray();
    }

    public void initNewQuery(){
        conceptList.clear();
        relationList.clear();
    }

    public boolean addSection(IRSection section) {
        if (section.getSectionID() >= MAX_SEC)
            return false;
        arrSections[section.getSectionID()] = section;
        return true;
    }

    public boolean write(int section, ArrayList conceptList) {
        Concept curCpt;
        int i;

        if (arrSections[section] == null) return false;

        for (i = 0; i < conceptList.size(); i++) {
            curCpt = (Concept) conceptList.get(i);
            addConcept(section, curCpt);
        }
        return true;
    }

    public boolean write(int section, ArrayList conceptList, ArrayList tripleList) {
        Triple curTriple;
        int i;

        if (arrSections[section] == null) return false;

        write(section,conceptList);
        for (i = 0; i < tripleList.size(); i++) {
            curTriple = (Triple) tripleList.get(i);
            addTriple(section, curTriple);
        }
        return true;
    }

    private void addTriple(int section, Triple triple) {
        Token first, second;
        Triple cur, old;
        int pos;

        if (indexConceptEntry)
            first = new Token(triple.getFirstConcept().getEntryID());
        else
            first = new Token(triple.getFirstConcept().getName());
        if((pos=conceptList.binarySearch(first))<0) return;
        first=(Token)conceptList.get(pos);

        if (indexConceptEntry)
            second = new Token(triple.getSecondConcept().getEntryID());
        else
            second = new Token(triple.getSecondConcept().getName());
        if((pos=conceptList.binarySearch(second))<0) return;
        second=(Token)conceptList.get(pos);

        if (first.getIndex()> second.getIndex())
            cur = new Triple(second, first);
        else
            cur = new Triple(first, second);
        cur.setWeight(arrSections[section].getWeight());
        cur.setIndex(relationList.size());
        if (!relationList.add(cur)) {
            old=(Triple)relationList.get(relationList.insertedPos());
            if(cur.getWeight()>old.getWeight())
                old.setWeight(cur.getWeight());
        }
    }

    private void addConcept(int section, Concept concept) {
        Token cur, old;
        if (indexConceptEntry)
            cur = new Token(concept.getEntryID());
        else
            cur = new Token(concept.getName());
        cur.setWeight(arrSections[section].getWeight());
        if (concept.isSubConcept())
            cur.setWeight(cur.getWeight() * subtermWeight);
        cur.setIndex(conceptList.size());
        if (!conceptList.add(cur)){
            old=(Token)conceptList.get(conceptList.insertedPos());
            if(cur.getWeight()>old.getWeight())
                old.setWeight(cur.getWeight());
        }
    }

    public void setSubTermWeight(double weight) {
        this.subtermWeight = weight;
    }

    public String getQuery() {
        Token curToken;
        Triple curTriple;
        StringBuffer query;
        DecimalFormat df;
        int i;

        df = FormatUtil.getNumericFormat(1, 2);
        query=new StringBuffer();

        for (i = 0; i < relationList.size(); i++) {
            curTriple=(Triple)relationList.get(i);
            query.append("R(");
            if (curTriple.getWeight() != 1.0) {
                query.append(df.format(curTriple.getWeight()));
                query.append(",");
            }
            query.append("TERM=");
            curToken=(Token)curTriple.getFirstConcept();
            query.append(curToken.getValue());
            query.append(" AND TERM2=");
            curToken=(Token)curTriple.getSecondConcept();
            query.append(curToken.getValue());
            query.append(") ");
        }

        for (i = 0; i < conceptList.size(); i++) {
            curToken=(Token)conceptList.get(i);
            query.append(" T(");
            if (curToken.getWeight() != 1.0) {
                query.append(df.format(curToken.getWeight()));
                query.append(",");
            }
            query.append("TERM=");
            query.append(curToken.getValue());
            query.append(")");
        }

        return query.toString();
    }
}