package dragon.nlp.ontology.mesh;

import dragon.nlp.ontology.BasicOntology;
import dragon.nlp.ontology.SimilarityMetric;
import dragon.nlp.tool.Lemmatiser;
import dragon.util.EnvVariable;
import dragon.util.FileUtil;

/**
 * <p>Ontology data structure for MeSH ontology</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicMeshOntology extends BasicOntology{
    private MeshSimilarity metric;

    public BasicMeshOntology(Lemmatiser lemmatiser, int similarityMode) {
        this(lemmatiser,similarityMode,EnvVariable.getDragonHome()+"/nlpdata/mesh/mesh.hier",
             EnvVariable.getDragonHome()+"/nlpdata/mesh/mesh.cui");
    }

    public BasicMeshOntology(Lemmatiser lemmatiser, int similarityMode, String hierFile, String conceptFile) {
        super(conceptFile, lemmatiser);
        if(!FileUtil.exist(hierFile) && FileUtil.exist(EnvVariable.getDragonHome()+"/"+hierFile))
            hierFile=EnvVariable.getDragonHome()+"/"+hierFile;
        metric=new MeshSimilarity(hierFile, similarityMode);
    }

    public SimilarityMetric getSimilarityMetric(){
        return metric;
    }
}