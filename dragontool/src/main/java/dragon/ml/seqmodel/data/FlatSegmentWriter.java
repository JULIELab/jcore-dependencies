package dragon.ml.seqmodel.data;

import dragon.util.FileUtil;
import java.io.*;

/**
 * <p>Storing segment information in a flat manner</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class FlatSegmentWriter implements DataWriter{
    private PrintWriter out;
    private LabelConverter labelConverter;
    private String tagDelimit; // seperator between tokens and tag number
    public FlatSegmentWriter(String outFile, LabelConverter labelConverter) {
        this.labelConverter=labelConverter;
        out = FileUtil.getPrintWriter(outFile);
        tagDelimit="|";
    }

    public boolean write(Dataset dataset){
        dataset.startScan();
        while(dataset.hasNext())
            write(dataset.next());
        return true;
    }

    public boolean write(DataSequence dataSeq) {
        StringBuffer segment;
        int i, segStart, segEnd;

        try {
            segStart=0;
            while(segStart<dataSeq.length()){
                segEnd=dataSeq.getSegmentEnd(segStart);
                segment=new StringBuffer(dataSeq.getToken(segStart).getContent());
                for(i=segStart+1;i<=segEnd;i++)
                    segment.append(" "+dataSeq.getToken(i).getContent());
                segment.append(tagDelimit);
                segment.append(labelConverter.getExternalLabelString(dataSeq.getOriginalLabel(segStart)));
                out.println(segment.toString());
                segStart=segEnd+1;
            }
            out.println();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void close(){
        out.close();
    }
}