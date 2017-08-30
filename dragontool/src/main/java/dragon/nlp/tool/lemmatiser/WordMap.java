package dragon.nlp.tool.lemmatiser;

/**
 * <p>Wordmap is the data structure for two words which has mater and slave relation between them</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class WordMap implements Comparable{
    String master, slave;

    public WordMap(String master, String slave) {
        this.master=master;
        this.slave=slave;
    }

    public int compareTo(Object obj){
        return master.compareToIgnoreCase(((WordMap)obj).getMasterWord());
    }

    public String getMasterWord(){
        return master;
    }

    public String getSlaveWord(){
        return slave;
    }

}