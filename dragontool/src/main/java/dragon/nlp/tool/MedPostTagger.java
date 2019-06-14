package dragon.nlp.tool;

import dragon.nlp.Sentence;
import dragon.util.EnvVariable;

//import gov.nih.nlm.nls.mps.POSTagger;

/**
 * <p>MedPost tagger adopted from gov.nih.nlm.nls.mps.POSTagger </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class MedPostTagger implements Tagger{

	public MedPostTagger(String workDir) {
		// this is just a dummy constructor for compatibility with BANNER
	}
	
	@Override
	public void tag(Sentence sent) {
		throw new IllegalStateException("This tagger is not supported by this version of the dragontools");
	}
//    private POSTagger tag;
//
    public MedPostTagger(){
        this(EnvVariable.getDragonHome()+"/nlpdata/tagger");
    }
//
//    public MedPostTagger(String workDir) {
//        try{
//            if(!FileUtil.exist(workDir) && FileUtil.exist(EnvVariable.getDragonHome()+"/"+workDir))
//                workDir=EnvVariable.getDragonHome()+"/"+workDir;
//            tag = new POSTagger(workDir+"/lexDB.serial",workDir+"/ngramOne.serial");
//            if(tag==null)
//            {
//                System.out.println("Failed to create POS tagger");
//            }
//        }
//        catch(Exception e)
//        {
//            e.printStackTrace();
//        }
//    }
//
//    public void tag(Sentence sent){
//        List result, sentenceForTagger;
//        String posLabel;
//        Word word;
//        int i;
//
//        sentenceForTagger = new ArrayList();
//        word=sent.getFirstWord();
//        while (word != null) {
//            sentenceForTagger.add(word.getContent());
//            word = word.next;
//        }
//        result=(List)tag.tagSentence(sentenceForTagger);
//        if(result.size()<=0) return;
//
//        word=sent.getFirstWord();
//        for(i=0;i<result.size();i++)
//        {
//            posLabel=(String)result.get(i);
//            word.setPOS(posLabel,getPOSIndex(posLabel,word));
//            word=word.next;
//        }
//    }
//
//    protected int getPOSIndex(String pos, Word word){
//        int posIndex;
//        if (word.isPunctuation()){
//            pos=word.getContent();
//            posIndex=0;
//        }
//
//        else if (pos.startsWith("noun"))
//            posIndex = Tagger.POS_NOUN;
//        else if (pos.startsWith("verb"))
//            posIndex = Tagger.POS_VERB;
//        else if (pos.startsWith("adj"))
//            posIndex = Tagger.POS_ADJECTIVE;
//        else if (pos.startsWith("adv"))
//            posIndex = Tagger.POS_ADVERB;
//        else if (pos.startsWith("conj"))
//            posIndex = Tagger.POS_CC;
//        else if (pos.startsWith("det"))
//            posIndex = Tagger.POS_DT;
//        else if (pos.startsWith("pron"))
//            posIndex = Tagger.POS_PRONOUN;
//        else if (pos.startsWith("prep"))
//            posIndex = Tagger.POS_IN;
//        else if (pos.startsWith("num"))
//            posIndex = Tagger.POS_NUM;
//        else if (pos.startsWith("aux"))
//            posIndex = Tagger.POS_VERB;
//        else if (pos.startsWith("model"))
//            posIndex = Tagger.POS_VERB;
//        else if (pos.startsWith("compl"))
//            posIndex = Tagger.POS_DT;
//        else
//            posIndex = 0;
//        if(posIndex>0 && posIndex!=Tagger.POS_NOUN && word.isAllCapital()){
//            posIndex=Tagger.POS_NOUN;
//        }
//        return posIndex;
//    }
}