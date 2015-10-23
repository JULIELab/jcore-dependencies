package relations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.DBUtils;

/**
 * 
 * @author Chinh
 * @Date: Oct 27, 2010
 * @Revision Jan 22, 2011
 */
public class TriggerLearner {

	Map<String, Counter>[] maptype = new HashMap[trigger_type.length];
	DBUtils db_sr, db_dest;
	Connection con;
	Statement stmt;
	ResultSet rs;
	PreparedStatement ps;
	SenSimplifier sim;
	int nr_event = 9;
	public List<Word> proWord = new ArrayList<Word>();
	public List<Word> prepWord = new ArrayList<Word>();
	public List<Word> relWords = new ArrayList<Word>();
	int fthreshold = 2; // thredhold to drop a trigger if its frequency is lower this value

	public TriggerLearner(DBUtils sr, DBUtils dbs) {
		db_sr = sr;
		db_dest = dbs;
		try {
			con = db_sr.getConnection();
			stmt = con.createStatement();
		} catch (Exception e) {
			System.out.println(e.getCause());
		}
		// trigger type <-> index
		for (int i = 0; i < trigger_type.length; i++) {
			hashType.put(trigger_type[i], i);

		}
		// list of none-relation
		for (int i = 0; i < none_rel.length; i++) {
			notrigger.put(none_rel[i], none_rel[i]);
		}
		sim = new SenSimplifier(db_sr);
	}

	public void clearList() {
		proWord.clear();
		prepWord.clear();
		relWords.clear();
	}

	/**
	 * Read raw data from tables, generate statistical values
	 */
	public void preparedData() {
		String sql =
				"select t_type,txt, count(txt) as num from (select event.t_type, triggers.txt from event, triggers " + "where event.PMID = triggers.PMID and event.TRIG_ID = triggers.TID) group by t_type,txt order by num desc";
		Map<String, Counter> item;
		String t_type, txt;
		int count, len = trigger_type.length;
		List<String> keylist = new ArrayList<String>();
		Map<String, String> mapkey = new HashMap<String, String>();
		String wds[];
		int idx;
		try {
			System.out.println("Creating trigger list .....");
			stmt.execute("DROP TABLE trigdata if exists");
			stmt.execute("CREATE CACHED TABLE TRIGDATA(KEY VARCHAR(80), expr INT, trans INT , " + "catabo int, phospho INT, local INT, bind int, pos_reg int, reg int, neg_reg int, ubi int, prot_mod int, deacet int, acet int, tt int) ");
			ps =
					con.prepareStatement("insert into trigdata(key,expr,trans,catabo,phospho,local," + "bind,pos_reg,reg,neg_reg,ubi,prot_mod,deacet,acet,tt) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			for (int i = 0; i < len; i++) {
				maptype[i] = new HashMap<String, Counter>(); // map of trigger type
			}
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				t_type = rs.getString(1);
				idx = SenSimplifier.hashType.get(t_type);
				item = maptype[idx]; // hash map of event type
				txt = rs.getString(2).toLowerCase();
				if (notrigger.containsKey(txt)) {
					continue; // skip this trigger since it in non-trigger list
				}
				count = rs.getInt(3);
				if (!item.containsKey(txt)) {
					item.put(txt, new Counter(count)); // add new entry
				} else {
					Counter c = item.get(txt); // increase frequency
					c.add(count);
				}
				if (!mapkey.containsKey(txt)) {
					mapkey.put(txt, txt);
					keylist.add(txt);
				}
			}
			rs.close();
			Map<String, Counter> array[] = maptype; // number of event types
			int data[] = new int[len];
			for (String s : keylist) { // loop over key list
				for (int i = 0; i < len; i++) { // loop over event type
					data[i] = 0;
					if (array[i].containsKey(s)) {
						data[i] = array[i].get(s).getValue();
					}
				}
				// store to TRIGDATA table
				saveData(s, data); // key -> [count1, count2, ....count9]: frequency of each event coresspond to this
									// key
			}
			// Now prepared list of trigger for each event type
			ps.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	Map<String, String> tmap = new HashMap<String, String>(); // shared dictionary

	/**
	 * Count number of events have frequency higher the given threshold.
	 * 
	 * @param data
	 * @return
	 */
	private List<Integer> getType(int[] data) {
		int freq = fthreshold; // Change threshold here
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < 9; i++) { // 9 event types
			if (data[i] >= freq) {
				list.add(i);
			}
		}
		if (list.size() == 1) {
			return list;
		} else { // for share/mix trigger ; increase threshold to 6
			int i = 0, idx;
			while (i < list.size()) {
				idx = list.get(i);
				if (data[idx] < freq + 3) {
					list.remove(i);
					continue;
				} else {
					i++;
				}
			}
		}
		return list;
	}

