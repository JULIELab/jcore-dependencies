package dragon.ml.seqmodel.data;

import dragon.util.FileUtil;
import java.io.*;
import java.util.StringTokenizer;

/**
 * <p>Flat segment reader</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class FlatSegmentReader implements DataReader {
    private int originalLabelNum, markovOrder;
    private String delimit; // used to define token boundaries
    private String tagDelimit; // seperator between tokens and tag number
    private String impDelimit; // delimiters to be retained for tagging
    private BufferedReader tin;
    private LabelConverter labelConverter;
    private int[] labels;
    boolean fixedColFormat;
    boolean tagged;

    public FlatSegmentReader(int originalLabelNum, int markovOrder, String taggedFile, LabelConverter labelConverter) {
        this.originalLabelNum=originalLabelNum;
        this.markovOrder=markovOrder;
        tin=FileUtil.getTextReader(taggedFile);
        this.labelConverter =labelConverter;
        delimit=",\t/ -():.;'?\\#`&\"_";
        tagDelimit="|";
        impDelimit=",";
        // read list of columns in the header of the tag file
        labels = readHeaderInfo(tin);
        if (labels != null)
            fixedColFormat = true;
        else
            fixedColFormat=false;
        tagged=true;
    }

    public FlatSegmentReader(int originalLabelNum, int markovOrder, String rawFile){
        this.originalLabelNum=originalLabelNum;
        this.markovOrder=markovOrder;
        tin=FileUtil.getTextReader(rawFile);
        this.labelConverter =null;
        delimit=" \t";
        tagDelimit="|";
        impDelimit="";
        tagged=false;
        fixedColFormat=false;
    }

    public Dataset read() {
        BasicDataset dataset;
        DataSequence dataSeq;

        dataset=new BasicDataset(originalLabelNum, markovOrder);
        while (true) {
            dataSeq=readRow();
            if (dataSeq==null || dataSeq.length()==0)
                break;
            else
                dataset.add(dataSeq);
        }
        return dataset;
    }

    public DataSequence readRow(){
        try{
            if (tagged) {
                if (fixedColFormat)
                    return readRowFixedCol(tin, labels);
                else
                    return readRowVarCol(tin);
            } else
                return readRaw();
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void close(){
        try{
            tin.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private String[]  getTokenList(String text) {
        StringTokenizer textTok;
        String[] cArray;
        int tlen;

        textTok = new StringTokenizer(text.toLowerCase(), delimit, true);
        tlen = 0;
        while (textTok.hasMoreTokens()) {
            String tokStr = textTok.nextToken();
            if (delimit.indexOf(tokStr) == -1 || impDelimit.indexOf(tokStr) != -1) {
                tlen++;
            }
        }
        cArray = new String[tlen];
        tlen = 0;
        textTok = new StringTokenizer(text.toLowerCase(), delimit, true);
        while (textTok.hasMoreTokens()) {
            String tokStr = textTok.nextToken();
            if (delimit.indexOf(tokStr) == -1 || impDelimit.indexOf(tokStr) != -1) {
                cArray[tlen++] = tokStr;
            }
        }
        return cArray;
    }

    private DataSequence readRowVarCol(BufferedReader tin) throws IOException{
        StringTokenizer firstSplit;
        BasicDataSequence dataSeq;
        BasicToken token;
        String w, line, arrToken[];
        int i, label;

        dataSeq=new BasicDataSequence();
        while (true) {
            line = tin.readLine();
            firstSplit = null;
            if (line != null) {
                firstSplit = new StringTokenizer(line.toLowerCase(), tagDelimit);
            }
            if ( (line == null) || (firstSplit.countTokens() < 2)) {
                // Empty Line
                return dataSeq;
            }
            w = firstSplit.nextToken();
            if(labelConverter!=null)
                label=labelConverter.getInternalLabel(firstSplit.nextToken());
            else
                label = Integer.parseInt(firstSplit.nextToken());
            arrToken = getTokenList(w);
            for(i=0;i<arrToken.length;i++){
                token=new BasicToken(arrToken[i], label);
                if(i==0)
                    token.setSegmentMarker(true);
                else
                    token.setSegmentMarker(false);
                dataSeq.add(token);
            }
        }
    }

    private DataSequence readRowFixedCol(BufferedReader tin, int labels[]) throws IOException {
        StringTokenizer firstSplit ;
        BasicDataSequence dataSeq;
        BasicToken token;
        String w, line, arrToken[];
        int i, label;

        line = tin.readLine();
        if (line == null)
            return null;

        dataSeq=new BasicDataSequence();
        firstSplit = new StringTokenizer(line.toLowerCase(), tagDelimit, true);
        for (i = 0; (i < labels.length) && firstSplit.hasMoreTokens(); i++) {
            if(labelConverter!=null)
                label = labelConverter.getInternalLabel(labels[i]);
            else
                label=labels[i];
            w = firstSplit.nextToken();
            if (tagDelimit.indexOf(w) != -1) {
                continue;
            }
            else {
                if (firstSplit.hasMoreTokens())
                    // skip the delimiter.
                    firstSplit.nextToken();
            }

            if ( (label >= 0) && (label <originalLabelNum)) {
               arrToken= getTokenList(w);
               for(i=0;i<arrToken.length;i++){
                   token=new BasicToken(arrToken[i], label);
                   if (i == 0)
                       token.setSegmentMarker(true);
                   else
                       token.setSegmentMarker(false);
                   dataSeq.add(token);
               }
            }
        }
        return dataSeq;
    }

    private int[] readHeaderInfo(BufferedReader tin)  {
        StringTokenizer firstSplit;
        String line;
        int i, labels[];

        try{
            tin.mark(1000);
            line = tin.readLine();
            if (line == null)
                return null;
            if (!line.toLowerCase().startsWith("fixed-column-format")) {
                tin.reset();
                return null;
            }

            line = tin.readLine();
            firstSplit = new StringTokenizer(line, tagDelimit);
            labels = new int[originalLabelNum];
            for (i = 0; (i < originalLabelNum) && firstSplit.hasMoreTokens(); ) {
                labels[i++] = Integer.parseInt(firstSplit.nextToken());
            }
            return labels;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private DataSequence readRaw() throws IOException {
        BasicDataSequence dataSeq;
        StringTokenizer tok;
        String line, tokStr;

        line = tin.readLine();
        dataSeq=new BasicDataSequence();
        tok = new StringTokenizer(line.toLowerCase(), delimit, true);
        while(tok.hasMoreTokens()){
            tokStr = tok.nextToken();
            if (delimit.indexOf(tokStr) == -1 || impDelimit.indexOf(tokStr) != -1) {
                dataSeq.add(new BasicToken(tokStr));
            }
        }
        return dataSeq;
    }

}