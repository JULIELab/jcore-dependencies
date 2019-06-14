package dragon.nlp.ontology.umls;

import dragon.nlp.ontology.BasicTerm;
import dragon.nlp.ontology.BasicTermList;
import dragon.nlp.ontology.Ontology;
import dragon.nlp.ontology.SemanticNet;
import dragon.nlp.tool.Lemmatiser;
import dragon.util.EnvVariable;
import dragon.util.FileUtil;

import java.io.File;

/**
 * <p>The class has functions of readingUMLS ontology from file</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class UmlsFileBackedOntology extends UmlsExactOntology implements Ontology{
    private UmlsCUIList cuiList;
    private BasicTermList termList;
    private File directory;
    private UmlsSemanticNet snNet;

    public UmlsFileBackedOntology(Lemmatiser lemmatiser){
        this(EnvVariable.getDragonHome()+"/nlpdata/umls",lemmatiser);
    }

    public UmlsFileBackedOntology(String workDir, Lemmatiser lemmatiser) {
        super(lemmatiser);
        if(!FileUtil.exist(workDir) && FileUtil.exist(EnvVariable.getDragonHome()+"/"+workDir))
            workDir=EnvVariable.getDragonHome()+"/"+workDir;
        this.directory=new File(workDir);
        cuiList=new UmlsCUIList(directory+"/cui.list");
        termList=new BasicTermList(directory+"/termindex.list");
        UmlsSTYList styList=new UmlsSTYList(directory+"/semantictype.list");
        UmlsRelationNet relationNet=new UmlsRelationNet(directory+"/semanticrelation.list",styList);
        snNet=new UmlsSemanticNet(this,styList,relationNet);
        System.out.println(new java.util.Date() +" Ontology Loading Done!");
    }

    public SemanticNet getSemanticNet(){
        return snNet;
    }

    public String[] getSemanticType(String cui)
    {
        UmlsCUI cur;

        cur=cuiList.lookup(cui);
        if(cur==null)
            return null;
        else
            return cur.getAllSTY();
    }

    public String[] getCUI(String term){
        BasicTerm cur;

        cur=termList.lookup(term);
        if(cur==null)
            return null;
        else
            return cur.getAllCUI();
    }

    public boolean isTerm(String term){
        return  termList.lookup(term)!=null;
    }

}