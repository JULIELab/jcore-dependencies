package dragon.nlp.tool.lemmatiser;

/**
 * <p>Suffix detach operation for lemmatising </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SuffixDetachOperation implements Operation{
    private String suffix, changeTo;
    private int pos;
    private boolean indexLookupOption;

    SuffixDetachOperation(int POS, String suffix, String changeTo){
        this.pos=POS;
        this.suffix=suffix;
        this.changeTo=changeTo;
        indexLookupOption=false;
    }

    public String execute(String derivation){
        if(derivation.length()>suffix.length() && derivation.endsWith(suffix)){
           if(changeTo==null){
               return derivation.substring(0,derivation.length()-suffix.length());
           }
           else
               return derivation.substring(0,derivation.length()-suffix.length())+changeTo;
        }
        else
            return null;
    }

    public boolean getIndexLookupOption(){
        return indexLookupOption;
    }

    public void setIndexLookupOption(boolean option){
        indexLookupOption=option;
    }

    public String getSuffix(){
        return suffix;
    }

    public String getChangeTo(){
        return changeTo;
    }

    public int getPOSIndex(){
        return pos;
    }
}
