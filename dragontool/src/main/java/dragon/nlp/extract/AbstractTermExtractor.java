package dragon.nlp.extract;

import dragon.nlp.Term;
import dragon.nlp.ontology.Ontology;
import dragon.nlp.tool.Lemmatiser;
import dragon.nlp.tool.Tagger;
import dragon.util.FileUtil;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * <p>Abstract class for UMLS term (CUI) extraction </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractTermExtractor extends AbstractConceptExtractor implements TermExtractor{
    protected Ontology ontology;
    protected Tagger tagger;
    protected Lemmatiser lemmatiser;
    protected boolean semanticCheck_enabled;
    protected boolean coordinatingTermPredict_enabled;
    protected boolean compoundTermPredict_enabled;
    protected boolean attributeCheck_enabled;
    protected boolean coordinatingCheck_enabled;
    protected boolean abbreviation_enabled;
    protected AttributeChecker attrChecker;
    protected CoordinatingChecker paraChecker;
    protected Abbreviation abbrChecker;
    protected CompoundTermFinder compTermFinder;

    public AbstractTermExtractor(Ontology ontology, Tagger tagger, Lemmatiser lemmatiser) {
       this.tagger = tagger;
       this.ontology =ontology;
       this.lemmatiser = lemmatiser;
       attrChecker = null;
       paraChecker = new CoordinatingChecker();
       abbrChecker = new Abbreviation();
       compTermFinder = new CompoundTermFinder();
       attrChecker = null;
       attributeCheck_enabled = false;
       semanticCheck_enabled = true;
       coordinatingTermPredict_enabled = false;
       compoundTermPredict_enabled = false;
       abbreviation_enabled = true;
       coordinatingCheck_enabled = true;
    }

    public boolean isExtractionMerged(){
        return false;
    }

    public boolean supportConceptName(){
        return true;
    }

    public boolean supportConceptEntry(){
        return true;
    }

    public Ontology getOntology() {
        return ontology;
    }

    public Tagger getPOSTagger() {
        return tagger;
    }

    public Lemmatiser getLemmatiser() {
        return lemmatiser;
    }

    public void setLemmatiser(Lemmatiser lemmatiser) {
        this.lemmatiser =lemmatiser;
    }

    public void setSubConceptOption(boolean option){
        subconcept_enabled = option;
        if (compTermFinder != null)
            compTermFinder.setSubTermOption(option);
    }

    public void setCoordinatingCheckOption(boolean option) {
        coordinatingCheck_enabled = option;
    }

    public boolean getCoordinatingCheckOption() {
        return coordinatingCheck_enabled;
    }

    public void setAbbreviationOption(boolean option) {
        abbreviation_enabled = option;
    }

    public boolean getAbbreviationOption() {
        return abbreviation_enabled;
    }

    public void setAttributeCheckOption(boolean option) {
        attributeCheck_enabled = option;
    }

    public boolean getAttributeCheckOption() {
        return attributeCheck_enabled;
    }

    public boolean enableAttributeCheckOption(AttributeChecker checker) {
        this.attrChecker = checker;
        attributeCheck_enabled = true;
        return true;
    }

    public boolean getSemanticCheckOption() {
        return semanticCheck_enabled;
    }

    public void setSemanticCheckOption(boolean option) {
        semanticCheck_enabled = option;
    }

    public boolean getCoordinatingTermPredictOption() {
        return coordinatingTermPredict_enabled;
    }

    public void setCoordinatingTermPredictOption(boolean option) {
        coordinatingTermPredict_enabled = option;
    }

    public boolean getCompoundTermPredictOption() {
        return compoundTermPredict_enabled;
    }

    public void setCompoundTermPredictOption(boolean option) {
        compoundTermPredict_enabled = option;
    }

    public boolean enableCompoundTermPredictOption(String suffixList) {
        compTermFinder = new CompoundTermFinder(suffixList);
        compoundTermPredict_enabled = true;
        return true;
    }

    public void initDocExtraction() {
        if (abbrChecker != null) {
            abbrChecker.clearCachedAbbr();
        }
    }

    public void print(PrintWriter out, ArrayList list) {
        Term term;
        int i, j;
        String[] arrStr;

        try {
            for (i = 0; i < list.size(); i++) {
                term = (Term) list.get(i);
                out.write(term.toString());
                for (j = 0; j < term.getAttributeNum(); j++) {
                    out.write('/');
                    out.write(term.getAttribute(j).toString());
                }
                out.write('(');
                out.write(String.valueOf(term.getFrequency()));
                out.write(')');
                arrStr = term.getCandidateTUI();
                if (arrStr != null) {
                    out.write(": ");
                    for (j = 0; j < arrStr.length; j++) {
                        out.write(arrStr[j]);
                        if (j == arrStr.length - 1) {
                            out.write(" (");
                        }
                        else {
                            out.write(';');
                        }
                    }
                    arrStr = term.getCandidateCUI();
                    for (j = 0; j < arrStr.length; j++) {
                        out.write(arrStr[j]);
                        if (term.getCUI() != null && term.getCUI().equalsIgnoreCase(arrStr[j])) {
                            out.write("*");
                        }
                        if (j == arrStr.length - 1) {
                            out.write(')');
                        }
                        else {
                            out.write(',');
                        }
                    }
                }
                if (term.isPredicted()) { //predicted term
                    out.write("(Predicted)");
                }
                out.write("\r\n");
            }
            out.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void extractTermFromFile(String filename) {
        PrintWriter out1, out2;
        ArrayList list;

        out1 = FileUtil.getPrintWriter(filename + ".term");
        out2 = FileUtil.getPrintWriter(filename + ".mergedterm");
        list = extractFromDoc(FileUtil.readTextFile(filename));

        //output all terms found
        try {
            print(out1, list);
            out1.close();
            print(out2, mergeConceptByName(list));
            out1.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected ArrayList filter(ArrayList termList){
            Term term;
            int i;
            for (i = 0; i < termList.size(); i++) {
                term = (Term) termList.get(i);
                if(!cf.keep(term)) {
                    term.getStartingWord().setAssociatedConcept(null);
                    termList.remove(i);
                    i = i - 1;
                }
            }
            return termList;
    }
}