package dragon.nlp.tool.lemmatiser;

/**
 * <p>SuffixE detach operation for lemmatising</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SuffixEDetachOperation implements Operation{
    private String vowel;
    private String suffix;
    private int pos;
    private boolean indexLookupOption;

    SuffixEDetachOperation(int POS, String suffix){
        this.pos=POS;
        this.suffix=suffix;
        vowel="aieou";
        indexLookupOption=true;
    }

    public boolean getIndexLookupOption(){
        return indexLookupOption;
    }

    public void setIndexLookupOption(boolean option){
        indexLookupOption=option;
    }

    public String execute(String derivation){
        String base;

        if(derivation.length()>suffix.length() && derivation.endsWith(suffix)){
            base=derivation.substring(0,derivation.length()-suffix.length());
            if (! endingEPattern(base)) {
                return base;
            }
            else
                return base + "e";
        }
        else
            return null;
    }

    private boolean endingEPattern(String base){
        int len;

        len=base.length();
        if(len<3) return false;
        if(vowel.indexOf(base.charAt(len-1))<0 && vowel.indexOf(base.charAt(len-2))>=0 && vowel.indexOf(base.charAt(len-3))<0)
            return true;
        return false;
    }

    public String getSuffix(){
        return suffix;
    }

    public int getPOSIndex(){
        return pos;
    }


}