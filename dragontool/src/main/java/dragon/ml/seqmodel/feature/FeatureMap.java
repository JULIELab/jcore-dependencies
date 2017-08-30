package dragon.ml.seqmodel.feature;

import java.io.*;
import java.util.*;

/**
 * <p>Feature map</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class FeatureMap {
    private Hashtable strToInt;
    private String idToName[];
    /*if the variable frozen is true, no new identifier will be added. In testing mode, after feature
      identifiers are read, it will be set to true. In training mode, after all feature identifiers
      are collected, it will be set to true.
    */
    private boolean frozen;

    public FeatureMap() {
        frozen=false;
        strToInt=new Hashtable();
    }

    public boolean isFrozen(){
        return frozen;
    }

    public int getId(FeatureIdentifier key) {
        Integer id;

        id=(Integer)strToInt.get(key);
        if (id!= null) {
            return id.intValue();
        }
        else
            return -1;
    }

    public int add(FeatureIdentifier id) {
        int newId;

        if(frozen)
            return -1;

        newId = strToInt.size();
        strToInt.put(id.copy(), new Integer(newId));
        return newId;
    }

    public void freezeFeatures() {
        FeatureIdentifier key;

        idToName = new String[strToInt.size()];
        for (Enumeration e = strToInt.keys(); e.hasMoreElements(); ) {
            key = (FeatureIdentifier)e.nextElement();
            idToName[getId(key)] = key.toString();
        }
        frozen=true;
    }

    public int getFeatureNum(){
        return strToInt.size();
    }

    public void write(PrintWriter out) throws IOException {
        FeatureIdentifier key;

        out.println(strToInt.size());
        for (Enumeration e = strToInt.keys(); e.hasMoreElements(); ) {
            key = (FeatureIdentifier)e.nextElement();
            out.println(key.toString() + " " + ( (Integer) strToInt.get(key)).intValue());
        }
    }

    public int read(BufferedReader in) throws IOException {
        StringTokenizer entry;
        FeatureIdentifier key;
        String line;
        int len, l, pos;

        len = Integer.parseInt(in.readLine());
        for (l = 0; (l < len) && ( (line = in.readLine()) != null); l++) {
            entry = new StringTokenizer(line, " ");
            key = new FeatureIdentifier(entry.nextToken());
            pos = Integer.parseInt(entry.nextToken());
            strToInt.put(key, new Integer(pos));
        }
        freezeFeatures();
        return strToInt.size();
    }

    public String getIdentifier(int id) {
        return idToName[id];
    }

    public String getName(int id) {
        return idToName[id];
    }
};
