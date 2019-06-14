package relations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.DBUtils;
import utils.SentenceSplitter;
import utils.SentenceSplitter.BioSemSentence;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Chinh
 * @Date: Oct 26, 2010
 * @Revision: Jan 26, 2011 Simplifying sentence: replacing real protein name
 *            with place holder
 */
public class SenSimplifier {

	private static final Logger log = LoggerFactory
			.getLogger(SenSimplifier.class);

	List<TData> plist; // list of protein belongs to the current abstract
	List<TData> tlist; // list of trigger belongs to the current abstract
	String abst, simp;
	DBUtils db;
	Connection con;
	Statement stmt;
	ResultSet rs;
	PreparedStatement ps_sen;
	Pattern pt;
	Matcher mc;
	boolean loaddict = false;
	List<String> idList = new ArrayList<String>();
	public Set<String> simpleDic = new HashSet<String>();
	int fvalues = 2; // threshold to skip a trigger
	double fscore = 0.1;

	public SenSimplifier(DBUtils dbs) {
		try {
			db = dbs;
			con = db.getConnection();
			stmt = con.createStatement();
			pt = Pattern.compile("PRO\\d{1,3}");
			// init preposition hash

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
	}

	public SenSimplifier() {

	}

	/**
	 * Set default database
	 * 
	 * @param dbs
	 */
	public void setDB(DBUtils dbs) {
		// if (db != null) {
		// db.closeDB();
		// }
		db = dbs;
		con = db.getConnection();
		try {
			stmt = con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load pattern from database
	 * 
	 * @param db
	 *            : DB source to load pattern (training DB)
	 * @return: list of patterns
	 */
	public Map<String, Rules>[] loadPatterns(DBUtils db) {
		Map<String, Rules> rules[] = new HashMap[SenSimplifier.trigger_type.length]; // number
																						// of
																						// event
																						// types
		for (int i = 0; i < SenSimplifier.trigger_type.length; i++) { // init
																		// map
			rules[i] = new HashMap<String, Rules>();
		}
		DBUtils dbs = db;
		Connection cons;
		Statement stms;
		ResultSet rst;
		int pcount;
		int verb_type;
		String POS; // POS of trigger
		String chunk_type; // chunk contains trigger // NP ; VB (include ADJP)
		boolean prep1_pos;// behind trigger; false: in front of trigger
		boolean prep2_pos;// behind trigger; false: in front of trigger // use
							// for binding and reg events
		boolean prep_order;// if(theme1_pos && theme2_pos): same side -> true if
							// theme1 behinds theme2 (position)
		String prep1;// of, on, in, to, between...
		boolean has_theme2;// whether has theme2 (binding) or cause (regulatory)
		String prep2;// cause or theme2: with, and, or, ....
		// In some cases, NP chunk can have subject as cause; in normal case,
		// this is applied for verb chunk
		boolean in_chunk;
		int count1, count2;
		// for regulatory event
		boolean event1; // theme as event
		boolean event2;// cause as event
		String trg1; // trigger of event that acts as theme / valid only if
						// event1 is true
		String trg2; // trigger of event that acts as cause / valid only if
						// event2 is true
		int ptcount = 0;
		try {
			cons = dbs.getConnection();
			stms = cons.createStatement();
			String sql = "select trgkey,type,verb_type, pos, chunk_type,pos1, pos2,"
					+ "prep1,prep2,prep_order,has_theme2,in_chunk,chunk1,chunk2,event1,event2,trg1,trg2,pcount, detected from PATTERNS";
			rst = stms.executeQuery(sql);
			Map<String, Rules> m;
			String type;
			Rules rule;
			RuleData p;
			int idx;
			String key;
			while (rst.next()) {
				ptcount++;
				key = rst.getString(1);
				type = rst.getString(2);
				idx = hashType.get(type);
				m = rules[idx];
				rule = m.get(key);
				if (rule == null) {
					rule = new Rules(idx, key);
					m.put(key, rule);
				}
				// now fill in data
				verb_type = rst.getInt(3);
				POS = rst.getString(4);
				chunk_type = rst.getString(5);
				prep1_pos = rst.getBoolean(6);
				prep2_pos = rst.getBoolean(7);
				prep1 = rst.getString(8);
				prep2 = rst.getString(9);
				prep_order = rst.getBoolean(10);
				has_theme2 = rst.getBoolean(11);
				in_chunk = rst.getBoolean(12);
				count1 = rst.getInt(13);
				count2 = rst.getInt(14);
				event1 = rst.getBoolean(15);
				event2 = rst.getBoolean(16);
				trg1 = rst.getString(17);
				trg2 = rst.getString(18);
				pcount = rst.getInt(19);
				rule.rcount += pcount;
				if (idx < 5) {
					p = new RuleData(verb_type, POS, chunk_type, prep1_pos,
							prep1, in_chunk, count1, trg1);
				} else if (idx == 5) {
					p = new RuleData(verb_type, POS, chunk_type, prep1_pos,
							prep2_pos, prep_order, prep1, prep2, has_theme2,
							in_chunk, count1, count2, trg1);
				} else {
					p = new RuleData(verb_type, POS, chunk_type, prep1_pos,
							prep2_pos, prep_order, prep1, prep2, has_theme2,
							in_chunk, count1, count2, event1, event2, trg1,
							trg2);
				}
				p.count = pcount;
				p.detected = rst.getInt(20);
				if (pcount >= 1) {
					rule.map.put(p.getKey(), p);
				}
			}
			rst.close();
			System.out
					.println("---DONE---> Loading patterns, number of patterns: "
							+ ptcount);
		} catch (Exception e) {
			System.out.println(e.getCause());
		}
		return rules;
	}

	public Map<String, RuleSet> loadRuleSet(DBUtils db) {
		Map<String, RuleSet> map = new HashMap<String, RuleSet>();
		DBUtils dbs = db;
		Connection cons;
		Statement stms;
		ResultSet rst;
		try {
			cons = dbs.getConnection();
			stms = cons.createStatement();
			String sql = "select key,inchunk,dist1,dist2,prep,prep2,t_order,pcount,ecount,t2count, "
					+ "pcause,ecause,inprep,in_front,prep_1,prep_2,detected, inchunk_count, apply from RULESET";
			rst = stms.executeQuery(sql);
			RuleSet rule;
			String key;
			String prep, prep2, inprep, prep_1, prep_2;
			String st[], st1[], st2[];
			while (rst.next()) {
				rule = new RuleSet();
				key = rst.getString(1);
				rule.in_chunk = rst.getBoolean(2);
				rule.dist1 = rst.getInt(3);
				rule.dist2 = rst.getInt(4);
				prep = rst.getString(5);
				prep2 = rst.getString(6);
				rule.order = rst.getBoolean(7);
				rule.pcount = rst.getInt(8);
				rule.ecount = rst.getInt(9);
				rule.t2count = rst.getInt(10);
				rule.pcause = rst.getInt(11);
				rule.ecause = rst.getInt(12);
				inprep = rst.getString(13);
				rule.in_front = rst.getInt(14);
				prep_1 = rst.getString(15);
				prep_2 = rst.getString(16);
				rule.detected = rst.getInt(17);
				if (rule.detected <= 1) {
					continue;
				}
				rule.inchunk_count = rst.getInt(18);
				rule.apply = rst.getInt(19);
				if (!prep.isEmpty()) {
					Set<String> set = new HashSet<String>();
					st = prep.split(" ");
					set.addAll(Arrays.asList(st));
					rule.prep = set;
				}
				if (!inprep.isEmpty()) {
					Set<String> set = new HashSet<String>();
					st = inprep.split(" ");
					set.addAll(Arrays.asList(st));
					rule.inchunk_prep = set;
				}
				if (!prep_1.isEmpty()) {
					Set<String> set = new HashSet<String>();
					st = prep_1.split(" ");
					set.addAll(Arrays.asList(st));
					rule.prep_1 = set;
				}
				if (!prep_2.isEmpty()) {
					Set<String> set = new HashSet<String>();
					st = prep_2.split(" ");
					set.addAll(Arrays.asList(st));
					rule.prep_2 = set;
				}
				if (!prep2.isEmpty()) {
					Map<String, Set<String>> prep2Map = new HashMap<String, Set<String>>();
					st = prep2.split("\\|");
					for (String s : st) {
						st1 = s.split(":");
						st2 = st1[1].split(" ");
						Set<String> set = new HashSet<String>();
						set.addAll(Arrays.asList(st2));
						prep2Map.put(st1[0], set);
					}
					rule.prep2 = prep2Map;
				}
				map.put(key, rule);
			}
			rst.close();
			System.out
					.println("---DONE---> Loading rule set, number of rules: "
							+ map.size());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getLocalizedMessage());
			System.out.println(e.getCause());
		}
		return map;
	}

	public void loadSimpleDic(Map<String, KeyData> map) {
		if (simpleDic.isEmpty()) {
			for (String s : map.keySet()) {
				simpleDic.add(s);
			}
		}
		System.out.println("Loading simple dict: " + simpleDic.size()
				+ " entries");
	}

	public void loadSimpleDic() {
		Connection cons;
		Statement stms;
		if (simpleDic.isEmpty()) {
			try {
				cons = db.getConnection();
				stms = cons.createStatement();
				rs = stms
						.executeQuery(" select key, tt from trigdata order by tt desc");
				String s, tmp[];
				while (rs.next()) {
					s = rs.getString(1);
					tmp = s.split(" ");
					if (tmp.length <= 2 && s.length() >= 3) {
						simpleDic.add(s);
					}
				}
				rs.close();
			} catch (Exception ex) {
				System.out.println("Error in loading simple dic, due to: "
						+ ex.getLocalizedMessage());
			}
			System.out.println("---> loading simple dict DONE!");
			List<String> remove = new ArrayList<String>();
			for (String s : simpleDic) {
				if (s.contains("-")) {
					String ww[] = s.split("-");
					if (ww.length == 2 && simpleDic.contains(ww[0] + ww[1])) {
						remove.add(s);
					}
				}
			}
			for (String s : remove) {
				simpleDic.remove(s);
			}
		}
	}

	public void setSimpleDic(Set<String> map) {
		simpleDic = map;
	}

	/**
	 * Load dictionary from default database DB: DB source to load dictionary
	 */
	public final void loadDict(DBUtils dbs) {
		DBUtils dbdict;
		dbdict = dbs; // use given database
		Connection cons;
		Statement stms;
		try {
			String sql = "select key,ktype,type,freq,total, pcount, ecount,pcause,ecause,t2count,child, parent from keydata";
			cons = dbdict.getConnection();
			stms = cons.createStatement();
			rs = stms.executeQuery(sql);
			String key, stype, child, parent;
			KeyData item;
			int freq, keytype, found, pcount, ecount, pcause, ecause, t2count;
			while (rs.next()) {
				key = rs.getString(1);
				keytype = rs.getInt(2);
				stype = rs.getString(3);
				freq = rs.getInt(4);
				found = rs.getInt(5);
				pcount = rs.getInt(6);
				ecount = rs.getInt(7);
				pcause = rs.getInt(8);
				ecause = rs.getInt(9);
				t2count = rs.getInt(10);
				child = rs.getString(11);
				parent = rs.getString(12);
				item = new KeyData(key, stype, freq, keytype, found);
				item.pcount = pcount;
				item.ecount = ecount;
				item.pcause = pcause;
				item.ecause = ecause;
				item.t2count = t2count;
				item.initData(child, parent);
				if (keytype == 1) {
					sharedDic.put(key, item);
				} else {
					KeyData tmp = sharedDic.get(key);
					if (tmp != null) {
						tmp.addToMap(item);
						tmp.freq += freq;
						tmp.score = (tmp.freq * 1f) / (found * 1f); // use to
																	// determine
																	// whether
																	// to skip
																	// if this
																	// value
																	// belows a
																	// threshold
					} else {
						tmp = new KeyData(key, null, 0, keytype, found);
						tmp.freq = freq;
						tmp.addToMap(item);
						tmp.score = (tmp.freq * 1f) / (tmp.found * 1f); // use
																		// to
																		// determine
																		// whether
																		// to
																		// skip
																		// if
																		// this
																		// value
																		// belows
																		// a
																		// threshold
						sharedDic.put(key, tmp);
					}
				}
			}
			rs.close();
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
		System.out.println("Loading dic ... done. Number of entries:"
				+ sharedDic.size());
		loaddict = true;
		List<String> rm = new ArrayList<String>();
		for (String s : sharedDic.keySet()) {
			KeyData k = sharedDic.get(s);
			if ((k.score < fscore && k.keytype == 1)
					|| (k.score < 0.15 && k.keytype != 1)) {
				rm.add(s);
			}
		}
		for (String k : rm) {
			sharedDic.remove(k); // remove key with confident score lower a
									// given threshold
		}
		loadSimpleDic(sharedDic);
		setModifier();
	}

	/**
	 * Set modifiers for mixed triggers. This method should only be called AFTER
	 * the dictionary is loaded
	 */
	/**
	 * These data learn form Modifier Learner; It would be nicer to
	 * automatically set these modifiers. However, due to their complexity, for
	 * now, semi-automatic approach is applied i.e. we need to check muanually
	 * for each trigger.
	 * 
	 */
	public void setModifier() {

		KeyData dt;
		String list[] = {
				// Gene_expression
				"appearance:Gene_expression:gene|genes:true",
				"induction:Gene_expression:gene1|genes1:true",
				"secreting:Gene_expression:gene|genes:true",
				// Transcription
				"expression:Transcription:mRNA|mRNAs|transcripts:true",
				"expressed:Transcription:RNA|mRNA|mRNAs:true",
				"induction:Transcription:RNA|mRNA|mRNAs:true",
				"express:Transcription:RNA|mRNA|mRNAs:true",
				"production:Transcription:RNA|mRNA|mRNAs|transcripts|level:true",
				"detected:Transcription:RNA|mRNA|mRNAs|level|transcripts:true",
				"levels:Transcription:mRNA|mRNAs|transcripts:true",
				"synthesis:Transcription:mRNA|mRNAs|transcripts:true",
				"transcription:Transcription:RNA|mRNA|mRNAs|level|transcripts|gene|genes:true",
				"transcriptional:Transcription:RNA|mRNA|mRNAs|level|transcripts|gene|genes:true",
				"transcript:Transcription:mRNA|mRNAs|genes|gene|level:true",
				"levels:Transcription:mRNA|mRNAs|transcript:true",
				// Localization
				"expression:Localization:nuclear|nucleus:true",
				"appearance:Localization:nuclear|nucleus:true" };
		String values[];
		String mdf[];
		System.out.println("----------- Set modifier: ");
		for (String s : list) {
			values = s.split(":");
			dt = sharedDic.get(values[0]);
			mdf = values[2].split("\\|");
			if (dt != null && dt.keytype == 3) {
				KeyData dt1 = dt.getKeyData(values[1]);
				if (dt1 != null) {
					dt1.setModifiers(mdf);
					if (values[3].equals("true")) {
						dt1.required = true;
					}
					// System.out.println("--> Key: "+dt1.key+
					// "  Type: "+dt1.type+" Mod: "+values[2]+" Required: "+dt1.required+
					// " etype "+dt1.keytype);
				}
			}
		}
	}

	public List<TData> loadProtein(String pmid) {
		List<TData> list = new ArrayList<TData>();
		String sql = "Select * from PROTEIN WHERE PMID LIKE '" + pmid + "'";
		TData dt;
		try {
			rs = stmt.executeQuery(sql);
			String pid, tid, txt;
			int pos[];
			while (rs.next()) {
				pos = new int[2];
				pid = rs.getString(1);
				tid = rs.getString(2);
				pos[0] = rs.getInt(3);
				pos[1] = rs.getInt(4);
				txt = rs.getString(5);
				dt = new TData(pid, tid, "protein", pos, txt);
				list.add(dt);
			}
			rs.close();
		} catch (Exception e) {
			System.out.println("Load Protein failed: reasons:");
			System.out.println(e.getLocalizedMessage());
		}
		Collections.sort(list);
		for (int i = 0; i < list.size(); i++) {
			list.get(i).new_name = "PRO" + i;
		}
		return list;
	}

	public int getPIndex(String pname) {
		int i = pname.length() - 1;
		while (Character.isDigit(pname.charAt(i))) {
			i--;
		}
		return Integer.parseInt(pname.substring(i + 1));
	}

	public List<String> getProteins(String s) {
		List<String> list = new ArrayList<String>();
		mc = pt.matcher(s);
		while (mc.find()) {
			String group = mc.group();
			list.add(group); // creating a list of proteins from text ;
		}
		return list;
	}

	public int findProIndex(List<TData> plist, int pos) {
		int i = 0;
		while (i < plist.size()) {
			TData data = plist.get(i);
			if (data.list[0] < pos) {
				i++;
			} else {
				return i;
			}
		}
		return plist.size() - 1;
	}

	public List<TData> loadTrigger(String pmid) {
		List<TData> list = new ArrayList<TData>();
		String sql = "Select pmid,tid,t_type,pos1,pos2,txt from TRIGGERS WHERE PMID LIKE '"
				+ pmid + "'";
		TData dt;
		try {
			rs = stmt.executeQuery(sql);
			String pid, tid, txt, ttype;
			int pos[];
			while (rs.next()) {
				pos = new int[2];
				pid = rs.getString(1);
				tid = rs.getString(2);
				ttype = rs.getString(3);
				pos[0] = rs.getInt(4);
				pos[1] = rs.getInt(5);
				txt = rs.getString(6);
				dt = new TData(pid, tid, ttype, pos, txt);
				list.add(dt);
			}
			rs.close();
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
		Collections.sort(list);
		return list;
	}

	public List<EData> loadEvent(String pmid) {
		List<EData> list = new ArrayList<EData>();
		String sql = "Select pmid,eid,t_type,trig_id,theme1,theme2,cause from event WHERE PMID LIKE '"
				+ pmid + "'";
		EData dt;
		try {
			rs = stmt.executeQuery(sql);
			String pid, eid, tid, th1, th2, cause, ttype;
			while (rs.next()) {
				pid = rs.getString(1);
				eid = rs.getString(2);
				ttype = rs.getString(3);
				tid = rs.getString(4);
				th1 = rs.getString(5);
				th2 = rs.getString(6);
				cause = rs.getString(7);
				dt = new EData(pid, eid, ttype, tid, th1, th2, cause);
				list.add(dt);
			}
			rs.close();
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
		return list;
	}

	/**
	 * Load abstract ID from database
	 * 
	 * @return: abstract list
	 */
	public List<String> loadPMIDs() {
		String sql = "SELECT pmid FROM ABSTRACT";
		String pmid;
		try {
			idList.clear();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				pmid = rs.getString(1);
				idList.add(pmid);
			}
			rs.close();
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
		return idList;
	}

	@Deprecated
	public String loadSimplified(String pmid) {
		String sql1 = "select text from simplify where pmid like '" + pmid
				+ "'";
		String text = "";
		try {
			rs = stmt.executeQuery(sql1);
			while (rs.next()) {
				text = rs.getString("text");
			}
			rs.close();
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
		return text;
	}

	public String loadSentence(String pmid) {
		String sql1 = "select text from abstract where pmid like '" + pmid
				+ "'";
		String text = "";
		try {
			rs = stmt.executeQuery(sql1);
			while (rs.next()) {
				text = rs.getString("text");
			}
			rs.close();
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
		return text;
	}

	public String trim(String txt) {
		String temp = txt;
		if (temp.endsWith(",") || temp.endsWith(";") || temp.endsWith(".")
				|| temp.endsWith(":") || temp.endsWith("?")
				|| temp.endsWith(")")) {
			temp = temp.substring(0, temp.length() - 1);
		}
		if (temp.endsWith(")") || temp.endsWith("}") || temp.endsWith("]")
				|| temp.endsWith("?") || temp.endsWith("\"")) {
			temp = temp.substring(0, temp.length() - 1);
		}
		if (temp.startsWith("(") || temp.startsWith("-")
				|| temp.startsWith("[") || temp.startsWith("\"")) {
			temp = temp.substring(1);
		}
		return temp;
	}

	public List<String[]> loadEquiv(String pmid) {
		String sql1 = "select tid1,tid2 from equiv where pmid like '" + pmid
				+ "'";
		List<String[]> list = new ArrayList<String[]>();
		String[] s;
		try {
			rs = stmt.executeQuery(sql1);
			s = new String[2];
			while (rs.next()) {
				s = new String[2];
				s[0] = rs.getString(1);
				s[1] = rs.getString(2);
				list.add(s);
			}
			rs.close();
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
		return list;
	}

	public void testSpliter() {
		String s = "";
		String txt[] = s.split(",");
		if (txt.length >= 1) {
			System.out.println("Values:" + txt[0] + " length: "
					+ txt[0].length());
		}
		System.out.println("Length: " + txt.length);
	}

	/**
	 * Used to split the text after the title which often does not end with a
	 * full stop, this posing an issue for the sentence splitter.
	 * 
	 * @param txt
	 * @param br
	 * @return
	 */
	public String[] brLine(String txt, String br) {
		int idx = txt.indexOf(br);
		String rst[] = new String[2];
		if (idx > 0) {
			rst[0] = txt.substring(0, idx);
			int off = br.equals("\n") ? 1 : 2;
			rst[1] = txt.substring(idx + off);
		} else { // no line break
			rst[0] = txt;
			rst[1] = null;
		}
		return rst;
	}

	@Deprecated
	public String[] abs2Sen(String txt, String br) {
		String ab[] = brLine(txt, br);
		List<BioSemSentence> ls1, ls2;
		ls1 = SentenceSplitter.spliter(ab[0]);
		if (ab[1] != null) {
			ls2 = SentenceSplitter.spliter(ab[1]);
			ls1.addAll(ls2);
		} else {
			ls2 = null;
		}
		List<String> sentences = new ArrayList<>();
		for (int i = 0; i < ls1.size(); i++)
			sentences.add(ls1.get(i).text);
		// for (int i = 0; i < sentences.length; i++) {
		// String string = sentences[i];
		// System.out.println(i + ": " + string);
		//
		// }
		// System.out.println(txt);
		// System.out.println("Number sentences: " + sentences.length);
		return sentences.toArray(new String[sentences.size()]);
	}

	public BioSemSentence[] doSentenceSplitting(String txt, String br) {
		String ab[] = brLine(txt, br);
		List<BioSemSentence> ls1, ls2;
		ls1 = SentenceSplitter.spliter(ab[0]);
		if (ab[1] != null) {
			int brIndex = txt.indexOf(br);
			ls2 = SentenceSplitter.spliter(ab[1]);
			// Correct the offsets for the title
			for (int i = 0; i < ls2.size(); ++i) {
				BioSemSentence s = ls2.get(i);
				s.begin = s.begin + brIndex + 1;
				s.end = s.end + brIndex + 1;
			}
			ls1.addAll(ls2);
		} else {
			ls2 = null;
		}
		return ls1.toArray(new BioSemSentence[ls1.size()]);
	}

	public int getSenIndex(int start, int[] subpos) {
		for (int i = subpos.length - 1; i > 0; i--) {
			if (start >= subpos[i]) {
				return i;
			}
		}
		return 0;
	}

	public int getOffset(int idx, int[] subpos) {
		return subpos[idx];
	}

	private void storeSentence(String pmid, String txt) {
		try {
			ps_sen.setString(1, pmid);
			ps_sen.setString(2, txt);
			ps_sen.executeUpdate();
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
	}

	int pmiss_count = 0;
	int trmiss_count = 0;

	public String[] doSimplifySentenceWise(BioSemSentence[] originalSentences,
			List<TData> proList) {
		List<TData> plist = proList;//loadProtein(proList);
		int pIndex = plist.size() - 1;
		String[] simplifiedSentences = new String[originalSentences.length];
		for (int i = originalSentences.length - 1; i >= 0; --i) {
			BioSemSentence sentence = originalSentences[i];
			StringBuilder sb = new StringBuilder(sentence.text);
			// iterate through those proteins which occur within the sentence
			while (pIndex >= 0 && plist.get(pIndex).list[0] >= sentence.begin) {
				TData dt = plist.get(pIndex);
//				String name = "PRO" + pIndex;
				String name = dt.new_name;
//				if (!name.equals(dt.new_name)) {
//				System.out.println(name);
//				System.out.println(dt.new_name);
//				}
				sb = sb.replace(dt.list[0] - sentence.begin, dt.list[1]
						- sentence.begin, name);
				--pIndex;
			}
			simplifiedSentences[i] = sb.toString();
		}
		return simplifiedSentences;
	}

	@Deprecated
	public void doSimplify() {
		loadPMIDs();// load PMID list
		TData dt;
		StringBuilder sb = new StringBuilder();
		String name = "", abtxt, ptxt = null, trtxt;
		try {
			stmt.executeUpdate("DROP TABLE SIMPLIFY IF EXISTS");
			// stmt.executeUpdate("CREATE CACHED TABLE SIMPLIFY(PMID VARCHAR(80), TEXT VARCHAR(25000))");
			stmt.executeUpdate("CREATE CACHED TABLE SIMPLIFY(PMID VARCHAR(80), TEXT CLOB)");
			ps_sen = con
					.prepareStatement("INSERT INTO SIMPLIFY(PMID,TEXT) VALUES(?,?)");
			for (String s : idList) { // for all abstracts
				// load Proteins
				sb = new StringBuilder();
				plist = loadProtein(s);
				abtxt = loadSentence(s);
				sb.append(abtxt);
				for (int i = plist.size() - 1; i >= 0; i--) {
					name = "PRO" + i;
					dt = plist.get(i);
					sb = sb.replace(dt.list[0], dt.list[1], name);
					ptxt = abtxt.substring(dt.list[0], dt.list[1]);
				}
				storeSentence(s, sb.toString());
			}
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			System.out.println("Text " + sb.toString());
			System.out.println("Protein" + name);
			e.printStackTrace();
		}
		log.debug("Miss proteins: {}", pmiss_count);
		// System.out.println("Miss proteins: " + pmiss_count);
	}

	public void closeDB() {
		if (db != null) {
			db.shutdownDB();
		}
	}

	/**
	 * Check whether this sentence qualify for extracting event
	 * 
	 * @return: true if the sentence contains at leat one PRO and one REL
	 */
	public int er_count = 0;
	/**
	 * Determine trigger type of a given trigger
	 * 
	 * @param words
	 *            : list of token of the given sentence
	 * @param list
	 *            : list of trigger of the given sentence
	 * @return
	 */
	int bug_count = 0;

	public static void main(String[] args) {
	}

	public final int nr_event = 9;
	public static final String[] preps = { "of", "to", "between", "with", "by",
			"through", "on", "for", "in", "upon", "after", "via" };
	public static final Set<String> prepmap = new HashSet<String>();
	public static final Map<String, String> modifiers = new HashMap<String, String>();
	public Map<String, String> equimap = new HashMap<String, String>();
	public static final Map<String, String> none_rel = new HashMap<String, String>();
	public static final String[] trigger_type = { "Gene_expression",
			"Transcription", "Protein_catabolism", "Phosphorylation",
			"Localization", "Binding", "Positive_regulation", "Regulation",
			"Negative_regulation", "Ubiquitination", "Protein_modification",
			"Deacetylation", "Acetylation" };
	public static final Map<String, Integer> hashType = new HashMap<String, Integer>();
	public Map<String, KeyData> sharedDic = new HashMap<String, KeyData>();
	public Map<String, List<String>> equiList = new HashMap<String, List<String>>();

	static {
		prepmap.addAll(Arrays.asList(preps));
		for (int i = 0; i < trigger_type.length; i++) {
			hashType.put(trigger_type[i], i);
		}
		for (String s : TriggerLearner.none_rel) {
			none_rel.put(s, s);
		}
		String[] mod = { "gene", "genes", "nuclear", "nucleus", "mRNA",
				"mRNAs", "RNA", "transcripts", "transcript" };
		for (String s : mod) {
			modifiers.put(s, s);
		}
	}
}
