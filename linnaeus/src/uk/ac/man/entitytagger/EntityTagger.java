package uk.ac.man.entitytagger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;

import java.sql.Connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import uk.ac.man.documentparser.DocumentParser;
import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.documentparser.input.DocumentIterator;
import uk.ac.man.entitytagger.doc.TaggedDocument.Format;
import uk.ac.man.entitytagger.entities.misc.CellLinePostprocessor;
import uk.ac.man.entitytagger.matching.MatchOperations;
import uk.ac.man.entitytagger.matching.Matcher;
import uk.ac.man.entitytagger.matching.Postprocessor;
import uk.ac.man.entitytagger.matching.Matcher.Disambiguation;
import uk.ac.man.entitytagger.matching.matchers.ACIDMatcher;
import uk.ac.man.entitytagger.matching.matchers.DuplicationMatcher;
import uk.ac.man.entitytagger.matching.matchers.MatchPostProcessor;
import uk.ac.man.entitytagger.matching.matchers.UnionMatcher;
import uk.ac.man.entitytagger.matching.matchers.PrecomputedMatcher;
import uk.ac.man.entitytagger.matching.matchers.RegexpMatcher;
import uk.ac.man.entitytagger.matching.matchers.SentenceMatcher;
import uk.ac.man.entitytagger.matching.matchers.TaxonGrabMatcher;
import uk.ac.man.entitytagger.matching.matchers.VariantDictionaryMatcher;
import uk.ac.man.entitytagger.networking.SimpleServer;
import uk.ac.man.entitytagger.networking.SimpleClientMatcher;
import martin.common.ArgParser;
import martin.common.Loggers;
import martin.common.SQL;
import martin.common.compthreads.IteratorBasedMaster;

/**
 * Main species recognition class
 * @author Martin *
 */
public class EntityTagger {
	public static final String LINNAEUS_VERSION = "LINNAEUS, v. 2.0 (July, 2011)";

	public static Postprocessor getPostprocessor(ArgParser ap, Logger logger, Map<String,String> comments){
		return getPostprocessor(ap, logger, comments,"");
	}

	public static Postprocessor getPostprocessor(ArgParser ap, Logger logger, Map<String,String> comments, String tag){
		if (ap.containsKey("postProcessing" + tag)){
			InputStream[] stop = ap.getInputStreams("ppStopTerms" + tag);
			InputStream[] acr = ap.getInputStreams("ppAcrProbs" + tag);
			InputStream[] spf = ap.getInputStreams("ppEntityFreqs" + tag);

			Postprocessor res = new Postprocessor(stop,acr,spf,comments,logger);

			try{
				if (stop != null)
					for (InputStream s : stop)
						s.close();
				if (acr != null)
					for (InputStream s : acr)
						s.close();
				if (spf != null)
					for (InputStream s : spf)
						s.close();
			} catch (Exception e){
				System.err.println(e);
				e.printStackTrace();
				System.exit(0);
			}
			return res;
		} else {	
			if (logger != null)
				logger.info("Not performing post-processing.\n");
			return null;
		}
	}

	/**
	 * returns a entity recognition Matcher based on the input parameters in ap (provided by the user on the command-line or in a configuration file)
	 * @param ap an ArgParser object containing arguments used to construct the matcher
	 * @param logger A logger to which information messages will be logged. Nothing will be logged if this is null.
	 * @return a matcher that can be used to find and normalize species names in text
	 */
	public static Matcher getMatcher(ArgParser ap, Logger logger){
		return getMatcher(ap,logger,null);
	}

