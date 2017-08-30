package dragon.ir.index;

import dragon.nlp.compare.IndexComparator;
import dragon.util.*;
import java.io.*;

/**
 * <p>The class is used to write or load the relation indexing information for a given IR relation or relation set </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicIRRelationIndexList implements IRRelationIndexList, IRSignatureIndexList{
    private RandomAccessFile raf;
    private SortedArray indexList;
    private int elementLength;
    private boolean writingMode;
    private String indexlistFilename;
    private int relationNum;
    private int maxOldIndex, maxIndex, maxCacheSize;
    private byte[] buf;

    public BasicIRRelationIndexList(String filename, boolean writingMode) {

        try {
            this.elementLength = 20;
            this.buf = new byte[elementLength];
            this.writingMode = writingMode;
            this.indexlistFilename = filename;
            this.maxCacheSize = 200000;
            if (writingMode) {
                raf = new RandomAccessFile(filename, "rw");
                if (raf.length() <4) {
                    raf.writeInt(0);
                    maxOldIndex = -1;
                }
                else
                    maxOldIndex = raf.readInt() - 1;
                maxIndex = maxOldIndex;
                raf.close();
                raf = null;
                indexList = new SortedArray(new IndexComparator());
            }
            else {
                if (FileUtil.exist(filename)) {
                    raf = new RandomAccessFile(filename, "r");
                    if (raf.length() > 0)
                        relationNum = raf.readInt();
                    else
                        relationNum = 0;
                }
                else
                    relationNum=0;
                maxIndex=relationNum-1;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCacheSize(int size) {
        maxCacheSize = size;
    }

    public int getCacheSize() {
        return maxCacheSize;
    }

    public IRSignature getIRSignature(int index){
        return get(index);
    }

    public IRRelation get(int index) {
        try {
            if (writingMode || index >= relationNum)
                return null;

            raf.seek(index * elementLength + 4);
            raf.read(buf);
            return getIRRelationFromByteArray(buf);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean add(IRRelation curRelation) {
        IRRelation oldRelation;

        if (!writingMode)
            return false;

        if (!indexList.add(curRelation.copy())) {
            oldRelation = (IRRelation) indexList.get(indexList.insertedPos());
            oldRelation.addFrequency(curRelation.getFrequency());
            oldRelation.setDocFrequency(oldRelation.getDocFrequency() + curRelation.getDocFrequency());
        }
        else {
            if (curRelation.getIndex() > maxIndex)
                maxIndex = curRelation.getIndex();
            if (indexList.size() > maxCacheSize)
                saveRelationIndexList(indexlistFilename, indexList);
        }
        return true;
    }

    public int size() {
        return maxIndex + 1;
    }

    public void close() {
        try {
            if (writingMode) {
                saveRelationIndexList(indexlistFilename, indexList);
                indexList.clear();
            }
            else {
                if(raf!=null)
                    raf.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveRelationIndexList(String filename, SortedArray list) {
        FastBinaryReader fbr;
        RandomAccessFile rafRelation;
        IRRelation cur;
        int cutoff, lastIndex, i, j;

        try {
            if (list == null || list.size() == 0)
                return;
            System.out.println(new java.util.Date() + " Saving Relation Index List...");

            //merge the counts with old relations
            fbr = new FastBinaryReader(filename);
            fbr.skip(4);
            lastIndex = 0;
            for (i = 0; i < list.size(); i++) {
                cur = (IRRelation) list.get(i);
                if (cur.getIndex() > maxOldIndex)
                    break;
                fbr.skip( (cur.getIndex() - lastIndex) * elementLength + 12);
                cur.addFrequency(fbr.readInt());
                cur.setDocFrequency(cur.getDocFrequency() + fbr.readInt());
                lastIndex = cur.getIndex() + 1;
            }
            cutoff = i - 1;
            fbr.close();

            //overwrite the old relations
            rafRelation = new RandomAccessFile(filename, "rw");
            rafRelation.writeInt(maxIndex + 1);
            for (i = 0; i <= cutoff; i++) {
                cur = (IRRelation) list.get(i);
                rafRelation.seek(cur.getIndex() * elementLength + 4);
                writeToByteArray(cur, buf);
                rafRelation.write(buf);
            }

            //append the new relations
            lastIndex = maxOldIndex;
            rafRelation.seek((maxOldIndex+1)*elementLength+4);
            for (i = cutoff + 1; i < list.size(); i++) {
                cur = (IRRelation) list.get(i);
                for (j = lastIndex + 1; j < cur.getIndex(); j++) {
                    writeToByteArray(j, buf);
                    rafRelation.write(buf);
                }
                writeToByteArray(cur, buf);
                rafRelation.write(buf);
                lastIndex=cur.getIndex();
            }
            rafRelation.close();
            maxOldIndex = maxIndex;
            list.clear();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void writeToByteArray(IRRelation cur, byte[] array) {
        ByteArrayConvert.toByte(cur.getIndex(), array, 0);
        ByteArrayConvert.toByte(cur.getFirstTerm(), array, 4);
        ByteArrayConvert.toByte(cur.getSecondTerm(), array, 8);
        ByteArrayConvert.toByte(cur.getFrequency(), array, 12);
        ByteArrayConvert.toByte(cur.getDocFrequency(), array, 16);
    }

    private void writeToByteArray(int index, byte[] array) {
        ByteArrayConvert.toByte(index, array, 0);
        ByteArrayConvert.toByte( -1, array, 4);
        ByteArrayConvert.toByte( -1, array, 8);
        ByteArrayConvert.toByte(0, array, 12);
        ByteArrayConvert.toByte(0, array, 16);
    }

    private IRRelation getIRRelationFromByteArray(byte[] array) {
        int relationIndex, first, second, frequency, docFrequency;

        relationIndex = ByteArrayConvert.toInt(array, 0);
        first = ByteArrayConvert.toInt(array, 4);
        second = ByteArrayConvert.toInt(array, 8);
        frequency = ByteArrayConvert.toInt(array, 12);
        docFrequency = ByteArrayConvert.toInt(array, 16);
        return new IRRelation(relationIndex, first, second, frequency, docFrequency);
    }
}