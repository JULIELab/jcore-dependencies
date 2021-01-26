package uk.ac.man.entitytagger.evaluate;

import martin.common.ArgParser;
import martin.common.compthreads.IteratorBasedMaster;
import martin.common.r.RGraphics;
import uk.ac.man.documentparser.DocumentParser;
import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.documentparser.input.DocumentIterator;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.matching.matchers.ACIDMatcher;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Evaluate {

	/**
	 * Simple dataholder class for a single mention
	 * @author Martin
	 *
	 */
	class Tag{
		int start,end;

		public Tag(int start, int end){
			this.start = start;
			this.end = end;
		}
		public Tag(Mention m) {
			this(m.getStart(), m.getEnd());
		}
		public boolean equals(Tag t){
			if (start == -1 || end == -1)
				return false;

			return t.start == start && t.end == end;
		}
		public boolean overlaps(Tag t){
			if (start == -1 || end == -1)
				return false;

			int s1 = start;
			int s2 = t.start;
			int e1 = end;
			int e2 = t.end;

			return (s1 >= s2 && s1 < e2) || (s2 >= s1 && s2 < e1);		
		}
		public String toString(){
			return "[" + start + "," + end + "]";
		}
	}

	/**
	 * The main evaluation processing method
	 * @param mainTags main tag set
	 * @param mainTagsByDoc main tag set, separated by document
	 * @param refTags reference (gold-standard) tag set
	 * @param refTagsByDoc reference (gold-standard) tag set, separated by document
	 * @param articleConversionMap mapping between equivalent article IDs (e.g. PMID <-> PMCID)
	 * @param validEntities a set of all entities that are valid (anything not in this set is ignored)
	 * @param logFile the file where a list of TPs, FPs and FNs should be listed (may be null)
	 * @param print if true, will print evaluation results to System.out
	 * @param validDocIDs similar to validEntities, a set of document IDs that are valid (mentions from any documents not in this set will be ignored)
	 * @param title job title (will be printed before results)
	 * @return Result[]{document-level-result, mention-level-result}
	 */
	Result[] process(
			Map<String, List<Mention>> mainTags, 
			Map<String, Map<String, List<Tag>>> mainTagsByDoc, 
			Map<String, List<Mention>> refTags, 
			Map<String, Map<String, List<Tag>>> refTagsByDoc, 
			Map<String,String> articleConversionMap, 
			Set<String> validEntities,
			File logFile,
			boolean print,
			Set<String> validDocIDs,
			String title
	) {
		try {
			Result documentLevelResult = new Result();
			Result microLevelResult = new Result();
			Result macroLevelResult = new Result();

			BufferedWriter logStream = logFile != null ? new BufferedWriter(new FileWriter(logFile)) : null;

			Set<String> mainDocsToProcess = mainTags.keySet(); 
			Set<String> refDocsToProcess = refTags.keySet(); 

			Map<String,List<Mention>> microFPs = new HashMap<String,List<Mention>>();
			Map<String,List<Mention>> microFNs = new HashMap<String,List<Mention>>();
			Map<String,List<Mention>> macroFPs = new HashMap<String,List<Mention>>();
			Map<String,List<Mention>> macroFNs = new HashMap<String,List<Mention>>();

			//run over all documents in the main set
			for  (String d : mainDocsToProcess){
				//process the document if valid, otherwise ignore
				if (validDocIDs == null || validDocIDs.contains(d) || (articleConversionMap != null && articleConversionMap.containsKey(d) && validDocIDs.contains(articleConversionMap.get(d)))){

					//get tags for this specific document (converting the document id if necessary)
					List<Mention> mainDocTags = mainTags.get(d);
					Map<String, List<Tag>> refDocTags = refTagsByDoc.get(d);

					if (refDocTags == null && articleConversionMap != null && articleConversionMap.get(d) != null)
						refDocTags = refTagsByDoc.get(articleConversionMap.get(d));

					Set<String> allDocumentTPIDs = new HashSet<String>();
					Set<String> allDocumentFPIDs = new HashSet<String>();

					//run over all mentions
					for (Mention m : mainDocTags){
						boolean microOK=false;
						boolean macroOK = false;
						String documentEntityOK = null;
						boolean validEntity = false;

						//run over all entity possibilities for the mention (there may be several if mentio is ambiguous)
						for (String id : m.getIds()){
							//process if the entity if valid, otherwise ignore
							if (validEntities.contains(id)){
								validEntity = true;
								//if the document exists in the reference tag set, and the entity has been seen in the reference document 
								if (refDocTags != null && refDocTags.containsKey(id)){

									//document-level is OK, since entity has been seen in ref document
									documentEntityOK = id;

									//get all mentions for the reference document, run over them and check
									//whether they match with the mention being processed
									List<Tag> refList = refDocTags.get(id);

									Tag mt = new Tag(m);

									for (Tag rt : refList){
										if (mt.equals(rt)){
											microOK=true;
											macroOK=true;
											break;								
										} else if (mt.overlaps(rt)){
											macroOK = true;
										}
									}
								}
							}
						}

						//if the entity type is valid
						if (validEntity){
							//if document-level TP, add the entity to the list of document-level TP entities, otherwise list of FPs
							if (documentEntityOK != null)
								allDocumentTPIDs.add(documentEntityOK);
							else
								allDocumentFPIDs.add(m.getIds()[0]);

							//mention-level
							if (microOK){
								microLevelResult.tp++;
								if (logStream != null)
									logStream.write("micro\ttp\t" + m.toString() + "\n");
							}else{
								microLevelResult.fp++;

								//if (logStream != null)
								//	logStream.write("micro\tfp\t" + m.toString() + "\n");

								if (!microFPs.containsKey(d))
									microFPs.put(d, new ArrayList<Mention>());
								microFPs.get(d).add(m);								
							}

							if (macroOK){
								macroLevelResult.tp++;
								if (logStream != null)
									logStream.write("macro\ttp\t" + m.toString() + "\n");
							}else{
								macroLevelResult.fp++;
								//if (logStream != null)
								//	logStream.write("macro\tfp\t" + m.toString() + "\n");

								if (!macroFPs.containsKey(d))
									macroFPs.put(d, new ArrayList<Mention>());
								macroFPs.get(d).add(m);								
							}
						}
					}
					documentLevelResult.tp += allDocumentTPIDs.size();
					documentLevelResult.fp += allDocumentFPIDs.size();

					if (logStream != null){
						for (String id : allDocumentTPIDs)
							logStream.write("document,tp," + id + "," + d + "\n");
						for (String id : allDocumentFPIDs)
							logStream.write("document,fp," + id + "," + d + "\n");
					}

				}
			}

			//run over all documents in the reference set
			//works in the same way as section above, but the other way around
			//(reference is compared to main set, so any "misses" are FNs rather than FPs)
			for  (String d : refDocsToProcess){
				if (validDocIDs == null || validDocIDs.contains(d) || (articleConversionMap != null && articleConversionMap.containsKey(d) && validDocIDs.contains(articleConversionMap.get(d)))){
					List<Mention> refDocTags = refTags.get(d);
					Map<String, List<Tag>> mainDocTags = mainTagsByDoc.get(d);

					if (mainDocTags == null && articleConversionMap != null && articleConversionMap.get(d) != null)
						mainDocTags = mainTagsByDoc.get(articleConversionMap.get(d));

					Set<String> allDocumentFNIDs = new HashSet<String>();
					for (Mention m : refDocTags){
						boolean macroOK = false, microOK=false, documentOK=false;
						boolean validID = false;

						for (String id : m.getIds()){
							if (validEntities.contains(id)){
								validID = true;
								if (mainDocTags != null && mainDocTags.containsKey(id)){
									documentOK=true;
									List<Tag> mainList = mainDocTags.get(id);

									Tag rt = new Tag(m);

									for (Tag mt : mainList){
										if (mt.equals(rt)){
											macroOK=true;
											microOK=true;
											break;								
										} else if (mt.overlaps(rt)){
											macroOK=true;								
										}
									}
								}
							}
						}

						if (validID){
							if (!documentOK)
								allDocumentFNIDs.add(m.getIds()[0]);

							if (!microOK){
								microLevelResult.fn++;

								//if (logStream != null)
								//	logStream.write("micro\tfn\t" + m.toString() + "\n");

								if (!microFNs.containsKey(d))
									microFNs.put(d, new ArrayList<Mention>());
								microFNs.get(d).add(m);								
							}
							if (!macroOK){
								macroLevelResult.fn++;
								//if (logStream != null)
								//	logStream.write("macro\tfn\t" + m.toString() + "\n");

								if (!macroFNs.containsKey(d))
									macroFNs.put(d, new ArrayList<Mention>());
								macroFNs.get(d).add(m);								
							}

						}
					}
					documentLevelResult.fn += allDocumentFNIDs.size();
					for (String id : allDocumentFNIDs)
						if (logStream != null)
							logStream.write("document\tfn\t" + id + "," + d + "\n");

				}
			}

			Map<String,List<Mention>> microFPFNs = new HashMap<String,List<Mention>>();
			Map<String,List<Mention>> macroFPFNs = new HashMap<String,List<Mention>>();

			for (String d : microFPs.keySet()){
				List<Mention> fps = microFPs.get(d);
				List<Mention> fns = microFNs.get(d);
				if (fps != null && fns != null){
					for (int i = 0; i < fps.size(); i++){
						int found = -1;
						for (int j = 0; j < fns.size(); j++){
							if (fps.get(i).overlaps(fns.get(j))){
								found = j;
								break;
							}
						}
						if (found != -1){
							if (!microFPFNs.containsKey(d))
								microFPFNs.put(d, new ArrayList<Mention>());
							microFPFNs.get(d).add(fps.get(i));
							fps.remove(i--);
							fns.remove(found);
							microLevelResult.fpfn++;
						}
					}
				}
			}

			for (String d : macroFPs.keySet()){
				List<Mention> fps = macroFPs.get(d);
				List<Mention> fns = macroFNs.get(d);
				if (fps != null && fns != null){
					for (int i = 0; i < fps.size(); i++){
						int found = -1;
						for (int j = 0; j < fns.size(); j++){
							if (fps.get(i).overlaps(fns.get(j))){
								found = j;
								break;
							}
						}
						if (found != -1){
							if (!macroFPFNs.containsKey(d))
								macroFPFNs.put(d, new ArrayList<Mention>());
							macroFPFNs.get(d).add(fps.get(i));
							fps.remove(i--);
							fns.remove(found);
							macroLevelResult.fpfn++;
						}
					}
				}
			}

			if (logStream != null){
				for (String d : microFPFNs.keySet())
					for (Mention m : microFPFNs.get(d))
						logStream.write("micro\tfpfn\t" + m.toString() + "\n");
				for (String d : macroFPFNs.keySet())
					for (Mention m : macroFPFNs.get(d))
						logStream.write("macro\tfpfn\t" + m.toString() + "\n");
				for (String d : microFPs.keySet())
					for (Mention m : microFPs.get(d))
						logStream.write("micro\tfp\t" + m.toString() + "\n");
				for (String d : microFNs.keySet())
					for (Mention m : microFNs.get(d))
						logStream.write("micro\tfn\t" + m.toString() + "\n");
				for (String d : macroFPs.keySet())
					for (Mention m : macroFPs.get(d))
						logStream.write("macro\tfp\t" + m.toString() + "\n");
				for (String d : macroFNs.keySet())
					for (Mention m : macroFNs.get(d))
						logStream.write("macro\tfn\t" + m.toString() + "\n");
			}

			if (print){
				System.out.println("result," + title + ",level," + Result.getHeader());
				System.out.println("result," + title + ",document," + documentLevelResult.toString());
				System.out.println("result," + title + ",micro," + microLevelResult.toString());
				System.out.println("result," + title + ",macro," + macroLevelResult.toString());
			}

			if (logStream != null)
				logStream.close();

			return new Result[] {documentLevelResult, microLevelResult};
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
		//dummy return
		return null;
	}

	private Map<String, Map<String, List<Tag>>> convert(Map<String,List<Mention>> hash){
		HashMap<String,Map<String,List<Tag>>> res = new HashMap<String,Map<String,List<Tag>>>();

		for (String doc : hash.keySet()){
			if (!res.containsKey(doc))
				res.put(doc, new HashMap<String,List<Tag>>());

			Map<String,List<Tag>> docHash = res.get(doc);

			for (Mention m : hash.get(doc)){
				for  (String id : m.getIds()){
					if (!docHash.containsKey(id))
						docHash.put(id, new ArrayList<Tag>());
					docHash.get(id).add(new Tag(m.getStart(),m.getEnd()));
				}						
			}				
		}

		return res;
	}

	/**
	 * Loads an index file correlating two sets of document ids with eachother,
	 * so that they can be mapped to eachother (becoming equivalent during the evaluation)
	 * @param file
	 * @return a map with mappings between document ids
	 */
	private static HashMap<String,String> loadIndexfile(File file){
		HashMap<String,String> hash = new HashMap<String,String>();
		System.out.print("Loading " + file.getAbsolutePath() + "...");
		try{
			BufferedReader inStream = new BufferedReader(new FileReader(file));
			String line = inStream.readLine();
			while (line != null){
				if (!line.startsWith("#")){
					String[] fields = line.split(",");

					if (fields[1].length() > 1){
						hash.put(fields[0], fields[1]);
						hash.put(fields[1], fields[0]);
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
		System.out.println(" done, loaded " + hash.size() + " mappings.");

		return hash;
	}

	private static Set<String> loadDocumentIDSet(File file){
		Set<String> res = new HashSet<String>();
		try{
			System.out.print("Loading " + file.getAbsolutePath() + "...");
			BufferedReader inStream = new BufferedReader(new FileReader(file));

			String line = inStream.readLine();
			while (line != null){
				if (!line.startsWith("#")){
					String[] fields = line.split(",");
					res.add(fields[0]);					
				}
				line = inStream.readLine();
			}

			inStream.close();
			System.out.println(" done, loaded " + res.size() + " document IDs.");
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
		return res;
	}

	/**
	 * Loads a list of entity ids that should be processed during evaluation (anything else will be ignored)
	 * @param file
	 * @param prefix prefix which will be added to the beginning of each ID (may be null)
	 * @return
	 */
	private static Set<String> loadValidEntities(File file, String prefix){
		System.out.print("Loading " + file.getAbsolutePath() + "...");
		Set<String> ids = new HashSet<String>(); 
		try{
			BufferedReader inStream = new BufferedReader(new FileReader(file));
			String line = inStream.readLine();
			while (line != null){
				if (!line.startsWith("#")){
					String[] fields = line.split(",");

					if (prefix != null)
						ids.add(prefix + fields[0]);
					else
						ids.add(fields[0]);
				}
				line = inStream.readLine();
			}
			inStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println(" done, loaded " + ids.size() + " species.");
		return ids;
	}

	/**
	 * Randomly returns n document ids from an array of document ids.
	 * This function is used for re-sampling statistical studies of accuracy robustness
	 * @param documents an array of document ids
	 * @param n
	 * @return a set of n ids from documents, randomly selected
	 */
	static Set<String> getDocumentSelection(String[] documents, int n){
		Set<String> res = new HashSet<String>();

		if (n >= documents.length){
			for (int i = 0; i < documents.length; i++)
				res.add(documents[i]);
			return res;
		}

		Random r = new Random();

		for (int i = 0; i < n; i++){
			int e = r.nextInt(documents.length);
			while (res.contains(documents[e]))
				e = r.nextInt(documents.length);
			res.add(documents[e]);
		}			

		return res;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArgParser ap = new ArgParser(args);

		if (args.length == 0 || ap.containsKey("help")){
			System.out.println("Usage: evaluate_tagset.jar --title <title> --taxonomy <file> --medlineIndex <file> --pmcIndex <file> --main <main tags file> --ref <ref tags file(s)> [--restrictToRefDocs]");
			System.exit(-1);
		}

		boolean recognitionOnly = ap.containsKey("recognitionOnly");

		//load regexp filters, will be used to filter out any mentions matching the filters
		HashMap<String,Pattern> regexpFilters = ap.containsKey("filterRegexp") ? ACIDMatcher.loadPatterns(ap.getFile("filterRegexp")).getA() : null;
		if (regexpFilters != null)
			System.out.println("Loaded " + regexpFilters.size() + " filters.");

		HashMap<String,Integer> docLengthFilters = ap.containsKey("filterDocLength") ? loadDocLengthFilters(DocumentParser.getDocuments(ap)) : null;

		String title = ap.get("title");
		int threads = ap.getInt("threads", 1);

		Set<String> validEntities = loadValidEntities(ap.getFile("validEntities"), null);
		if (validEntities != null && recognitionOnly)
			validEntities.add("0");		

		HashMap<String,String> conversionMap = ap.containsKey("conversionIndex") ? loadIndexfile(ap.getFile("conversionIndex")) : null;

		//load sets of valid documents for the main and reference sets
		Set<String> mainSet = loadDocumentIDSet(ap.getFile("mainSet"));
		Set<String> refSet = loadDocumentIDSet(ap.getFile("refSet"));

		//load main set of mentions (and filter them if necessary)
		System.out.print("Loading " + ap.getFile("mainTaggedSet").getAbsolutePath() + "...");
		Map<String,List<Mention>> mainTaggedSet = Mention.loadFromFileToHash(ap.getFile("mainTaggedSet"), refSet, null, conversionMap);
		System.out.println(" done, loaded tags for " + mainTaggedSet.size() + " documents.");

		if (regexpFilters != null)
			filterTagsByRegexp(mainTaggedSet, regexpFilters);
		if (docLengthFilters != null)
			filterTagsByDocLength(mainTaggedSet, docLengthFilters);
		if (recognitionOnly)
			reduceIDs(mainTaggedSet);

		//split them up by document, and print some stats
		Map<String, Map<String, List<Tag>>> mainTagsBySpecies = new Evaluate().convert(mainTaggedSet);
		printEffectiveStats(title + ",main", mainTaggedSet, mainTagsBySpecies, mainSet, refSet, conversionMap);

		//load reference (gold-standard) set of mentions (and filter them if necessary)
		System.out.print("Loading " + ap.getFile("refTaggedSet").getAbsolutePath() + "...");
		Map<String,List<Mention>> refTaggedSet = Mention.loadFromFileToHash(ap.getFile("refTaggedSet"), mainSet, null, conversionMap);
		System.out.println(" done, loaded tags for " + refTaggedSet.size() + " documents.");

		if (regexpFilters != null)
			filterTagsByRegexp(refTaggedSet, regexpFilters);
		if (docLengthFilters != null)
			filterTagsByDocLength(refTaggedSet, docLengthFilters);
		if (recognitionOnly)
			reduceIDs(refTaggedSet);

		//split up by document, and print some stats
		Map<String, Map<String, List<Tag>>> refTagsBySpecies = new Evaluate().convert(refTaggedSet);
		printEffectiveStats(title + ",ref", refTaggedSet, refTagsBySpecies, mainSet, refSet, conversionMap);

		//get intersection of valid docs for main and reference set, 
		//in order to only process documents that are valid for both sets
		Set<String> validDocs = getValidDocs(mainSet, mainTaggedSet.keySet(), refSet, refTaggedSet.keySet(), conversionMap);

		if (ap.containsKey("evaluateSimple")){
			//perform evaluation
			new Evaluate().process(mainTaggedSet,mainTagsBySpecies,refTaggedSet,refTagsBySpecies,conversionMap,validEntities, ap.getFile("log"),true,validDocs,title);
		}

		if (ap.containsKey("evaluateMulti")){
			//perform re-sampling evaluation in order to evaluate robustness of accuracy measures
			//this will require a local installation of R.

			RGraphics R = new RGraphics(ap.getFile("rscriptdir"),null,ap.getFile("tempdir"));

			int[] sizes = ap.getInts("sizes");
			int numPerRun = ap.getInt("numPerRun", 20);

			System.out.println("numPerRun set to " + numPerRun);

			String[] a = validDocs.toArray(new String[0]);

			ArrayList<Double>[] rValues = new ArrayList[sizes.length];
			ArrayList<Double>[] pValues = new ArrayList[sizes.length];

			ArrayList<Double>[] rMicroValues = new ArrayList[sizes.length];
			ArrayList<Double>[] pMicroValues = new ArrayList[sizes.length];

			String[] labels = new String[sizes.length];

			for (int i = 0; i < sizes.length; i++){
				labels[i] = ""+Math.min(sizes[i], a.length);
				rValues[i] = new ArrayList<Double>();
				pValues[i] = new ArrayList<Double>();
				rMicroValues[i] = new ArrayList<Double>();
				pMicroValues[i] = new ArrayList<Double>();
				EvaluateProblem[] problems = new EvaluateProblem[numPerRun];

				for (int j = 0; j < numPerRun; j++)
					problems[j] = new EvaluateProblem(mainTaggedSet,mainTagsBySpecies,refTaggedSet,refTagsBySpecies,conversionMap,validEntities,sizes[i],a,title);

				IteratorBasedMaster<Result[]> master = new IteratorBasedMaster<Result[]>(problems,threads);
				master.startThread();

				while (master.hasNext()){
					Result[] r = master.next();
					rValues[i].add(r[0].getRecall());
					pValues[i].add(r[0].getPrecision());
					rMicroValues[i].add(r[1].getRecall());
					pMicroValues[i].add(r[1].getPrecision());
				}
			}

			if (ap.containsKey("outR"))
				R.boxPlot(rValues, labels, ap.getFile("outR"), 800, 400);
			if (ap.containsKey("outP"))
				R.boxPlot(pValues, labels, ap.getFile("outP"), 800, 400);

			if (ap.containsKey("outRMicro"))
				R.boxPlot(rMicroValues, labels, ap.getFile("outRMicro"), 800, 400);
			if (ap.containsKey("outPMicro"))
				R.boxPlot(pMicroValues, labels, ap.getFile("outPMicro"), 800, 400);
		}
	}

	private static void reduceIDs(Map<String, List<Mention>> mainTaggedSet) {
		for (List<Mention> l : mainTaggedSet.values())
			for (Mention m : l){
				m.setIds(new String[]{"0"});
				m.setProbabilities(null);
			}
	}

	private static void filterTagsByDocLength(Map<String, List<Mention>> mainTaggedSet, Map<String, Integer> docLengthFilters) {
		Set<String> toRemove = new HashSet<String>();

		for (String d : mainTaggedSet.keySet()){
			if (docLengthFilters.containsKey(d)){
				int length = docLengthFilters.get(d);
				List<Mention> matches = mainTaggedSet.get(d);
				for (int i = 0; i < matches.size(); i++){
					if (matches.get(i).getEnd() > length){
						matches.remove(i);
						i--;
					}
				}
			} else {
				toRemove.add(d);
			}
		}

		for (String d : toRemove)
			mainTaggedSet.remove(d);
	}

	private static HashMap<String, Integer> loadDocLengthFilters(DocumentIterator documents) {
		if (documents == null)
			return null;

		System.out.print("Loading document lengths...");

		HashMap<String,Integer> retres = new HashMap<String, Integer>();

		for (Document d : documents)
			retres.put(d.getID(), d.toString().length());

		System.out.println(" done, parsed " + retres.size() + " documents.");

		return retres;
	}

	/**
	 * 
	 * @param mainTaggedSet
	 * @param filters
	 */
	public static void filterTagsByRegexp(Map<String, List<Mention>> mainTaggedSet, Map<String, Pattern> filters) {
		for (List<Mention> matches : mainTaggedSet.values()){
			for (int i = 0; i < matches.size(); i++){
				Mention m = matches.get(i);
				if (m.getIds().length == 1 && filters.containsKey(m.getIds()[0])){
					Matcher matcher = filters.get(m.getIds()[0]).matcher(m.getText());
					if (matcher.matches()){
						matches.remove(i);
						i--;
					}
				}
			}
		}		
	}

	/**
	 * Function which will print a few statistics to System.out
	 * @param title
	 * @param mainTaggedSet
	 * @param mainTagsBySpecies
	 * @param doclist_a
	 * @param doclist_b
	 * @param conversionMap
	 */
	private static void printEffectiveStats(String title, Map<String, List<Mention>> mainTaggedSet, Map<String, Map<String, List<Tag>>> mainTagsBySpecies, Set<String> doclist_a, Set<String> doclist_b, Map<String, String> conversionMap) {
		int numTags = 0;
		Set<String> ids = new HashSet<String>();

		for (String d : mainTaggedSet.keySet()){
			numTags += mainTaggedSet.get(d).size();
			for (String id : mainTagsBySpecies.get(d).keySet())
				ids.add(id);
		}

		int numdocs = 0;

		for (String d : doclist_a)
			if (doclist_b.contains(d) || (conversionMap.containsKey(d) && doclist_b.contains(conversionMap.get(d))))
				numdocs++;

		System.out.println("#size,title,title2,docs,ids,tags");
		System.out.println("size," + title + "," + mainTaggedSet.size() + "," + ids.size() + "," + numTags);
		System.out.println("Intersecting documents: " + numdocs);
	}

	/**
	 * Function which given document id sets will return a set of IDs that are relevant for evaluation. 
	 * @param mainSet the set of document IDs in our main set that could _potentially_ have been tagged (some may not have any tags due to not containing any entities).
	 * @param mainTaggedSet the set of document IDS in our main set that have been tagged as containing entities
	 * @param refSet the set of document IDs in our reference set that could _potentially_ have been tagged (some may not have any tags due to not containing any entities).
	 * @param refTaggedSet the set of document IDS in our reference set that have been tagged as containing entities
	 * @param articleConversions String <-> String conversion map, for mapping e.g. PMIDs to PMCIDs. 
	 * @return the set of document ids: (mainTaggedSet * refSet) + (refTaggedSet * mainSet). 
	 */
	private static Set<String> getValidDocs(Set<String> mainSet, Set<String> mainTaggedSet, Set<String> refSet, Set<String> refTaggedSet, HashMap<String, String> articleConversions) {
		Set<String> s = new HashSet<String>();

		for (String str : mainTaggedSet)
			if (refSet.contains(str) || (articleConversions.containsKey(str) && refSet.contains(articleConversions.get(str))))
				s.add(str);

		for (String str : refTaggedSet)
			if (mainSet.contains(str) || (articleConversions.containsKey(str) && mainSet.contains(articleConversions.get(str))))
				s.add(str);

		return s;			 
	}
}
