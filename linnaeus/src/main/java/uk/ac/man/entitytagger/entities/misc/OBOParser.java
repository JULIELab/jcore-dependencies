package uk.ac.man.entitytagger.entities.misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import martin.common.ArgParser;
import martin.common.SQL;
import martin.common.StreamIterator;
import martin.common.Loggers;
import martin.common.Misc;

import uk.ac.man.entitytagger.generate.DictionaryEntry;
import uk.ac.man.entitytagger.generate.GenerateMatchers;

public class OBOParser {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArgParser ap = new ArgParser(args);

		Logger logger = Loggers.getDefaultLogger(ap);

		File[] inFiles = ap.getFiles("in");
		File outFile = ap.getFile("out");
		Connection conn = ap.containsKey("outDB") ? SQL.connectMySQL(ap, logger, "dictionaries") : null;

		File invalidParentsFile = ap.getFile("ipf");
		File invalidParentsRecursiveFile = ap.getFile("iprf");

		PreparedStatement pstmt = conn != null ? GenerateMatchers.initVariantTable(conn, ap.get("outDB"), !ap.containsKey("noClear")) : null;
		
		run(inFiles, outFile, invalidParentsFile, invalidParentsRecursiveFile, logger, pstmt, ap.get("namespace"));	
	}

	private static void run(File[] inFiles, File outFile, File invalidParentsFile, File invalidParentsRecursiveFile, Logger logger, PreparedStatement pstmt, String requiredNamespace) {
		try{
			BufferedWriter outStream = outFile != null ? new BufferedWriter(new FileWriter(outFile)) : null;
			if (outStream != null)
				outStream.write("#id\tregexp\tcomment\n");

			//these sets are used to remove certain branches of the OBO structure
			//used for branches that causes excessive FPs
			Set<String> invalidParents = invalidParentsFile != null ? Misc.loadStringSetFromFile(invalidParentsFile) : new HashSet<String>();
			Set<String> invalidParentsRecursive = invalidParentsRecursiveFile != null ? Misc.loadStringSetFromFile(invalidParentsRecursiveFile) : new HashSet<String>();

			logger.info("%t: Loaded " + invalidParents.size() + " invalid IDs.\n");
			logger.info("%t: Loaded " + invalidParentsRecursive.size() + " invalid recursive IDs.\n");

			populateRecursive(invalidParentsRecursive, invalidParents, inFiles);

			logger.info("%t: Populate recursive IDs, total now: " + invalidParentsRecursive.size() + "\n");

			for (File file : inFiles){
				logger.info("%t Processing file " + file.getAbsolutePath() + "... ");
				StreamIterator fileContents = new StreamIterator(file);

				for (String line : fileContents){
					if (line.equals("[Term]")){
						String id=null;
						String namespace=null;
						List<String> names = new ArrayList<String>();
						List<String> parents = new ArrayList<String>();

						while (!line.equals("")){
							line = fileContents.next();

							if (line.startsWith("id: ")){
								id = line.substring(4);
							} else if (line.startsWith("namespace: ")){
								namespace = line.substring(11);
							} else if (line.startsWith("name: ")){
								names.add(line.substring(6));
							} else if (line.startsWith("synonym: \"")){
								int end = line.indexOf('"', 10);
								names.add(line.substring(10,end));
							} else if (line.startsWith("is_a: ")){
								int end = line.indexOf(' ', 7);
								if (end != -1)
									parents.add(line.substring(6,end));
								else
									parents.add(line.substring(6));
							}
						}

						boolean isInvalid = false;

						for (String p : parents)
							if (invalidParentsRecursive != null && invalidParentsRecursive.contains(p)){
								isInvalid = true;
								invalidParentsRecursive.add(id);								
							}

						if (invalidParents != null && invalidParents.contains(id)){
							isInvalid = true;
						}
						
						if (requiredNamespace != null && (namespace == null || !namespace.equals(requiredNamespace)))
							isInvalid = true;
						
						if (id == null)
							isInvalid = true;

						if (!isInvalid){
							names = regexpify(names, id);
							filterNames(names);

							if (id != null && names.size() > 0){
								DictionaryEntry de = new DictionaryEntry(id, Misc.implode(parents.toArray(new String[0]), "|"));

								for (int i = 0; i < names.size(); i++)
									de.addPattern(names.get(i));

								if (outStream != null)
									outStream.write(de.toString() + "\n");
								if (pstmt != null)
									de.saveVariantsToDB(pstmt, null);
							}
						}
					}
				}

				logger.info(" done.\n");
			}

			if (outStream != null)
				outStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static void populateRecursive(Set<String> invalidParentsRecursive,
			Set<String> invalidParents, File[] inFiles) {

		Map<String,List<String>> children = new HashMap<String,List<String>>();

		for (File f : inFiles){
			StreamIterator fi = new StreamIterator(f);
			for (String str : fi){
				if (str.equals("[Term]")){
					String id = null;
					while (str.length() > 0){
						if (str.startsWith("id: "))
							id = str.substring(4);
						if (str.startsWith("is_a: ") && id != null){
							int end = str.indexOf(' ', 7);
							String p = end != -1 ? str.substring(6,end) : str.substring(6);
							if (!children.containsKey(p))
								children.put(p, new ArrayList<String>());
							children.get(p).add(id);
						}
						str = fi.next();
					}
				}
			}
		}

		for (String s : invalidParentsRecursive)
			clear(children, s, invalidParents);
	}

	private static void clear(Map<String, List<String>> children, String s, Set<String> invalidParents) {
		invalidParents.add(s);
		System.out.println("clearing " + s);

		if (children.containsKey(s))
			for (String c : children.get(s))
				clear(children, c, invalidParents);

	}

	private static List<String> regexpify(List<String> names, String id) {
		List<String> res = new ArrayList<String>();
		for (String name : names)
			res.add(regexpify(name, id));
		return res;
	}

	/*
	private static List<String> modify_EMAP(List<String> names) {
		List<String> res = new ArrayList<String>();

		for (String name : names){
			if (name.startsWith("TS")){
				String[] fields =  name.split(",");

				for (int i = 1; i < fields.length; i++)
					res.add(fields[i]);

			} else {
				res.add(name);
			}
		}

		return res;
	}*/

	/**
	 * Removes any entries in names that have a length shorter than four characters (that would cause a very large number of FNs)
	 * @param names
	 */
	private static void filterNames(List<String> names) {
		for (int i = 0; i < names.size(); i++){
			if (names.get(i) == null || names.get(i).length() < 3)
				names.remove(i--);
		}
	}

	private static String regexpify(String str, String id) {
		if (str.length() == 0)
			return null;
		if (str.length() < 3)
			return null;
		if (str.length() == 3 && !str.toLowerCase().equals(str))
			return null;
		if (str.length() == 3 && (id.startsWith("FBbt:") || id.startsWith("TGMA:") || id.startsWith("WBbt:") || id.startsWith("ZFA:")))
			return null;
		if (str.length() == 3)
			System.out.print(str + ", ");
			

		str = str.replace('_', ' ');

		str = GenerateMatchers.escapeRegexp(str);

		if (str.startsWith("\\"))
			return null;

		if (Character.isLetter(str.charAt(0)))
			str = "[" + str.substring(0,1).toUpperCase() + str.substring(0,1).toLowerCase() + "]" + str.substring(1);

		if ("abcdefghijklmnoprtu".contains(str.substring(str.length()-1,str.length())))
			str += "s?";

		return str;
	}
}
