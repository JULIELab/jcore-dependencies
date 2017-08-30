package dragon.nlp.ontology.umls;

import dragon.nlp.ontology.*;
/**
 * <p>UMLS semantic net for semantic relation operations</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class UmlsSemanticNet implements SemanticNet{
    private Ontology ontology;
    private UmlsSTYList styList;
    private UmlsRelationNet relationNet; //it is used to cache the relations of the combinations of two semantic types

    public UmlsSemanticNet(Ontology ontology, UmlsSTYList styList, UmlsRelationNet relationNet) {
        this.ontology =ontology;
        this.styList=styList;
        this.relationNet=relationNet;
    }

    public String[] getRelations(String[] arrFirstST, String[] arrSecondST){
        return relationNet.getRelations(arrFirstST,arrSecondST);
    }

    public String[] getRelations(String firstST,String secondST){
        String relation, arrRelation[];

        relation=relationNet.getRelations(firstST,secondST);
        if(relation==null)
            return null;
        else
        {
            arrRelation=new String[1];
            arrRelation[0]=relation;
            return arrRelation;
        }
    }

    public boolean isSemanticRelated(String[] arrFirstST, String[] arrSecondST){
        return relationNet.isSemanticRelated(arrFirstST, arrSecondST);
    }

    public boolean isSemanticRelated(String firstST,String secondST){
        return relationNet.isSemanticRelated(firstST,secondST);
    }


    public String getSemanticTypeDesc(String id){
        return styList.lookup(id).getDescription();
    }

    public String getRelationDesc(String id){
        return styList.lookup(id).getDescription();
    }

    public String getHierarchy(String id){
        return styList.lookup(id).getHier();
    }

    public Ontology getOntology(){
        return ontology;
    }


}