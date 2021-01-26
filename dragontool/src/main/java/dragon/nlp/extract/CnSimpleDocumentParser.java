package dragon.nlp.extract;

import dragon.nlp.Sentence;
import dragon.nlp.Word;

import java.util.ArrayList;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class CnSimpleDocumentParser extends EngDocumentParser{
    public static final String punctuations="\r\n\t_-.;,?/\"'`:(){}!+[]><=%$#*@&^~|";

    public CnSimpleDocumentParser() {
        this.sentDelimitor =sentDelimitor+"";
    }

    public Sentence parseSentence(String sentence){
        Sentence newSent;
        char ch;
        boolean checkPeriod;
        int i,len=0,flag=0,start=0;

        if(sentence==null || sentence.length()==0) return null;

        newSent=new Sentence();
        checkPeriod=(wordDelimitor.indexOf('.')<0);
        sentence=sentence.trim();
        len=sentence.length();
        if(len<=0) return null;
        if(sentDelimitor.indexOf(sentence.charAt(len-1))>=0)
            newSent.setPunctuation(sentence.charAt(len-1));
        else{
            sentence = sentence + ".";
            newSent.setPunctuation('.');
            len=len+1;
        }

        for(i=0;i<len-1;i++)
        {
            //flag=0 initial value or Chinese character
            //flag=1 the previous character is space
            //flag=2 the previous is a punctuation
            //flag=3 the previous is a character which can make a english word

            ch=sentence.charAt(i);
            if(ch>255){
                //Chinese character
                if (flag >= 2) {
                    newSent.addWord(parseWord(sentence.substring(start, i)));
                }
                newSent.addWord(parseWord(sentence.substring(i, i + 1)));
                flag = 0;
                continue;
            }

            if(checkPeriod && ch=='.'){
                //if . is not a part of a token.
                if(!isPeriodAsWord(i,start,sentence)){
                    if(flag>=2)
                        newSent.addWord(parseWord(sentence.substring(start, i)));
                    flag=2;
                    start=i;
                    continue;
                }
            }

            if(ch==' ')
            {
                if (flag>=2) //flag=2 or 3
                {
                    newSent.addWord(parseWord(sentence.substring(start, i)));
                }
                flag=1;
            }
            else if(wordDelimitor.indexOf(ch)>=0)
            {
                if(flag>=2)
                {
                    newSent.addWord(parseWord(sentence.substring(start, i)));
                }
                start=i;
                flag=2;

            }
            else
            {
                if(flag==2)
                {
                    newSent.addWord(parseWord(sentence.substring(start, i)));
                    start=i;
                }
                else if(flag==1 || flag==0)
                    start=i;
                flag=3;
            }
        }
        if(flag>=2 && len-1>start)
            newSent.addWord(parseWord(sentence.substring(start, len-1)));
        return newSent;

    }

    protected Word parseWord(String content){
        Word cur;

        cur=new Word(content);
        if(content.charAt(0)<255 && isNumber(content))
            cur.setType(Word.TYPE_NUMBER);
        else if(content.length()==1 && punctuations.indexOf(content)>=0)
            cur.setType(Word.TYPE_PUNC);
        return cur;
    }

    public ArrayList parseTokens(String content){
        ArrayList tokenList;
        String cnPunc;
        int i,len,flag,start;
        boolean checkPeriod;
        char ch;

        if(content==null)
            return null;
        if((content=content.trim()).length()==0)
            return null;
        cnPunc="";
        len=content.length();
        flag=0;
        start=0;
        tokenList=new ArrayList();
        checkPeriod=(wordDelimitor.indexOf('.')<0);

        for(i=0;i<len;i++)
        {
            //flag=0 initial value or Chinese character
            //flag=1 the previous is a word delimitor including space, punctuation and return
            //flag=2 the previous is a character which can make a word
            ch=content.charAt(i);

            if(ch>255){
                //Chinese character
                if (flag >= 2)
                    tokenList.add(content.substring(start, i));
                if (cnPunc.indexOf(ch) < 0)
                    tokenList.add(content.substring(i, i + 1));
                flag=0;
                continue;
            }

            if(checkPeriod && ch=='.'){
                //if . is not a part of a token.
                if(!isPeriodAsToken(i,start,content)){
                    if(flag>=2)
                        tokenList.add(content.substring(start, i));
                    flag=1;
                    continue;
                }
            }

            if(wordDelimitor.indexOf(ch)>=0)
            {
                if(flag>=2)
                    tokenList.add(content.substring(start, i));
                flag=1;
            }
            else
            {
                if(flag==1 || flag==0){
                    start = i;
                    flag = 2;
                }
            }
        }
        if(flag>=2)
            tokenList.add(content.substring(start, len));
        return tokenList;
    }

}