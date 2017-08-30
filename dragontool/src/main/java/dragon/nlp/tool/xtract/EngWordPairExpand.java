package dragon.nlp.tool.xtract;

import dragon.nlp.*;
import dragon.util.*;
import dragon.nlp.tool.*;
import dragon.matrix.*;
import dragon.nlp.compare.*;
import java.util.*;

/**
 * <p>Expanding word pair (not necessary consecutive) to noun phrase</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class EngWordPairExpand implements WordPairExpand{
    protected IntSparseMatrix sentMatrix;
    protected SimpleElementList wordList;
    protected int maxSpan;
    protected String indexFolder;
    protected IntSparseMatrix[] arrPairSentLeftMatrix, arrPairSentRightMatrix;
    protected double threshold;

    public EngWordPairExpand(int maxSpan, String indexFolder, double threshold) {
        this.maxSpan = maxSpan;
        this.threshold = threshold;
        this.indexFolder = indexFolder;

        wordList = new SimpleElementList(indexFolder + "/wordkey.list", false);
        arrPairSentRightMatrix = new IntSparseMatrix[maxSpan];
        sentMatrix = new IntSuperSparseMatrix(indexFolder + "/sentencebase.index",indexFolder + "/sentencebase.matrix");
        for (int i = 1; i <= maxSpan; i++) {
            arrPairSentRightMatrix[i - 1] = new IntGiantSparseMatrix(indexFolder + "/pairsentr" + i +".index",
                                              indexFolder + "/pairsentr" + i +".matrix");
        }
        arrPairSentLeftMatrix = new IntSparseMatrix[maxSpan];
        for (int i = 1; i <= maxSpan; i++) {
            arrPairSentLeftMatrix[i -1] = new IntGiantSparseMatrix(indexFolder + "/pairsentl" + i + ".index",
                                              indexFolder + "/pairsentl" + i + ".matrix");
        }
    }

    public ArrayList expand(WordPairStat wordPairStat, int span) {
        ArrayList sentList, phraseList;
        Token token;
        String expandStr;
        int sentNum, firstWord, secondWord;
        boolean pass;

        try{
            firstWord = wordPairStat.getFirstWord();
            secondWord = wordPairStat.getSecondWord();
            expandStr = null;

            //return all sentences containing the word pair and the position of the first word in the corresponding sentence
            sentList =getSentenceList(wordPairStat,span);
            sentNum = sentList.size();

            //expand middle
            pass = true;
            if (span > 1 || span < -1) {
                if (span > 1)
                    token = expandSecion(1, span - 1, sentNum, false, 0, sentList);
                else
                    token = expandSecion(1, -span - 1, sentNum, true, 0, sentList);
                if (token == null)
                    pass = false;
                else {
                    pass = true;
                    sentList = (ArrayList) token.getMemo();
                    if (span > 1)
                        expandStr = getWordContent(firstWord) + " " + token.getName().trim() + " " +getWordContent(secondWord);
                    else
                        expandStr = getWordContent(secondWord) + " " + token.getName().trim() + " " +getWordContent(firstWord);
                }
            }
            else {
                if (span == 1)
                    expandStr = (getWordContent(firstWord)+ " " + getWordContent(secondWord)).trim();
                else
                    expandStr = (getWordContent(secondWord).trim() + " " + getWordContent(firstWord)).trim();
            }

            if (!pass)
                return null;

            //expand left
            if (span > 0)
                token = expandSecion(1, maxSpan, sentNum, true, -1, sentList);
            else
                token = expandSecion( -span + 1, maxSpan - span, sentNum, true, -1, sentList);
            if (token != null) {
                sentList = (ArrayList) token.getMemo();
                expandStr = token.getName().trim() + " " + expandStr;
            }

            //expand right
            if (span > 0)
                token = expandSecion(span + 1, span + maxSpan, sentNum, false, 1, sentList);
            else
                token = expandSecion(1, maxSpan, sentNum, false, 1, sentList);
            if (token != null) {
                sentList = (ArrayList) token.getMemo();
                expandStr = expandStr + " " + token.getName().trim();
            }

            phraseList=new ArrayList(1);
            phraseList.add(new Token(expandStr.trim()));
            return phraseList;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    protected ArrayList getSentenceList(WordPairStat wordPairStat, int span){
        IntSparseMatrix pairSentMatrix;
        ArrayList sentList;
        Token sentToken;
        int i, j, firstWord, secondWord, wordKey, sentIndex, sentLength, sentNum, pairIndex;

        pairIndex = wordPairStat.getIndex();
        firstWord = wordPairStat.getFirstWord();
        secondWord = wordPairStat.getSecondWord();

        if (span < 0)
            pairSentMatrix = arrPairSentLeftMatrix[ -span - 1];
        else
            pairSentMatrix = arrPairSentRightMatrix[span - 1];
        sentNum = pairSentMatrix.getNonZeroNumInRow(pairIndex);
        sentList = new ArrayList(sentNum);

        for (i = 0; i < sentNum; i++) {
            sentIndex = pairSentMatrix.getNonZeroColumnInRow(pairIndex, i);
            sentLength = sentMatrix.getNonZeroNumInRow(sentIndex);
            for (j = 0; j < sentLength; j++) {
                wordKey = sentMatrix.getNonZeroColumnInRow(sentIndex, j);
                if (wordKey != firstWord)
                    continue;
                if ( (j + span) >= 0 && (j + span) <sentLength &&
                    sentMatrix.getNonZeroColumnInRow(sentIndex, j + span) == secondWord) {
                    sentToken = new Token(String.valueOf( sentIndex));
                    sentToken.setIndex(j);
                    sentToken.setFrequency(sentMatrix.getNonZeroIntScoreInRow(sentIndex,j+span));
                    sentList.add(sentToken);
                }
                break;
            }
        }
        return sentList;
    }

    //direction 0:middle -1:left, 1:right
    protected Token expandSecion(int start, int end, int sentNum, boolean inverse, int direction, ArrayList sentList) {
        Token token;
        String expandStr, word, marginalWord;
        int posIndex, marginalPOS;
        int i, j, pos;

        expandStr="";
        marginalWord=null;
        marginalPOS=-1;

        for (i=start; i<= end; i++) {
            if(inverse)
                j=-i;
            else
                j=i;
            token =checkSentPos(j, sentList);
            if (token!= null){
                sentList = (ArrayList) token.getMemo();
                if( (token.getFrequency() / (double) sentNum) >=threshold) {
                    //if the direction is not middle, check if the word is valid as a part of the phrase
                    word = (getWordContent(Integer.parseInt(token.getName())));
                    posIndex=token.getIndex();
                    if (direction==0 || checkValidation(word, posIndex)) {
                       if (inverse)
                            expandStr = word+" " + expandStr;
                        else
                            expandStr = expandStr + " " + word;
                        expandStr=expandStr.trim();
                        if(direction==1 && !inverse || direction==-1 && inverse){
                            marginalWord=word;
                            marginalPOS=posIndex;
                        }
                    }
                    else
                        break;
                }
                else
                    break;
            }
            else
                break;
        }

        if(i<=end && direction==0)
            return null;
        else if (!expandStr.equals("")) {
            if(direction==1 && !inverse && !checkEndingWordValidation(marginalWord,marginalPOS)){
                pos=expandStr.lastIndexOf(' ');
                if(pos>=0)
                    expandStr=expandStr.substring(0,pos);
                else
                    return null;
            }
            else if(direction==-1 && inverse && !checkStartingWordValidation(marginalWord,marginalPOS)){
                pos=expandStr.indexOf(' ');
                if(pos>=0)
                    expandStr=expandStr.substring(pos+1);
                else
                    return null;
            }
            token = new Token(expandStr);
            token.setMemo(sentList);
            return token;
        }
        else
            return null;
    }

    protected Token checkSentPos(int spanFromFirstWord, ArrayList sentList) {
        SortedArray tokenList;
        ArrayList sList;
        Token wordToken, sentToken;
        int i, sentIndex, sentLength, firstWordPos, wordKey, tokenIndex;

        tokenList = new SortedArray();

        for (i = 0; i < sentList.size(); i++) {
            sentToken = (Token) sentList.get(i);
            sentIndex = Integer.parseInt(sentToken.getName());
            sentLength = sentMatrix.getNonZeroNumInRow(sentIndex);
            firstWordPos = sentToken.getIndex();

            if ( (firstWordPos + spanFromFirstWord) >= 0 && (firstWordPos + spanFromFirstWord < sentLength)) {
                wordKey = sentMatrix.getNonZeroColumnInRow(sentIndex,firstWordPos + spanFromFirstWord);
                wordToken = new Token(String.valueOf(wordKey));
                tokenIndex = tokenList.binarySearch(wordToken);
                if (tokenIndex < 0) {
                    sList = new ArrayList();
                    sList.add(sentToken);
                    wordToken.setFrequency(1);
                    wordToken.setIndex(sentMatrix.getNonZeroIntScoreInRow(sentIndex,firstWordPos + spanFromFirstWord)); //the part of speech
                    wordToken.setMemo(sList);
                    tokenList.add(wordToken);
                }
                else {
                    wordToken = (Token) tokenList.get(tokenIndex);
                    wordToken.addFrequency(1);
                    sList = (ArrayList) wordToken.getMemo();
                    sList.add(sentToken);
                }
            }
        }

        if(tokenList.size()>0){
            tokenList.setComparator(new FrequencyComparator(true));
            wordToken = (Token) tokenList.get(0);
            tokenList.clear();
            return wordToken;
        }
        else
            return null;
    }

    protected String getWordContent(int index){
        return wordList.search(index).trim();
    }

    protected boolean checkValidation(String word, int posIndex){
        if(posIndex==Tagger.POS_ADJECTIVE || posIndex==Tagger.POS_NOUN || posIndex==0 && word.equals("-"))
            return true;
        return false;
    }

    protected boolean checkEndingWordValidation(String word, int posIndex){
        if(posIndex==Tagger.POS_NOUN)
            return true;
        else
            return false;
    }

    protected boolean checkStartingWordValidation(String word, int posIndex){
        if(posIndex==Tagger.POS_NOUN || posIndex==Tagger.POS_ADJECTIVE)
            return true;
        else
            return false;
    }
}