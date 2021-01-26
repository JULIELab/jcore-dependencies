/**
 * BioSemApplication.java
 *
 * Author: matthies
 *
 * Current version: //TODO insert current version number
 * Since version:   //TODO insert version number of first appearance of this class
 *
 * Creation date: Jun 4, 2015
 *
 * This class is a convenient wrapper for the Event Extraction System of Bui & Sloot
 * see:	Bui, Q.-C., & Sloot, P. M. A. (2012). A robust approach to extract biomedical
 * 		events from literature. Bioinformatics (Oxford, England), 28(20), 2654–2661.
 *		[http://www.ncbi.nlm.nih.gov/pubmed/22859502]
 **/

package application;

import corpora.DataLoader;
import org.apache.commons.cli.*;
import relations.EventExtraction;
import relations.RuleLearner;
import relations.TriggerLearner;
import utils.DBUtils;

import java.util.ArrayList;
//import jargs.gnu.CmdLineParser;
//import jargs.gnu.CmdLineParser.IllegalOptionValueException;
//import jargs.gnu.CmdLineParser.UnknownOptionException;

public class BioSemApplication {
	static final String PATH_DELIM = System.getProperty("file.separator");
	static final String NEWLINE = System.getProperty("line.separator");
	static final ArrayList<String> SET_NAMES = new ArrayList<String>() {
		{
			add("train");
			add("test");
			add("devel");
		}
	};

	private static String input_root;
	private static String db_root;
	private static String modus;
	private static String set;
	private static String destination;

	private void readCommandLineArgs(String[] args) {
		CommandLineParser cmd = new BasicParser();
		CommandLine paramParser = null;
		Options options = new Options();
		options.addOption("i", "input", true, "input folder");
		options.addOption("o", "output", true, "database folder");
		options.addOption("m", "modus", true, "step of BioSEM Extractor");
		options.addOption("s", "set", true, "what set");
		options.addOption("d", "destination", true, "destination");
		options.addOption("x", "mix", false, "whether to use mix set");
		// final CmdLineParser paramParser = new CmdLineParser();
		// final CmdLineParser.Option textFolderOption =
		// paramParser.addStringOption(
		// 'i', "input");
		// final CmdLineParser.Option dbFolderOption =
		// paramParser.addStringOption(
		// 'o', "output");
		// final CmdLineParser.Option modusOption = paramParser.addStringOption(
		// 'm', "modus");
		// final CmdLineParser.Option setOption = paramParser.addStringOption(
		// 's', "set");
		// final CmdLineParser.Option destOption = paramParser.addStringOption(
		// 'd', "destination");
		// final CmdLineParser.Option mixOption = paramParser.addStringOption(
		// 'x', "mix");

		try {
			paramParser = cmd.parse(options, args);
			Object param;

			/** ----- Modus ----- **/
			param = paramParser.getOptionValue("m");
			if (param != null) {
				modus = (String) param;
				if (!(getModus().equalsIgnoreCase("complete") || getModus().equalsIgnoreCase("data-preparation")
						|| getModus().equalsIgnoreCase("trigger-learn") || getModus().equalsIgnoreCase("pattern-learn")
						|| getModus().equalsIgnoreCase("event-extraction"))) {
					System.out.println("Modus \"" + getModus() + "\" is not known. Aborting.");
					System.exit(-1);
				}
				System.out.println("Modus: " + getModus());
			} else {
				modus = "complete";
				System.out.println("No modus specified; using default." + NEWLINE + "Modus: \"complete\".");
			}

			/** ----- Input Folder ----- **/
			param = paramParser.getOptionValue("i");
			if (param != null) {
				input_root = (String) param;
				System.out.println("Input: " + getInput_root());
			} else {
				System.out.println("Specify input directory root (-i). Aborting.");
				System.exit(-1);
			}

			/** ----- DataBase Folder ----- **/
			param = paramParser.getOptionValue("o");
			if (param != null) {
				db_root = (String) param;
				System.out.println("Database-Root: " + getDb_root());
			} else {
				System.out.println("Specify output database directory (-o). Aborting.");
			}

			/** ----- Data Set ----- **/
			param = paramParser.getOptionValue("s");
			if (param != null) {
				set = (String) param;
				if (!(getSet().equalsIgnoreCase("train") || getSet().equalsIgnoreCase("test")
						|| getSet().equalsIgnoreCase("devel") || getSet().equalsIgnoreCase("mix"))) {
					System.out.println(NEWLINE + "A specific set needs to be declared (-s). \"" + getSet()
							+ "\" is no valid set; choose: \"train\",\"test\", \"devel\" or \"mix\". Aborting.");
					System.exit(-1);
				}
				if (getSet().equalsIgnoreCase("mix")) {
					SET_NAMES.add("mix");
					System.out.println(
							NEWLINE + "\"mix\" option was not set but declared in set-option; added automatically.");
				}
			} else {
				if (!getModus().equalsIgnoreCase("complete")) {
					System.out.println(NEWLINE + "A specific set needs to be declared (-s);"
							+ " choose: \"train\",\"test\", \"devel\" or \"mix\". Aborting.");
					System.exit(-1);
				}
			}

			/** ----- Mix Train and Devel ----- **/
			param = paramParser.getOptionValue("x");
			if (param != null) {
				String mix = (String) param;
				if (mix.equalsIgnoreCase("true")) {
					SET_NAMES.add("mix");
				}
			}

			/** ----- Destination ----- **/
			param = paramParser.getOptionValue("d");
			if (param != null) {
				destination = (String) param;
				if (!SET_NAMES.contains(getDestination().toLowerCase())) {
					System.out.println(
							NEWLINE + "\"" + getDestination() + "\" is no valid destination; choose: \"train\","
									+ " \"test\", \"devel\" or \"mix\" (if mix option is set to \"true\"). Aborting.");
					System.exit(-1);
				} else if (getDestination().equalsIgnoreCase("mix")) {
					SET_NAMES.add("mix");
					System.out.println(NEWLINE
							+ "\"mix\" option was not set but declared in destination-option; added automatically.");
				}
			} else {
				if (!getModus().equalsIgnoreCase("data-preparation")) {
					System.out.println(NEWLINE + "For modes other than \"data-preparation\" there needs to be"
							+ " a destination database declared (-d). Aborting.");
					System.exit(-1);
				}
			}
		} catch (ParseException e) {
			System.out.println(e);
		}
	}

