package uk.ac.man.entitytagger.entities.species;

import martin.common.ArgParser;
import martin.common.Misc;
import martin.common.StreamIterator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

public class CatalogueOfLife {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArgParser ap = new ArgParser(args);

		File inCommon = ap.getFile("inCommon");
		File inScientific = ap.getFile("inScientific");

		if (ap.containsKey("outRegexp")){
			File out = ap.getFile("outRegexp");
			process(inCommon,inScientific, out, ap.getRequired("idPrefix"),true);
		}

		if (ap.containsKey("outVariants")){
			File out = ap.getFile("outVariants");
			process(inCommon,inScientific, out, ap.getRequired("idPrefix"),false);
		}
	}

	private static void process(File inCommon, File inScientific, File out, String idPrefix, boolean doRegexp) {
		try {
			BufferedWriter outStream = new BufferedWriter(new FileWriter(out));

			if (inCommon != null){
				StreamIterator data = new StreamIterator(inCommon);
				data.next(); //skip first row which contains headers only

				for (String s : data){
					String[] fs = s.split("\t");
					if (fs.length == 2){
						String id = idPrefix + fs[0];
						String name = fs[1];

						Set<String> names = getCommonNames(name);

						if (doRegexp)
							throw new IllegalStateException();
						else
							outStream.write(id + "\t" + Misc.unsplit(names, "|") + "\n");					

					}
				}
			}

			if (inScientific != null){
				StreamIterator data = new StreamIterator(inScientific);
				data.next(); //skip first row which contains headers only
				Set<String> coveredNames = new HashSet<String>();

				for (String s : data){
					String[] fs = s.split("\t");
					if (fs.length == 4){
						String id = idPrefix + fs[0];
						String genus = fs[1];
						String sp = fs[2];

						if (fs.length > 3 && fs[3].length() > 0 && !fs[3].equals("NULL"))
							sp += " " + fs[3];

						if (!coveredNames.contains(genus + " " + sp)){

							String v1 = genus + " " + sp;
							String v2 = doRegexp ? (genus.charAt(0) + "\\. " + sp) : (genus.charAt(0) + ". " + sp);
							String v = doRegexp ? "(" + v1 + ")|(" + v2 + ")" : (v1 + "|" + v2);

							outStream.write(id + "\t" + v + "\n");					

							coveredNames.add(genus + " " + sp);
						}
					}
				}
			}

			outStream.close();

		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}		
	}

	private static Set<String> getCommonNames(String name) {
		//	} else if ((type.contains("common name") || type.contains("include")) && !name.endsWith("s") && !name.endsWith("family")){
		//		names.add(Character.toLowerCase(c) + name.substring(1));
		//		names.add(Character.toUpperCase(c) + name.substring(1));
		//
		//		names.add(Character.toLowerCase(c) + name.substring(1) + "s");
		//		names.add(Character.toUpperCase(c) + name.substring(1) + "s");
		//	} else {
		//		names.add(Character.toLowerCase(c) + name.substring(1));
		//		names.add(Character.toUpperCase(c) + name.substring(1));
		//	}



		Set<String> names = new HashSet<String>();

		char c = name.charAt(0);

		if (!name.endsWith("s")){
			names.add(Character.toLowerCase(c) + name.substring(1));
			names.add(Character.toUpperCase(c) + name.substring(1));

			names.add(Character.toLowerCase(c) + name.substring(1) + "s");
			names.add(Character.toUpperCase(c) + name.substring(1) + "s");
		} else {
			names.add(Character.toLowerCase(c) + name.substring(1));
			names.add(Character.toUpperCase(c) + name.substring(1));
		}



		return names;


	}

}
