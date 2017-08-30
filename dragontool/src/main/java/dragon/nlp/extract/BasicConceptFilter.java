package dragon.nlp.extract;

import dragon.nlp.*;
import dragon.util.*;
import java.io.*;

/**
 * <p>Basic Concept Filter</p>
 * <p> A utility for junk term filtering</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicConceptFilter implements ConceptFilter{
    private SortedArray stoplist;
    private SortedArray excludedTypeList;
    private SortedArray supportedTypeList;

    public BasicConceptFilter() {
        excludedTypeList = null;
        supportedTypeList = null;
        stoplist = null;
    }

    public BasicConceptFilter(String stoplistFile) {
        excludedTypeList = null;
        supportedTypeList = null;
        stoplist = loadlist(stoplistFile);
    }

    public BasicConceptFilter(String stoplistFile, String supportSTYFile, String excludeSTYFile) {
        excludedTypeList = loadlist(excludeSTYFile);
        supportedTypeList = loadlist(supportSTYFile);
        stoplist = loadlist(stoplistFile);
    }

    public boolean keep(Concept concept){
        if(concept.getConceptType()!=Concept.TYPE_TERM)
            return keep(concept.getName());

        Term term=(Term)concept;
        if(term.isPredicted()) return true;
        if(isExcludedTerm(term)) return false;
        if(isStopTerm(term)) return false;
        return isSupportedTerm(term);
    }

    public boolean keep(String concept){
        return !isStopConcept(concept);
    }

    public boolean addExcludedSTY(String tui) {
        if (excludedTypeList == null) {
            excludedTypeList = new SortedArray();
        }
        excludedTypeList.add(tui);
        return true;
    }

    public boolean addMultiExcludedSTY(String tuis) {
        //TUIs are separated by semicolon
        String[] arrTUI;
        int i;

        arrTUI = tuis.split(";");
        for (i = 0; i < arrTUI.length; i++) {
            addExcludedSTY(arrTUI[i]);

        }
        return true;
    }

    public boolean addSupportedSTY(String tui) {
        if (supportedTypeList == null) {
            supportedTypeList = new SortedArray();
        }
        supportedTypeList.add(tui);
        return true;
    }

    public boolean addMultiSupportedSTY(String tuis) {
        //TUIs are separated by semicolon
        String[] arrTUI;
        int i;

        arrTUI = tuis.split(";");
        for (i = 0; i < arrTUI.length; i++)
            addSupportedSTY(arrTUI[i]);
        return true;
    }

    private boolean isStopConcept(String concept) {
        if(stoplist==null) return false;

        return stoplist.contains(concept.toLowerCase());
    }

    private boolean isStopTerm(Term term) {
        if(stoplist==null || term.getWordNum()>=2)
            return false;

        return stoplist.contains(term.getName().toLowerCase());
    }

    private boolean isExcludedTerm(Term term) {
        int num, i;

        num = term.getCandidateTUINum();
        if(num==0) return false;

        for (i = 0; i < num && !isExcludedType(term.getCandidateTUI(i)); i++);
        if (i < num) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean isSupportedTerm(Term term) {
        int num, i;

        num = term.getCandidateTUINum();
        if(num==0) return true;

        for (i = 0; i < num && !isSupportedType(term.getCandidateTUI(i)); i++);
        if (i < num) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean isExcludedType(String tui) {
        if (tui == null || excludedTypeList == null) {
            return false;
        }
        return excludedTypeList.contains(tui);
    }

    private boolean isSupportedType(String tui) {
        if (tui == null || supportedTypeList == null) {
            return true;
        }
        return supportedTypeList.contains(tui);
    }

    private SortedArray loadlist(String filename) {
        SortedArray list;
        BufferedReader br;
        String line;
        int i, total, pos;

        try {
            if (filename == null || filename.trim().length() == 0) {
                return null;
            }
            if(!FileUtil.exist(filename) && FileUtil.exist(EnvVariable.getDragonHome()+"/"+filename))
                filename=EnvVariable.getDragonHome()+"/"+filename;

            br = FileUtil.getTextReader(filename);
            line = br.readLine();
            total = Integer.parseInt(line);
            list = new SortedArray(total);

            for (i = 0; i < total; i++) {
                line = br.readLine();
                pos=line.indexOf('\t');
                if(pos>0)
                    line=line.substring(0,pos);
                list.add(line.trim().toLowerCase());
            }
            br.close();
            return list;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}