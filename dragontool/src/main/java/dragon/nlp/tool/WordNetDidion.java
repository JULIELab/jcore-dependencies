package dragon.nlp.tool;

import dragon.util.EnvVariable;
import dragon.util.FileUtil;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;

import java.io.FileInputStream;

/**
 * <p>A Java-implementation of WordNet provided by Didion</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class WordNetDidion implements Lemmatiser, WordNetUtil{
	private Dictionary dict;

    public WordNetDidion(String workDir){
        String propsFile;

        try{
            if(!FileUtil.exist(workDir) && FileUtil.exist(EnvVariable.getDragonHome()+"/"+workDir))
                workDir=EnvVariable.getDragonHome()+"/"+workDir;
            propsFile=workDir+"/file_properties.xml";
            if(!checkDataDirectory(propsFile,workDir)){
                System.out.println("The wordnet data directory is not correct!");
                return;
            }
            JWNL.initialize(new FileInputStream(propsFile));
            dict = Dictionary.getInstance();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public WordNetDidion(){
        this(EnvVariable.getDragonHome()+"/nlpdata/wordnet");
    }

    public Dictionary getDictionary(){
    	return dict;
    }
    
    public String lemmatize(String word)
    {
        IndexWord indexWord;
        int i;

        if(Character.isDigit(word.charAt(word.length()-1)))
           return word;

        for(i=Lemmatiser.FIRSTPOS;i<=Lemmatiser.LASTPOS;i++){
            try {
                indexWord = dict.lookupIndexWord(getPOS(i), word);
                if (indexWord != null)
                    return indexWord.getLemma();
            }
            catch (JWNLException ex) {
                ex.printStackTrace();
                return null;
            }
        }
        return word;
    }

    public String lemmatize(String word,int pos)
    {
        IndexWord indexWord;
        if(Character.isDigit(word.charAt(word.length()-1)))
           return word;

        if(pos>Lemmatiser.LASTPOS || pos<Lemmatiser.FIRSTPOS) return word;
        try{
            indexWord = dict.lookupIndexWord(getPOS(pos), word);
            if (indexWord != null)
                return indexWord.getLemma();
            else
                return word;
        }
        catch(JWNLException ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    public String stem(String word){
        return lemmatize(word);
    }

    private POS getPOS(int index){
        switch(index){
            case 1:
                return POS.NOUN;
            case 2:
                return POS.VERB;
            case 3:
                return POS.ADJECTIVE;
            case 4:
                return POS.ADVERB;
            default:
                return null;
        }
    }

    private boolean checkDataDirectory(String propsFile, String dataDir){
        String content;
        int start, end;

        try{
            content = FileUtil.readTextFile(propsFile);
            start = content.indexOf("dictionary_path");
            start=content.indexOf("value=",start)+7;
            end=content.indexOf("\"",start);
            if(content.substring(start,end).equals(dataDir))
                return true;
            content=content.substring(0,start)+dataDir+content.substring(end);
            return FileUtil.saveTextFile(propsFile,content);
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}