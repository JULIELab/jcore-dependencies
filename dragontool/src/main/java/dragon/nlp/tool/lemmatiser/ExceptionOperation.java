package dragon.nlp.tool.lemmatiser;

import dragon.util.FileUtil;
import dragon.util.SortedArray;

import java.io.BufferedReader;
import java.util.ArrayList;

/**
 * <p>Exception operation class </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class ExceptionOperation implements Operation{
    SortedArray wordList;
    int pos;

    public ExceptionOperation(int POS, String exceptionFile) {
        this.pos=POS;
        wordList=loadExceptions(exceptionFile);
    }

    public boolean getIndexLookupOption(){
        return false;
    }

    public int getPOSIndex(){
        return pos;
    }

    public String execute(String derivation){
        int index;
        WordMap wordMap;

        index=wordList.binarySearch(new WordMap(derivation,null));
        if(index>=0){
            wordMap=(WordMap) wordList.get(index);
            if(wordMap.getSlaveWord()==null || wordMap.getSlaveWord().length()==0)
                return wordMap.getMasterWord();
            else
                return wordMap.getSlaveWord();
        }
        else
            return null;
    }

    private SortedArray loadExceptions(String filename){
        BufferedReader br;
        String line;
        String[] arrField;
        int i, total;
        ArrayList list;
        SortedArray exceptionlist;
        WordMap cur;

        try{
            br=FileUtil.getTextReader(filename);
            line=br.readLine();
            total=Integer.parseInt(line);
            list=new ArrayList(total);

            for(i=0;i<total;i++){
                line=br.readLine();
                arrField=line.split(" ");
                if(arrField.length>=2)
                    cur=new WordMap(arrField[0],arrField[1]);
                else
                    cur=new WordMap(arrField[0],null);
                list.add(cur);
            }
            br.close();
            exceptionlist=new SortedArray();
            exceptionlist.addAll(list);
            return exceptionlist;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

}