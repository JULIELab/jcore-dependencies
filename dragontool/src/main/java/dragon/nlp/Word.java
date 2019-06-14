package dragon.nlp;

import dragon.nlp.tool.Tagger;
/**
 * <p>This is a supporting class for Term and Phrase class which can contain multi words </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class Word {
    public static final int SUBJ=1;
    public static final int PREDICATE=2;
    public static final int OBJ=3;
    public static final int TYPE_WORD=1;
    public static final int TYPE_NUMBER=2;
    public static final int TYPE_PUNC=4;
    public Word next;
    public Word prev;

    private Sentence parent;
    private String content;
    private String lemma;
    private boolean isBaseForm;
    private boolean ignored;
    private String posLabel;
    private int freq;
    private int index;
    private int offset;
    private byte posIndex;
    private byte types;
    private byte posInSentence;
    private byte roleInClause;
    private byte parallelGroup;
    private byte clauseID;
    private Concept associatedConcept;

    public Word(String content)
    {
        prev=null;
        next=null;
        parent=null;
        freq=1;
        offset=-1;
        posInSentence=-1;
        roleInClause=-1;
        associatedConcept=null;
        posLabel=null;
        posIndex=-1;
        this.content=content;
        lemma=null;
        ignored=false;
        isBaseForm=false;
        types=(byte)TYPE_WORD;
        parallelGroup=-1;
        parent=null;
        clauseID=-1;
        index=Integer.MIN_VALUE;
    }

    public Word copy()
    {
        Word newWord;

        newWord=new Word(content);
        newWord.setLemma(getLemma());
        newWord.setPOS(getPOSLabel(),getPOSIndex());
        newWord.setParallelGroup(getParallelGroup());
        newWord.setRoleInClause(getRoleInClause());
        newWord.setIndex(getIndex());
        newWord.setAssociatedConcept(getAssociatedConcept());
        return newWord;
    }

    public String getName(){
        if(getLemma()!=null)
            return getLemma();
        else
            return content;
    }

    public String getEntryID() {
        return null;
    }

    public String getSemanticType() {
        return null;
    }

    public void setIndex(int index){
        this.index=index;
    }

    public int getIndex(){
        return index;
    }

    public int getOffset(){
        return offset;
    }

    public void setOffset(int offset){
        this.offset =offset;
    }

    public void setLemma(String lemma){
        if(lemma==null)
        {
            this.lemma=null;
            isBaseForm=false;
            return;
        }

        lemma=lemma.toLowerCase(); //make sure lemma is in lower case.
        if(lemma.compareTo(content)==0)
        {
            lemma=null;
            isBaseForm=true;
        }
        else
        {
            isBaseForm=false;
            this.lemma=lemma;
        }
    }

    public String getLemma(){
        if(isBaseForm)
            return content;
        else
            return lemma;
    }

    public Sentence getParent(){
        return parent;
    }

    public void setParent(Sentence parent){
        this.parent=parent;
    }

    public int getType(){
        return types;
    }

    public void setType(int wordType){
        if(wordType==TYPE_NUMBER || wordType==TYPE_PUNC){
            types=(byte)wordType;
            isBaseForm=true;
        }
        else
            types=(byte)TYPE_WORD;
    }

    public int getPosInSentence(){
        return posInSentence;
    }

    public void setPosInSentence(int offset){
        posInSentence=(byte)offset;
    }

    public boolean isNumber(){
        return types==TYPE_NUMBER;
    }

    public boolean isWord(){
        return types==TYPE_WORD;
    }

    public boolean isAllCapital(){
        if(Character.isUpperCase(content.charAt(0)) && Character.isUpperCase(content.charAt(content.length()-1)))
            return true;
        else
            return false;
    }
    
    public boolean isInitialCapital(){
    	return Character.isUpperCase(content.charAt(0));
    }

    public int getParallelGroup(){
        return parallelGroup;
    }

    public void setParallelGroup(int groupNo){
        parallelGroup=(byte)groupNo;
    }

    public int getClauseID(){
        return clauseID;
    }

    public void setClauseID(int clauseID){
        this.clauseID=(byte)clauseID;
    }

    public boolean isPunctuation()
    {
        return types==TYPE_PUNC;
    }

    public String getContent(){
        return content;
    }
    
    public void setContent(String content){
    	this.content=content;
    }

    public void setPOS(String posLabel, int posIndex){
        this.posLabel=posLabel;
        this.posIndex=(byte)posIndex;
        if(posIndex<=0 || posIndex==Tagger.POS_CC || posIndex==Tagger.POS_DT || posIndex==Tagger.POS_IN){
            isBaseForm = true;
        }
    }

    public void setIgnore(boolean ignored){
        this.ignored=ignored;
    }

    public boolean canIgnore(){
        return ignored;
    }

    public String getPOSLabel(){
        return posLabel;
    }

    public int getPOSIndex(){
        return posIndex;
    }

    public int getFrequency(){
        return freq;
    }

    public void addFrequency(int inc){
        freq=freq+inc;
    }

    public void setFrequency(int freq){
        this.freq=freq;
    }

    public void setAssociatedConcept(Concept concept){
        associatedConcept=concept;
    }

    public Concept getAssociatedConcept(){
        return associatedConcept;
    }

    public void setRoleInClause(int role){
        roleInClause=(byte)role;
    }

    public int getRoleInClause(){
        return roleInClause;
    }

    public int hashCode(){
        if (index >= 0)
            return index;
        else
            return super.hashCode();
    }
}