	/**
	 * returns a entity recognition Matcher based on the input parameters in ap (provided by the user on the command-line or in a configuration file)
	 * @param ap an ArgParser object containing arguments used to construct the matcher
	 * @param logger A logger to which information messages will be logged. Nothing will be logged if this is null.
	 * @param tag Allows the use of multiple different matchers. For example, if tag == "Genes", properties will need to have the postfix "Genes" to be read. This allows the method to be called multiple times with different tags for different matching options.
	 * @return a matcher that can be used to find and normalize species names in text
	 */
	public static Matcher getMatcher(ArgParser ap, Logger logger, String tag){
		if (tag != null && !tag.startsWith("-"))
			tag = "-" + tag;
		if (tag == null)
			tag = "";

		//decides whether to perform abbreviation resolution (detect when authors declare abbreviation or acronyms)
		boolean abbrevResolution = !ap.containsKey("noAbbreviationResolution" + tag);
		logger.info("%t: Abbreviation resolution mode set to: " + abbrevResolution + ".\n");

		//determine what level of disambiguation to use - typically this does not need to be changed
		Disambiguation disambiguation = Disambiguation.ON_WHOLE;
		if (ap.containsKey("noDisambiguation" + tag)) 
			disambiguation = Disambiguation.OFF;
		if (ap.containsKey("longestDisambiguation"))
			disambiguation = Disambiguation.ON_LONGEST_ONLY;
		if (ap.containsKey("earlierDisambiguation" + tag)) 
			disambiguation = Disambiguation.ON_EARLIER;
		if (ap.containsKey("wholeDisambiguation" + tag)) 
			disambiguation = Disambiguation.ON_WHOLE;
		logger.info("%t: Disambiguation mode set to: " + disambiguation + ".\n");

		//if comments should be added, load them (this is typically not performed)
		Map<String,String> comments = ap.containsKey("comments" + tag) ? loadComments(ap.getFiles("comments" + tag), logger) : null;

		//construct a post-processor
		Postprocessor defaultPostprocessor = getPostprocessor(ap, logger, comments, tag);

		List<Matcher> matchers = new ArrayList<Matcher>();

		if (ap.containsKey("automatons" + tag)){
			//allows us to use automaton dictionaries for matching
			for (File f : ap.getFiles("automatons" + tag)){
				logger.info("%t: Loading automatons from file " + f.getAbsolutePath() + "...\n");
				Matcher m = uk.ac.man.entitytagger.matching.matchers.AutomatonMatcher.loadMatcher(f);
				matchers.add(m);
				logger.info("%t: Done, loaded " + m.size() + " automatons.\n");
			}
		}
		if (ap.containsKey("regexpMatcher" + tag)){
			//allows us to use files with raw regular expressions for matching
			//beware: this will be very slow if the dictionaries are large.
			for (File f : ap.getFiles("regexpMatcher" + tag)){
				logger.info("Loading regular expressions from file " + f.getAbsolutePath() + "...");
				HashMap<String,Pattern> patterns = ACIDMatcher.loadPatterns(f).getA();
				Matcher m = new RegexpMatcher(patterns);
				matchers.add(m);
				logger.info(" done, loaded " + m.size() + " patterns.\n");
			}
		} 
		if (ap.containsKey("sentenceMatcher" + tag)){
			//splits texts into individual sentences
			matchers.add(new SentenceMatcher());
		} 
		if (ap.containsKey("taxongrabMatcher"))
			matchers.add(new TaxonGrabMatcher(ap.getFile("taxongrabMatcher")));
		if (ap.containsKey("precomputedMatcher" + tag)){
			//allows us to take previously computed mentions (in stand-off format)
			//and re-use them (needs to be stored in a file created with the --out option)
			for (File f : ap.getFiles("precomputedMatcher" + tag)){
				logger.info("Loading precomputed match data from file " + f.getAbsolutePath() + "...");
				Matcher m = new PrecomputedMatcher(f);
				matchers.add(m);
				logger.info(" done, loaded data for " + m.size() + " documents.\n");
			}
		} 
		if (ap.containsKey("precomputedDBMatcher" + tag)){
			//allows us to take previously computed mentions (in stand-off format)
			//and re-use them (needs to be stored in a database)
			String db = ap.gets("precomputedDBMatcher" + tag)[0];
			String table = ap.gets("precomputedDBMatcher" + tag)[1];
			Connection conn = SQL.connectMySQL(ap, logger, db);
			Matcher m = new PrecomputedMatcher(conn, table);
			matchers.add(m);
			logger.info("%t: Created precomputedDBMatcher.\n");
		} 
		if (ap.containsKey("ACIDs" + tag)){
			//special mode for recognizing accession IDs that follow certain forms.
			for (File f : ap.getFiles("ACIDs" + tag)){
				logger.info("Loading ACID data from file " + f.getAbsolutePath() + "...");
				Matcher m = new ACIDMatcher(f);
				matchers.add(m);
				logger.info(" done.\n");
			}
		} 
		if (ap.containsKey("celllines" + tag)){
			//special mode for recognizing cell lines (uses a specialized postprocessor)
			boolean ignoreCase = ap.containsKey("ignoreCase");
			defaultPostprocessor = CellLinePostprocessor.getPostprocessor(ap, logger, comments);
			logger.info("%t: Loading variants from file " + ap.getFile("celllines" + tag).getAbsolutePath() + "...");
			Connection conn = SQL.connectMySQL(ap, logger, "dictionaries");

			//Matcher m = VariantDictionaryMatcher.load(ap.getFile("celllines" + tag),ignoreCase,disambiguation,abbrevResolution,defaultPostprocessor);
			Matcher m = new VariantDictionaryMatcher(conn, ap.gets("celllines"), null, ignoreCase);

			matchers.add(m);
			logger.info(" done.\n");
		}
		if (ap.containsKey("networkMatcher" + tag)){
			//specifies that the software should connect to a LINNAEUS server, asking it to perform the matching
			//for us. The server should have been started using another invocation of the software with the --server command.

			//format of the argument: host1_1:port1_1[|host1_2:port1_2|...] host2_1:port2_1[|...]
			//in this example, two matchers will be created, both of which always will be called on the documents
			//the first will connect randomly to host1_1 or host1_2, enabling load balancing
			//the second will always connect to host2_1.

			String[] strs = ap.gets("networkMatcher" + tag);
			for (String s : strs) {
				matchers.add(new SimpleClientMatcher(s));
			}
		}

		if (ap.containsKey("variantMatcher" + tag)){
			boolean ignoreCase = ap.containsKey("ignoreCase");
			//loads a list of entity ids and "variants" - allows simple matching
			//of normal terms (i.e. not regular expressions)
			for (int i = 0; i < ap.gets("variantMatcher").length; i++){
				logger.info("%t: Loading variantMatcher from " + ap.gets("variantMatcher")[i] + ", ignoreCase = " + ignoreCase + "...\n");
				matchers.add(VariantDictionaryMatcher.load(ap.getInputStreams("variantMatcher")[i], ignoreCase));

			}
		}

		if (ap.containsKey("variantMatcherDB" + tag)){
			boolean ignoreCase = ap.containsKey("ignoreCase");
			Connection conn = SQL.connectMySQL(ap, logger, "dictionaries");
			matchers.add(new VariantDictionaryMatcher(conn, ap.gets("variantMatcherDB" + tag), null, ignoreCase));
		}

		if (ap.containsKey("internalSpeciesMatcher")){

		}

		if (tag == null || tag.length() == 0)
			if (ap.containsKey("matchers"))
				for (String t : ap.gets("matchers")){
					Matcher m = getMatcher(ap, logger, "-" + t);
					if (m != null)
						matchers.add(m);
				}

		if (matchers.size() == 0){
			logger.warning("Warning: no matcher have been chosen.\n");
			return null;
		}

		Matcher matcher = matchers.size() == 1 ? matchers.get(0) : new UnionMatcher(matchers, true);
		if (ap.containsKey("duplicates" + tag))
			matcher = new DuplicationMatcher(matcher);

		File ppConvertIDs = ap.getFile("ppConvertIDs");

		matcher = new MatchPostProcessor(matcher, disambiguation, abbrevResolution, ppConvertIDs, defaultPostprocessor);

		matcher.match("test", new Document("none",null,null,null,null,null,null,null,null,null,null,null,null,null,null));

		return matcher;
	}

