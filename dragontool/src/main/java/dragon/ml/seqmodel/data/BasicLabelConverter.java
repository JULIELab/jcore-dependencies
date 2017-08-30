package dragon.ml.seqmodel.data;

/**
 * <p>Basic Label Converter</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicLabelConverter implements LabelConverter{
    public BasicLabelConverter() {
    }

    public int getInternalLabel(int externalLabel){
        return externalLabel-1;
    }

    public int getInternalLabel(String externalLabel){
        try{
            return Integer.parseInt(externalLabel)-1;
        }
        catch(Exception e){
            return -1;
        }
    }

    public int getExternalLabelID(int internalLabel){
        return internalLabel+1;
    }

    public String getExternalLabelString(int internalLabel){
        return Integer.toString(internalLabel+1);
    }
}