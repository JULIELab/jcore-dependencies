/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import relations.Chunk;

import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.FastCache;
import com.aliasi.util.Streams;

/**
 * 
 * @author Chinh
 */
public final class Parser {

	private static final Logger log = LoggerFactory.getLogger(Parser.class);
	
	public Parser() {
		initParser();
		map.addAll(Arrays.asList(conj_list));
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	ChunkerME chunker;
	Tokenizer tokenizer;
	HmmDecoder tagger, tagger2;
	public String genia_tag[]; // POS tags of current sentence
	public String old_txt = null;
	static TokenizerFactory TOKENIZER_FACTORY = new RegExTokenizerFactory(
			"(\\/|\\+|-|'|\\d|\\p{L})++|\\S|\\.");

	@SuppressWarnings("CallToThreadDumpStack")
	public void initParser() {
		InputStream tok_modelIn = null, chunk_modelIn = null, hmmIn = null;
		try {
			// token
//			System.out.println("Loading parser data....");
			log.debug("Loading parser data....");
			try {
				tok_modelIn = new FileInputStream("lib/model/en-token.bin");
			} catch (FileNotFoundException e) {
				tok_modelIn = getClass().getResourceAsStream(
						"/nl/uva/biosem/en-token.bin");
				if (null == tok_modelIn)
					throw new FileNotFoundException(
							"TokenizerModel not found. Deliver either file lib/model/en-token.bin or classpath resource /nl/uva/biosem/en-token.bin.");
			}
			TokenizerModel model = new TokenizerModel(tok_modelIn);
			tokenizer = new TokenizerME(model);

			// tagger
			FastCache<String, double[]> cache = new FastCache<String, double[]>(
					50000);
			try {
				hmmIn = new FileInputStream(
						"lib/model/pos-en-bio-genia.HiddenMarkovModel");
			} catch (FileNotFoundException e) {
				hmmIn = getClass().getResourceAsStream(
						"/nl/uva/biosem/pos-en-bio-genia.HiddenMarkovModel");
				if (null == hmmIn)
					throw new FileNotFoundException(
							"POS model not found. Deliver either file lib/model/pos-en-bio-genia.HiddenMarkovModel or classpath resource /nl/uva/biosem/pos-en-bio-genia.HiddenMarkovModel.");
			}
			ObjectInputStream objIn = new ObjectInputStream(hmmIn);
			HiddenMarkovModel hmm = (HiddenMarkovModel) objIn.readObject();
			Streams.closeQuietly(objIn);
			tagger = new HmmDecoder(hmm, null, cache);

			// chunker
			try {
				chunk_modelIn = new FileInputStream("lib/model/en-chunker.bin");
			} catch (FileNotFoundException e) {
				chunk_modelIn = getClass().getResourceAsStream(
						"/nl/uva/biosem/en-chunker.bin");
				if (null == chunk_modelIn)
					throw new FileNotFoundException(
							"Chunker model not found. Deliver either file lib/model/en-chunker.bin or classpath resource /nl/uva/biosem/en-chunker.bin.");
			}
			ChunkerModel chunk_model = new ChunkerModel(chunk_modelIn);
			chunker = new ChunkerME(chunk_model);
			System.out.println("Loading data ... done!");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (tok_modelIn != null) {
				try {
					tok_modelIn.close();
				} catch (IOException e) {
				}
			}

			if (chunk_modelIn != null) {
				try {
					chunk_modelIn.close();
				} catch (IOException e) {
				}
			}
			
			if (hmmIn != null) {
				try {
					hmmIn.close();
				} catch (IOException e) {
				}
			}
		}

	}

	/**
	 * Tag a list of tokens with POS
	 * 
	 * @param tokens
	 * @return: array of POS tags coressponding to tokens
	 */
	public String[] POSTag(String tokens[]) {
		List<String> tokenList = Arrays.asList(tokens);
		Tagging<String> tagging = tagger.tag(tokenList);
		return tagging.tags().toArray(new String[tokens.length]);
	}

	/**
	 * Split text into tokens
	 * 
	 * @param txt
	 *            : sentence
	 * @return: list of words (tokens)
	 */
	public String[] splitWord(String txt) {
		// return tokenizer.tokenize(txt);
		char[] cs = txt.toCharArray();
		return TOKENIZER_FACTORY.tokenizer(cs, 0, cs.length).tokenize();
	}

	/**
	 * Shallow parser: generating chunks (NP,VP, PP) from untokenized text
	 * 
	 * @param token
	 *            : array of word
	 * @return: list of chunks
	 */
	public List<Chunk> parse(String[] token) {
		genia_tag = POSTag(token);
		return parse(token, genia_tag);
	}

	/**
	 * Check CONJ in a chunk
	 * 
	 * @param p1
	 * @param p2
	 * @param tokens
	 * @return
	 */
	private int hasConj(int p1, int p2, String[] tokens) {
		for (int i = p2 - 1; i > p1; i--) {
			if (map.contains(tokens[i])) {
				return i;
			}
		}
		return -1;
	}

	private String formTxt(int start, int end, String[] tokens) {
		String txt = tokens[start];
		for (int i = start + 1; i <= end; i++) {
			txt += " " + tokens[i];
		}
		return txt;
	}

	private void fixConj(List<Chunk> ls, String tokens[]) {
		int pos = 0;
		int j = 0, old_end;
		String new_txt;
		try {
			while (j < ls.size() - 2) {
				Chunk c = ls.get(j);
				Chunk c1 = ls.get(j + 1);
				Chunk c2 = ls.get(j + 2);
				if (c.txt.equals("of") && c2.txt.equals("of")) {
					pos = hasConj(c1.begin, c1.end, tokens);
					if (pos != -1 && !c1.txt.contains(",")) {
						new_txt = formTxt(pos + 1, c1.end, tokens);
						c1.txt = formTxt(c1.begin, pos - 1, tokens);
						Chunk con_chunk = new Chunk("O");
						con_chunk.begin = pos;
						con_chunk.end = pos;
						con_chunk.txt = tokens[pos];
						Chunk new_chunk = new Chunk("NP");
						new_chunk.begin = pos + 1;
						new_chunk.end = c1.end;
						new_chunk.txt = new_txt;
						c1.end = pos - 1;
						ls.add(j + 2, new_chunk);
						ls.add(j + 2, con_chunk);
						j += 3;
						continue;
					}
					break;
				}
				j++;
			}
		} catch (Exception ex) {
			System.out.println("Error at: " + j);
			printChunk(ls);
			System.out.println(ex.getLocalizedMessage());
			System.exit(0);
		}
	}

	public List<Chunk> parse(String[] token, String tags[]) {
		List<Chunk> result = new ArrayList<Chunk>();
		genia_tag = tags;
		String chunks[] = chunker.chunk(token, tags);
		String previous = "";
		Chunk chunk = null;
		int i = 0;
		for (String s : chunks) {
			if (s.startsWith("B-")
					|| (s.startsWith("I-") && !previous
							.endsWith(s.substring(2)))) {
				if (chunk != null) {
					chunk.end = i - 1;
					result.add(chunk);
				}
				chunk = new Chunk(s.substring(2));
				chunk.begin = i;
			}
			if (!s.equals("O")) {
				chunk.addWord(token[i]);
			} else if (chunk != null) {
				chunk.end = i - 1;
				result.add(chunk);
				chunk = new Chunk("O");
				chunk.begin = i;
				chunk.end = i;
				chunk.addWord(token[i]);
				result.add(chunk);
				chunk = null;
			} else {
				chunk = new Chunk("O");
				chunk.begin = i;
				chunk.end = i;
				chunk.addWord(token[i]);
				result.add(chunk);
				chunk = null;
			}
			i++;
			if (i == token.length) {
				if (chunk != null) {
					chunk.end = token.length - 1;
					result.add(chunk);
				}
			}
			previous = s;
		}
		if (debug) {
			System.out.print("--> Before: ");
			printChunk(result);
		}
		result = fixParenthesis(result);
		fixConj(result, token);
		if (debug) {
			System.out.print("\n ---->After: ");
			printChunk(result);
			System.out.println("------------------");
		}
		return result;
	}

	private List<Chunk> fixParenthesis(List<Chunk> ls) {
		int i = 0;
		Chunk tmp, prev = null;
		while (i < ls.size()) {
			tmp = ls.get(i);
			if (tmp.type.equals("O")
					&& (tmp.txt.startsWith("(") || tmp.txt.startsWith("["))) {
				List<Chunk> remove = new ArrayList<Chunk>();
				boolean found = false;
				int j = i + 1;
				while (j < ls.size()) {
					Chunk c = ls.get(j);
					if (c.txt.contains(")") || c.txt.contains("]")) {
						found = true;
						break;
					} else {
						j++;
					}
				}
				if (prev != null && found) {
					for (int k = i; k <= j; k++) {
						Chunk c = ls.get(k);
						prev.txt = prev.txt + " " + c.txt;
						prev.end = c.end;
						remove.add(c);
					}
					for (Chunk c : remove) {
						ls.remove(c);
					}
				} else if (found) {
					for (int k = i + 1; k <= j; k++) {
						Chunk c = ls.get(k);
						tmp.txt = tmp.txt + " " + c.txt;
						tmp.end = c.end;
						remove.add(c);
					}
					for (Chunk c : remove) {
						ls.remove(c);
					}
				}
				if (found) {
					continue;
				}
			} else if (tmp.txt.equals(")") || tmp.txt.equals("]")) {
				if (prev != null) {
					prev.txt = prev.txt + " " + tmp.txt;
					prev.end = tmp.end;
					ls.remove(tmp);
					continue;
				} else {
					System.out.println("--->" + old_txt);
					printChunk(ls);
					System.out.println("--BUG- here:" + tmp.getText()
							+ " POS: " + tmp.begin);
					System.exit(1);
				}

			} else if (prev != null && prev.txt.equals("but")) {
				boolean found = false;
				if (tmp.txt.equals("not")) {
					prev.txt = prev.txt + " " + tmp.txt;
					prev.end = tmp.end;
					ls.remove(tmp);
					found = true;
				} else if (tmp.type.equals("NP") && tmp.txt.startsWith("not ")) {
					prev.txt = prev.txt + " " + "not";
					prev.end += 1;
					tmp.txt = tmp.txt.substring(4);
					tmp.begin += 1;
					found = true;
				}
				if (found) {
					continue;
				}
			} else if (prev != null
					&& (prev.txt.equals("as well") || (prev.txt.equals("as") && tmp.txt
							.startsWith("well")))) {
				if (prev.txt.equals("as well") && tmp.txt.equals("as")) {
					prev.type = "CONJP";
					prev.txt = "as well as";
					prev.end += 1;
					ls.remove(tmp);
					continue;
				} else if (prev.txt.equals("as") && tmp.txt.equals("well")
						&& ls.get(i + 1).txt.equals("as")) {
					prev.type = "CONJP";
					prev.txt = "as well as";
					prev.end += 2;
					ls.remove(i);
					ls.remove(i);
					continue;
				} else if (prev.txt.equals("as") && tmp.txt.equals("well as")) {
					prev.type = "CONJP";
					prev.txt = "as well as";
					prev.end += 2;
					ls.remove(i);
					continue;
				} else if (prev.txt.equals("as")
						&& tmp.txt.startsWith("well as")
						&& tmp.type.equals("NP")) {
					prev.type = "CONJP";
					prev.txt = "as well as";
					prev.end += 2;
					tmp.txt = tmp.txt.substring(8);
					tmp.begin += 2;
				}
			}
			prev = tmp;
			i++;
		}
		return ls;
	}

	public int total = 0;

	public void printChunk(String txt) {
		List<Chunk> ls = parse(splitWord(txt));
		Chunk c;
		for (int k = 0; k < ls.size(); k++) {
			c = ls.get(k);
			System.out.print("[" + c.type + " " + c.txt + "] ");
		}
		System.out.println("");
	}

	public void printChunk(List<Chunk> ls) {
		Chunk c;
		for (int k = 0; k < ls.size(); k++) {
			c = ls.get(k);
			System.out.print("[" + c.type + " " + c.txt + "] ");
		}
		System.out.println("");
	}

	public void testParser(String txt) {
		String tokens[] = splitWord(txt);
		List<Chunk> ls = parse(tokens);
		for (int i = 0; i < tokens.length; i++) {
			System.out.print(tokens[i] + "_" + genia_tag[i] + " ");
		}
		System.out.println("");
		// printChunk(ls);
	}

	public String id;
	public int senid;
	Set<String> map = new HashSet<String>();
	String conj_list[] = { "and", "or" };
	boolean debug = false;

	public static void main(String[] args) {
		// TODO code application logic here
		Parser parser = new Parser();
		String txt = "PRO3 mediates PRO4 induction and PRO5 is efficiently up-regulated by PRO6 and PRO7 in human PRO8+ T cells.";
		parser.printChunk(txt);

	}
}