	private void saveKeyData(Map<String, KeyData> map) {
		Connection con1;
		Statement stmt1;
		PreparedStatement ps1;
		String sql =
				" Insert into keydata(key,ktype,type,freq,total, pcount, ecount,pcause,ecause,t2count,child, parent) " + "values(?,?,?,?,?,?,?,?,?,?,?,?)";
		System.out.println("Saving trigger data..........");
		try {
			System.out.println("---> Saving dictionary, number of entries: " + map.size());
			con1 = db_dest.getConnection();
			stmt1 = con1.createStatement();
			stmt1.execute("DROP TABLE keydata if exists");
			stmt1.execute(" Create cached table KEYDATA(key varchar(80), ktype int, type varchar(50), freq int, total int, pcount int," + "ecount int, pcause int, ecause int, t2count int,child varchar(2000), parent varchar(2000))");
			ps1 = con1.prepareStatement(sql);
			KeyData dt;
			int total;
			List<KeyData> ls = new ArrayList<KeyData>();
			for (String s : map.keySet()) {
				dt = map.get(s);
				total = dt.found;
				if (total < fthreshold) {
					continue;
				}
				ls.clear();
				if (dt.keytype == 1) {
					ls.add(dt);
				} else {
					ls.addAll(dt.getMap().values());
				}
				for (KeyData k : ls) {
					ps1.setString(1, k.key);
					ps1.setInt(2, k.keytype);
					ps1.setString(3, k.type);
					ps1.setInt(4, k.freq);
					ps1.setInt(5, total);
					ps1.setInt(6, k.pcount);
					ps1.setInt(7, k.ecount);
					ps1.setInt(8, k.pcause);
					ps1.setInt(9, k.ecause);
					ps1.setInt(10, k.t2count);
					ps1.setString(11, k.set2String(k.child));
					ps1.setString(12, k.set2String(k.parent));
					ps1.executeUpdate();
				}
			}
			ps1.close();
		} catch (Exception e) {
			System.out.println(e.getCause());
		}
		System.out.println("Saving trigger data... done!");
	}

	/**
	 * Creating trigger list from training data
	 */
	public void createTriggers() {
		preparedData();
		generateKeyData();
	}

	public static void main(String[] args) {
		String dbsr = "D:/DataNLP/Mix2011/Data";
		DBUtils dbsrc = new DBUtils();
		dbsrc.openDB(dbsr); // source database
		String dbname = "D:/DataNLP/Dev2011/Data";
		DBUtils dbdest = new DBUtils();
		dbdest.openDB(dbname); // destination database
		TriggerLearner learner = new TriggerLearner(dbsrc, dbdest); // store all triggers (both train and dev) into
																	// train DB
		learner.createTriggers();
		dbdest.shutdownDB();
		// dbsrc.closeDB();
	}

	private void saveData(String key, int[] data) {
		int len = trigger_type.length;
		int sc[] = new int[len];
		int sum = 0;
		try {
			for (int i = 0; i < len; i++) {
				sc[i] = data[i];
				score[i] += sc[i]; // total frequency of this trigger
				sum += sc[i];
			}
			if (sum <= fthreshold) {
				return;
			}
			ps.setString(1, key);
			for (int i = 0; i < len; i++) {
				ps.setInt(i + 2, sc[i]);
			}
			ps.setInt(len + 2, sum);
			ps.executeUpdate();
		} catch (Exception e) {
			System.out.println(e.getCause());
		}
	}

	public boolean initEventData(String pid) {
		plist = sim.loadProtein(pid); // load Protein list
		tlist = sim.loadTrigger(pid); // load Trigger list based on PMID
		elist = sim.loadEvent(pid); // load Event list

		// preparing Protein Map
		mprotein.clear();
		for (TData dt : plist) {
			mprotein.put(dt.tid, dt); // map <id, protein>
		}
		// preparing trigger map
		mtrigger.clear();
		for (TData dt : tlist) {
			mtrigger.put(dt.tid, dt); // map <trigger id , trigger>
		}
		// preparing event map
		mevent.clear();
		for (EData edt : elist) {
			mevent.put(edt.eid, edt); // map <event id , event>
		}
		for (EData ed : elist) {
			ed.init(mprotein, mtrigger, mevent);
		}
		return true;
	}

