package dragon.nlp.tool.lemmatiser;

import dragon.nlp.tool.Lemmatiser;
import dragon.nlp.tool.Tagger;
import dragon.util.EnvVariable;
import dragon.util.FileUtil;
import dragon.util.SortedArray;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
/**
 * <p>English lemmatiser which is adapted from WordNet </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class EngLemmatiser implements Lemmatiser{
    private LemmatiserPOS[] arrLemmatiser,arrOrderedLemmatiser;
    private String nounSuffix,verbSuffix,adjSuffix, advSuffix;
    private String nounSuffixE,verbSuffixE,adjSuffixE, advSuffixE;
    private String directory;
    private boolean indexLookupOption, disableVerbAdjective;

    public static void main(String[] args){
        EngLemmatiser lemmatiser;

        lemmatiser=new EngLemmatiser(false,true);
        System.out.println(lemmatiser.lemmatize("married",Tagger.POS_NOUN));
    }

    public EngLemmatiser() {
        this(EnvVariable.getDragonHome()+ "/nlpdata/lemmatiser", false, true);
    }

    public EngLemmatiser(boolean indexLookupOption, boolean disableVerbAdjective) {
        this(EnvVariable.getDragonHome()+ "/nlpdata/lemmatiser",indexLookupOption, disableVerbAdjective);
    }

    public EngLemmatiser(String directory, boolean indexLookupOption, boolean disableVerbAdjective) {
        initialize(directory,indexLookupOption, disableVerbAdjective);
    }

    private void initialize(String workDir, boolean indexLookupOption, boolean disableVerbAdjective){
        int i;

        if(!FileUtil.exist(workDir) && FileUtil.exist(EnvVariable.getDragonHome()+"/"+workDir))
            workDir=EnvVariable.getDragonHome()+"/"+workDir;
        this.directory=workDir;
        this.indexLookupOption=indexLookupOption;
        this.disableVerbAdjective =disableVerbAdjective;
        nounSuffix="ches=ch;shes=sh;ses=s;xes=x;zes=z;men=man;ies=y;s=";
        verbSuffix="s=";
        adjSuffix="";
        advSuffix="";
        nounSuffixE="";
        verbSuffixE="ed;ing";
        adjSuffixE="er;est";
        advSuffixE="er;est";

        arrLemmatiser=new LemmatiserPOS[4];
        System.out.println(new java.util.Date()+" loading exception data for lemmatiser...");
        for(i=0;i<4;i++)
            arrLemmatiser[i]=loadLemmatiser(i+1);
        arrOrderedLemmatiser=new LemmatiserPOS[4];
        arrOrderedLemmatiser[0]=arrLemmatiser[1];
        arrOrderedLemmatiser[1]=arrLemmatiser[2];
        arrOrderedLemmatiser[2]=arrLemmatiser[3];
        arrOrderedLemmatiser[3]=arrLemmatiser[0];
        System.out.println(new java.util.Date()+" loading done");
    }

    public String lemmatize(String derivation, int POS){
        String base;

        derivation=derivation.toLowerCase();
        if(POS>Lemmatiser.LASTPOS || POS<Lemmatiser.FIRSTPOS) return derivation;
        base=arrLemmatiser[POS-1].lemmatise(derivation);
        if(base==null)
            return derivation;
        else
            return base;
    }

    public String lemmatize(String derivation){
        String base;
        int i;

        derivation=derivation.toLowerCase();
        for(i=Lemmatiser.FIRSTPOS;i<=Lemmatiser.LASTPOS;i++){
            base=arrLemmatiser[i-1].lemmatise(derivation);
            if(base!=null)
                return base;
        }
        return derivation;
    }

    public String stem(String derivation){
        return lemmatize(derivation);
    }

    private LemmatiserPOS loadLemmatiser(int POS){
        String exceptionFile, indexFile, suffix, suffixE;
        Operation[] operations;
        SortedArray indexlist;
        ArrayList list;
        int i;

        switch(POS)
        {
            case Tagger.POS_NOUN:
                exceptionFile=directory+"/noun.exc";
                indexFile=directory+"/noun.index";
                suffix=nounSuffix;
                suffixE=nounSuffixE;
                break;
            case Tagger.POS_VERB:
                exceptionFile=directory+"/verb.exc";
                indexFile=directory+"/verb.index";
                suffix=verbSuffix;
                suffixE=verbSuffixE;
                break;
            case Tagger.POS_ADJECTIVE:
                exceptionFile=directory+"/adj.exc";
                indexFile=directory+"/adj.index";
                suffix=adjSuffix;
                suffixE=adjSuffixE;
                break;
            case Tagger.POS_ADVERB:
                exceptionFile=directory+"/adv.exc";
                indexFile=directory+"/adv.index";
                suffix=advSuffix;
                suffixE=advSuffixE;
                break;
            default:
                return null;
        }
        list=new ArrayList();
        list.add(new ExceptionOperation(POS,exceptionFile));
        if(POS==Tagger.POS_VERB){
        	loadSuffixDetachOperations(POS, "ies=y",list);
        	loadSuffixEDetachOperations(POS, "es",list);
        }
        loadSuffixDetachOperations(POS, suffix,list);
    	loadSuffixEDetachOperations(POS, suffixE,list);
        if(POS==Tagger.POS_ADJECTIVE && disableVerbAdjective){
            indexlist=arrLemmatiser[Tagger.POS_VERB-1].getIndexList();
            if(indexlist==null)
                indexlist=loadIndexList(directory+"/verb.index");
            list.add(new VerbOperation(indexlist));
        }
        operations=new Operation[list.size()];
        for(i=0;i<list.size();i++)
            operations[i]=(Operation)list.get(i);
        if(indexLookupOption){
            indexlist=loadIndexList(indexFile);
        }
        else
            indexlist=null;
        return new LemmatiserPOS(POS, operations,indexlist);
    }

    private void loadSuffixDetachOperations(int POS, String suffix, ArrayList operations){
        SuffixDetachOperation curOperation;
        String[] arrSuffix;
        String master, slave;
        int i,j;

        if(suffix==null || suffix.trim().length() ==0)
            return;

        arrSuffix=suffix.split(";");
        for(i=0;i<arrSuffix.length;i++){
            j=arrSuffix[i].indexOf('=');
            master=arrSuffix[i].substring(0,j);
            if(j==arrSuffix[i].length()-1)
                slave=null;
            else
                slave=arrSuffix[i].substring(j+1);
            curOperation=new SuffixDetachOperation(POS,master,slave);
            curOperation.setIndexLookupOption(indexLookupOption);
            operations.add(curOperation);
        }
    }

    private void loadSuffixEDetachOperations(int POS, String suffixE, ArrayList operations){
        SuffixEDetachOperation curOperation;
        String[] arrSuffixE;
        int i;

        if(suffixE==null || suffixE.trim().length() ==0)
            return;

        arrSuffixE=suffixE.split(";");
        for(i=0;i<arrSuffixE.length;i++){
            curOperation=new SuffixEDetachOperation(POS,arrSuffixE[i]);
            curOperation.setIndexLookupOption(indexLookupOption);
            operations.add(curOperation);
        }
    }

    private SortedArray loadIndexList(String filename){
        BufferedReader br;
        File file;
        String line;
        int i, total;
        ArrayList list;
        SortedArray indexlist;

        try{
            file=new File(filename);
            if(!file.exists())
                return null;

            br=FileUtil.getTextReader(filename);
            line=br.readLine();
            total=Integer.parseInt(line);
            list=new ArrayList(total);

            for(i=0;i<total;i++){
                line=br.readLine();
                list.add(line);
            }
            br.close();
            indexlist=new SortedArray();
            indexlist.addAll(list);
            return indexlist;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }


}