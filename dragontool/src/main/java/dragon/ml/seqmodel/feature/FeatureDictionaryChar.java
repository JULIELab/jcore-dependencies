package dragon.ml.seqmodel.feature;

import dragon.util.MathUtil;
import dragon.util.SortedArray;

import java.io.*;
import java.util.StringTokenizer;


/**
 * <p>Character based feature dictionary </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


public class FeatureDictionaryChar extends AbstractFeatureDictionary implements FeatureDictionary{
    private SortedArray dictionary;

    public FeatureDictionaryChar(int stateNum, int capacity) {
        super(stateNum);
        dictionary = new SortedArray(capacity);
        finalized=false;
    }

    public int getIndex(Object feature) {
        int pos;

        pos=dictionary.binarySearch(new HEntry((String)feature,-1));
        if(pos<0)
            return -1;
        else
            return ((HEntry) (dictionary.get(pos))).getIndex();
    }

    public int getStateNum(){
        return stateNum;
    }

    public boolean contain(Object feature) {
        return dictionary.contains(feature);
    }

    public int size() {
        return dictionary.size();
    }

    public int addFeature(Object feature, int label) {
        HEntry index;

        if(label<0 || finalized)
            return -1;

        index=new HEntry((String)feature,dictionary.size(), stateNum);
        if(!dictionary.add(index))
            index= (HEntry) dictionary.get(dictionary.insertedPos());
        index.addFrequency(label,1);
        return index.getIndex();
    }

    public void finalize() {
        HEntry entry;
        int i;

        cntsOverAllFeature = new int[stateNum];
        cntsArray = new int[dictionary.size()][];
        cntsOverAllState=new int[dictionary.size()];
        for (i=0;i<dictionary.size();i++ ) {
            entry = (HEntry) dictionary.get(i);
            cntsArray[entry.index] = entry.stateArray;
            cntsOverAllState[entry.index]=MathUtil.sumArray(entry.stateArray);
        }

        for (i = 0; i < stateNum; i++) {
            cntsOverAllFeature[i] = 0;
            for (int m = 0; m < cntsArray.length; m++) {
                cntsOverAllFeature[i] += cntsArray[m][i];
            }
            allTotal += cntsOverAllFeature[i];
        }
        finalized=true;
    }

    public boolean read(String filename) {
        BufferedReader in;
        HEntry hEntry;
        StringTokenizer entry, scp;
        String line, key;
        int dictLen, pos, l,i, state, cnt;

        try{
            in=new BufferedReader(new FileReader(new File(filename)));
            dictLen = Integer.parseInt(in.readLine());
            cntsOverAllFeature = new int[stateNum];
            cntsArray = new int[dictLen][stateNum];
            cntsOverAllState=new int[dictLen];
            for (l = 0; (l < dictLen) && ( (line = in.readLine()) != null); l++) {
                entry = new StringTokenizer(line, " ");
                key = entry.nextToken();
                pos = Integer.parseInt(entry.nextToken());
                hEntry = new HEntry(key, pos);
                dictionary.add(hEntry);
                while (entry.hasMoreTokens()) {
                    scp = new StringTokenizer(entry.nextToken(), ":");
                    state = Integer.parseInt(scp.nextToken());
                    cnt = Integer.parseInt(scp.nextToken());
                    cntsArray[pos][state] = cnt;
                }
                cntsOverAllState[pos]=MathUtil.sumArray(cntsArray[pos]);
            }
            for (i = 0; i < stateNum; i++) {
                cntsOverAllFeature[i] = 0;
                for (int m = 0; m < cntsArray.length; m++) {
                    cntsOverAllFeature[i] += cntsArray[m][i];
                }
                allTotal += cntsOverAllFeature[i];
            }
            finalized=true;
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean write(String filename){
        PrintWriter out;
        HEntry entry;
        String key;
        int i, pos,s;

        try{
            out=new PrintWriter(new FileOutputStream(new File(filename)));
            out.println(dictionary.size());
            for (i=0;i<dictionary.size();i++) {
                entry=(HEntry)dictionary.get(i);
                key = entry.getFeature();
                pos = entry.getIndex();
                out.print(key + " " + pos);
                for (s = getNextStateWithFeature(pos, -1); s != -1; s = getNextStateWithFeature(pos, s)) {
                    out.print(" " + s + ":" + cntsArray[pos][s]);
                }
                out.println("");
            }
            out.close();
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private class HEntry implements Comparable{
        private String feature;
        private int index;
        private int stateArray[];

        public HEntry(String feature, int v) {
            this.feature =feature;
            index = v;
            stateArray=null;
        }

        public HEntry(String feature, int v, int numStates) {
            this.feature =feature;
            index = v;
            stateArray = new int[numStates];
        }

        public void addFrequency(int state, int inc){
            stateArray[state]+=inc;
        }

        public int getIndex(){
            return index;
        }

        public String getFeature(){
            return feature;
        }

        public int compareTo(Object o){
            return feature.compareTo(((HEntry)o).getFeature());
        }
    };
};