	public static String getDefaultHelpMessage(){
		String msg = "[--automatons <automaton file>]\n" +
		"[--regexpMatcher <regexp file>]\n\n" +
		"[--out <output file>]\n" +
		"[--outDir <output dir>]\n" +
		"[--outHTML <html output file>]\n\n" +
		"[--threads <number of threads>]\n" +
		"[--properties <file>]";

		msg += DocumentParser.getDocumentHelpMessage();

		return msg;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArgParser ap = new ArgParser(args);

		if (args.length == 0 || ap.containsKey("help")){
			System.out.println(getDefaultHelpMessage());
			System.exit(0);
		}
		if (ap.containsKey("version")){
			System.out.println(LINNAEUS_VERSION);
		}

		//determines number of threads used for concurrent processing
		int numThreads = ap.getInt("threads",1);

		//determines progress reporting: e.g. report = 1000 will print a short message after every 1000 processed documents.
		int report = ap.getInt("report", -1);

		Logger logger = Loggers.getDefaultLogger(ap);

		if (ap.containsKey("default")){
			ap.addProperties("internal:/resources-linnaeus/properties.conf");
		} else if (ap.containsKey("default-proxy")){
			ap.addProperties("internal:/resources-linnaeus/properties-proxy.conf");
		}
		
		//load the matcher
		Matcher matcher = getMatcher(ap,logger);
		
		if (matcher == null){
			logger.info("%t: No species matcher has been chosen, so loading the default species matcher...\n");
			ap.addProperties("internal:/resources-linnaeus/properties.conf");
			matcher = getMatcher(ap,logger);
		}

		if (ap.containsKey("out")){
			//saves all matches to a single file
			DocumentIterator documents = DocumentParser.getDocuments(ap);
			MatchOperations.runToFile(matcher,documents, numThreads, report, ap.getFile("out"), logger);
		}

		if (ap.containsKey("outDir")){
			//saves matches to one file per document
			DocumentIterator documents = DocumentParser.getDocuments(ap);
			MatchOperations.runToDir(matcher,documents, numThreads, report, ap.getFile("outDir"),logger);
		}

		if (ap.containsKey("outHTML")){
			//produces HTML document output for visual inspection
			DocumentIterator documents = DocumentParser.getDocuments(ap);
			MatchOperations.runHTML(matcher,documents, numThreads, ap.getFile("outHTML"), report,logger, Format.HTML, !ap.containsKey("nolinks"));
		}

		if (ap.containsKey("outXML")){
			//produces XML document output
			DocumentIterator documents = DocumentParser.getDocuments(ap);
			MatchOperations.runHTML(matcher,documents, numThreads, ap.getFile("outXML"), report,logger, Format.XMLTags);
		}

		if (ap.containsKey("outDB")){
			//saves the output to a database
			Connection dbConn = martin.common.SQL.connectMySQL(ap, logger, ap.gets("outDB")[0]);
			DocumentIterator documents = DocumentParser.getDocuments(ap);
			MatchOperations.runDB(matcher,documents,numThreads,ap.gets("outDB")[1], report, logger, dbConn, ap.containsKey("clear"));
		}

		if (ap.containsKey("outWithContext")){
			//saves the output to a file, together with text directly surrounding the identified mentions
			int pre = ap.getInt("pre", 50); //number of chars prior to the mention to include
			int post = ap.getInt("post", 50); //number of chars past the mention to include
			DocumentIterator documents = DocumentParser.getDocuments(ap);
			MatchOperations.runOutWithContext(matcher, documents, numThreads, report, ap.getFile("outWithContext"), logger, pre, post);
		}

		if (ap.containsKey("server")){
			//starts a local server, listening on a specified port.
			//other linnaeus invocations may call this server in order to request matching of documents - see EntityTagger.getMatcher() for details on what parameters to use when connecting. 

			int port = ap.getInt("serverPort", 55000);
			int numConns = ap.getInt("conns", 4);

			doServer(matcher, port, numConns, logger, ap.containsKey("enableCache"), ap.getInt("report", -1));
		}
	}

