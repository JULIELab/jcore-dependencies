package uk.ac.man.entitytagger.entities.species;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import martin.common.ArgParser;
import martin.common.Loggers;
import martin.common.Misc;
import martin.common.Pair;
import martin.common.StreamIterator;
import martin.common.xml.XPath;
import uk.ac.man.entitytagger.generate.DictionaryEntry;
import uk.ac.man.entitytagger.generate.GenerateMatchers;

public class GenerateDictionary {

	/*	public static RegexpMatcher loadRegexpMatcher(String file, File[] extraSynonymFiles, Postprocessor postprocessor, Logger logger){
		HashMap<String,DictionaryEntry> hashMap = loadSpecies(file, extraSynonymFiles, logger);
		return new RegexpMatcher(hashMap, Disambiguation.ON_EARLIER, postprocessor);
	}*/

	/**
	 * Catalogue of Life web service used to perform species lookups in order to determine the accepted names
	 * for species where latin names have been deprecated.
	 */
	private static final String COL_WEBSERVICE_URL = "http://webservice.catalogueoflife.org/annual-checklist/2009/search.php";

	public static void main(String[] args){
		ArgParser ap = new ArgParser(args);

		Logger logger = Loggers.getDefaultLogger(ap);
		File[] extraSynonymFiles = ap.getFiles("extraSynonyms");
		boolean includeLineNumbers = ap.containsKey("includeLineNumbers");

		logger.info("%t: includeLineNumbers = " + includeLineNumbers + "\n");

		int report = ap.getInt("report", -1);		
		
		if (ap.containsKey("inSpecies")){
			File in = ap.getFile("inSpecies");

			if (ap.containsKey("outRegexp")){
				File out = ap.getFile("outRegexp");
				HashMap<String,DictionaryEntry> dict = generateSpeciesDictionary(in, extraSynonymFiles, includeLineNumbers, logger, report);
				save(out, dict, logger);
			}
			if (ap.containsKey("outNames")){
				File out = ap.getFile("outNames");
				HashMap<String,List<String>> dict = generateSpeciesNames(in, extraSynonymFiles, includeLineNumbers, logger);
				
				//used to get comments
				HashMap<String,DictionaryEntry> dictWithComments = generateSpeciesDictionary(in, extraSynonymFiles, includeLineNumbers, logger, report);
				
				saveNames(out, dict, dictWithComments, logger);
			}
		}
	}

	private static void saveNames(File outFile, HashMap<String, List<String>> dict,
			HashMap<String, DictionaryEntry> dictWithComments, Logger logger) {

		logger.info("%t: Saving to file " + outFile.getAbsolutePath() + "...\n");

		try {
			BufferedWriter outStream = new BufferedWriter(new FileWriter(outFile));


			for (String id : dict.keySet()){
				if (dictWithComments.containsKey(id) && dictWithComments.get(id).getComment() != null){
					outStream.write(id + "\t" + Misc.implode(dict.get(id).toArray(new String[0]), "|") + "\t" + dictWithComments.get(id).getComment() + "\n");
				} else {
					outStream.write(id + "\t" + Misc.implode(dict.get(id).toArray(new String[0]), "|") + "\t\n");
				}
			}


			outStream.close();

			logger.info("%t: Done.\n");
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}		
	}

	private static HashMap<String, List<String>> generateSpeciesNames(File in,
			File[] extraSynonymFiles, boolean includeLineNumbers, Logger logger) {

		logger.info("%t: Generating species name variants...\n");

		HashMap<String, List<String>> res = new HashMap<String, List<String>>();

		for (File f : extraSynonymFiles){
			StreamIterator inData = new StreamIterator(f);
			for (String s : inData){
				String[] fields  = s.split("\\t");
				String[] fields2 = fields[1].split("\\|");
				for (String field : fields2){
					addName(res, fields[0], field, "common name");
				}
			}
		}

		StreamIterator inData = new StreamIterator(in);
		int lineNumber = 0;
		for (String s : inData){
			String[] fields = s.split(",");

			if (fields.length == 4){
				String id = "species:ncbi:" + fields[0];
				if (includeLineNumbers)
					id += "|" + lineNumber;

				String type = fields[3];
				if (!type.contains("acronym") && !fields[1].matches("([\\[\\(\\{\\?].*)") && !fields[1].contains("@")){
					String name = fields[1];

					addName(res, id, name, type);
				}
			}

			lineNumber++;
		}

		logger.info("%t: Done.\n");

		return res;
	}

