package dragon.ml.seqmodel.feature;

import dragon.util.MathUtil;
import java.io.*;
import java.util.*;


/**
 * <p>Numarical feature dictionary </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


public class FeatureDictionaryNum extends AbstractFeatureDictionary implements FeatureDictionary{
    private Hashtable dictionary;

    public FeatureDictionaryNum(int stateNum, int capacity) {
        super(stateNum);
        dictionary = new Hashtable(capacity);
        finalized=false;
    }

    public int getIndex(Object feature) {
        HEntry entry;

        entry=(HEntry) (dictionary.get(feature));
        if(entry==null)
            return -1;
        else
            return entry.getIndex();
    }

    public boolean contain(Object feature) {
        return (dictionary.get(feature) != null);
    }

    public int size() {
        return dictionary.size();
    }

    public int addFeature(Object feature, int label) {
        HEntry index;

        if(label<0 || finalized) return -1;

        index = (HEntry) dictionary.get(feature);
        if (index == null) {
            index = new HEntry(dictionary.size(), stateNum);
            dictionary.put(feature, index);
        }
        index.addFrequency(label,1);
        return index.getIndex();
    }

    public void finalize() {
        Enumeration e;
        Integer key;
        HEntry entry;
        int i;

        cntsOverAllFeature = new int[stateNum];
        cntsArray = new int[dictionary.size()][];
        cntsOverAllState=new int[dictionary.size()];
        for (e = dictionary.keys(); e.hasMoreElements(); ) {
            key = (Integer)e.nextElement();
            entry = (HEntry) dictionary.get(key);
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
        String line;
        Integer key;
        int dictLen, pos, l,i, state, cnt;

        try{
            in=new BufferedReader(new FileReader(new File(filename)));
            dictLen = Integer.parseInt(in.readLine());
            cntsOverAllFeature = new int[stateNum];
            cntsArray = new int[dictLen][stateNum];
            cntsOverAllState=new int[dictLen];
            for (l = 0; (l < dictLen) && ( (line = in.readLine()) != null); l++) {
                entry = new StringTokenizer(line, " ");
                key = Integer.getInteger(entry.nextToken());
                pos = Integer.parseInt(entry.nextToken());
                hEntry = new HEntry(pos);
                dictionary.put(key, hEntry);
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
        Enumeration element;
        PrintWriter out;
        Integer key;
        int pos,s;

        try{
            out=new PrintWriter(new FileOutputStream(new File(filename)));
            out.println(dictionary.size());
            for (element= dictionary.keys(); element.hasMoreElements(); ) {
                key = (Integer) element.nextElement();
                pos = getIndex(key);
                out.print(key.toString() + " " + pos);
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

    private class HEntry {
        private int index;
        private int stateArray[];

        public HEntry(int v) {
            index = v;
            stateArray=null;
        }

        public HEntry(int v, int numStates) {
            index = v;
            stateArray = new int[numStates];
        }

        public void addFrequency(int state, int inc){
            stateArray[state]+=inc;
        }

        public int getIndex(){
            return index;
        }
    };
};