	private boolean inSentence(int begin, int end, EData ev) {
		boolean theme = false, cause = false;
		TData tg = ev.trgdata;

		if (tg.list[0] >= begin && tg.list[1] <= end) { // same trigger
			// check theme
			if (ev.data1 instanceof TData) {
				TData pr1 = (TData) ev.data1;
				if (pr1.list[0] >= begin && pr1.list[1] <= end) {
					theme = true;
				}
			} else {
				EData ev1 = (EData) ev.data1;
				theme = inSentence(begin, end, ev1);
			}
			// check cause
			if (ev.ecause != null) {
				if (ev.ecause instanceof EData) {

					cause = inSentence(begin, end, (EData) ev.ecause);

				} else {
					TData pr2 = (TData) ev.ecause;
					if (pr2.list[0] >= begin && pr2.list[1] <= end) {
						cause = true;
					}
				}
			} else if (ev.data2 != null) { // theme2
				TData pr2 = (TData) ev.data2;
				if (pr2.list[0] >= begin && pr2.list[1] <= end) {
					cause = true;
				}
			} else {
				cause = true;
			}
			if (theme && cause) {
				return true;
			}
		}

		return false;
	}

	private String getKey(String tg, Set<String> dic) {
		if (dic.contains(tg)) {
			return tg;
		}
		String txt[] = tg.split(" ");
		for (int i = txt.length - 1; i >= 0; i--) {
			if (dic.contains(txt[i])) {
				return txt[i];
			}
		}
		return null;
	}

