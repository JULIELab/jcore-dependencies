package corpora;

import utils.DBUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Chinh
 * @Date: Aug 30, 2010
 */
public class DataLoader {

	DataSaver saver;
	String currentPid = null;
	int countabs = 0;

	public DataLoader() {
		// saver = new DataSaver("D:/DB");
	}

	private void readProtein(String filename) {
		File file = new File(filename);
		BufferedReader reader = null;
		String st[];
		try {
			reader = new BufferedReader(new FileReader(file));
			String text = null;
			// repeat until all lines is read
			while ((text = reader.readLine()) != null) {
				readProteinLine(text);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Reads, and stores in the DB, a single protein mention given in the BioNLP
	 * Shared Task 2011 format:<br/>
	 * <code>
	 * ID&lt;tab&gt;Entity-Type[Protein]&lt;tab&gt;start&lt;tab&gt;end&lt;tab&gt;Mention name
	 * </code> <br/>
	 * Example: <samp> T3 Protein 166 174 TGF-beta </samp>
	 * 
	 * @param proteinLine
	 */
	private void readProteinLine(String proteinLine) {
		String[] st;
		st = proteinLine.split("\\t|\\s+", 5);
		int lWs = 0, rWs = 0;
		String name = st[4];
		// remove leading whitespaces
		for (int i = 0; i < name.length(); ++i) {
			if (name.charAt(i) == ' ')
				lWs++;
			else
				break;
		}
		for (int i = name.length() - 1; i > 0; --i) {
			if (name.charAt(i) == ' ')
				rWs++;
			else
				break;
		}
		// store with possibly corrected offsets due to leading or trailing
		// whitespaces
		saver.saveProtein(currentPid, st[0], Integer.parseInt(st[2]) + lWs,
				Integer.parseInt(st[3]) - rWs, name.trim());
		// just some code to quickly check the correction by whitespace removal
		// if (rWs != 0 || lWs != 0) {
		// System.out.println(st[4] + " " + st[2] + "-" + st[3]);
		// System.out.println(name.trim() + " " + (Integer.parseInt(st[2])+lWs)
		// + "-" + (Integer.parseInt(st[3])-rWs));
		// }
	}

	private void readAbstract(String filename) {
		File file = new File(filename);
		BufferedReader reader = null;
		String st[];
		try {
			reader = new BufferedReader(new FileReader(file));
			String text = null;
			List<String> list = new ArrayList<String>();
			// repeat until all lines is read
			while ((text = reader.readLine()) != null) {
				list.add(text);
			}
			text = list.get(0);
			if (list.size() > 1) {
				for (int i = 1; i < list.size(); i++) {
					text = text + "\n" + list.get(i);
				}
			}
			saver.saveAbstract(currentPid, text);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void readTrigger(String filename) {
		File file = new File(filename);
		BufferedReader reader = null;
		String st[];
		String[] sub1, sub2, sub3, sub4;
		String cause_id = "", theme2 = "";
		try {
			reader = new BufferedReader(new FileReader(file));
			String text = null;

			// repeat until all lines is read
			while ((text = reader.readLine()) != null) {
				if (text.startsWith("T")) {
					st = text.split("\\t|\\s+", 5);
					saver.saveTrigger(currentPid, st[0], st[1],
							Integer.parseInt(st[2]), Integer.parseInt(st[3]),
							st[4]);
				} else {
					st = text.split("\\t|\\s+", 0);
				}
				if (st[0].startsWith("E")) { // Event
					if (st.length >= 4) { // trigger, theme and cause
						sub3 = st[3].split(":");
						if (sub3[0].startsWith("Theme2")) {
							theme2 = sub3[1];
							cause_id = "";
						} else if (sub3[0].startsWith("Cause")) {
							cause_id = sub3[1];
							theme2 = "";
						} else {
							cause_id = "";
							theme2 = "";
						}
					} else {
						cause_id = "";
						theme2 = "";
					}
					sub1 = st[1].split(":"); // type and trigger id
					sub2 = st[2].split(":"); // theme
					saver.saveEvent(currentPid, st[0], sub1[0], sub1[1],
							sub2[1], theme2, cause_id);
				} else if (st[0].startsWith("M")) {
					saver.saveModify(currentPid, st[0], st[1], st[2]);
				} else if (st[0].startsWith("*")) {
					st = text.split("\\t|\\s+", 4);
					saver.saveEquiv(currentPid, st[2], st[3]);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void loadData(String path, boolean train) {
		// Set filter to .txt, extract filename
		// Get list of all .txt in the given folder
		// For each file .a1: read protein, save to Protein table
		// For each file .a2: read trigger and event, save them to the
		// corresponding tables
		String name, n, n1, n2, file_path;
		int count = 0;
		try {
			File file = new File(path);
			File[] list = file.listFiles();
			for (File f : list) {
				if (f.isFile()) {
					name = f.getName();
					file_path = f.getParent();
					if (name.endsWith("txt")) {
						String[] ns = name.split("\\.txt");
						currentPid = ns[0];
						n = file_path + File.separatorChar + ns[0] + ".txt";
						readAbstract(n);
						n1 = file_path + File.separatorChar + ns[0] + ".a1";
						readProtein(n1);
						if (train) {
							n2 = file_path + File.separatorChar + ns[0] + ".a2";
							readTrigger(n2);
						}
						count++;
					}
				}
			}

		} catch (Exception e) {
			throw new RuntimeException("Could not load data at " + path, e);
		}
		System.out.println("Number of abstracts: " + count);
	}

	public void Txt2Db(String path, String dest, boolean train) {
		DBUtils db = new DBUtils();
		db.openDB(dest);
		System.out.println("Loading data .... !");
		saver = new DataSaver(db);
		loadData(path, train);
		System.out.println("Loading data .... done!");
		// simplification is done once at prediction time so it is of no
		// specific use to do it here.
		// on the other hand, at prediction time, the original text as well as
		// the simplified text are sentence-segmented. Sometimes the segments
		// are not equal due to minor errors in gene tagging or just because the
		// sentence splitter makes a different decision for the simplified text.
		// Thus, we simplify at prediction time and do it sentence-wise.
		// SenSimplifier simp = new SenSimplifier(db);
		// simp.doSimplify();
		// System.out.println("Simplifying data ... done!");
		// System.out.println("Generating dictionary ... done!");
		db.closeDB();

	}

	/**
	 * Creates a database for event extraction as an programmatic API-call where
	 * all values are given directly rather then reading the values from files.
	 * In contrast to {@link #Txt2Db(String, String, boolean)}, created
	 * in-memory database is returned for further processing and not persisted
	 * to file.<br/>
	 * The protein lines have to match the Shared Task 2011 format:<br/>
	 * <code>
	 * ID&lt;tab&gt;Entity-Type[Protein]&lt;tab&gt;start&lt;tab&gt;end&lt;tab&gt;Mention name
	 * </code> <br/>
	 * Example: <samp> T3 Protein 166 174 TGF-beta </samp>
	 * 
	 * @param pid
	 * @param text
	 * @param proteins
	 * @return
	 */
	public DBUtils Txt2Db(String pid, String text, List<String> proteins) {
		currentPid = pid;
		// open in-mem database
		DBUtils db = new DBUtils(pid, "mem");
		db.openDB();
		// System.out.println("Loading data .... !");
		saver = new DataSaver(db);
		// store document text in the database
		saver.saveAbstract(currentPid, text);
		// store the protein mentions
		for (String proteinLine : proteins)
			readProteinLine(proteinLine);
		// System.out.println("Loading data .... done!");
//		SenSimplifier simp = new SenSimplifier(db);
//		simp.doSimplify();
		// System.out.println("Simplifying data ... done!");
		// System.out.println("Generating dictionary ... done!");
		return db;
	}

	public static void main(String[] args) {
//		DataLoader data = new DataLoader();
//		data.Txt2Db("D:/DataNLP/Data2011TestText", "D:/DataNLP/Data2011TestPrepared/Data", false);

		 DataLoader data = new DataLoader();
		 if (args.length == 3) {
		 Boolean a2 = Boolean.valueOf(args[2]);
		 data.Txt2Db(args[0], args[1], a2);
		 }
		 else if (args.length == 2) {
		 data.Txt2Db(args[0], args[1], true);
		 }
		 else {
		 System.out.println("No input and output folder declared.");
		 System.exit(1);
		 }
	}

	static final String new_line = System.getProperty("line.separator");
}
