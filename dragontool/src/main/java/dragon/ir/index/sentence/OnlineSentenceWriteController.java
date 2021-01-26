package dragon.ir.index.sentence;

import dragon.ir.index.OnlineIndexWriteController;
import dragon.nlp.Sentence;
/**
 * <p>Write controller for storing sentence index to disk </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class OnlineSentenceWriteController extends OnlineIndexWriteController{
    private OnlineSentenceBase sentBase;

    public OnlineSentenceWriteController(boolean relationSupported, boolean indexConceptEntry) {
        super(relationSupported,indexConceptEntry);
        sentBase=new OnlineSentenceBase();
    }

    public boolean addRawSentence(Sentence sent){
        if(curDocKey==null)
            return false;
        return sentBase.add(sent.toString(),curDocKey);
    }

    public OnlineSentenceBase getSentenceBase(){
        return sentBase;
    }
}