package dragon.config;

import dragon.nlp.extract.EngDocumentParser;
import dragon.nlp.tool.*;
import dragon.nlp.tool.xtract.*;
import dragon.onlinedb.*;
import dragon.util.*;
import java.io.*;
import java.util.ArrayList;

/**
 * <p>Phrase extraction application configuration</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class PhraseExtractAppConfig {
    public PhraseExtractAppConfig() {
    }

    public static void main(String[] args) {
        PhraseExtractAppConfig phraseApp;
        ConfigureNode root,phraseAppNode;
        ConfigUtil util;

        if(args.length!=2){
            System.out.println("Please input two parameters: configuration xml file and phrase extraction applicaiton id");
            return;
        }

        root=new BasicConfigureNode(args[0]);
        util=new ConfigUtil();
        phraseAppNode=util.getConfigureNode(root,"phraseextractapp",Integer.parseInt(args[1]));
        if(phraseAppNode==null)
            return;
        phraseApp=new PhraseExtractAppConfig();
        phraseApp.phraseExtract(phraseAppNode);
    }

    public void phraseExtract(ConfigureNode phraseAppNode){
        CollectionReaderConfig collectionConfig;
        LemmatiserConfig lemmatiserConfig;
        TaggerConfig taggerConfig;
        Tagger tagger;
        Lemmatiser lemmatiser;
        CollectionReader[] arrCollectionReader;
        String wordDelimitor, indexFolder, phraseFile, vobFile, collectionIDs, arrCollection[];
        boolean indexing;
        double strength, peakZScore, spread, expandRatio;
        int maxSpan, maxPhraseLength, taggerID, lemmatiserID;
        int i;

        maxSpan=phraseAppNode.getInt("maxspan",4);
        indexFolder=phraseAppNode.getString("indexfolder");
        phraseFile=phraseAppNode.getString("phrasefile");
        indexing=phraseAppNode.getBoolean("indexing",true);
        strength=phraseAppNode.getDouble("strength",1.0);
        peakZScore=phraseAppNode.getDouble("peakzscore",1.0);
        spread=phraseAppNode.getDouble("spread",maxSpan);
        expandRatio=phraseAppNode.getDouble("expandratio",0.75);
        vobFile=phraseAppNode.getString("vobfile",null);
        maxPhraseLength=phraseAppNode.getInt("maxphraselength",4);
        
        if(indexing){
            collectionConfig = new CollectionReaderConfig();
            lemmatiserConfig = new LemmatiserConfig();
            taggerConfig=new TaggerConfig();
            lemmatiserID= phraseAppNode.getInt("lemmatiser",0);
            lemmatiser=lemmatiserConfig.getLemmatiser(phraseAppNode,lemmatiserID);
            taggerID = phraseAppNode.getInt("tagger", 0);
            tagger=taggerConfig.getTagger(phraseAppNode,taggerID);
            collectionIDs = phraseAppNode.getString("collectionreader");
            arrCollection=collectionIDs.split(";");
            arrCollectionReader = new CollectionReader[arrCollection.length];
            for (i = 0; i < arrCollection.length; i++) {
                arrCollectionReader[i] = collectionConfig.getCollectionReader(phraseAppNode,
                    Integer.parseInt(arrCollection[i]));
            }
            wordDelimitor=getWordDelimitor(phraseAppNode.getString("notworddelimitor","."));
            phraseExtract(indexFolder,maxSpan,arrCollectionReader, lemmatiser, tagger, wordDelimitor,
            		strength,peakZScore,spread,expandRatio,maxPhraseLength,phraseFile, vobFile);
        }
        else
        	phraseExtract(indexFolder,maxSpan,strength,peakZScore,spread,expandRatio,maxPhraseLength,phraseFile, vobFile);
    }
    
    public void phraseExtract(String indexFolder, int maxSpan, double strength, double peakZScore, 
    		double spread, double expandRatio, int maxPhraseLength, String phraseFile, String vobFile){
    	SimpleXtract xtract;
    	
    	xtract=new SimpleXtract(maxSpan,indexFolder);
    	xtract.extract(strength,spread,peakZScore,expandRatio,phraseFile);
    	if(vobFile!=null)
    		generateVocabulary(phraseFile,maxPhraseLength,vobFile);
    }
    
    public void phraseExtract(String indexFolder, int maxSpan, CollectionReader[] crs,
    		Lemmatiser lemmatiser, Tagger tagger,String wordDelimitor, double strength, double peakZScore, double spread, 
    		double expandRatio, int maxPhraseLength, String phraseFile, String vobFile){
    	SimpleXtract xtract;
    	
    	xtract=new SimpleXtract(maxSpan,indexFolder);
    	xtract.index(crs, tagger, lemmatiser, wordDelimitor);
    	xtract.extract(strength,spread,peakZScore,expandRatio,phraseFile);
    	if(vobFile!=null)
    		generateVocabulary(phraseFile,maxPhraseLength,vobFile);
    }
    
    public void generateVocabulary(String phraseFile, int maxPhraseLen, String vobFile){
    	generateVocabulary(postProcessExtractedPhrase(phraseFile),maxPhraseLen,vobFile);
    }

    private void generateVocabulary(ArrayList phraseList, int maxLen, String outputFile){
        BufferedWriter bw;
        ArrayList newList;
        int num, min, max;
        int i;

        try{
            System.out.println((new java.util.Date()).toString()+" Printing vocabulary file...");
            bw=FileUtil.getTextWriter(outputFile);
            min=Integer.MAX_VALUE;
            max=0;
            newList=new ArrayList(phraseList.size());
            for(i=0;i<phraseList.size();i++){
                num=getTokenNum((String)phraseList.get(i));
                if(num<=maxLen){
                    newList.add(phraseList.get(i));
                    if (num > max)
                        max = num;
                    if (num < min)
                        min = num;
                }
            }

            bw.write(newList.size()+"\t"+min+"\t"+max+"\n");
            for(i=0;i<newList.size();i++)
            {
                bw.write((String)newList.get(i));
                bw.write('\t');
                bw.write(String.valueOf(i));
                bw.write('\n');
            }
            bw.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private SortedArray postProcessExtractedPhrase(String phraseFile){
        SortedArray list;
        BufferedReader br;
        String line;
        int pos;

        try{
            System.out.println((new java.util.Date()).toString()+" Postprocessing Extracted Phrases...");
            list = new SortedArray();
            br=FileUtil.getTextReader(phraseFile);
            br.readLine();//skip the first line

            while((line=br.readLine())!=null){
                pos=line.indexOf('\t');
                if(pos>=0)
                    line=line.substring(0,pos);
                line=postProcessPhrase(line);
                if(line.indexOf(' ')>0)
                    list.add(line);
            }
            return list;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private String postProcessPhrase(String content){
        try{

            content = content.replace('-', ' ');
            content = content.replace('_', ' ');
            content = content.replace('\'', ' ');
            content = content.replaceAll("   ", " ");
            content = content.replaceAll("  ", " ");
            content = content.replaceAll("  ", " ");
            content=removePersonTitle(content);
            return content.toLowerCase();
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private String removePersonTitle(String content){
        int pos;

        content=content.trim();
        pos=content.indexOf(' ');
        if(pos>0){
            if(content.charAt(pos-1)=='.' && content.lastIndexOf('.',pos-2)<0){
                return removePersonTitle(content.substring(pos + 1));
            }
        }
        return content;
    }

    private int getTokenNum(String term){
        int count, i;

        count = 0;
        for (i = 0; i < term.length(); i++)
            if (Character.isWhitespace(term.charAt(i)))
                count++;
        return count + 1;
    }

    private String getWordDelimitor(String notWordDelimitor){
        String delimitors;
        StringBuffer sb;
        int i;

        sb=new StringBuffer();
        delimitors=EngDocumentParser.defWordDelimitor;
        if(notWordDelimitor==null && notWordDelimitor.length()==0)
            return delimitors;
        for(i=0;i<delimitors.length();i++){
            if(notWordDelimitor.indexOf(delimitors.charAt(i))<0)
                sb.append(delimitors.charAt(i));
        }
        return sb.toString();
    }
}