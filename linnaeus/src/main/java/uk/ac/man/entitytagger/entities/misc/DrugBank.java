package uk.ac.man.entitytagger.entities.misc;

import martin.common.ArgParser;
import martin.common.Loggers;
import uk.ac.man.entitytagger.generate.GenerateMatchers;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This class will, given a DrugBank data file, parse its contents and output a tab-delimited
 * regular expression file suitable for import by the automaton generating software. Using it, an automaton can be 
 * generated that may be used to locate and identify drug names as specified by DrugBank.  
 * @author Martin Gerner
 */
public class DrugBank {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArgParser ap = new ArgParser(args);
		File inFile = ap.getFile("in");
		File outFile = ap.getFile("out");
		Logger logger = Loggers.getDefaultLogger(ap);
		Set<String> stopwords = loadStopwords(ap.getFile("wordlist"), logger);

		run(inFile,outFile,stopwords,logger);
	}

	private static Set<String> loadStopwords(File file, Logger logger) {
		if (file == null){
			logger.info("%t: Not using wordlist.\n");
			return new HashSet<String>();
		}
		
		Set<String> res = new HashSet<String>();
		
		try{
			BufferedReader inStream = new BufferedReader(new FileReader(file));
			

			String line = inStream.readLine();
			while (line != null){
				res.add(line);
				line = inStream.readLine();
			}
			
			inStream.close();
			logger.info("%t: Loaded " + res.size() + " stopwords.\n");
			return res;
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	private static void run(File inFile, File outFile, Set<String> stopset, Logger logger) {
		logger.info("%t: processing " + inFile.getAbsolutePath() + "...\n");

		try{
			BufferedReader inStream = new BufferedReader(new FileReader(inFile));
			BufferedWriter outStream = new BufferedWriter(new FileWriter(outFile));

			outStream.write("#ID\tregexp\tCAS ID\n");

			String line = inStream.readLine();

			while (line != null){
				if (line.startsWith("#BEGIN_DRUGCARD ")){
					String id_DB = line.substring(16);

					Set<String> names = new HashSet<String>();

					while (!line.equals("# Brand_Names:"))
						line = inStream.readLine();

					line = inStream.readLine();
					while (!line.equals("")){
						//if (line.matches("[^\\(\\)]* \\(.*\\)")){
							String[] fs = line.split(" \\(");
							
							if (!stopset.contains(fs[0].toLowerCase()))
								names.add(fs[0]);
							else
								logger.info("removed b\t" + fs[0] + "\n");
							
							if (fs.length > 1)
							logger.info(line + " -> " + fs[0] + "\n");
						//} else {
//							names.add(line);
	//					}
						
						line = inStream.readLine();
					}					

					while (!line.equals("# CAS_Registry_Number:"))
						line = inStream.readLine();

					String id_CAS = inStream.readLine();

					while (!line.equals("# Chemical_IUPAC_Name:"))
						line = inStream.readLine();

					names.add(inStream.readLine());

					while (!line.equals("# Synonyms:"))
						line = inStream.readLine();

					line = inStream.readLine();
					while (!line.equals("")){
						if (!stopset.contains(line.toLowerCase()))
							names.add(line);
						else
							logger.info("removed s\t" + line + "\n");
						line = inStream.readLine();
					}

					String regexp = toregexp(names);

					if (regexp.length() > 0)
						outStream.write("drug:DrugBank:" + id_DB + "\t" + regexp + "\t" + id_CAS + "\n");
				}

				line = inStream.readLine();
			}

			inStream.close();
			outStream.close();
		} catch (Exception e){
			e.printStackTrace();
			System.err.println(e);
			System.exit(-1);
		}
		logger.info("%t: Completed.\n");
	}

	private static String toregexp(Set<String> names) {
		StringBuffer sb = new StringBuffer();

		names.remove("Not Available");

		if (names.size() == 0)
			return "";

		for (String s : names)
			if (sb.length() == 0)
				sb.append("(" + escape(s) + ")");
			else
				sb.append("|(" + escape(s) + ")");

		return sb.toString();
	}

	private static String escape(String s) {
		s = GenerateMatchers.escapeRegexp(s);		
		s = s.replace("\t", " ");

		return s;
	}
}
