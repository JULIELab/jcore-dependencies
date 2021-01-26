package uk.ac.man.entitytagger.generate;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicOperations;
import dk.brics.automaton.CustomRunAutomaton;
import dk.brics.automaton.RegExp;
import martin.common.Tuple;
import martin.common.compthreads.ArrayBasedMaster;
import martin.common.compthreads.IteratorBasedMaster;
import martin.common.compthreads.Problem;

import java.io.*;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Class providing functions used to generate automatons for efficient regular expression matching
 * @author Martin
 *
 */
class GenerateAutomatons {
	/**
	 * Class used to join several automatons together and minimize the result (put into a class to enable concurrent computations)
	 * @author Martin
	 *
	 */
	private class ProcessProblem implements Problem<Automaton>{
		private ArrayList<Automaton> automatons;
		private boolean minimize;

		public ProcessProblem(ArrayList<Automaton> automatons, boolean minimize){
			this.automatons = automatons;
			this.minimize = minimize;
		}

		public Automaton compute() {
			Automaton res;

			if (automatons.size() > 1){
				res = BasicOperations.union(automatons);
				res.setDeterministic(false);
			} else {
				res = automatons.get(0);
			}

			res.determinize();

			if (minimize)
				res.minimize();

			return res;
		}

	}
	private class ToAutomatonProblem implements Problem<Automaton>{
		private DictionaryEntry entry;
		private boolean lowercase;
		public ToAutomatonProblem(DictionaryEntry entry, boolean lowercase){
			this.entry = entry;
			this.lowercase = lowercase;
		}

		public Automaton compute() {
			//System.out.println("(" + entry.getRegexp() + ")" + AutomatonMatcher.delimiter + species);
			String sr = "(" + entry.getRegexp() + ")";

			if (lowercase)
				sr = sr.toLowerCase();

			if (sr.contains(""+CustomRunAutomaton.delimiter))
				throw new IllegalStateException("The regular expression " + sr + " contains the CustomRunAutomaton.delimiter character. You need to change character.");

			RegExp r = null;
			try{
				r = new RegExp(sr + CustomRunAutomaton.delimiter + entry.getId());
			} catch (Exception e){
				System.err.println("Detected exception when creating regular expression: '" + sr + CustomRunAutomaton.delimiter + entry.getId() + "'");
				System.err.println(e);
				e.printStackTrace();
				System.exit(-1);
			}

			Automaton a = r.toAutomaton();

			String shortest = a.getShortestExample(true);

			if (shortest == null || shortest.equals(CustomRunAutomaton.delimiter + entry.getId())){
				System.err.println("Warning: detected erroneous regexp for id " + entry.getId() + ", returning null automaton (regexp: '" + sr + "'");
				return null;
			}

			a.setDeterministic(false);
			a.determinize();
			return a;
		}
	}
	private class ToAutomatonProblemIterator implements Iterator<Problem<Automaton>>{
		private ArrayList<DictionaryEntry> dictionaryEntries;
		int currentItem = 0;
		private boolean lowercase;

		public ToAutomatonProblemIterator(ArrayList<DictionaryEntry> dictionaryEntries, boolean lowercase){
			this.dictionaryEntries = dictionaryEntries;
			this.lowercase = lowercase;
		}

		public boolean hasNext() {
			return currentItem < dictionaryEntries.size();
		}

		public ToAutomatonProblem next() {
			return new ToAutomatonProblem(dictionaryEntries.get(currentItem++),lowercase);
		}

		/**
		 * not implemented
		 */
		public void remove() {
			throw new IllegalStateException("not implemented");
		}
	}

	/**
	 * Function which will take a list of automatons and join them together in groups of size multiJoin (e.g. input 12 automatons and multiJoin=3 would give output of 4 automatons)
	 * @param automatons the list of automatons to be joined together
	 * @param multiJoin the number of automatons that should be joined at a time
	 * @param minimize whether to perform automaton minimization afterwards (will produce smaller automatons requiring less memory, but requires more time to perform)
	 * @param showNumStates whether to print some statistics at the end
	 * @param numThreads the number of concurrent joins to perform (note that multiple threads will increase memory requirements)
	 * @param logger 
	 * @return a list of joined automatons of size (automatons.size() / multiJoin).
	 */
	ArrayList<Automaton> process(ArrayList<Automaton> automatons, int multiJoin, boolean minimize, boolean showNumStates, int numThreads, Logger logger){
		ArrayList<Automaton> res = new ArrayList<Automaton>();

		int numRuns = automatons.size() / multiJoin;

		int numStartStates=0,numEndStates=0;

		if (numRuns * multiJoin < automatons.size())
			numRuns++;

		Problem<Automaton> problems[] = new ProcessProblem[numRuns];

		for (int i = 0; i < numRuns; i++){
			ArrayList<Automaton> temp = new ArrayList<Automaton>();

			for (int j = 0; j < multiJoin; j++){
				int index = i * multiJoin + j;
				if (index < automatons.size()){
					temp.add(automatons.get(index));
					if (showNumStates)
						numStartStates += automatons.get(index).getNumberOfStates();
				}
			}

			problems[i] = new ProcessProblem(temp,minimize);
		}

		ArrayBasedMaster<Automaton> master = new ArrayBasedMaster<Automaton>(problems, numThreads);

		logger.info("%t: Processing automatons... ");

		new Thread(master).start();

		Object[] solutions = master.getSolutions();

		for (int i = 0; i < solutions.length; i++){
			Automaton a = (Automaton) solutions[i];

			numEndStates += a.getNumberOfStates();
			res.add(a);
		}

		logger.info("done. " + automatons.size() + " automatons (" + numStartStates + " states) -> " + res.size() + " (" + numEndStates + " states)\n");

		return res;
	}

