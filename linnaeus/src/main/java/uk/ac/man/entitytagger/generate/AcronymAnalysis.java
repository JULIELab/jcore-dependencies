package uk.ac.man.entitytagger.generate;

import martin.common.ArgParser;
import martin.common.Loggers;
import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.entitytagger.EntityTagger;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.matching.Matcher;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class AcronymAnalysis {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArgParser ap = new ArgParser(args);
		Logger logger = Loggers.getDefaultLogger(ap);
		Matcher matcher = EntityTagger.getMatcher(ap,logger);

		HashMap<String,HashMap<String,Integer>> acromineResults = loadAcromineFile(ap.getFile("acromine"));
		//HashMap<String,Integer> NCBIMappings = loadAcromineFile2(ap.getFileFollower("acromine"));

		HashMap<String,HashMap<String,Integer>> speciesFrequencies = match(acromineResults,matcher);
		HashMap<String,Set<String>> acronymSpeciesMappings = process(speciesFrequencies);

		//check(acronymSpeciesMappings, NCBIMappings);

		if (ap.containsKey("outSynonyms"))
			outputSynonyms(acronymSpeciesMappings, ap.getFile("outSynonyms"));
		if (ap.containsKey("outProbabilities"))
			outputProbabilities(speciesFrequencies, ap.getFile("outProbabilities"));
	}

	private static void outputProbabilities(HashMap<String, HashMap<String, Integer>> speciesFrequencies,File file) {
		try {
			BufferedWriter outStream = new BufferedWriter(new FileWriter(file));

			for (String acr : speciesFrequencies.keySet()){
				HashMap<String,Integer> species = speciesFrequencies.get(acr);

				int sum = 0;

				for (int f : species.values())
					sum += f;

				for (String s : species.keySet()){
					if (!s.equals("-1")){
						double p = ((double)species.get(s)) / (double)sum;
						outStream.write(s + "," + acr + "," + p  +"\n");
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

	/*
	private static void check(HashMap<String, Set<Integer>> acronymSpeciesMappings,HashMap<String, Integer> mappings) {
		for (String a : mappings.keySet())
			if (acronymSpeciesMappings.containsKey(a)){
				if (acronymSpeciesMappings.get(a).size() > 1 || !acronymSpeciesMappings.get(a).contains(mappings.get(a)))
					System.out.println(a + "," + mappings.get(a) + "," + Misc.implode(acronymSpeciesMappings.get(a).toArray(new Integer[0]), "|"));
			} else {
				System.out.println(a + "," + mappings.get(a) + ",");
			}		
	}*/

	private static HashMap<String, Set<String>> process(HashMap<String, HashMap<String, Integer>> speciesFrequencies) {
		HashMap<String, Set<String>> hash = new HashMap<String, Set<String>>();

		for (String a : speciesFrequencies.keySet()){
			int max = 0;
			int sum = 0;

			for (int f : speciesFrequencies.get(a).values()){
				sum += f;
				if (max < f)
					max = f;
			}

			//if (!speciesFrequencies.get(a).containsKey(-1) || speciesFrequencies.get(a).get(-1) < 0.5 * (double) sum){
			for (String s : speciesFrequencies.get(a).keySet()){
				//if (s != -1 && speciesFrequencies.get(a).get(s) > 0.1 * (double) max){
				if (!s.equals("-1")){
					if (!hash.containsKey(a))
						hash.put(a, new HashSet<String>());
					hash.get(a).add(s);
				}
				//}					
			}
			//}
		}

		return hash;
	}

	private static void outputSynonyms(HashMap<String, Set<String>> acronymSpeciesMappings, File file) {
		try{
			BufferedWriter outStream = new BufferedWriter(new FileWriter(file));
			for (String a : acronymSpeciesMappings.keySet()){
				Set<String> sp = acronymSpeciesMappings.get(a);

				//System.out.print(a);
				for (String s : sp)
					outStream.write(s + "," + a + "\n");
				//System.out.print("," + s);
				//System.out.println();
			}
			outStream.close();

		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static HashMap<String, HashMap<String, Integer>> match(HashMap<String, HashMap<String, Integer>> acromineResults, Matcher matcher) {

		HashMap<String,HashMap<String,Integer>> hash = new HashMap<String,HashMap<String,Integer>>();

		for (String acronym : acromineResults.keySet()){
			if (!hash.containsKey(acronym))
				hash.put(acronym, new HashMap<String,Integer>());

			HashMap<String,Integer> h2 = acromineResults.get(acronym);

			for (String lf : h2.keySet()){
				List<Mention> matches = matcher.match(lf, (Document)null);

				String id = "-1";

				for (Mention m : matches)
					if (m.getStart() == 0 && m.getEnd() == lf.length() && m.getIds().length == 1){
						id = m.getIds()[0];
					}


				if (!hash.get(acronym).containsKey(id))
					hash.get(acronym).put(id,h2.get(lf));
				else
					hash.get(acronym).put(id,hash.get(acronym).get(id) + h2.get(lf));
			}
		}

		return hash;
	}

	private static HashMap<String, HashMap<String, Integer>> loadAcromineFile(File file) {
		HashMap<String,HashMap<String,Integer>> hash = new HashMap<String,HashMap<String,Integer>>();
		try{
			BufferedReader inStream = new BufferedReader(new FileReader(file));

			String line = inStream.readLine();
			while (line != null){
				String[] fields = line.split("\t");

				if (!hash.containsKey(fields[1]))
					hash.put(fields[1], new HashMap<String,Integer>());

				hash.get(fields[1]).put(fields[2], Integer.parseInt(fields[3]));

				line = inStream.readLine();
			}

			inStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
		return hash;
	}

	/*
	private static HashMap<String, Integer> loadAcromineFile2(File file) {
		HashMap<String,Integer> hash = new HashMap<String, Integer>();
		try{
			BufferedReader inStream = new BufferedReader(new FileReader(file));

			String line = inStream.readLine();
			while (line != null){
				String[] fields = line.split("\t");

				hash.put(fields[1], Integer.parseInt(fields[0]));

				line = inStream.readLine();
			}

			inStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
		return hash;
	}*/
}
