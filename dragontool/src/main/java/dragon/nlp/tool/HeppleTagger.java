package dragon.nlp.tool;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import dragon.nlp.Sentence;
import dragon.nlp.Word;
import dragon.util.EnvVariable;
import dragon.util.FileUtil;
import hepple.postag.POSTagger;

/**
 * <p>
 * Hepple tagger adopted from hepple.postag
 * </p>
 * <p>
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class HeppleTagger extends AbstractTagger implements Tagger {
	private POSTagger tag;

	public HeppleTagger() {
		this(EnvVariable.getDragonHome() + "/nlpdata/tagger");
	}

	public HeppleTagger(String workDir) {
		String oldProtocolHandlerProp = System.getProperty("java.protocol.handler.pkgs");
		try {
			if (!FileUtil.exist(workDir) && FileUtil.exist(EnvVariable.getDragonHome() + "/" + workDir))
				workDir = EnvVariable.getDragonHome() + "/" + workDir;
			File fileLexicon = new File(workDir + "/lexicon_all");
			File fileRules = new File(workDir + "/rules_cap");
			// first check if the resources are accessible as regular files
			URL urlLexicon = fileLexicon.toURI().toURL();
			URL urlRules = fileRules.toURI().toURL();

			// Now, if at least one resource couldn't be found we will try to
			// look at the classpath.
			String classpath = null;
			if ((!fileLexicon.exists() || !fileRules.exists()) && workDir.contains("!")) {
				classpath = "classpath:" + workDir.substring(workDir.indexOf('!') + 1);
				// We need to pass URL instances to the POSTagger constructor
				// (see below). In order to be able to get the resources from
				// the classpath, we need to use a custom handler:
				// de.julielab.protocols.Handler. With the System property we
				// can set that this class should be used for the 'classpath'
				// protocol.
				System.setProperty("java.protocol.handler.pkgs", "de.julielab.protocols");
			}
			if (!fileLexicon.exists()) {
				urlLexicon = new URL(classpath + "/lexicon_all");
			}
			if (!fileRules.exists()) {
				urlRules = new URL(classpath + "/rules_cap");
			}
			tag = new POSTagger(urlLexicon, urlRules);
			if (tag == null) {
				System.out.println("Failed to create POS tagger");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// reset the protocol settings
			if (oldProtocolHandlerProp != null)
				System.setProperty("java.protocol.handler.pkgs", oldProtocolHandlerProp);
			else
				System.clearProperty("java.protocol.handler.pkgs");
		}
	}

	public void tag(Sentence sent) {
		String[] output;
		List result, sentenceForTagger, sentencesForTagger;
		int i;

		sentenceForTagger = new ArrayList();
		sentencesForTagger = new ArrayList(1);
		sentencesForTagger.add(sentenceForTagger);
		Word word = sent.getFirstWord();
		while (word != null) {
			sentenceForTagger.add(word.getContent());
			word = word.next;
		}
		result = (List) tag.runTagger(sentencesForTagger);
		if (result.size() <= 0)
			return;

		result = (List) result.get(0);
		word = sent.getFirstWord();
		for (i = 0; i < result.size(); i++) {
			output = (String[]) result.get(i);
			word.setPOS(output[1], getPOSIndex(output[1], word));
			word = word.next;
		}

	}

	protected int getPOSIndex(String pos, Word word) {
		int posIndex;

		if (word.isPunctuation()) {
			pos = word.getContent();
			posIndex = 0;
		} else if (pos.startsWith("N")) {
			posIndex = HeppleTagger.POS_NOUN;
		} else if (pos.startsWith("VB")) {
			posIndex = Tagger.POS_VERB;
		} else if (pos.startsWith("JJ")) {
			posIndex = Tagger.POS_ADJECTIVE;
		} else if (pos.startsWith("RB")) {
			posIndex = Tagger.POS_ADVERB;
		} else if (pos.startsWith("CC")) {
			posIndex = Tagger.POS_CC;
		} else if (pos.startsWith("DT")) {
			posIndex = Tagger.POS_DT;
		} else if (pos.startsWith("PRP")) {
			posIndex = Tagger.POS_PRONOUN;
		} else if (pos.startsWith("IN")) {
			if (isConjunction(word.getContent())) {
				posIndex = Tagger.POS_CC;
			} else {
				posIndex = Tagger.POS_IN;
			}
		} else if (pos.startsWith("TO")) {
			posIndex = Tagger.POS_IN;
		} else if (pos.startsWith("CD")) {
			if (word.isNumber()) {
				posIndex = Tagger.POS_NUM;
			} else {
				posIndex = Tagger.POS_NOUN;
			}
		} else if (pos.startsWith("W")) {
			posIndex = Tagger.POS_CC;
		} else {
			posIndex = 0;
		}

		if (posIndex > 0 && posIndex != Tagger.POS_NOUN && word.isAllCapital()) {
			posIndex = Tagger.POS_NOUN;
		}
		return posIndex;
	}
}