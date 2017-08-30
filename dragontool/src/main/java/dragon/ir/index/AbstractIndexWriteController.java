package dragon.ir.index;

import dragon.nlp.*;
import dragon.nlp.compare.IndexComparator;
import dragon.util.SortedArray;
import java.util.ArrayList;
/**
 * <p>AbstractIndexWriteController implements basics functions including generating IRTermList, IRRelationList for the useage of later processing.</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractIndexWriteController {
    protected SimpleElementList termKeyList;
    protected SimpleElementList docKeyList;
    protected SimplePairList relationKeyList;
    protected int curDocIndex;
    protected String curDocKey;
    protected int processedDoc;
    protected boolean relationSupported;
    protected boolean initialized;
    private SortedArray termList, relationList;
    private boolean indexConceptEntry;

    public AbstractIndexWriteController(boolean relationSupported, boolean indexConceptEntry) {
        this.relationSupported=relationSupported;
        this.indexConceptEntry =indexConceptEntry;
        processedDoc=0;
        initialized=false;
        termList=new SortedArray();
        if(relationSupported)
            relationList=new SortedArray();
    }

    public abstract void initialize();

    public boolean indexed(String docKey){
        return docKeyList.contains(docKey);
    }

    public int size(){
        return processedDoc;
    }

    public boolean isRelationSupported(){
        return relationSupported;
    }

    public boolean setDoc(String docKey){
        curDocIndex=docKeyList.add(docKey);
        termList.clear();
        if(relationSupported)
            relationList.clear();

        if(curDocIndex==docKeyList.size()-1){
            curDocKey=docKey;
            processedDoc++;
            return true;
        }
        else
        {
            curDocKey=null;
            return false;
        }
    }

    protected IRTerm[] getIRTermArray(ArrayList irtermList, IRDoc curDoc){
         IRTerm[] arrTerms;
         int i, count;

         arrTerms=new IRTerm[irtermList.size()];
         curDoc.setTermNum(arrTerms.length);
         count=0;
         for(i=0;i<irtermList.size();i++){
             arrTerms[i] = (IRTerm) irtermList.get(i);
             count+=arrTerms[i].getFrequency();
         }
         curDoc.setTermCount(count);
         return arrTerms;
     }

     protected IRRelation[] getIRRelationArray(ArrayList irRelationList, IRDoc curDoc){
         IRRelation[] arrRelations;
         int i, count;

         arrRelations=new IRRelation[irRelationList.size()];
         curDoc.setRelationNum(arrRelations.length);
         count=0;
         for(i=0;i<irRelationList.size();i++){
             arrRelations[i] = (IRRelation) irRelationList.get(i);
             count+=arrRelations[i].getFrequency();
         }
         curDoc.setRelationCount(count);
         return arrRelations;
     }

     protected SortedArray generateIRTermList(ArrayList termList){
         SortedArray newList;
         IRTerm curIRTerm;
         int i;

         newList=new SortedArray();
         for(i=0;i<termList.size();i++){
             curIRTerm=getIRTerm((Concept)termList.get(i));
             if(!newList.add(curIRTerm)){
                ((IRTerm)newList.get(newList.insertedPos())).addFrequency(curIRTerm.getFrequency());
             }
         }
         return newList;
     }

     protected SortedArray generateIRRelationList(ArrayList tripleList){
         SortedArray newList;
         IRRelation curIRRelation;
         int i;

         newList=new SortedArray(new IndexComparator());
         for(i=0;i<tripleList.size();i++){
             curIRRelation=getIRRelation((Triple)tripleList.get(i));
             if(!newList.add(curIRRelation)){
                 ((IRRelation)newList.get(newList.insertedPos())).addFrequency(curIRRelation.getFrequency());
             }
         }
         return newList;
     }

     private IRRelation getIRRelation(Triple triple) {
         int first, second, index;
         IRRelation cur;

         first=getIRTerm(triple.getFirstConcept()).getIndex();
         second=getIRTerm(triple.getSecondConcept()).getIndex();

         if(first>second)
             cur=new IRRelation(second,first,triple.getFrequency());
         else
             cur=new IRRelation(first,second,triple.getFrequency());
         if(relationList.add(cur)){
            index=relationKeyList.add(cur.getFirstTerm(),cur.getSecondTerm());
            cur.setIndex(index);
         }
         else{
             cur.setIndex(((IRRelation)relationList.get(relationList.insertedPos())).getIndex());
         }
         return cur;
     }

     private IRTerm getIRTerm(Concept concept) {
         IRTerm cur;
         int index;

         if (indexConceptEntry)
             cur = new IRTerm(new String(concept.getEntryID()), -1, concept.getFrequency());
         else
             cur = new IRTerm(new String(concept.getName()), -1, concept.getFrequency());
         if(termList.add(cur)){
            index=termKeyList.add(cur.getKey());
            cur.setIndex(index);
         }
         else{
             cur.setIndex(((IRTerm)termList.get(termList.insertedPos())).getIndex());
         }
         return cur;
     }


}