	static CustomRunAutomaton[] loadRArray(File file, Logger logger){
		CustomRunAutomaton[] r = null;
		try {
			ObjectInputStream inStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));

			int size = inStream.readInt();

			logger.info("%t: Loading runautomatons...");

			r = new CustomRunAutomaton[size];

			for (int i = 0; i < r.length; i++)
				r[i] = (CustomRunAutomaton) inStream.readObject();

			logger.info(" done, loaded " + size + " automatons from file " + file.getAbsolutePath() + ".\n");

			inStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
		return r;
	}

	static void storeRArray(ArrayList<Automaton> list, boolean ignoreCase, boolean tableize, File file, Logger logger){
		CustomRunAutomaton[] r = new CustomRunAutomaton[list.size()];

		logger.info("%t: Converting automatons to runautomatons (tableize = " + tableize + ")...");
		for (int i = 0; i < list.size(); i++)
			r[i] = new CustomRunAutomaton(list.get(i),tableize);
		logger.info(" done.\n");

		try {
			logger.info("%t: Storing runautomatons...");
			ObjectOutputStream outStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

			outStream.writeInt(list.size());
			outStream.writeBoolean(ignoreCase);

			for (int i = 0; i < r.length; i++)
				outStream.writeObject(r[i]);

			outStream.close();

			logger.info(" done. Stored " + r.length + " runautomatons to file " + file.getAbsolutePath() + "\n");
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * converts a list of dictionary entries to their corresponding automatons
	 * @param dictionaryEntries  the list of dictionary entries
	 * @param numThreads the number of concurrent threads to use for conversion
	 * @param report null if the function should not output progress, will otherwise print progress after every report:th conversion)
	 * @param b 
	 * @param logger 
	 * @return the list of automatons representing the list of dictionary entries
	 */
	ArrayList<Automaton> toAutomatons(ArrayList<DictionaryEntry> dictionaryEntries, int numThreads, Integer report, boolean ignoreCase, Logger logger){
		logger.info("%t: Generating automatons, ignoreCase = " + ignoreCase + "...\n");

		Iterator<Problem<Automaton>> problems = new ToAutomatonProblemIterator(dictionaryEntries,ignoreCase);
		IteratorBasedMaster<Automaton> master = new IteratorBasedMaster<Automaton>(problems, numThreads);
		new Thread(master).start();

		int i = 0;

		ArrayList<Automaton> res = new ArrayList<Automaton>();

		while (master.hasNext()){
			Automaton a = master.next();

			if (a != null)
				res.add(a);

			if (report != null && ++i % report == 0)
				logger.info(i + " / " + dictionaryEntries.size() + "\n");
		}

		logger.info("%t: Automatons complete.\n");

		return res;
	}

	static Tuple<ArrayList<Automaton>, Boolean> loadArray(File file) {
		ArrayList<Automaton> l = new ArrayList<Automaton>();
		boolean ignoreCase = false;
		try {
			ObjectInputStream inStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));

			int size = inStream.readInt();
			ignoreCase = inStream.readBoolean();			

			for (int i = 0; i < size; i++)
				l.add((Automaton)inStream.readObject());

			inStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
		return new Tuple<ArrayList<Automaton>, Boolean>(l, ignoreCase);
	}

	static void storeArray(File file, ArrayList<Automaton> l, boolean ignoreCase) {
		try {
			ObjectOutputStream outStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

			outStream.writeInt(l.size());
			outStream.writeBoolean(ignoreCase);

			for (int i = 0; i < l.size(); i++){
				outStream.writeObject(l.get(i));
			}

			outStream.close();

		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}		
	}

	public static void storeVariants(File file, PreparedStatement pstmt, List<Automaton> automatons,
			Logger logger, int report) {
		try{
			BufferedWriter outStream = file != null ? new BufferedWriter(new FileWriter(file)) : null;

			Pattern splitter = Pattern.compile(""+CustomRunAutomaton.delimiter);
			int c = 0;
			logger.info("%t: Starting automaton conversion.\n");
			for (Automaton a : automatons){
				Set<String> variants = a.getFiniteStrings();

				if (variants == null)
					throw new IllegalStateException("Non-finite automaton detected - this cannot be converted");

				if (variants.size() == 0)
					System.err.println("0");

				for (String v : variants){
					String[] parts = splitter.split(v);

					if (parts.length != 2)
						throw new IllegalStateException("parts.length == " + parts.length);

					if (outStream != null)
						outStream.write(parts[1] + "\t" + parts[0] + "\n");
					if (pstmt != null){
						pstmt.setString(1, parts[1]);
						pstmt.setString(2, parts[0]);
						pstmt.setNull(3, java.sql.Types.NULL);
						pstmt.addBatch();
					}
				}	
				
				if (pstmt != null)
					pstmt.executeBatch();

				if (report != -1 && ++c % report == 0)
					logger.info("%t: Converted " + c + " automatons to variants.\n");
			}

			if (outStream != null)
				outStream.close();

			logger.info("%t: Completed.\n");
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