	private void loadData(String set) {
		DataLoader data = new DataLoader();
		boolean a2 = true;
		if (set.equalsIgnoreCase("test")) {
			a2 = false;
		}
		data.Txt2Db(getInput_root() + PATH_DELIM + set, getDb_root() + PATH_DELIM + set, a2);
	}

	private void learnTrigger(String src, String dest) {
		// source database
		String dbsr = getDb_root() + PATH_DELIM + src;
		DBUtils dbsrc = new DBUtils();
		dbsrc.openDB(dbsr);

		// destination database
		String dbname = getDb_root() + PATH_DELIM + dest;
		DBUtils dbdest = new DBUtils();
		dbdest.openDB(dbname);

		TriggerLearner learner = new TriggerLearner(dbsrc, dbdest); // store all
																	// triggers
																	// (both
																	// train and
																	// dev) into
																	// train DB
		learner.createTriggers();
		dbdest.shutdownDB();
		if (!src.equals(dest))
			dbsrc.closeDB();
	}

	private void learnRules(String src, String dest) {
		// source database
		String dbsr = getDb_root() + PATH_DELIM + src;
		DBUtils dbsrc = new DBUtils();
		dbsrc.openDB(dbsr);

		// destination database
		String dbname = getDb_root() + PATH_DELIM + dest;
		DBUtils dbdest = new DBUtils();
		dbdest.openDB(dbname);

		RuleLearner learner = new RuleLearner(dbsrc, dbdest);
		learner.LearnData();
		dbsrc.shutdownDB();
		if (!src.equals(dest))
			dbdest.shutdownDB();
	}

	private void extractEvents(String src, String dest) {
		// source database
		String sr_path = getDb_root() + PATH_DELIM + src;
		DBUtils sr = new DBUtils();
		sr.openDB(sr_path);

		// database on which to extract events
		String dest_path = getDb_root() + PATH_DELIM + dest;
		DBUtils des = new DBUtils();
		des.openDB(dest_path);

		EventExtraction xtr = new EventExtraction(sr, des);
		xtr.Test(getDb_root() + PATH_DELIM + "results" + PATH_DELIM + getDestination());
		sr.closeDB();
		if (!src.equals(dest))
			des.closeDB();
	}

	public static void main(String[] args) {
		BioSemApplication app = new BioSemApplication();
		app.readCommandLineArgs(args);

		/** ----- Loading Data ----- **/
		if (modus.equalsIgnoreCase("data-preparation")) {
			app.loadData(set);
		}

		/** ----- Learning Trigger ----- **/
		if (modus.equalsIgnoreCase("trigger-learn")) {
			app.learnTrigger(set, destination);
		}

		/** ----- Learning Pattern ----- **/
		if (modus.equalsIgnoreCase("pattern-learn")) {
			app.learnRules(set, destination);
		}

		/** ----- Extracting Events ----- **/
		if (modus.equalsIgnoreCase("event-extraction")) {
			app.extractEvents(set, destination);
		}

		/** ----- Complete Run ----- **/
		if (modus.equalsIgnoreCase("complete")) {
			for (String s : SET_NAMES) {
				if (s.equalsIgnoreCase(app.getSet()) || s.equalsIgnoreCase(app.getDestination())) {
					System.out.println(NEWLINE + "[Loading " + s + "-set]");
					app.loadData(s);
				}
			}
			System.out.println(NEWLINE);
			app.learnTrigger(set, set);
			System.out.println(NEWLINE);
			app.learnRules(set, set);
			System.out.println(NEWLINE);
			app.extractEvents(set, destination);
		}

		System.out.println(NEWLINE + "DONE PROCESSING");
	}

	public String getInput_root() {
		return input_root;
	}

	public String getDb_root() {
		return db_root;
	}

	public String getModus() {
		return modus;
	}

	public String getSet() {
		return set;
	}

	public String getDestination() {
		return destination;
	}
}