	public static void doServer(Matcher matcher, int port, int numConns, Logger logger, boolean enableCache, int report) {
		logger.info("%t: Starting server at port " + port + ", maximum " + numConns + " concurrent connections (caching: " + enableCache + ")... ");

		SimpleServer server = new SimpleServer(port, matcher, enableCache);
		IteratorBasedMaster<Object> master = new IteratorBasedMaster<Object>(server, numConns);
		master.startThread();
		logger.info("done.\n");

		//just to consume connection objects, irrelevant
		long c = 0;
		while (master.hasNext()){
			master.next();
			if (report != -1 && ++c % report == 0)
				logger.info("%t: Served " + c + " requests.\n");
		}
	}

	public static Map<String,String> loadComments(File[] files, Logger logger) {
		if (logger != null)
			logger.info("%t Loading comments... ");

		try{
			Map<String,String> res = new HashMap<String,String>();

			for (File f : files){
				BufferedReader inStream = new BufferedReader(new FileReader(f));

				String line = inStream.readLine();
				while (line != null){

					if (!line.startsWith("#")){
						String[] fields = line.split("\t");
						if (fields.length > 2 && fields[2].length() > 0)
							res.put(fields[0], fields[2]);
					}

					line = inStream.readLine();
				}

				inStream.close();
			}

			if (logger != null)
				logger.info("done.\n");

			return res;
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}

		return null;
	}
}