	private static void addName(HashMap<String, List<String>> res, String id,
			String name, String type) {

		while (name.startsWith(" "))
			name = name.substring(1);

		String[] parts = name.split(" ");

		List<String> names = new ArrayList<String>();

		int firstSpace = name.indexOf(" ");
		char c = name.charAt(0);

		if ((type.equals("scientific name") || type.contains("synonym") || type.contains("anamorph")) && parts.length > 1 && !(parts.length == 2 && parts[1].length() < 4 )){
			names.add(Character.toLowerCase(c) + name.substring(1));
			names.add(Character.toUpperCase(c) + name.substring(1));

			names.add(Character.toLowerCase(c) + "." + name.substring(firstSpace));
			names.add(Character.toUpperCase(c) + "." + name.substring(firstSpace));
		} else if ((type.contains("common name") || type.contains("include")) && !name.endsWith("s") && !name.endsWith("family")){
			names.add(Character.toLowerCase(c) + name.substring(1));
			names.add(Character.toUpperCase(c) + name.substring(1));

			names.add(Character.toLowerCase(c) + name.substring(1) + "s");
			names.add(Character.toUpperCase(c) + name.substring(1) + "s");
		} else {
			names.add(Character.toLowerCase(c) + name.substring(1));
			names.add(Character.toUpperCase(c) + name.substring(1));
		}

		if (!res.containsKey(id))
			res.put(id, names);
		else
			res.get(id).addAll(names);
	}

