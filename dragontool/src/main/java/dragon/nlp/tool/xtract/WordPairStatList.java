package dragon.nlp.tool.xtract;

import dragon.nlp.compare.IndexComparator;
import dragon.util.ByteArrayConvert;
import dragon.util.FastBinaryReader;
import dragon.util.SortedArray;

import java.io.RandomAccessFile;

/**
 * <p>List of word pairs with statistical information </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class WordPairStatList {
    private RandomAccessFile raf;
    private SortedArray indexList;
    private int maxSpan;
    private int elementLength;
    private boolean writingMode;
    private String indexlistFilename;
    private int pairNum;
    private int maxOldIndex, maxIndex, maxCacheSize;
    private byte[] buf;

    public WordPairStatList(String filename, int maxSpan, boolean writingMode) {
        try {
            this.maxSpan =maxSpan;
            this.elementLength = 4*(2*maxSpan+3);
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
                raf = new RandomAccessFile(filename, "r");
                if (raf.length() > 0)
                    pairNum = raf.readInt();
                else
                    pairNum = 0;
                maxIndex=pairNum-1;
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

    public WordPairStat get(int index) {
        try {
            if (writingMode || index >= pairNum)
                return null;

            raf.seek(index * elementLength + 4);
            raf.read(buf);
            return getWordPairStatFromByteArray(buf);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean add(WordPairStat stat) {
        WordPairStat oldStat;
        int i;

        if (!writingMode)
            return false;

        if (!indexList.add(stat)) {
            oldStat = (WordPairStat) indexList.get(indexList.insertedPos());
            for(i=1;i<=maxSpan;i++)
                oldStat.addFrequency(i,stat.getFrequency(i));
            for(i=1;i<=maxSpan;i++)
                oldStat.addFrequency(-i,stat.getFrequency(-i));
        }
        else {
            if (stat.getIndex() > maxIndex)
                maxIndex = stat.getIndex();
            if (indexList.size() > maxCacheSize)
                saveIndexList(indexlistFilename, indexList);
        }
        return true;
    }

    public int size() {
        return maxIndex + 1;
    }

    public void close() {
        try {
            if (writingMode) {
                saveIndexList(indexlistFilename, indexList);
                indexList.clear();
            }
            else {
                raf.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveIndexList(String filename, SortedArray list) {
        FastBinaryReader fbr;
        RandomAccessFile rafPair;
        WordPairStat cur;
        int cutoff, lastIndex, i, j;

        try {
            if (list == null || list.size() == 0)
                return;
            System.out.println(new java.util.Date() + " Saving Word Pair List...");

            //merge the counts with old relations
            fbr = new FastBinaryReader(filename);
            fbr.skip(4);
            lastIndex = 0;
            for (i = 0; i < list.size(); i++) {
                cur = (WordPairStat) list.get(i);
                if (cur.getIndex() > maxOldIndex)
                    break;
                fbr.skip( (cur.getIndex() - lastIndex) * elementLength + 12);
                for(j=0;j<maxSpan;j++)
                    cur.addFrequency(j-maxSpan,fbr.readInt());
                for(j=1;j<=maxSpan;j++)
                    cur.addFrequency(j,fbr.readInt());
                lastIndex = cur.getIndex() + 1;
            }
            cutoff = i - 1;
            fbr.close();

            //overwrite the old relations
            rafPair = new RandomAccessFile(filename, "rw");
            rafPair.writeInt(maxIndex + 1);
            for (i = 0; i <= cutoff; i++) {
                cur = (WordPairStat) list.get(i);
                rafPair.seek(cur.getIndex() * elementLength + 4);
                writeToByteArray(cur, buf);
                rafPair.write(buf);
            }

            //append the new relations
            lastIndex = maxOldIndex;
            rafPair.seek((maxOldIndex+1)*elementLength+4);
            for (i = cutoff + 1; i < list.size(); i++) {
                cur = (WordPairStat) list.get(i);
                for (j = lastIndex + 1; j < cur.getIndex(); j++) {
                    writeToByteArray(j, buf);
                    rafPair.write(buf);
                }
                writeToByteArray(cur, buf);
                rafPair.write(buf);
                lastIndex=cur.getIndex();
            }
            rafPair.close();
            maxOldIndex = maxIndex;
            list.clear();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void writeToByteArray(WordPairStat cur, byte[] array) {
        int i;

        ByteArrayConvert.toByte(cur.getIndex(), array, 0);
        ByteArrayConvert.toByte(cur.getFirstWord(), array, 4);
        ByteArrayConvert.toByte(cur.getSecondWord(), array, 8);
        for(i=0;i<maxSpan;i++)
            ByteArrayConvert.toByte(cur.getFrequency(i-maxSpan), array, i*4+12);
        for(i=0;i<maxSpan;i++)
            ByteArrayConvert.toByte(cur.getFrequency(i+1), array, (i+maxSpan)*4+12);
    }

    private void writeToByteArray(int index, byte[] array) {
        int i;

        ByteArrayConvert.toByte(index, array, 0);
        ByteArrayConvert.toByte(-1, array, 4);
        ByteArrayConvert.toByte(-1, array, 8);
        for(i=0;i<2*maxSpan;i++)
            ByteArrayConvert.toByte(0, array, i*4+12);
    }

    private WordPairStat getWordPairStatFromByteArray(byte[] array) {
        WordPairStat cur;
        int i;

        cur=new WordPairStat(ByteArrayConvert.toInt(array, 0),ByteArrayConvert.toInt(array, 4),ByteArrayConvert.toInt(array,8),maxSpan);
        for(i=0;i<maxSpan;i++)
            cur.addFrequency(i-maxSpan,ByteArrayConvert.toInt(array, 4*i+12));
        for(i=0;i<maxSpan;i++)
            cur.addFrequency(i+1,ByteArrayConvert.toInt(array, 4*(i+maxSpan)+12));
        return cur;
    }
}
