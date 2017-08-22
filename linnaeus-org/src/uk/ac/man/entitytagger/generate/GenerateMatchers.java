package uk.ac.man.entitytagger.generate;

import martin.common.ArgParser;
import martin.common.Misc;
import martin.common.StreamIterator;
import martin.common.Loggers;
import martin.common.Pair;
import martin.common.Tuple;
import martin.common.compthreads.IteratorBasedMaster;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.logging.Logger;
import java.io.*;

import dk.brics.automaton.Automaton;

/**
 * Main class for loading a species name dictionary and custom synonyms, and then calling other classes to generate regular expressions for species entries and convert them into automatons for efficient text matching.  
 * @author Martin
 */
public class GenerateMatchers {

	public ArrayList<Pair<String>> loadExtraSynonyms(File file){
		System.out.print("Loading " + file.getAbsolutePath() + "...");
		ArrayList<Pair<String>> entries = new ArrayList<Pair<String>>();

		try{
			BufferedReader inStream = new BufferedReader(new FileReader(file));
			String line = inStream.readLine();
			while (line != null){
				if (!line.startsWith("#") && line.length() > 0){
					String[] fields = line.split("\t");
					entries.add(new Pair<String>(fields[0],fields[1]));
				}
				line = inStream.readLine();
			}
			inStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}

		System.out.println(" done, loaded " + entries.size() + " synonyms.");

		return entries;
	}

	private static ArrayList<DictionaryEntry> hashToList(HashMap<String,DictionaryEntry> hashMap){
		ArrayList<DictionaryEntry> retres = new ArrayList<DictionaryEntry>();
		for (DictionaryEntry de : hashMap.values()){
			retres.add(de);
		}
		return retres;		
	}