	private static String getAcceptedName(String name){
		if (name.contains("<") || name.contains(">"))
			return null;

		String url = COL_WEBSERVICE_URL + "?name=" + name.replace(" ", "+") + "&format=xml&response=terse";
		try{
			String content = Misc.downloadURL(new URL(url));

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			org.w3c.dom.Document doc = db.parse(new InputSource(new StringReader(content)));

			Node n = XPath.getNode("results/result/accepted_name/name", doc);

			if (n == null)
				return null;
			else{
				return n.getTextContent();
			}

		} catch (Exception e){
			System.err.println(e.toString());
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	private static void save(File out, HashMap<String, DictionaryEntry> dict,
			Logger logger) {
		try{

			logger.info("Writing regular expressions to file " + out.getAbsolutePath() + "...\n");

			BufferedWriter outStream = new BufferedWriter(new FileWriter(out));

			for (String id : dict.keySet())
				outStream.write(dict.get(id).toString() + "\n");

			outStream.close();

		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}		
	}

	private static int addGenusName(DictionaryEntry de, String name, String type){
		int numGen = 1;

		while (name.startsWith(" "))
			name = name.substring(1);

		name = name.replace("\"", "");
		name = name.replace("'", "");
		name = name.replace("(", "\\(");
		name = name.replace(")", "\\)");
		name = name.replace(".", "\\.");
		name = name.replace("<", "\\<");
		name = name.replace(">", "\\>");
		name = name.replace("{", "\\{");
		name = name.replace("}", "\\}");
		name = name.replace("[", "\\[");
		name = name.replace("]", "\\]");

		String[] parts = name.split(" ");
		String[] regExp = new String[parts.length];

		char first = parts[0].charAt(0);

		boolean abbrev = false;

		/*		if ((type.equals("scientific name") || type.contains("synonym") || type.contains("anamorph")) && parts.length > 1 && !(parts.length == 2 && parts[1].length() < 4 )){
			if (parts[0].length() > 1){
				regExp[0] = "(" + Character.toUpperCase(first) + "|" + Character.toLowerCase(first) + ")(\\. ?|" + parts[0].substring(1) + " )";
				abbrev = true;
				numGen = 4;
			}else
				regExp[0] = "(" + Character.toUpperCase(first) + "|" + Character.toLowerCase(first) + ")";
		} else {*/
		regExp[0] = "(" + Character.toUpperCase(first) + "|" + Character.toLowerCase(first) + ")"+parts[0].substring(1);
		//}

		//String[] specialCases = new String[] {};
		String res = regExp[0];

		for (int i = 1; i < parts.length; i++){
			//String s = parts[i];

			//if (s.matches("(a)|(A)|(alpha)|(Alpha)"))
			//regExp[i] = parts[i];

			if (!abbrev || i > 1)
				res += " ";

			res += parts[i];
		}

		if ((type.contains("common name") || type.contains("include")) && !name.endsWith("s") && !name.endsWith("family")){
			res += "s?";
			numGen = 2;
		}

		de.addPattern(res);

		return numGen;
	}

	private static void addSynonyms(HashMap<String,DictionaryEntry> hashMap, File file){
		ArrayList<Pair<String>> entries = new GenerateMatchers().loadExtraSynonyms(file);

		for (Pair<String> e : entries){
			if (!hashMap.containsKey(e.getX())){ //if id doesn't exist
				hashMap.put(e.getX(),new DictionaryEntry(e.getX()));
			}
			hashMap.get(e.getX()).addPattern(e.getY());
		}
	}

	/*	public static RegexpMatcher loadRegexpMatcher(String file, File[] extraSynonymFiles, Postprocessor postprocessor, Logger logger){
		HashMap<String,DictionaryEntry> hashMap = loadSpecies(file, extraSynonymFiles, logger);
		return new RegexpMatcher(hashMap, Disambiguation.ON_EARLIER, postprocessor);
	}*/

	public static HashMap<String,DictionaryEntry> generateGenusDictionary(File file, File[] extraSynonymFiles, boolean includeLineNumbers, Logger logger){
		logger.info("Loading NCBI taxonomy data... ");
		int numPatterns = 0;
		HashMap<String,DictionaryEntry> hashMap = new HashMap<String,DictionaryEntry>();
		int lineCounter =0;

		try {
			BufferedReader inStream = new BufferedReader(new FileReader(file));

			String line = inStream.readLine();

			while (line != null){

				line = line.replaceAll("<.*?,.*?>", "<...>");

				String[] fields = line.split("\t\\|\t");

				String id;
				if (includeLineNumbers)
					id = "genus:ncbi:" + fields[0] + "|" + lineCounter++;
				else
					id = "genus:ncbi:" + fields[0];

				if (fields.length == 4){
					String type = fields[3];

					//					if (type.matches("(synonym)|(includes)|(equivalent name)|(scientific name)|(genbank common name)|(common name)|(genbank anamorph)")){
					//						if (!fields[1].matches("(.*\\s19\\d\\d.*)|(.*\\s20\\d\\d.*)|(.*\\s18\\d\\d.*)|([\\[\\(\\{\\?].*)")){
					if (!type.contains("acronym") && !fields[1].matches("([\\[\\(\\{\\?].*)") && !fields[1].contains("@")){
						String name = fields[1];

						if (!hashMap.containsKey(id))
							hashMap.put(id,new DictionaryEntry(id));

						numPatterns += addGenusName(hashMap.get(id), name, type);
					}

				}
				line = inStream.readLine();
			}

			inStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}

		for (File f : extraSynonymFiles)
			addSynonyms(hashMap, f);

		logger.info("Done, loaded " + hashMap.size() + " genus and "  + numPatterns + " name variants.\n");

		return hashMap;
	}

	public static HashMap<String,DictionaryEntry> generateSpeciesDictionary(File file, File[] extraSynonymFiles, boolean includeLineNumbers, Logger logger, int report){
		logger.info("Loading NCBI taxonomy data... ");
		int numPatterns = 0;
		HashMap<String,DictionaryEntry> hashMap = new HashMap<String,DictionaryEntry>();

		try {
			BufferedReader inStream = new BufferedReader(new FileReader(file));

			String line = inStream.readLine();

			int linecounter = 0;

			while (line != null){
				String[] fields = line.split(",");

				String id;
				if (includeLineNumbers)
					id = "species:ncbi:" + fields[0] + "|" + linecounter;
				else
					id = "species:ncbi:" + fields[0];
				
				linecounter++;

				if (fields.length == 4){
					String type = fields[3];

					//					if (type.matches("(synonym)|(includes)|(equivalent name)|(scientific name)|(genbank common name)|(common name)|(genbank anamorph)")){
					//						if (!fields[1].matches("(.*\\s19\\d\\d.*)|(.*\\s20\\d\\d.*)|(.*\\s18\\d\\d.*)|([\\[\\(\\{\\?].*)")){
					if (!type.contains("acronym") && !fields[1].matches("([\\[\\(\\{\\?\"'].*)") && !fields[1].contains("@")){
						String name = fields[1];
						
						if (name.startsWith(". "))
							name = name.substring(2);

						if (!hashMap.containsKey(id))
							hashMap.put(id,new DictionaryEntry(id));

						numPatterns += addName(hashMap.get(id), name, type);

						if (includeLineNumbers){
							if (type.equals("misnomer") || type.equals("misspelling") || type.equals("in-part"))
								hashMap.get(id).setComment(type);

							if (type.equals("scientific name")){
								String accepted_name = getAcceptedName(name);
								if (accepted_name != null)
									hashMap.get(id).setComment("accepted: \"" + accepted_name + "\"");
							}
						}
					}	
				}
				
				if (report != -1 && linecounter % report == 0)
					logger.info("%t: generateSpeciesDictionary: processed " + linecounter + " lines.\n");
				
				line = inStream.readLine();
			}

			inStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}

		for (File f : extraSynonymFiles)
			addSynonyms(hashMap, f);

		logger.info("Done, loaded " + hashMap.size() + " species and "  + numPatterns + " name variants.\n");

		return hashMap;
	}

	private static int addName(DictionaryEntry de, String name, String type){

		//if (name.equals("environmental samples"))
		//	return 0;

		int numGen = 1;

		while (name.startsWith(" "))
			name = name.substring(1);

		name = GenerateMatchers.escapeRegexp(name);

		String[] parts = name.split(" ");
		String[] regExp = new String[parts.length];

		char first = parts[0].charAt(0);

		boolean abbrev = false;

		if ((type.equals("scientific name") || type.contains("synonym") || type.contains("anamorph")) && parts.length > 1 && !(parts.length == 2 && parts[1].length() < 4 )){
			if (parts[0].length() > 1){
				regExp[0] = "(" + Character.toUpperCase(first) + "|" + Character.toLowerCase(first) + ")(\\. ?|" + parts[0].substring(1) + " )";
				abbrev = true;
				numGen = 4;
			}else
				regExp[0] = "(" + Character.toUpperCase(first) + "|" + Character.toLowerCase(first) + ")";
		} else {
			regExp[0] = "(" + Character.toUpperCase(first) + "|" + Character.toLowerCase(first) + ")"+parts[0].substring(1);
		}

		//String[] specialCases = new String[] {};
		String res = regExp[0];

		for (int i = 1; i < parts.length; i++){
			//String s = parts[i];

			//if (s.matches("(a)|(A)|(alpha)|(Alpha)"))
			//regExp[i] = parts[i];

			if (!abbrev || i > 1)
				res += " ";

			res += parts[i];
		}

		if ((type.contains("common name") || type.contains("include")) && !name.endsWith("s") && !name.endsWith("family")){
			res += "s?";
			numGen = 2;
		}

		de.addPattern(res);

		return numGen;
	}

}
