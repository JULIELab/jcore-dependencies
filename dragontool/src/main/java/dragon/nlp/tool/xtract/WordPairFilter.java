package dragon.nlp.tool.xtract;

import dragon.util.FileUtil;

import java.io.BufferedReader;
import java.util.ArrayList;

/**
 * <p>Filtering word pairs that is not satisfied with Xtract statistics</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class WordPairFilter {
    private String workDir;
    private int maxSpan;
    private double minStrength;
    private double minSpread;
    private double minZScore;

    public WordPairFilter(String workDir,int maxSpan, double minStrength, double minSpread, double minZScore) {
        this.minStrength=minStrength;
        this.minSpread=minSpread;
        this.minZScore=minZScore;
        this.workDir=workDir;
        this.maxSpan =maxSpan;
    }

    public WordPairStat[] execute(){
        WordPairStatList list;
        int wordNum;
        double[][] arrWordStat;

        list=new WordPairStatList(workDir+"/pairstat.list",maxSpan, false);
        wordNum=readWordNum();
        arrWordStat=computeWordStat(wordNum,list);
        return filterWordPair(arrWordStat,list);
    }

    private double[][] computeWordStat(int wordNum, WordPairStatList list){
        WordPairStat curPair;
        double[][] arrWordStat;
        int i,j;

        System.out.println((new java.util.Date()).toString()+" Computing Word Stat...");

        arrWordStat=new double[wordNum][3]; //0:average, 1: standard deviation, 2: number of samples
        for(i=0;i<wordNum;i++)
            for(j=0;j<3;j++)
                arrWordStat[i][j]=0;

        for(i=0;i<list.size();i++){
            curPair=list.get(i);
            arrWordStat[curPair.getFirstWord()][0]+=curPair.getTotalFrequency();
            arrWordStat[curPair.getFirstWord()][1]+=curPair.getTotalFrequency()*curPair.getTotalFrequency();
            arrWordStat[curPair.getFirstWord()][2]+=1;

            arrWordStat[curPair.getSecondWord()][0]+=curPair.getTotalFrequency();
            arrWordStat[curPair.getSecondWord()][1]+=curPair.getTotalFrequency()*curPair.getTotalFrequency();
            arrWordStat[curPair.getSecondWord()][2]+=1;
        }

        for(i=0;i<wordNum;i++){
            if(arrWordStat[i][2]>0){
                arrWordStat[i][0] = arrWordStat[i][0] / arrWordStat[i][2];
                arrWordStat[i][1] = Math.sqrt(arrWordStat[i][1] / arrWordStat[i][2]-Math.pow(arrWordStat[i][0],2));
            }
        }
        return arrWordStat;
    }

    private WordPairStat[] filterWordPair(double[][] arrWordStat, WordPairStatList list){
        ArrayList selectedList;
        WordPairStat curPair, filteredPair, arrSelected[];
        double strength;
        int i;

        selectedList=new ArrayList();
        for(i=0;i<list.size();i++){
            if(i%10000==0) System.out.println((new java.util.Date()).toString()+" processed: "+i);
            curPair=list.get(i);
            if(arrWordStat[curPair.getFirstWord()][1]==0)
                strength=0;
            else
                strength=(curPair.getTotalFrequency()-arrWordStat[curPair.getFirstWord()][0])/arrWordStat[curPair.getFirstWord()][1];
            if(strength<minStrength){
                if(arrWordStat[curPair.getSecondWord()][1]==0)
                    strength=0;
                else
                    strength=(curPair.getTotalFrequency()-arrWordStat[curPair.getSecondWord()][0])/arrWordStat[curPair.getSecondWord()][1];
            }
            if(strength>=minStrength){
                filteredPair=filterWordPair(curPair);
                if(filteredPair!=null)
                    selectedList.add(filteredPair);
            }
        }

        arrSelected=new WordPairStat[selectedList.size()];
        for(i=0;i<arrSelected.length;i++)
            arrSelected[i]=(WordPairStat)selectedList.get(i);
        return arrSelected;
    }

    private WordPairStat filterWordPair(WordPairStat pair){
        double sum, mean, squareSum, spread;
        int i, freq;
        boolean found;

        sum=0;
        squareSum=0;
        for(i=1;i<=maxSpan;i++){
            freq=pair.getFrequency(i);
            sum+=freq;
            squareSum+=freq*freq;

            freq=pair.getFrequency(-i);
            sum+=freq;
            squareSum+=freq*freq;
        }
        mean=sum/2/maxSpan;
        spread=squareSum/2.0/maxSpan-mean*mean;
        if(spread<minSpread)
            return null;

        found=false;
        spread=Math.sqrt(spread);
        for(i=1;i<=maxSpan;i++){
            freq=pair.getFrequency(i);
            if((freq-mean)/spread>=minZScore)
                found=true;
            else
                pair.addFrequency(i,-freq);

            freq=pair.getFrequency(-i);
            if((freq-mean)/spread>=minZScore)
                found=true;
            else
                pair.addFrequency(-i,-freq);
        }

        if(found)
            return pair;
        else
            return null;
    }

    private int readWordNum(){
        BufferedReader br;
        int num;

        try{
            br = FileUtil.getTextReader(workDir + "/wordkey.list");
            num=Integer.parseInt(br.readLine());
            br.close();
            return num;
        }
        catch(Exception e){
            e.printStackTrace();
            return 0;
        }
    }
}