	/**
	 * Will create a database table and prepare it for insertion of dictionary entries
	 * @param conn the established sql connection
	 * @param tableName name of the database table
	 * @param clear whether to create and clear the table first
	 * @return a PreParedStatement expecting three arguments: entity id, entity name, tag.
	 */
	public static PreparedStatement initVariantTable(Connection conn, String tableName, boolean clear){
		String deletestmt = "DROP TABLE IF EXISTS `" + tableName + "`";

		String createstmt = "CREATE TABLE  `" + tableName + "` (" +
		"`id` int(10) unsigned NOT NULL auto_increment," +
		"`id_entity` varchar(128) NOT NULL," +
		"`name` TEXT NOT NULL," +
		"`tag` varchar(4096) default NULL," +
		"PRIMARY KEY  (`id`)," +
		"KEY `Index_2` (`tag`)" +
		") ENGINE=MyISAM DEFAULT CHARSET=latin1;";

		try{
			if (clear){
			conn.createStatement().execute(deletestmt);
			conn.createStatement().execute(createstmt);
			}
			return conn.prepareStatement("INSERT INTO " + tableName + " (id_entity,name,tag) VALUES (?,?,?)");
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}

		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArgParser ap = new ArgParser(args);

		if (args.length == 0 || ap.containsKey("help")){
			System.out.println("Usage (default values in capitals): generate.jar [--taxonomy <taxonomy data file> [--examine <species>] [--report <report interval>]]|[--loadArray <file>]|[--loadRArray <file>] [--multiJoin <num>] [--minimize] [--storeArray <file>] [--storeRArray <file>] [--threads <number of threads>]");
			System.exit(0);
		}

		Logger logger = Loggers.getDefaultLogger(ap);

		int numThreads = ap.containsKey("threads") ? ap.getInt("threads") : 1;
		int report = ap.getInt("report", -1);

		Tuple<ArrayList<Automaton>, Boolean> automatons = null;

		if (ap.containsKey("species") || ap.containsKey("genus") || ap.containsKey("regexp")){
			logger.info("Using additional synonym files: " + ap.containsKey("extraSynonymFiles") + "\n");

			File[] extraSynonymFiles = ap.getFiles("extraSynonymFiles");
			boolean includeLineNumbers = ap.containsKey("includeLineNumbers");

			logger.info("Including line numbers: " + includeLineNumbers + "\n");

			HashMap<String,DictionaryEntry> dict = null;
			if (ap.containsKey("species"))
				dict = uk.ac.man.entitytagger.entities.species.GenerateDictionary.generateSpeciesDictionary(ap.getFile("species"), extraSynonymFiles, includeLineNumbers, logger, report);
			if (ap.containsKey("genus"))
				dict = uk.ac.man.entitytagger.entities.species.GenerateDictionary.generateGenusDictionary(ap.getFile("genus"), extraSynonymFiles, includeLineNumbers, logger);
			if (ap.containsKey("regexp"))
				dict = loadRegexp(ap.getFile("regexp"), includeLineNumbers, logger);

			if (ap.containsKey("listKeys") && dict != null){
				for (String k : dict.keySet())
					System.out.println("'" + k + "'");
			}

			if (ap.containsKey("examine") && dict != null){
				String[] ids = ap.gets("examine");
				for (int i = 0; i < ids.length; i++)
					if (dict.containsKey(ids[i]))
						System.out.println(ids[i] + ": " + dict.get(ids[i]).getRegexp());
					else
						System.out.println(ids[i] + ": <does not exist in dictionary>");
			}

			if (ap.containsKey("examineGraph") && dict != null){
				String[] ids = ap.gets("examineGraph");
				ArrayList<DictionaryEntry> de = new ArrayList<DictionaryEntry>();
				for (int i = 0; i < ids.length; i++)
					if (dict.containsKey(ids[i]))
						de.add(dict.get(ids[i]));
					else
						System.out.println(ids[i] + ": <does not exist in dictionary>");

				boolean ignoreCase  = ap.containsKey("ignoreCase");
				ArrayList<Automaton> as = new GenerateAutomatons().toAutomatons(de, numThreads, -1, ignoreCase, logger);

				for (Automaton a : as)
					System.out.println(a.toDot());
			}

			if (ap.containsKey("convertToVariants") || ap.containsKey("convertToVariantsDB")){
				File outFile = ap.getFile("convertToVariants");
				PreparedStatement pstmt = ap.containsKey("convertToVariantsDB") ? initVariantTable(martin.common.SQL.connectMySQL(ap, logger, "dictionaries"), ap.get("convertToVariantsDB"), true) : null;

				convertToVariants(dict, outFile, pstmt, numThreads, logger, report);
			}

			if (ap.containsKey("storeArray") || ap.containsKey("storeRArray")){
				if (dict != null){
					logger.info("%t: Escaping dictionary ID regular expressions...");
					dict = escapeIDs(dict);
					logger.info(" done.\n");
				}

				boolean ignoreCase  = ap.containsKey("ignoreCase");
				automatons = new Tuple<ArrayList<Automaton>, Boolean>(new GenerateAutomatons().toAutomatons(hashToList(dict), numThreads, ap.getInt("report"), ignoreCase, logger), ignoreCase);
			}

		} else if (ap.containsKey("loadArray")){
			logger.info("%t: Loading array...");
			automatons = GenerateAutomatons.loadArray(ap.getFile("loadArray"));
			logger.info(" done. Loaded " + automatons.getA().size() + " automatons from file " + ap.get("loadArray") + ".\n");
		}

		if  (ap.containsKey("multiJoin") || ap.containsKey("minimize")){
			ArrayList<Automaton> l = new GenerateAutomatons().process(automatons.getA(), ap.getInt("multiJoin",1), ap.containsKey("minimize"), true, numThreads, logger);
			automatons = new Tuple<ArrayList<Automaton>, Boolean>(l, automatons.getB());
		}

		if (ap.containsKey("storeArray") && automatons != null){
			logger.info("%t: Storing...");
			GenerateAutomatons.storeArray(ap.getFile("storeArray"), automatons.getA(), automatons.getB());
			logger.info(" done. Stored " + automatons.getA().size() + " arrays to file " + ap.get("storeArray") + ".\n");
		}

		if (ap.containsKey("storeRArray")){
			GenerateAutomatons.storeRArray(automatons.getA(), automatons.getB(), ap.containsKey("tableize"), ap.getFile("storeRArray"),logger);
		}
	}

