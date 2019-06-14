package dragon.nlp.ontology.umls;

import dragon.matrix.DoubleSuperSparseMatrix;
import dragon.matrix.SparseMatrix;
import dragon.nlp.Sentence;
import dragon.nlp.Term;
import dragon.nlp.Token;
import dragon.nlp.Word;
import dragon.nlp.ontology.Ontology;
import dragon.nlp.ontology.SemanticNet;
import dragon.nlp.tool.Lemmatiser;
import dragon.nlp.tool.Tagger;
import dragon.util.EnvVariable;
import dragon.util.FileUtil;
import dragon.util.SortedArray;

import java.io.File;
import java.util.ArrayList;
/**
 * <p>UMLS ontology with sense disambiguation  </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class UmlsAmbiguityOntology extends UmlsOntology implements Ontology{
    private double minScore, subtermMinScore;
    private int maxSkippedWords;
    private double minSelectivity;

    private SparseMatrix wtMatrix;
    private UmlsTokenList tokenList;
    private UmlsCUIList cuiList;
    private ArrayList cuiListByIndex;
    private File directory;
    private UmlsSemanticNet snNet;

    public UmlsAmbiguityOntology(Lemmatiser lemmatiser){
        this(EnvVariable.getDragonHome()+"/nlpdata/umls",lemmatiser);
    }

    public UmlsAmbiguityOntology(String workDir, Lemmatiser lemmatiser) {
        super(lemmatiser);
        if(!FileUtil.exist(workDir) && FileUtil.exist(EnvVariable.getDragonHome()+"/"+workDir))
            workDir=EnvVariable.getDragonHome()+"/"+workDir;
        this.directory=new File(workDir);
        System.out.println(new java.util.Date() +" Loading Token CUI Matrix...");
        if(FileUtil.exist(workDir+"/tokencui.index"))
        	wtMatrix = new DoubleSuperSparseMatrix(directory+"/tokencui.index", directory+"/tokencui.matrix");
        else
        	wtMatrix = new DoubleSuperSparseMatrix(directory+"/index.list", directory+"/tokencui.matrix");
        if(FileUtil.exist(workDir+"/token.bin"))
        	tokenList=new UmlsTokenList(directory+"/token.bin",true);
        else
        	tokenList=new UmlsTokenList(directory+"/token.list",false);
        if(FileUtil.exist(workDir+"/cui.bin"))
        	cuiList=new UmlsCUIList(directory+"/cui.bin",true,false);
        else
        	cuiList=new UmlsCUIList(directory+"/cui.list",false);
        cuiListByIndex=cuiList.getListSortedByIndex();
        UmlsSTYList styList=new UmlsSTYList(directory+"/semantictype.list");
        UmlsRelationNet relationNet=new UmlsRelationNet(directory+"/semanticrelation.list",styList);
        snNet=new UmlsSemanticNet(this,styList,relationNet);
        System.out.println(new java.util.Date() +" Ontology Loading Done!");

        maxSkippedWords=1;
        minScore=0.95;
        subtermMinScore=0.99;
        minSelectivity=0;
    }

    public void setMinScore(double minScore){
        this.minScore=minScore;
    }

    public double getMinScore(){
        return minScore;
    }

    public void setMinSelectivity(double minSelectivity){
        this.minSelectivity=minSelectivity;
    }

    public double getMinSelectivity(){
        return minSelectivity;
    }

    public void setMaxSkippedWords(int num){
        maxSkippedWords=num;
    }

    public int getMaxSkippedWords(){
        return maxSkippedWords;
    }

    public SemanticNet getSemanticNet(){
        return snNet;
    }

    public String[] getSemanticType(String[] cuis){
        SortedArray typeList;
        String[] arrTypes;
        int i,j;

        typeList=new SortedArray(3);
        for(i=0;i<cuis.length;i++)
        {
            arrTypes=getSemanticType(cuis[i]);
            if(arrTypes!=null){
                for(j=0;j<arrTypes.length;j++)
                    typeList.add(arrTypes[j]);
            }
        }
        if(typeList.size()>0){
            arrTypes=new String[typeList.size()];
            for(i=0;i<typeList.size();i++)
                arrTypes[i]=(String)typeList.get(i);
            return arrTypes;
        }
        else
            return null;
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
        return null;
    }

    public String[] getCUI(Word starting, Word ending){
        return null;
    }

    public boolean isTerm(String term){
        return false;
    }

    public boolean isTerm(Word starting, Word ending){
        return false;
    }

    public ArrayList findAllTerms(Word start){
        return findAllTerms(start,null);
    }

    public ArrayList findAllTerms(Word start, Word end){
        ArrayList termList, canTermList;
        CandidateTerm  canTerm;
        Term curTerm;
        Word curWord;
        int i;

        termList=null;
        curTerm=null;
        canTermList=searchAllCandidates(start,end, minScore);
        if(canTermList==null || canTermList.size()<=0)
            return null;
        else
            termList=new ArrayList();

        for (i = canTermList.size()-1; i >=0; i--){
            canTerm = (CandidateTerm) canTermList.get(i);
            if (getSenseDisambiguationOption() && canTerm.getCandidateCUINum() > 1) {
                canTerm = disambiguateCandidateTerm(canTerm);
            }
            curTerm=generateTerm(canTerm,true);
            termList.add(curTerm);
        }
        curTerm=(Term)termList.get(0);
        curTerm.setSubConcept(false);
        curTerm.getStartingWord().setAssociatedConcept(curTerm);
        end=curTerm.getEndingWord();
        curWord=start.next;

        while(curWord!=null && curWord.getPosInSentence()<=end.getPosInSentence()){
            if(!isStartingWord(curWord)){
               curWord=curWord.next;
               continue;
            }
            canTermList=searchAllCandidates(curWord,end,subtermMinScore);
            if(canTermList!=null){
                for (i = canTermList.size()-1; i >=0; i--) {
                    canTerm = (CandidateTerm) canTermList.get(i);
                    if (getSenseDisambiguationOption() && canTerm.getCandidateCUINum() > 1) {
                        canTerm = disambiguateCandidateTerm(canTerm);
                    }
                    curTerm = generateTerm(canTerm, true);
                    termList.add(curTerm);
                }
            }
            curWord=curWord.next;
        }
        return termList;
    }

    public Term findTerm(Word start){
        return findTerm(start,null);
    }

    public Term findTerm(Word start, Word end){
        ArrayList canTermList;
        CandidateTerm  canTerm;

        canTermList=searchAllCandidates(start,end,minScore);
        if(canTermList==null || canTermList.size()<=0) return null;

        canTerm=(CandidateTerm)canTermList.get(canTermList.size()-1);

        //use contextual words to narrow down the sense
        if(getSenseDisambiguationOption() && canTerm.getCandidateCUINum()>1)
        {
            canTerm=disambiguateCandidateTerm(canTerm);
        }

        return generateTerm(canTerm,false);
    }

    private Term generateTerm(CandidateTerm canTerm, boolean isSubTerm){
        Term curTerm;
        String[] arrCUI;
        int i, candidateNum;

        //remove remaining candidates if there exists candidates with its score equal to or above 1.0
        i=0;
        candidateNum=canTerm.getCandidateCUINum();
        while(i<candidateNum && canTerm.getCandidateCUI(i).getScore()>=1.0)
            i++;
        if(i>0)
            candidateNum=i;

        //generate the term
        arrCUI = new String[candidateNum];
        for (i = 0; i < candidateNum; i++)
            arrCUI[i] = ((UmlsCUI) cuiListByIndex.get(canTerm.getCandidateCUI(i).getIndex())).toString();

        curTerm = new Term(canTerm.getStartingWord(),canTerm.getEndingWord());
        curTerm.setSubConcept(isSubTerm);
        if(!curTerm.isSubConcept())
            canTerm.getStartingWord().setAssociatedConcept(curTerm);
        if(candidateNum<=1 || canTerm.getCandidateCUI(1).getScore()<canTerm.getCandidateCUI(0).getScore())
            curTerm.setCUI(arrCUI[0]);
        curTerm.setCandidateCUI(arrCUI);
        if (curTerm.getCUI() == null) {
            curTerm.setCandidateTUI(getSemanticType(curTerm.getCandidateCUI()));
        }
        else {
            curTerm.setCandidateTUI(getSemanticType(curTerm.getCUI()));
        }
        if (curTerm.getCandidateTUINum() == 1) {
            curTerm.setTUI(curTerm.getCandidateTUI(0));
        }
        return curTerm;
    }

    private CandidateTerm disambiguateCandidateTerm(CandidateTerm canTerm){
        ArrayList contextList;
        Word curWord;
        int candidateNum, narrowedNum;
        int i, j, index;
        int[] arrCandidateCUI;
        double[] arrCandidateScore;
        double score;

        candidateNum=canTerm.getCandidateCUINum();
        arrCandidateCUI=new int[candidateNum];
        arrCandidateScore=new double[candidateNum];
        for(i=0;i<candidateNum;i++)
        {
            arrCandidateCUI[i] = canTerm.getCandidateCUI(i).getIndex();
            arrCandidateScore[i] = canTerm.getCandidateCUI(i).getScore();
        }
        contextList = generateContextWindow(canTerm.getStartingWord().getParent(), canTerm.getStartingWord(), canTerm.getEndingWord());

        for(i=0;i<contextList.size();i++)
        {
            curWord=(Word)contextList.get(i);
            index = getIndexInTokenList(curWord);
            if (index<0) continue;
            narrowedNum = 0;
            for (j = 0; j < candidateNum; j++) {
                if ( (score = wtMatrix.getDouble(index, arrCandidateCUI[j])) > 0) {
                    arrCandidateCUI[narrowedNum] = arrCandidateCUI[j];
                    arrCandidateScore[narrowedNum] = arrCandidateScore[j] + score;
                    narrowedNum += 1;
                }
            }
            if (narrowedNum > 0) candidateNum = narrowedNum;
        }
        if(candidateNum<canTerm.getCandidateCUINum())
            canTerm=buildCandidateTerm(canTerm.getStartingWord(),canTerm.getEndingWord(),arrCandidateCUI,arrCandidateScore,candidateNum,minScore);
        contextList.clear();
        return canTerm;
    }

    private ArrayList searchAllCandidates(Word start, Word end, double minScore){
        ArrayList canTermList;
        CandidateTerm  canTerm;
        Sentence sent;
        Word curWord;
        int candidateNum, narrowedNum;
        int j, index;
        int[] arrCandidateCUI;
        double[] arrCandidateScore;
        double score;
        int skippedWords;

        if((index=getIndexInTokenList(start))<0) return null;
        sent=start.getParent();

        //set the right bounary of the possible term.
        curWord=start.next;
        if(end==null){
            j = 0;
            while (j <4 && curWord != null && end==null) {
                if(isBoundaryWord(curWord))
                    end = curWord.prev;
                if (!curWord.isPunctuation())
                    j++;
                curWord = curWord.next;
            }
            if (curWord == null)
                curWord = sent.getLastWord();
            if (end == null)
                end = curWord;
        }

        //get the number of none zero columns
        candidateNum = wtMatrix.getNonZeroNumInRow(index);
        //if none of them contain it, continue
        if (candidateNum <= 0) return null;
        //get array of non zero column index, that is the CUIs that contain the word
        arrCandidateCUI = wtMatrix.getNonZeroColumnsInRow(index);
        //get array of non zero column scores
        arrCandidateScore = wtMatrix.getNonZeroDoubleScoresInRow(index);

        canTermList=new ArrayList(3);
        if((canTerm=buildCandidateTerm(start,start,arrCandidateCUI,arrCandidateScore,candidateNum,minScore))!=null)
            canTermList.add(canTerm);

        //extract tokens within window
        curWord=start.next;
        skippedWords=0;
        while (curWord!=null && skippedWords<=maxSkippedWords && curWord.getPosInSentence() <=end.getPosInSentence()) {
            if (! isUsefulForTerm(curWord)) {
                curWord = curWord.next;
                continue;
            }

            index=getIndexInTokenList(curWord);
            if(index<0) {
                curWord = curWord.next;
                skippedWords++;
                continue;
            }
            narrowedNum = 0;
            for (j = 0; j < candidateNum; j++) {
                if ( (score = wtMatrix.getDouble(index,arrCandidateCUI[j])) > 0) {
                    arrCandidateCUI[narrowedNum] = arrCandidateCUI[j];
                    arrCandidateScore[narrowedNum] = arrCandidateScore[j] + score;
                    narrowedNum += 1;
                }
            }
            if (narrowedNum > 0)
            {
                candidateNum = narrowedNum;
                if((canTerm=buildCandidateTerm(start,curWord,arrCandidateCUI,arrCandidateScore,candidateNum,minScore))!=null)
                    canTermList.add(canTerm);
                skippedWords=0;
            }
            else{
                skippedWords++;
            }
            curWord = curWord.next;
        }
        return canTermList;
    }

    /*The follow fucnction generates the contextual words for a term specified by its starting word and ending word.
    We take up to three three left words (noun or adjective) and up to three right words as the window.*/
    private ArrayList generateContextWindow(Sentence sent, Word start, Word end) {
        ArrayList contexts;
        Word cur;
        int i;

        contexts = new ArrayList(6);
        cur = start.prev;
        i = 0;
        while (i < 3 && cur != null) {
            if (cur.getPOSIndex() == Tagger.POS_NOUN || cur.getPOSIndex() == Tagger.POS_ADJECTIVE) {
                contexts.add(cur);
                i = i + 1;
            }
            cur = cur.prev;
        }

        cur = start.next;
        i = 0;
        while (i < 3 && cur != null) {
            if (cur.getPOSIndex() == Tagger.POS_NOUN || cur.getPOSIndex() == Tagger.POS_ADJECTIVE) {
                contexts.add(cur);
                i = i + 1;
            }
            cur = cur.next;
        }

        return contexts;
    }

    private CandidateTerm buildCandidateTerm(Word starting, Word ending, int[] arrCandidateCUI, double[] arrCandidateScore, int candidateNum,double minScore){
        CandidateTerm cTerm;
        int i;

        if(ending.getPOSIndex()==Tagger.POS_ADJECTIVE && (!getAdjectiveTermOption() || !ending.equals(starting))) return null;
        if(ending.getPOSIndex()==Tagger.POS_NUM && ending.equals(starting)) return null;
        if(1.0/candidateNum<minSelectivity) return null;

        cTerm=new CandidateTerm(starting, ending);
        for(i=0;i<candidateNum;i++)
            if(arrCandidateScore[i]>=minScore)
                cTerm.addCandidateCUI(new CandidateCUI(arrCandidateCUI[i],arrCandidateScore[i]));
        if(cTerm.getCandidateCUINum()>0){
            if(ending.getPOSIndex()==Tagger.POS_ADJECTIVE && cTerm.getCandidateCUI(0).getScore()<1)
                return null;
            else
                return cTerm;
        }
        else
            return null;
    }

    private int getIndexInTokenList(Word word){

        if(word.getIndex()==Integer.MIN_VALUE)
        {
            Token token=tokenList.lookup(getLemma(word));;
            if (token==null)
                word.setIndex(-1);
            else
                word.setIndex(token.getIndex());
        }
        return word.getIndex();
    }

    private class CandidateCUI implements Comparable{
        private int index;
        private double score;

        public CandidateCUI(int index, double score){
            this.index=index;
            this.score=score;
        }

        public double getScore(){
            return score;
        }

        public int getIndex(){
            return index;
        }

        public int compareTo(Object obj){
            double objScore;
            int objIndex;

            objScore=((CandidateCUI)obj).getScore();
            if(score>objScore)
                return -1;
            else if(score<objScore)
                return 1;
            else
            {
                objIndex=((CandidateCUI)obj).getIndex();
                if(index>objIndex)
                    return 1;
                else if(index<objIndex)
                    return -1;
                else
                    return 0;
            }
        }
    }

    private class CandidateTerm {
        Word starting, ending;
        SortedArray candidates;

        public CandidateTerm(Word starting, Word ending){
            this.starting=starting;
            this.ending=ending;
            candidates=new SortedArray();
        }

        public void addCandidateCUI(CandidateCUI cui){
            candidates.add(cui);
        }

        public Word getStartingWord(){
            return starting;
        }

        public Word getEndingWord(){
            return ending;
        }

        public CandidateCUI getCandidateCUI(int index){
            return (CandidateCUI)candidates.get(index);
        }

        public int getCandidateCUINum(){
            return candidates.size();
        }
    }
}