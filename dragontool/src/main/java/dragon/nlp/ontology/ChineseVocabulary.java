package dragon.nlp.ontology;

import dragon.nlp.Word;
import dragon.util.FileUtil;
import dragon.util.SortedArray;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;

/**
 * <p>Chinese Vocabulary</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class ChineseVocabulary implements Vocabulary{
    private SortedArray list;
    private String[] arrPhrase;

    public ChineseVocabulary(String vobFile) {
        load(vobFile);
    }

    public int getPhraseIndex(long phraseUID){
        int pos;

        pos=list.binarySearch(new ChinesePhrase(phraseUID,-1));
        if(pos<0)
            return -1;
        else
            return ((ChinesePhrase)list.get(pos)).getIndex();
    }

    public boolean isPhrase(String term){
        return list.binarySearch(new ChinesePhrase(getUID(term),-1))>=0;
    }

    public boolean isPhrase(Word start, Word end){
        return false;
    }

    public boolean isStartingWord(Word cur){
        return true;
    }

    public Word findPhrase(Word start){
        return null;
    }


    public int getPhraseNum(){
        return arrPhrase.length;
    }

    public String getPhrase(int index){
        return arrPhrase[index];
    }


    public int maxPhraseLength(){
        return 4;
    }

    public int minPhraseLength(){
        return 2;
    }

    public void setAdjectivePhraseOption(boolean enabled){

    }

    public boolean getAdjectivePhraseOption(){
        return false;
    }

    public void setNPPOption(boolean enabled){

    }

    public boolean getNPPOption(){
        return false;
    }

    public void setCoordinateOption(boolean enabled){

    }

    public boolean getCoordinateOption(){
        return false;
    }

    public void setLemmaOption(boolean enabled){

    }

    public boolean getLemmaOption(){
        return false;
    }

    public long getUID(String phrase){
        long uid;
        int i;

        if( phrase==null || phrase.length()>4 || phrase.length()==0)
            return -1;
        uid=phrase.charAt(0);
        i=1;
        while(i<phrase.length()){
            uid=(uid<<16)+phrase.charAt(i);
            i++;
        }
        return uid;
    }

    private void load(String vobFile){
        ArrayList phraseList;
        BufferedReader br;
        String line, arrField[];
        int i, total;

        try{
            br=FileUtil.getTextReader(vobFile,"GBK");
            line=br.readLine();
            arrField=line.split("\t");
            total=Integer.parseInt(arrField[0]);
            arrPhrase=new String[total];
            phraseList=new ArrayList(total);

            for(i=0;i<total;i++){
                line=br.readLine();
                arrField=line.split("\t");
                arrPhrase[i]=arrField[0];
                if(arrPhrase[i].length()>4)
                    continue;
                phraseList.add(new ChinesePhrase(getUID(arrPhrase[i]),i));
            }
            Collections.sort(phraseList);
            list=new SortedArray(total);
            list.addAll(phraseList);
            phraseList.clear();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private class ChinesePhrase implements Comparable{
        private long id;
        private int index;

        public ChinesePhrase(long id, int index){
            this.id=id;
            this.index =index;
        }

        public int compareTo(Object obj){
            long objId;

            objId=((ChinesePhrase)obj).getID();
            if(id>objId)
                return 1;
            else if(id<objId)
                return -1;
            else
                return 0;
        }

        public long getID(){
            return id;
        }

        public int getIndex(){
            return index;
        }
    }

}