	public void generateKeyData() {
		TData tg, pro, tg2;
		int ev_type = 0;
		List<String> pmids = sim.loadPMIDs();
		sim.loadSimpleDic();// load trigger from trigger data
		SenAnalyzer analyzer = new SenAnalyzer(sim);
		Object data1, data2;
		Map<String, TriggerData>[] allTrg = new HashMap[trigger_type.length];
		Map<String, int[]> keys = new HashMap<String, int[]>();
		for (int i = 0; i < trigger_type.length; i++) {
			allTrg[i] = new HashMap<String, TriggerData>();
		}
		Map<String, Counter> sharedTrg = new HashMap<String, Counter>();
		Map<String, String> tempMap = new HashMap<String, String>();
		TriggerData tgdt, tmtrg;
		Set<String> dict = sim.simpleDic;
		Counter ct;
		int counter[];
		Map<TData, TData> usedTG = new HashMap<TData, TData>();
		String tg_value, tg_value2, tg_key;
		int sen_begin, sen_end;
		try {
			System.out.println("Generating trigger related data.....");
			for (String id : pmids) { // list of abstract
				if (analyzer.initData(id)) {
					initEventData(id); // load events, triggers, proteins
					List<EData> events[] = analyzer.splitEvents(elist);
					for (int i = 0; i < analyzer.shortsen.length; i++) {
						tempMap.clear();
						usedTG.clear();
						sen_begin = analyzer.senpos[i];
						sen_end = analyzer.senpos[i] + analyzer.longsen[i].length();
						for (EData ed : events[i]) {
							ev_type = hashType.get(ed.type);
							if (!inSentence(sen_begin, sen_end, ed)) {
								continue; // Skip: pro belongs to the other sentence
							}
							tg = ed.getTrigger();
							data1 = ed.data1;
							tg_value = tg.name.toLowerCase();
							if (tg_value.contains("-") && tg_value.length() > 8) {
								String ww[] = tg_value.split("-");
								if (ww.length == 2 && dict.contains(ww[0] + ww[1])) {
									tg_value = ww[0] + ww[1];
								}
							}
							// tg_value = getKey(tg_value, dict);
							if (dict.contains(tg_value)) { // known trigger
								if (!usedTG.containsKey(tg)) {
									usedTG.put(tg, tg);
								} else {
									continue;// this trigger has been used
								}
								// setup shared triggers
								tg_key = tg_value + tg.list[0] + "" + tg.list[1];
								if (!tempMap.containsKey(tg_key)) {
									tempMap.put(tg_key, ed.type);
								} else {
									String old_type = tempMap.get(tg_key);
									if (!old_type.equals(ed.type)) {
										Counter c = sharedTrg.get(tg_value);
										if (c == null) {
											c = new Counter(1);
											sharedTrg.put(tg_value, c);
										} else {
											c.inc();
										}
									}
								}
								// count trigger frequency
								if ((counter = keys.get(tg_value)) == null) {
									counter = new int[trigger_type.length + 1];
									keys.put(tg_value, counter);
								}
								counter[ev_type]++;
								// count theme/cause
								tgdt = allTrg[ev_type].get(tg_value);
								if (tgdt == null) {
									tgdt = new TriggerData(tg_value, tg.type);
									allTrg[ev_type].put(tg_value, tgdt);
								}
								if (data1 instanceof TData) {
									tgdt.pcount++; // protein
								} else {// theme1 is an event
									tgdt.ecount++; // event
									EData obj = (EData) data1;
									int idx = hashType.get(obj.type);
									tg2 = obj.getTrigger();
									String tg2_value = tg2.name.toLowerCase();
									if (dict.contains(tg2_value)) { // known trigger
										tgdt.child[idx].add(tg2_value);
									}
								}
								if (ed.data2 != null) { // binding event
									tgdt.t2_count++;
								}
								if (ed.ecause != null) {
									if (ed.ecause instanceof TData) {
										tgdt.pcause++;
									} else {
										tgdt.ecause++;
										EData obj = (EData) ed.ecause;
										tg2 = obj.getTrigger();
										String tg2_value = tg2.name.toLowerCase();
										if (dict.contains(tg2_value)) { // known trigger
											tgdt.parent.add(tg2_value);
										}
									}
								}
							}
						} // end event loop
							// count detected triggers
						if (analyzer.detectedTrg[i].size() > 0 && analyzer.detectedPro[i].size() > 0) {
							for (Word w : analyzer.detectedTrg[i]) {
								counter = keys.get(w.word);
								if (counter == null) {
									counter = new int[trigger_type.length + 1];
								}
								counter[trigger_type.length]++;
							}
						}
					}
				}
			}
			List<Integer> list;
			int skip = 0, idx;
			Map<String, KeyData> mdict = new HashMap<String, KeyData>();
			KeyData item;
			for (String s : keys.keySet()) {
				counter = keys.get(s);
				list = getType(counter);
				if (list.isEmpty()) {
					System.out.print("Skip:  " + s + " --> freq: ");
					for (int u = 0; u < counter.length; u++) {
						System.out.print(counter[u] + "  ");
					}
					System.out.println("");
					skip++;
					continue;
				} else if (list.size() == 1) {
					idx = list.get(0);
					item = new KeyData(s, trigger_type[idx], counter[idx], 1, counter[trigger_type.length]);// stand-alone
																											// key
					mdict.put(s, item);
				} else {
					int ktype = 3;
					if (sharedTrg.containsKey(s) && sharedTrg.get(s).count > fthreshold + 2) { // share key: threshold
																								// +2 -> avoid noise
						ktype = 2;
						for (int z = 0; z < list.size(); z++) {
							if (counter[list.get(z)] * 1f / sum(counter) < 0.3) {// ~ 0.5 -> number of keys are equals
																					// -> shared trigger
								ktype = 3;
								break;
							}
						}
					}
					item = new KeyData(s, null, sum(counter), ktype, counter[trigger_type.length]);// shared/mix key
					mdict.put(s, item);
					for (int j = 0; j < list.size(); j++) {
						int k = list.get(j);
						KeyData kdt = new KeyData(s, trigger_type[k], counter[k], ktype, counter[9]);
						item.addToMap(kdt);
					}
				}

			}
			// Now analyzing trigger map
			// Loop over reg events: pos, reg, and neg
			Map<String, TriggerData> mtg;
			for (int i = 6; i < 9; i++) {
				mtg = allTrg[i];// regulatory event
				for (String s : mtg.keySet()) { // loop over list of trigger
					tgdt = mtg.get(s); // trigger data
					// Find parent for transcription event
					if (tgdt.child[1].isEmpty()) {
						continue;
					}
					for (String st : tgdt.child[1]) {
						tmtrg = allTrg[1].get(st);
						if (tmtrg == null) {
							continue;
						}
						tmtrg.parent.add(tgdt.trigger);
					}
				}
			}
			// Assigning value to keydata
			StringBuilder ls, cs;
			KeyData kdt = null;
			List<KeyData> kls = new ArrayList<KeyData>();
			for (String s : mdict.keySet()) {
				kls.clear();
				kdt = mdict.get(s);
				if (kdt.keytype == 1) {
					kls.add(kdt);
				} else {
					kls.addAll(kdt.getMap().values());
				}
				for (KeyData dt : kls) {
					idx = hashType.get(dt.type);
					tgdt = allTrg[idx].get(s);
					if (tgdt == null) {
						continue;
					}
					// Loop inside trigger over simple type
					ls = new StringBuilder();
					cs = new StringBuilder();
					for (int j = 0; j < tgdt.child.length; j++) {
						for (String st : tgdt.child[j]) {
							ls.append(st);
							ls.append(',');
						}
						for (String st : tgdt.parent) {
							cs.append(st);
							cs.append(',');
						}
					}
					dt.pcount = tgdt.pcount;
					dt.ecount = tgdt.ecount;
					dt.pcause = tgdt.pcause;
					dt.ecause = tgdt.ecause;
					dt.t2count = tgdt.t2_count;
					dt.initData(ls.toString(), cs.toString());
				}
			}
			saveKeyData(mdict); // store into Database

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getCause());
		}
		System.out.println("Trigger data generating .... Done!");
	}

	/**
	 * Sum of all frequencies of all event types that shared the same trigger
	 * 
	 * @param ls
	 *            : list of frequencies of each event types coressponding to a given trigger
	 * @return
	 */
	private int sum(int[] ls) {
		int total = 0;
		for (int i = 0; i < 9; i++) {
			total += ls[i];
		}
		return total;
	}

	/**
	 * Find location of a given protein
	 * 
	 * @param pr
	 *            : protein name
	 * @return: Word (location)
	 */
	Map<String, TData> mprotein = new HashMap<String, TData>();
	Map<String, TData> mtrigger = new HashMap<String, TData>();
	Map<String, EData> mevent = new HashMap<String, EData>();
	List<TData> plist, tlist; // protein list, trigger list
	List<EData> elist; // event list
	String simp, simpsen[], longsen[];
	TData tgr, prt;
	int[] simp_pos, full_pos;// starting position of the sentence related to the abstract
	int split_count = 0;
	/**
	 * Map between absolute position into related position of a given trigger with abstract/sentence
	 */
	public static String[] trigger_type = { "Gene_expression", "Transcription", "Protein_catabolism",
			"Phosphorylation", "Localization", "Binding", "Positive_regulation", "Regulation", "Negative_regulation",
			"Ubiquitination", "Protein_modification", "Deacetylation", "Acetylation" };
	int[] score = new int[trigger_type.length];
	Map<String, Integer> hashType = new HashMap<String, Integer>();
	Map<String, String> notrigger = new HashMap<String, String>();
	public final static String[] none_rel = { "over", "when", "by", "via", "after", "high", "lower", "under",
			"transcripts", "transcript", "upon", "poor", "potent", "in", "low", "through", "a", "the", "are", "is",
			"was", "for", "into", "not", "it", "that", "level", "levels", "negative", "higher", "low", "because",
			"due", "to", "with", "without", "at", "from", "more", "pair", "both", "and", "on", "inhibitor",
			"inhibitors", "receptors", "receptor", "complex", "complexes", "transcriptional", "heterodimers",
			"heterodimer", "homodimer", "during", "crucial", "failed", "exist", "critical", "of", "due to",
			"because of", "by", "an", "of", "positive", "mrna", "mrnas" };

	public class TriggerData {

		String trigger;
		String type;
		int ttype = -1;
		int pcount = 0; // protein count -> all events
		int ecount = 0; // event count -> reg events ; for simple events: number of reg events infront
		int pcause = 0; // cause as protein -> reg events
		int ecause = 0; // cause as event -> reg events
		int t2_count = 0; // theme2 count -> binding event
		public Set<String>[] child = new HashSet[trigger_type.length];
		public Set<String> parent = new HashSet<String>();

		public TriggerData(String trg, String tp) {
			trigger = trg;
			type = tp;
			for (int i = 0; i < trigger_type.length; i++) {
				child[i] = new HashSet<String>();
			}
		}
	}
}