	private static void convertToVariants(
			HashMap<String, DictionaryEntry> dict, File file, PreparedStatement pstmt, int numThreads,
			Logger logger, int report) {

		IteratorBasedMaster<Tuple<DictionaryEntry,Set<String>>> master = new IteratorBasedMaster<Tuple<DictionaryEntry,Set<String>>>(new ConvertToVariantsProblemIterator(dict),numThreads);
		master.startThread();

		try{
			BufferedWriter outStream = file != null ? new BufferedWriter(new FileWriter(file)) : null;
			int c = 0;
			for (Tuple<DictionaryEntry,Set<String>> t : master){
				if (outStream != null){
					String s = t.getA().getId() + "\t" + Misc.unsplit(t.getB(), "|") + "\t";
					if (t.getA().getComment() != null)
						s += t.getA().getComment();

					outStream.write(s + "\n");
				}

				if (pstmt != null){
					Set<String> set = t.getB();
					DictionaryEntry de = t.getA();

					for (String s : set){
						pstmt.setString(1, de.getId());
						pstmt.setString(2, s);
						if (de.getComment() != null)
							pstmt.setString(3, de.getComment());
						else
							pstmt.setNull(3, java.sql.Types.NULL);
						pstmt.addBatch();
					}
					pstmt.executeBatch();
				}

				if (report != -1 && ++c % report == 0)
					logger.info("%t: Converted " + c + " dictionary entries to variants.\n");
			}

			if (outStream != null)
				outStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static HashMap<String, DictionaryEntry> escapeIDs(
			HashMap<String, DictionaryEntry> dict) {

		HashMap<String, DictionaryEntry> res = new HashMap<String, DictionaryEntry>();

		for (String k : dict.keySet()){
			DictionaryEntry v = dict.get(k);
			v.setId(escapeRegexp(v.getId()));

			res.put(k, dict.get(k));
		}

		return res;
	}

	private static HashMap<String, DictionaryEntry> loadRegexp(File file, boolean includeLineNumbers, Logger logger) {
		if (file != null)
			logger.info("%t: Loading regular expressions from file " + file.getAbsolutePath() + "...");
		else
			logger.info("%t: Loading regular expressions from STDIN...");

		HashMap<String,DictionaryEntry> dict = new HashMap<String, DictionaryEntry>();

		int lineCounter = 0;

		StreamIterator data = file != null ? new StreamIterator(file, true) : new StreamIterator(System.in, true);

		for (String line : data){
			String[] fields = line.split("\t");

			String id;
			if (includeLineNumbers)
				id = fields[0] + "|" + lineCounter++;
			else
				id = fields[0];

			if (!dict.containsKey(id)){
				DictionaryEntry e = new DictionaryEntry(id);
				e.addPattern(fields[1]);
				dict.put(id,e);						
			} else {
				dict.get(id).addPattern(fields[1]);
			}					
		}

		logger.info(" done.\n");

		return dict;
	}

	public static String escapeRegexp(String s) {
		s = s.replace("\\", "\\\\");

		s = s.replace("(", "\\(");
		s = s.replace(")", "\\)");
		s = s.replace("[", "\\[");
		s = s.replace("]", "\\]");
		s = s.replace("{", "\\{");
		s = s.replace("}", "\\}");
		s = s.replace("<", "\\<");
		s = s.replace(">", "\\<");

		s = s.replace("*", "\\*");
		s = s.replace(".", "\\.");
		//s = s.replace("-", "\\-");

		s = s.replace("+", "\\+");
		s = s.replace("?", "\\?");

		s = s.replace("&", "\\&");
		s = s.replace("|", "\\|");

		s = s.replace("^", "\\^");
		s = s.replace("$", "\\$");
		s = s.replace("~", "\\~");
		s = s.replace("#", "\\#");
		s = s.replace("@", "\\@");

		s = s.replace("'", "\\'");
		s = s.replace("\"", "\\\"");
		return s;
	}
}
