package dragon.nlp.tool;

import dragon.nlp.Sentence;
import dragon.nlp.Word;
import dragon.nlp.extract.EngDocumentParser;
import dragon.util.EnvVariable;

/**
 * <p>Java version of Brill's Part-of-Speech Tagger</p>
 * <p>This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, please visit 
 * http://www.gnu.org/copyleft/gpl.html</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Jimmy Lin, Davis Zhou
 * @version 1.0
 */

public class BrillTagger extends AbstractTagger implements Tagger{
	
	static {
		System.loadLibrary("brilltagger");
	}
	
	public static void main(String[] args){
		BrillTagger tagger;
		Sentence sent;
		EngDocumentParser parser;
		
		tagger=new BrillTagger();
		parser=new EngDocumentParser();
		sent=parser.parseSentence("five osmanthus producing areas");
		tagger.tag(sent);
		System.out.println(sent.toPOSTaggedString());
	}
	
	public BrillTagger(){
		this(EnvVariable.getDragonHome()+"/nlpdata/tagger/brill");
	}
	
	public BrillTagger(String dataDir){
		initialize(dataDir+"/BIGRAMS",dataDir+"/CONTEXTUALRULEFILE",dataDir+"/LEXICALRULEFILE",dataDir+"/LEXICON");
	}
	
	public void tag(Sentence sent){
		Word word;
        String tagged, splitted[],tag;
        int i, start;

        tagged=tag(sent.toBrillTaggerString());
        splitted=tagged.split(" ");
        word=sent.getFirstWord();
        for(i=0;i<splitted.length-1;i++)
        {
        	start=splitted[i].lastIndexOf('/');
        	tag=splitted[i].substring(start+1);
            word.setPOS(tag, getPOSIndex(tag,word));
            word=word.next;
        }
    }
	
	protected int getPOSIndex(String pos, Word word) {
        int posIndex;

        if (word.isPunctuation()) {
            pos = word.getContent();
            posIndex = 0;
        }
        else if (pos.startsWith("N")) {
            posIndex = HeppleTagger.POS_NOUN;
        }
        else if (pos.startsWith("VB")) {
            posIndex = Tagger.POS_VERB;
        }
        else if (pos.startsWith("JJ")) {
            posIndex = Tagger.POS_ADJECTIVE;
        }
        else if (pos.startsWith("RB")) {
            posIndex = Tagger.POS_ADVERB;
        }
        else if (pos.startsWith("CC")) {
            posIndex = Tagger.POS_CC;
        }
        else if (pos.startsWith("DT")) {
            posIndex = Tagger.POS_DT;
        }
        else if (pos.startsWith("PRP")) {
            posIndex = Tagger.POS_PRONOUN;
        }
        else if (pos.startsWith("IN")) {
            if (isConjunction(word.getContent())) {
                posIndex = Tagger.POS_CC;
            }
            else {
                posIndex = Tagger.POS_IN;
            }
        }
        else if (pos.startsWith("TO")) {
            posIndex = Tagger.POS_IN;
        }
        else if (pos.startsWith("CD")) {
            if (word.isNumber()) {
                posIndex = Tagger.POS_NUM;
            }
            else {
                posIndex = Tagger.POS_NOUN;
            }
        }
        else if (pos.startsWith("W")) {
            posIndex = Tagger.POS_CC;
        }
        else {
            posIndex = 0;
        }

        if(posIndex>0 && posIndex!=Tagger.POS_NOUN && word.isAllCapital()){
            posIndex=Tagger.POS_NOUN;
        }
        return posIndex;
    }
	
	/**
	 * Initialize the tagger
	 * @param bigramFile the bigram file
	 * @param crFile the contextual rule file
	 * @param lrFile the lexicon rule file
	 * @param lexiconFile the lexicon file
	 */
	public native void initialize(String bigramFile, String crFile, String lrFile, String lexiconFile);

	/**
	 * Close the opened resources
	 */
	public native void close();

	/**
	 * The native function to call brill's POS tagger
	 * @param sentence the sentence in the representation of a string
	 * @return the tagged string
	 */
	public native String tag(String sentence);
}