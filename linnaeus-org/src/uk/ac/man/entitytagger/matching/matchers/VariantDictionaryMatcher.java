package uk.ac.man.entitytagger.matching.matchers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.matching.Matcher;

import martin.common.CacheMap;
import martin.common.Function;
import martin.common.Misc;
import martin.common.Pair;
import martin.common.Sizeable;
import martin.common.StreamIterator;

/**
 * Class for performing NER dictionary matching against text. The dictionaries should
 * contain a list of all possible variations of the strings that one would like to match
 * 
 * Class objects are created using database details and potentially a identifier; when used the first time,
 * the matcher will load dictionary terms and identifiers from the database for the specified identifier
 * 
 * @author Martin Gerner
 */
public class VariantDictionaryMatcher extends Matcher implements Sizeable {
	/** array containing all terms in the dictionary */
	private String[] terms = null;

	/** array mapping terms to dictionary identifiers: termToIdsMap[i] contains all IDs for the term terms[i] */
	private String[][] termToIdsMap = null;

	/** SQL database connection and table names, from which dictionaries should be loaded initially */
	private Connection conn;
	private String[] tableNames;

	/** the identifier for this particular matcher; dictionary terms will only be loaded from the database where this tag matches a tag column */
	private String tag;

	private final Pattern tokenizationPattern = Pattern.compile("\\b");

	private boolean ignoreCase;

	/** very rough estimate of the dictionary memory footprint, in bytes */
	private long size=-1;

	public VariantDictionaryMatcher(String[][] termToIdsMap, String[] terms, boolean ignoreCase) {
		this.termToIdsMap = termToIdsMap;
		this.terms = terms;
		this.ignoreCase = ignoreCase;
	}

	public int size(){
		return terms.length;
	}

	/**
	 * 
	 * @param conn Connection to the database from which the dictionary should be loaded
	 * @param tableNames Names of the table(s) where terms should be loaded from
	 * @param species species identifier, specifying what part of the tables to load
	 * @param ignoreCase whether to ignore case when matching or not 
	 */
	public VariantDictionaryMatcher(Connection conn, String[] tableNames, String tag, boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
		this.conn = conn;
		this.tableNames =  tableNames;
		this.tag = tag;
	}

	public static VariantDictionaryMatcher load(File inFile, boolean ignoreCase){
		try{
			return load(new FileInputStream(inFile), ignoreCase);
		} catch (FileNotFoundException e){
			System.err.println("Could not find file " + inFile.getAbsolutePath() + ".");
			System.exit(0);
		}

		return null;
	}

	public static VariantDictionaryMatcher load(InputStream stream, boolean ignoreCase){
		Map<String,Set<String>> termToIdsMap = loadStream(stream, ignoreCase);

		String[] terms = new String[termToIdsMap.size()];
		int i = 0;

		for (String term : termToIdsMap.keySet()){
			terms[i++] = term;
		}

		Arrays.sort(terms);

		String[][] termToIdsMapArray = new String[terms.length][];

		for (int j = 0; j < terms.length; j++)
			termToIdsMapArray[j] = termToIdsMap.get(terms[j]).toArray(new String[0]);

		return new VariantDictionaryMatcher(termToIdsMapArray, terms, ignoreCase);
	}

	/**
	 * Will load the dictionary terms and identifiers from the database, sort the terms and set up the proper mappings 
	 */
	private void init(){
		Map<String,Set<String>> termToIdsMap = loadFromDB();

		String[] terms = new String[termToIdsMap.size()];
		int i = 0;

		for (String term : termToIdsMap.keySet()){
			terms[i++] = term;
		}

		Arrays.sort(terms);

		String[][] termToIdsMapArray = new String[terms.length][];

		for (int j = 0; j < terms.length; j++)
			termToIdsMapArray[j] = termToIdsMap.get(terms[j]).toArray(new String[0]);

		this.termToIdsMap = termToIdsMapArray;
		this.terms = terms;	
	}

