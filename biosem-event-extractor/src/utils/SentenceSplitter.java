package utils;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import relations.TData;

/**
 * 
 * @author Chinh
 * @Date: Oct 28, 2010
 */
public class SentenceSplitter {
	static final TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
	static TokenizerFactory TOKENIZER = new RegExTokenizerFactory(
			"(\\/|\\+|-|'|\\d|\\p{L})++|\\S");
	static final SentenceModel SENTENCE_MODEL = new MedlineSentenceModel();
	static final SentenceChunker SENTENCE_CHUNKER = new SentenceChunker(
			TOKENIZER_FACTORY, SENTENCE_MODEL);

	public static List<BioSemSentence> spliter(String str) {
		char cc[] = str.toCharArray();
		List<BioSemSentence> list = new ArrayList<>();
		Chunking chunks = SENTENCE_CHUNKER.chunk(cc, 0, cc.length);
		Set<Chunk> ls = chunks.chunkSet();
		if (ls.size() < 1) {
			System.out.println("No sentence found.");
			return list;
		}
		String sub_sen = chunks.charSequence().toString();
		for (Iterator<Chunk> it = ls.iterator(); it.hasNext();) {
			Chunk sentence = it.next();
			int start = sentence.start();
			int end = sentence.end();
			list.add(new BioSemSentence(sub_sen.substring(start, end), start, end));
		}
		return list;
	}

	public static List<String>[] wordSpliter(String txt) {
		List<String> ls[] = new ArrayList[2];
		ls[0] = new ArrayList<String>();
		ls[1] = new ArrayList<String>();
		char cc[] = txt.toCharArray();
		Tokenizer tk = TOKENIZER.tokenizer(cc, 0, cc.length);
		tk.tokenize(ls[0], ls[1]);
		return ls;
	}

	public static void main(String[] args) {
		String txt = "In Th17 cells that expressed PRO31 as well as PRO32 mRNA (Figure6C), PRO33 and PRO34 mRNA were undetectable (data not shown), whereas that of PRO35 was high (Figure6C) (Ivanov etal., 2007).";
		List<String>[] words = SentenceSplitter.wordSpliter(txt);
		for (String s : words[0]) {
			System.out.println(s);
		}
	}

	public static class BioSemSentence {
		
		public BioSemSentence(String sentence, int begin, int end) {
			super();
			this.text = sentence;
			this.begin = begin;
			this.end = end;
		}
		public BioSemSentence() {}
		public String text;
		public int begin;
		public int end;
		List<TData> prots;
		
	}
}
