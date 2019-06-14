package dragon.onlinedb;

import dragon.nlp.SimpleElement;
import dragon.nlp.SimpleElementList;
import dragon.util.FileUtil;
import dragon.util.SortedArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
/**
 * <p>Basic class of handling article index information</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicArticleIndex {
    private long collectionSize;
    private SortedArray list;
    private SimpleElementList fileList;
    private boolean writingMode;
    private String indexFilename;

    public BasicArticleIndex(String indexFilename, boolean writingMode) {
        this.writingMode =writingMode;
        this.indexFilename=indexFilename;
        loadKeyList(indexFilename);
    }

    public boolean isWritingMode(){
        return writingMode;
    }

    public boolean add(String key, long offset){
        BasicArticleKey basicKey;

        if(!writingMode)
            return false;
        basicKey=new BasicArticleKey(new String(key));
        basicKey.setOffset(offset);
        if(list.add(basicKey))
            return true;
        else
            return false;
    }

    public boolean add(String key, String filename, long offset, int length){
        BasicArticleKey basicKey;

        if(!writingMode)
            return false;
        basicKey=new BasicArticleKey(new String(key));
        basicKey.setOffset(offset);
        basicKey.setFileIndex(fileList.add(filename));
        basicKey.setLength(length);
        if(list.add(basicKey))
            return true;
        else
            return false;
    }

    public String getFilename(int fileIndex){
        return fileList.search(fileIndex);
    }

    public boolean contains(String key){
        return list.contains(new BasicArticleKey(key));
    }

    public BasicArticleKey search(String key){
        int pos;

        pos=list.binarySearch(new BasicArticleKey(key));
        if(pos>=0)
            return (BasicArticleKey)list.get(pos);
        else
            return null;
    }

    public void setCollectionFileSize(long size){
        this.collectionSize=size;
    }

    public long getCollectionFileSize(){
        return collectionSize;
    }

    public void close(){
        if(writingMode)
            saveKeyList(indexFilename,list, fileList);
        list.clear();
    }

    private void loadKeyList(String indexFilename){
        BufferedReader br;
        File file;
        BasicArticleKey cur;
        String line;
        String[] arrField;
        int i, total;

        try {
            file = new File(indexFilename);
            if (!file.exists()){
                this.list = new SortedArray();
                this.fileList=new SimpleElementList();
                return;
            }

            br = FileUtil.getTextReader(file);
            arrField=br.readLine().split("\t");
            total=Integer.parseInt(arrField[0]);
            this.list = new SortedArray(total);
            this.collectionSize=Long.parseLong(arrField[1]);
            this.fileList=new SimpleElementList();

            for(i=0;i<total;i++) {
                line=br.readLine();
                arrField = line.split("\t");
                cur=new BasicArticleKey(arrField[0]);
                cur.setOffset(Long.parseLong(arrField[1]));
                if(arrField.length>=4){
                    cur.setLength(Integer.parseInt(arrField[2]));
                    cur.setFileIndex(Integer.parseInt(arrField[3]));
                }
                list.add(cur);
            }

            while((line=br.readLine())!=null){
                arrField = line.split("\t");
                fileList.add(new SimpleElement(arrField[0],Integer.parseInt(arrField[1])));
            }
            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveKeyList(String docKeyListFilename, SortedArray list, SimpleElementList fileList) {
        BufferedWriter bw;
        BasicArticleKey cur;
        int i;

        try {
            if (list == null)
                return;

            bw = FileUtil.getTextWriter(docKeyListFilename);
            bw.write(list.size()+"\t"+this.collectionSize+"\t"+fileList.size()+"\n");
            //output article indexing information
            for (i = 0; i < list.size(); i++) {
                cur = (BasicArticleKey) list.get(i);
                bw.write(cur.getKey() + "\t" + cur.getOffset());
                if(cur.getLength()>0)
                    bw.write("\t"+cur.getLength()+"\t"+cur.getFileIndex());
                bw.write("\n");
                bw.flush();
            }
            //output file indexing information
            for (i = 0; i < fileList.size(); i++) {
                bw.write(fileList.search(i)+ "\t" + i+"\n");
                bw.flush();
            }
            bw.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}