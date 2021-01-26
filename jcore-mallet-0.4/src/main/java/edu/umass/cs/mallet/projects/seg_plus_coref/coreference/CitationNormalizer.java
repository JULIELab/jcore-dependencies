package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import com.wcohen.secondstring.tokens.SimpleTokenizer;
import com.wcohen.secondstring.tokens.Token;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: michaelhay
 * Date: Feb 17, 2004
 * Time: 8:36:50 PM
 * To change this template use Options | File Templates.
 */
public class CitationNormalizer {
    public static final String[] STOP_WORDS = {
        "a", "and", "are", "as", "at", "be", "but", "by",
        "for", "if", "in", "into", "is", "it",
        "no", "not", "of", "on", "or", "s", "such",
        "t", "that", "the", "their", "then", "there", "these",
        "they", "this", "to", "was", "will", "with",
    };
    private static Hashtable stopTable;
    private static SimpleTokenizer tokenizer;

    private void makeStopTable(String[] stopWords) {
        stopTable = new Hashtable(stopWords.length);
        for (int i = 0; i < stopWords.length; i++)
            stopTable.put(stopWords[i], stopWords[i]);
    }

    public CitationNormalizer() {
        makeStopTable(STOP_WORDS);
        tokenizer = new SimpleTokenizer(true, true);
    }

    public List getTokens(String s) {
        return makeList(tokenizer.tokenize(s));
    }

    public Set getTokensAsSet(String s) {
        return new HashSet(getTokens(s));
    }

    public String norm1(String s) {
        List tokens = makeList(tokenizer.tokenize(s));
        tokens = removeStopWords(tokens);
        tokens = makeSubstitutions(tokens);
        return tokensToString(tokens);
    }

	public String authorNorm(String a) {
		a = a.replaceAll(" and", "");
		a = a.replaceAll("\\p{Punct}", " ");
		a = a.replaceAll("[\t\n\f\r-]","");
		a = a.replaceAll(" +"," ");
		return a;
	}
	
 	public String norm(String s) {
		return norm2(norm1(s));
	}

	// this removes all the spaces and hyphens
	public String norm2(String s) {
		s.replaceAll("[ \t\n\f\r-]","");
		//s.replaceAll("reinforcement learning", ""); // wtf is this? -awc
		return s;
	}

    public String getFourDigitString(String s) {
        List tokens = makeList(tokenizer.tokenize(s));
        for (int i = 0; i < tokens.size(); i++) {
            String token = (String) tokens.get(i);
            if (token.matches("[0-9][0-9][0-9][0-9]")) {
                return token;
            }
        }
        return "";
    }

    private List makeList(Token[] tokens) {
        List strTokens = new ArrayList();
        for (int i = 0; i < tokens.length; i++) {
            strTokens.add(tokens[i].getValue());
        }
        return strTokens;
    }

    public List makeSubstitutions(List tokens) {
        return makeSubstitutions(tokens, substitutionMap());
    }

    public List makeSubstitutions(List tokens, Map subMap) {

        for (int i = 0; i < tokens.size(); i++) {
            String token = (String) tokens.get(i);
            Iterator iterator = subMap.keySet().iterator();
            while (iterator.hasNext()) {
                String s1 = (String) iterator.next();
                String s2 = (String) subMap.get(s1);
                if (s1.equals(token)) {
                    tokens.set(i, s2);
                }
            }
        }
        return tokens;
    }

    private String tokensToString(List tokens) {
        String tokenizedStr = "";
        for (int i = 0; i < tokens.size(); i++) {
            String token = (String) tokens.get(i);
            tokenizedStr += (tokenizedStr == "") ? token
                    : " "+ token;
        }
        return tokenizedStr;
    }

    public List removeStopWords(List tokens) {
        List tokenList = new ArrayList();
        for (int i = 0; i < tokens.size(); i++) {
            String token = (String) tokens.get(i);
            if (stopTable.get(token) == null) {
                tokenList.add(token);
            }
        }
        return tokenList;
    }

    private Map substitutionMap() {
        Map sub = new HashMap();
        sub.put("proc", "proceedings");
        sub.put("conf", "conference");
        sub.put("intl","international");
        sub.put("int","international");
        sub.put("trans","transactions");
        sub.put("assoc", "associates");

        sub.put("jair","journal artificial intelligence research");
        sub.put("nips","advances neural information processing systems");
        sub.put("nrl","naval research laboratory");
        sub.put("colt","computational learning theory");
        sub.put("sigir","international conference research development information retrieval");
        sub.put("cacm","communications association computing machinery");
        sub.put("cmu","carnegie mellon university");
        sub.put("cs","computer science");
        sub.put("ijcai","international joint conference artificial intelligence");
        sub.put("ai","artificial intelligence");
        sub.put("mit","massachusetts institute technology");
        sub.put("icml","international conference machine learning");
        sub.put("ieee","institute electrical electronics engineers");
        sub.put("aaai","national conference american association artificial intelligence");
        sub.put("mlc","international machine learning conference");
        sub.put("ml","international machine learning");
        sub.put("acm", "association computing machinery");
        return sub;
    }

    public String getNumericOnly(String s) {

        List digitTokens = new ArrayList();
        List tokens = makeList(tokenizer.tokenize(s));
        for (int i = 0; i < tokens.size(); i++) {
            String token = (String) tokens.get(i);
            if (token.matches("[0-9]+")) {
                digitTokens.add(token);
            }
        }
        return tokensToString(digitTokens);
    }

    public String getAlphaOnly(String s) {

        List alphaTokens = new ArrayList();
        List tokens = makeList(tokenizer.tokenize(s));
        for (int i = 0; i < tokens.size(); i++) {
            String token = (String) tokens.get(i);
            if (token.matches("[a-zA-Z]+")) {
                alphaTokens.add(token);
            }
        }
        return tokensToString(alphaTokens);
    }

}
