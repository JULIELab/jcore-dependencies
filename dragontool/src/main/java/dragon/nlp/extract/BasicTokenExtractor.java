package dragon.nlp.extract;

import dragon.nlp.*;
import dragon.nlp.tool.*;
import java.util.ArrayList;

/**
 * <p>Token extraction</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicTokenExtractor extends AbstractTokenExtractor{
    public BasicTokenExtractor(Lemmatiser lemmatiser) {
        super(lemmatiser);
    }

    public BasicTokenExtractor(Lemmatiser lemmatiser, String stoplistFile) {
        super(lemmatiser);
        setConceptFilter(new BasicConceptFilter(stoplistFile));
    }

    public ArrayList extractFromDoc(String content){
        ArrayList list;
        String value;
        int i;

        list=parser.parseTokens(content);
        conceptList=new ArrayList();
        if(list==null)
            return conceptList;
        for(i=0;i<list.size();i++){
            value=(String)list.get(i);
            addToken(value,conceptList);
        }
        list.clear();
        return conceptList;
    }

    public ArrayList extractFromSentence(Sentence sent){
        ArrayList tokenList;
        Word cur;

        tokenList=new ArrayList();
        cur=sent.getFirstWord();
        while(cur!=null){
            if(cur.getType()!=Word.TYPE_PUNC)
                addToken(new String(cur.getContent()), tokenList);
            cur=cur.next;
        }
        return tokenList;
    }

    private Token addToken(String value, ArrayList tokenList){
        Token token;

        if(lemmatiser!=null)
            value=lemmatiser.lemmatize(value);
        if(conceptFilter_enabled && !cf.keep(value)) return null;

        token=new Token(value);
        tokenList.add(token);
        return token;
    }
}