package dragon.nlp.extract;

import dragon.nlp.*;

import java.util.ArrayList;

/**
 * <p>Document Parser for English Text </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class EngDocumentParser implements DocumentParser{
    public static final String defParaDelimitor="\n\n";
    public static final String defSentDelimitor=".;?!";
    public static final String defWordDelimitor=" \r\n\t_-.;,?/\"'`:(){}!+[]><=%$#*@&^~|\\";
    public static final String punctuations="\r\n\t_-.;,?/\"'`:(){}!+[]><=%$#*@&^~|\\";
    protected String wordDelimitor, paraDelimitor, sentDelimitor;

    public EngDocumentParser(){
        this(null);
    }

    public EngDocumentParser(String wordDelimitor) {
        this.paraDelimitor =defParaDelimitor;
        this.sentDelimitor =defSentDelimitor;
        if(wordDelimitor==null)
            this.wordDelimitor=defWordDelimitor;
        else
            this.wordDelimitor =wordDelimitor;
    }

    public Document parse(String doc){
        Document newDoc;
        int start,end;

        if(doc==null || doc.length()==0)
            return null;

        newDoc=new Document();
        doc=doc.replaceAll("\r\n","\n");
        doc=doc.replace('\r','\n');
        try {
            start = 0;
            while (start<doc.length()-1) {
                end=doc.indexOf(paraDelimitor,start);
                if(end>0)
                {
                    if(end>start)
                        newDoc.addParagraph(parseParagraph(doc.substring(start,end)));
                    start=end+2;
                }
                else
                {
                    newDoc.addParagraph(parseParagraph(doc.substring(start)));
                    start=doc.length() ;
                }
            }
            return newDoc;
        }
        catch (Exception e)
        {
        	e.printStackTrace();
            return null;
        }
    }

    public Paragraph parseParagraph(String paragraph){
        Paragraph newPara;
        String sent;
        int i=0,j=0,start=0,len,min;

        if(paragraph==null || paragraph.length()==0)
            return null;

        newPara=new Paragraph();
        if(sentDelimitor.indexOf('.')<0)
            sentDelimitor="."+sentDelimitor;
        paragraph=paragraph.replace('\n',' ');
        paragraph=paragraph.trim();
        len=paragraph.length();
        if(len==0)
        	return null;
        
        if(sentDelimitor.indexOf(paragraph.charAt(len-1))<0)
        {
            paragraph=paragraph+".";
            len=len+1;
        }

        while(start<len)
        {
            min=-1;
            j=start;
            while(min==-1 && j<len)
            {
                //find period
                min = paragraph.indexOf('.',j);
                if(min>=0)
                {
                    j=min+1;
                    if(!isSentencePeriod(min,paragraph))
                        min=-1;
                }
                else j=len;
            }

            for(i=0;i<sentDelimitor.length();i++)
            {
                j=paragraph.indexOf(sentDelimitor.charAt(i),start);
                if(j>=0 && sentDelimitor.charAt(i)!='.')
                    if(min<0 || j<min) min=j;
            }

            if(min>0)
            {
                sent=paragraph.substring(start,min+1);
                newPara.addSentence(parseSentence(sent));
                start=min+1;
            }
            else{
                sent=paragraph.substring(start)+".";
                newPara.addSentence(parseSentence(sent));
                start = len;
            }
        }
        return newPara;
    }

    public Sentence parseSentence(String sentence){
        Sentence newSent;
        char ch;
        boolean checkPeriod, checkApostrophes;
        int i,ret,len,flag, start;

        if(sentence==null || sentence.length()==0) return null;

        flag=0;
        start=0;
        newSent=new Sentence();
        checkPeriod=(wordDelimitor.indexOf('.')<0);
        checkApostrophes=(wordDelimitor.indexOf('\'')<0);
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
            //flag=0 initial value
            //flag=1 the previous character is space
            //flag=2 the previous is a punctuation
            //flag=3 the previous is a character which can make a word

            ch=sentence.charAt(i);
            if(checkPeriod && ch=='.'){
                //if . is not a part of a word.
                if(!isPeriodAsWord(i,start,sentence)){
                    if(flag>=2)
                        newSent.addWord(parseWord(sentence.substring(start, i)));
                    flag=2;
                    start=i;
                    continue;
                }
            }
            else if(checkApostrophes && ch=='\''){
            	//if ' is not a part of a word.
            	ret=isApostrophesAsWord(i,start,sentence);
                if(ret<2){
                    if(flag>=2)
                        newSent.addWord(parseWord(sentence.substring(start, i)));
                    flag=2; //ret==0 punctuation ret==1 word 's
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
        if(isNumber(content))
            cur.setType(Word.TYPE_NUMBER);
        else if(content.length()==1 && punctuations.indexOf(content)>=0)
            cur.setType(Word.TYPE_PUNC);
        return cur;
    }

    public ArrayList parseTokens(String content){
        ArrayList tokenList;
        int i,ret,len,flag,start;
        boolean checkPeriod, checkApostrophes;
        char ch;

        if(content==null)
            return null;
        if((content=content.trim()).length()==0)
            return null;
        len=content.length();
        flag=0;
        start=0;
        tokenList=new ArrayList();
        checkPeriod=(wordDelimitor.indexOf('.')<0);
        checkApostrophes=(wordDelimitor.indexOf('\'')<0);

        for(i=0;i<len;i++)
        {
            //flag=0 initial value
            //flag=1 the previous is a word delimitor including space, punctuation and return
            //flag=2 the previous is a character which can make a word
            ch=content.charAt(i);
            if(checkPeriod && ch=='.'){
                //if . is not a part of a token.
                if(!isPeriodAsToken(i,start,content)){
                    if(flag>=2)
                        tokenList.add(content.substring(start, i));
                    flag=1;//ret==0 punctuation ret==1 word 's
                    continue;
                }
            }
            else if(checkApostrophes && ch=='\''){
            	//if ' is not a part of a token.
            	ret=isApostrophesAsWord(i,start,content);
                if(ret<2){
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

    protected boolean isPeriodAsWord(int periodPos, int startPos, String context){
        int len;
        char ch;

        len = context.length();
        if(periodPos==startPos || ! Character.isLetter(context.charAt(periodPos-1)) || context.charAt(periodPos-1)>255 )
            return false;

        if(periodPos-2>0 && context.charAt(periodPos-2)=='.')
            return true;

        if(periodPos==startPos+1)
        	return true;
        
        if (periodPos - startPos >=4)
            return false;

        if (periodPos < len - 2){
            ch=context.charAt(periodPos + 1);
            if(ch=='\r' || ch=='\n')
                return false;
        }
        else if(periodPos==len-1)
            return false;
        return true;
    }

    protected boolean isPeriodAsToken(int periodPos, int startPos, String context){
        int len;
        len = context.length();

        if(periodPos==startPos || ! Character.isLetter(context.charAt(periodPos-1)) || context.charAt(periodPos-1)>255)
            return false;

        if(periodPos>2 && context.charAt(periodPos-2)=='.')
            return true;

        if (periodPos - startPos >=4)
            return false;

        if (periodPos < len - 1){
            if(!Character.isLetter(context.charAt(periodPos+1)))
                return false;
        }
        else
            return false;
        return true;
    }
    
    protected int isApostrophesAsWord(int apoPos, int startPos, String context){
        char ch;

        if(apoPos==00 || context.charAt(apoPos-1)==' '|| apoPos==context.length()-1)
        	return 0;
        ch=context.charAt(apoPos+1);
        if(ch==' ' || !Character.isLetter(ch))
        	return 0;
        else if(ch=='s' && apoPos+2<context.length() && context.charAt(apoPos+2)==' ')
        	return 1;
        return 2;
    }

    protected boolean isSentencePeriod(int pos, String context){
        int len, start;

        if(pos==0)
            return false;
        else if(context.charAt(pos-1)>255) //such as Chinese character
            return true;

        len=context.length();
        if (pos< len - 1 && context.charAt(pos + 1) != ' ')
           return false;
        //example: George W. Bush
        if(Character.isUpperCase(context.charAt(pos-1)) && (pos==1 || Character.isWhitespace(context.charAt(pos-2))))
        	return false;  
        //example: U.S.
        if(pos-2>0 && context.charAt(pos-2)=='.')
            return false;
        if (pos < len - 2 && !isUpper(context.charAt(pos + 2)))
        {
            start=context.lastIndexOf(' ',pos);
            if(start>=0 && pos-start<=5 && isUpper(context.charAt(start+1)))
                return false;
        }
        return true;
    }

    private boolean isUpper(char ch){
        if (ch < 97 || ch > 122)
            return true;
        else
            return false;
    }

    protected boolean isNumber(String str)
    {
        try {
            Double.parseDouble(str);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
}