	public static Map<String,Matcher> loadSeparatedFromDB(Connection conn, String[] tableNames, boolean ignoreCase){
		try{
			Statement stmt = conn.createStatement();
			Map<String,Matcher> res = new HashMap<String, Matcher>();

			for (String tableName : tableNames){
				ResultSet rs = stmt.executeQuery("SELECT DISTINCT(tag) FROM " + tableName);

				while (rs.next()){
					String tag = rs.getString(1);
					if (!res.containsKey(tag))
						res.put(tag, new VariantDictionaryMatcher(conn, tableNames, tag, ignoreCase));
				}
			}

			conn.close();

			return res;
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	/**
	 * Creates a Map from tag identifiers to VariantDictionaryMatcher objects. 
	 * Using the map, dictionaries for particular tags can be retrieved, and used fro gene NER matching for that particular species
	 * The map is cached: the first time that a particular tag is retrieved, it will be loaded from a database; subsequent accesses will use the pre-loaded dictionary
	 * If the total size of the dictionaries (as given by the sum of their sizeof() method) exceeds maxSize, rarely used dictionaries will be unloaded from memory
	 * Access calls for unloaded dictionaries will result in them being loaded from the database again.
	 * @param conn
	 * @param tableNames The tables from which the dictionaries should be loaded
	 * @param maxSize The maximum size that the user would like the dictionaries to occupy, in bytes. The map will try to adhere to this, roughly. Also note that the size estimates of the dictionaries are very rough.
	 * @param logger
	 * @return
	 */
	public static CacheMap<String,VariantDictionaryMatcher> loadSeparatedFromDBCached(final Connection conn, final String[] tableNames, final boolean ignoreCase, final long maxSize, final Logger logger){
		Function<VariantDictionaryMatcher> factory = new Function<VariantDictionaryMatcher>(){
			public VariantDictionaryMatcher function(Object[] args){
				String key = (String) args[0];

				return new VariantDictionaryMatcher(conn, tableNames, key, ignoreCase);
			}
		};

		return new CacheMap<String,VariantDictionaryMatcher>(maxSize, factory, logger);
	}

	public static Map<String,Matcher> loadSeparated(File[] inFiles, boolean ignoreCase){
		Map<String,Matcher> res = new HashMap<String, Matcher>();

		for (File inFile : inFiles){
			Map<String,Map<String,Set<String>>> termToIdsMapSeparated = loadFileSeparated(inFile, ignoreCase);

			for (String k : termToIdsMapSeparated.keySet()){
				Map<String,Set<String>> termToIdsMap = termToIdsMapSeparated.get(k);
				String[] terms = new String[termToIdsMap.size()];
				int i = 0;

				for (String term : termToIdsMap.keySet()){
					terms[i++] = term;
				}

				Arrays.sort(terms);

				String[][] termToIdsMapArray = new String[terms.length][];

				for (int j = 0; j < terms.length; j++)
					termToIdsMapArray[j] = termToIdsMap.get(terms[j]).toArray(new String[0]);

				res.put(k, new VariantDictionaryMatcher(termToIdsMapArray, terms, ignoreCase));
			}
		}

		return res;
	}

	private static Map<String, Map<String, Set<String>>> loadFileSeparated(
			File inFile, boolean ignoreCase) {

		Map<String,Map<String,Set<String>>> res = new HashMap<String, Map<String,Set<String>>>();

		StreamIterator fileData = new StreamIterator(inFile, true);
		for (String s  : fileData){
			String[] fields = s.split("\t");

			if (fields.length < 3)
				throw new IllegalStateException("The input file need three columns when calling loadFileSeparated");

			if (ignoreCase)
				fields[1] = fields[1].toLowerCase();

			if (!res.containsKey(fields[2]))
				res.put(fields[2], new HashMap<String,Set<String>>());

			Map<String,Set<String>> map = res.get(fields[2]);

			String[] names  = fields[1].split("\\|");

			for (String n : names){
				if (!res.containsKey(n))
					map.put(n, new HashSet<String>());

				map.get(n).add(fields[0]);
			}
		}

		return res;
	}

	private static Map<String, Set<String>> loadStream(InputStream inputStream, boolean ignoreCase) {

		Map<String,Set<String>> res = new HashMap<String,Set<String>>();

		StreamIterator fileData = new StreamIterator(inputStream, true);
		int c = 0;

		Pattern tabPattern = Pattern.compile("\t");
		Pattern pipePattern = Pattern.compile("\\|");

		for (String s  : fileData){
			//System.out.println(c++);

			String[] fields = tabPattern.split(s);

			if (ignoreCase)
				fields[1] = fields[1].toLowerCase();

			String[] names  = pipePattern.split(fields[1]);
			for (String n : names){
				if (!res.containsKey(n))
					res.put(n, new HashSet<String>());

				res.get(n).add(fields[0]);
			}
		}

		return res;
	}

	/**
	 * Loads a dictionary from a database
	 * @return
	 */
	private Map<String,Set<String>> loadFromDB(){
		try{
			if (tag != null)
				System.out.println("Loading variantMatcher from " + Misc.implode(this.tableNames,", ") + " (" + tag + ")... ");
			else
				System.out.println("Loading variantMatcher from " + Misc.implode(this.tableNames,", ") + "... ");

			Map<String,Set<String>> res = new HashMap<String,Set<String>>();

			for (String tableName : tableNames){
				ResultSet rs;
				if (tag != null)
					rs = conn.createStatement().executeQuery("SELECT id_entity, name FROM " + tableName + " WHERE tag = '" + tag + "'");
				else
					rs = conn.createStatement().executeQuery("SELECT id_entity, name FROM " + tableName);

				while (rs.next()){
					String id = rs.getString(1);
					String name = rs.getString(2);

					if (ignoreCase)
						name = name.toLowerCase();

					if (!res.containsKey(name)){
						res.put(name, new HashSet<String>());
					}

					res.get(name).add(id);
				}
			}

			return res;
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	/**
	 * @param text
	 * @param doc object containing the document ID for the mentions
	 * @return a list of gene mentions found in the text using the loaded dictionary
	 */
	@Override
	public List<Mention> match(String text, Document doc) {
		if (terms == null || termToIdsMap == null)
			init();

		List<Mention> matches = new ArrayList<Mention>();

		String matchText = this.ignoreCase ? text.toLowerCase() : text;

		String docid = doc != null ? doc.getID() : null;
		java.util.regex.Matcher splitter = this.tokenizationPattern.matcher(matchText);

		List<Pair<Integer>> tokenLocations = new ArrayList<Pair<Integer>>();

		int prev = -1;
		while (splitter.find()){
			if (prev != -1 && Character.isLetterOrDigit(matchText.charAt(prev))){
				tokenLocations.add(new Pair<Integer>(prev, splitter.start()));
			}

			prev = splitter.start();
		}

		for (int i = 0; i < tokenLocations.size(); i++){
			Pair<Integer> p = tokenLocations.get(i);
			List<Integer> foundMatches = getMatchIds(tokenLocations, i, matchText);

			for (int fm : foundMatches){
				String term = ignoreCase ? text.substring(p.getX(), p.getX() + terms[fm].length()) : terms[fm];
				Mention m = new Mention(termToIdsMap[fm].clone(), p.getX(), p.getX() + term.length(), term);
				m.setDocid(docid);
				matches.add(m);
			}
		}

		return matches;
	}

	/**
	 * Performs dictionary matching on the text, finding terms that start with the token 'token'.   
	 * @param tokenLocations a list of all token coordinates in the text
	 * @param token the token for which we would like to start our scan
	 * @param text the text that we would like to scan 
	 * @return
	 */
	private List<Integer> getMatchIds(List<Pair<Integer>> tokenLocations, int i, String matchText) {
		//start token coordinates
		Pair<Integer> p = tokenLocations.get(i);

		//storage for any mentions that have been located
		List<Integer> res = new LinkedList<Integer>();

		//for first round, only look at the first token (don't consider multi-token mentions) 
		int add = 0;

		do {
			//get the text substring stretching from the starting token to the end token
			//the end token is initially the same as the start token, but will gradually increase
			//getX() is the start coordinate of the token, getY() is the end coordinate of the token
			String term = matchText.substring(p.getX(), tokenLocations.get(i+add).getY());

			//search in our dictionary for an exact match to the text substring
			int s = Arrays.binarySearch(terms, term);

			if (s >= 0){
				//We found a term! Add it to our list of recognized terms.
				res.add(s);
			} else if (-s-1 < terms.length){
				//_if_ we would have had a term in the dictionary matching the text substring,
				//it would have been at position -s-1 in the dictionary.
				//Since the dictionary is sorted alphabetically, we can now look at that position
				//to scan for potential terms starting with our substring, indicating that we should 
				//extend our substring to account for multi-token terms.
				if (!terms[-s-1].startsWith(term))
					//There is no term in the dictionary starting with the substring - let's jump
					//out of the do-while loop
					break;
			} else {
				//The position -s-1 is outside of the dictionary length, i.e. there is no term
				//in the dictionary starting with the text substring. Let's jump out of the 
				//do-while loop.
				break;
			}

			//Since we're still in the do-while loop, we have either:
			//1) found an exact match, or
			//2) found a term in the dictionary starting with the substring we're currently looking at.
			//
			//In either case, it is possible that we may have hit a multi-token term.

			//Increase add by one, allowing us to look one token position further into the text.
			add++;

			//while (we don't go outside the text length)
		} while (i + add < tokenLocations.size());

		//We are out of the do-while loop, meaning that we have either hit the end of the text, or 
		//have determined that there are no further possibilities for multi-token terms in the dictionary.

		return res;
	}

	/**
	 * Gives a rough estimate of the memory consumption of this object.
	 */
	public long sizeof() {
		//data type size reference: http://java.sun.com/docs/books/tutorial/java/nutsandbolts/datatypes.html

		//load the dictionary if it's not yet loaded...
		if (terms == null || termToIdsMap == null)
			init();

		//if we've already computed the size, then use that value
		if (this.size != -1)
			return this.size;

		long size = 0;

		for (String t : terms)
			size += t.length() * 2; //each char in the string consumes two bytes

		for (int i = 0; i < termToIdsMap.length; i++)
			for (int j = 0; j < termToIdsMap[i].length; j++)
				size += termToIdsMap[i][j].length() * 2; //each char in the string consumes two bytes

		this.size = size;
		return size;				
	}
}