package relations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import parser.Parser;
import utils.DBUtils;
import utils.SentenceSplitter.BioSemSentence;

/**
 * 
 * @author Chinh
 * @Date: Jun 10, 2011 Analyzing sentence: 1. Detect triggers 2. Detect proteins
 *        3. Split paragraph or abstract into single sentences. 4. Parse
 *        sentences -> chunks, form relation between chunks
 * 
 *        Input: abstract or paragraph Output: sentences with anotated data
 */
public class SenAnalyzer {

	SenSimplifier simp;
	Map<String, TData> proMap = new HashMap<String, TData>(); // ID -> Protein
	List<TData> proList; // given proteins list
	String longtxt, current, shortsen[], longsen[];
	@Deprecated
	String shorttxt;
	TData trig, pro;
	int[] senpos;// offset of sentences related to the abstract/paragraph
	String current_id;
	public Map<String, String> proIDMap = new HashMap<String, String>(); // PRO
																			// (theme1)
																			// ->
																			// Event
																			// ID
	List<Word> allTriggers, longTrg[], detectedTrg[], detectedPro[]; // all
																		// triggers
																		// ,
																		// triggers
																		// per
																		// sentence
	List<TData> longPro[]; // protein from long sentence/ or / given triggers
	List<String[]> tokenList = new ArrayList(); // tokens per sentence
	List<String[]> tagList = new ArrayList(); // tokens per sentence
	Parser parser = new Parser();
	boolean default_db = true;
	Set<String> simpleDic;
	DBUtils db_sr, db; // DB source and DB destination; DB source: to load dict
						// and patterns ; DB dest: load testing data
	double conf[] = { 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.15, 0.1, 0.1 };
	int tgfreq[] = { 5, 5, 3, 3, 3, 5, 5, 5, 5 };

	/**
	 * For creating new dictionary only
	 * 
	 * @param simp
	 *            : SenSimplifier
	 */
	public SenAnalyzer(SenSimplifier simp) {
		this.simp = simp;
		simpleDic = simp.simpleDic;
	}

	/**
	 * Use given database
	 * 
	 * @param dbs
	 *            : DBUtils
	 */
	public SenAnalyzer(DBUtils sr, DBUtils db_dest) {
		db_sr = sr;
		db = db_dest;
		simp = new SenSimplifier(db);
	}

	/**
	 * Set default database
	 * 
	 * @param dbs
	 */
	public void setDB(DBUtils dbs) {
		db = dbs;
		simp.setDB(db);
	}

	/**
	 * Close database [never called]
	 */
	public void closeDB() {
		if (db != null) {
			db.shutdownDB();
		}
	}

	/**
	 * Loading dictionaries
	 */
	public void init() {
		simp.loadDict(db_sr);
		simpleDic = simp.simpleDic;
	}

	/*
	 * Init abstract or paragraph 1. Break long sentence into single sentences;
	 * 2. Load proteins, assign ID 3. Caculate offsets
	 */
	public boolean initAbstract(String pid) {
		proMap.clear();
		longtxt = simp.loadSentence(pid); // load long sentence / abstract
		if (longtxt == null) {
			System.out.println("PMID: " + pid + " -> no text");
			return false;
		}
		proList = simp.loadProtein(pid); // load Protein list
		BioSemSentence[] originalSentences = simp.doSentenceSplitting(longtxt, "\n");
		shortsen = simp.doSimplifySentenceWise(originalSentences, pid);
		longsen = new String[originalSentences.length];
		for (int i = 0; i < originalSentences.length; i++)
			longsen[i] = originalSentences[i].text;
		// shorttxt = simp.loadSimplified(pid); // simplified sentence
		// shortsen = simp.abs2Sen(shorttxt, "\n");// array of simplified
		// sentences
		// longsen = simp.abs2Sen(longtxt, "\n");// array of full sentences
		senpos = new int[longsen.length];// offset of long sentences
		if (shortsen.length != longsen.length) {
			System.out.println("Skip due to number of long sentences != short sentences---> " + pid);
			return false;
		}
		for (int i = 1; i < shortsen.length; i++) {
			senpos[i] = originalSentences[i].begin;// longtxt.indexOf(longsen[i],
													// senpos[i - 1]); //
													// calculate offset between
													// short and long sentence
		}

		// init protein map
		for (TData dt : proList) {
			proMap.put(dt.tid, dt); // map <id, protein>
			proIDMap.put(dt.new_name, dt.tid);
		}
		return true;
	}

	/**
	 * Workflow: 1. Splitting abstract/parahraph into single sentences 2.
	 * Detecting triggers and proteins belong to each sentence 3. Assign trigger
	 * locations (offset to long sentence), protein ID
	 * 
	 * @param id
	 *            : abstract/paragrap ID Output: list of triggers and proteins
	 *            stored into
	 */
	public boolean initData(String id) {
		current_id = id;
		Word w1, w2;
		TData trg;
		// preparing data
		if (!initAbstract(id)) {
			System.out.println(" Skip this sentence due to init failed " + id);
			return false;
		}
		detectedPro = new ArrayList[senpos.length];// list of Pros per sentence
		detectedTrg = new ArrayList[senpos.length];// list of Triggers per
													// sentence
		allTriggers = detectTrg(longtxt); // all triggers from full sentences
		longTrg = splitTrg(allTriggers, senpos);// map triggers from long
												// sentence into short
												// sentences-> to use original
												// locations
		longPro = splitData(proList, senpos); // split given proteins into
												// corresponding sentence
												// (proteins belong to this
												// sentence)

		// Remove trigger that embedded inside protein name
		List<Word> removeList = new ArrayList<Word>();
		for (int i = 0; i < senpos.length; i++) {
			removeList.clear();
			for (Word w : longTrg[i]) { // triggers that embedded inside protein
										// should be removed
				for (TData dt : longPro[i]) {
					if (w.locs[0] >= dt.list[0] && w.locs[1] <= dt.list[1]) {
						removeList.add(w);
					}
				}
			}
			if (removeList.size() > 0) {
				for (Word w : removeList) {
					longTrg[i].remove(w);
				}
			}
		}

		tokenList.clear();
		for (int i = 0; i < senpos.length; i++) {
			current = shortsen[i];
			String token[] = parser.splitWord(current);
			tokenList.add(token);// keeps track of all token
			if (current.length() < 5) {
				detectedPro[i] = new ArrayList<Word>();
				detectedTrg[i] = new ArrayList<Word>();
				continue;
			}
			processSentence(current, token, i); // detect trigger and protein
												// for current sentence
			// Map trigger from short sentence into long sentence
			if (detectedTrg[i].size() == longTrg[i].size()) {
				for (int f = 0; f < detectedTrg[i].size(); f++) {
					w1 = detectedTrg[i].get(f);
					w2 = longTrg[i].get(f);
					w1.locs = w2.locs;
				}
			} else {
				System.out.println("Number of triggers doesn't match, sentence " + i + ": " + id);
				System.out.println("Long: " + longsen[i]);
				for (Word tg : longTrg[i]) {
					System.out.print(tg.word + " " + tg.pos + " | ");
				}
				System.out.println("");
				System.out.println("Short: " + shortsen[i]);
				for (Word tg : detectedTrg[i]) {
					System.out.print(tg.word + " " + tg.pos + " | ");
				}
				// System.exit(1);
				throw new IllegalStateException();
			}
			// Asign ID for PRO
			if (detectedPro[i].size() != longPro[i].size()) {
				System.err.println("Sentence " + i + ": Miss protein, given: " + longPro[i].size() + " detectted " + detectedPro[i].size());
				System.err.println("Long: " + longsen[i]);
				System.err.println("");
				System.err.println("Short: " + shortsen[i]);
				// System.exit(1);
				throw new IllegalStateException();
			}

			// TODO remove
			// if (i==5) {
			// System.out.println(longsen[5]);
			// System.out.println(shortsen[5]);
			// List<TData> list = longPro[5];
			// for (TData tData : list) {
			// System.out.println(tData.name + " " + tData.tid);
			// }
			// System.out.println(Arrays.toString(token));
			// }

			for (Word pr : detectedPro[i]) {
				pr.TID = proIDMap.get(pr.word);// assign PRO ID
				if (pr.TID == null) {
					// if this is happening, we have the problem that in the
					// simplified sentence, processSentence() got a wrong
					// protein name by calling simp.getProteins(w1). This may
					// happen if a gene is only the part of a longer token, e.g.
					// "EPLIN100kb" (occurs this way in PMID 10806352). This is
					// simplified to "PRO9100kb". The protein name should be
					// "PRO9", but the getProteins() method just employs a
					// string match and returns "PRO91" and "PRO910". Thus we
					// search for the longest actually existing protein that is
					// a prefix of the detected protein name to identify the
					// correct one.
					int maxLength = 0;
					String bestCandidate = null;
					for (String prName : proIDMap.keySet()) {
						if (pr.word.startsWith(prName) && prName.length() > maxLength) {
							maxLength = prName.length();
							bestCandidate = prName;
						}
					}
					pr.TID = proIDMap.get(bestCandidate);
				}
				pr.locs = proMap.get(pr.TID).list; // location (orignal
													// location)
			}
		}
		return true;
	}

	int ecount = 0;

	/**
	 * Main method to analyze text
	 * 
	 * @param id
	 *            : abstract ID
	 * @return : array list of chunk + triggers and proteins per sentence
	 */
	public List<Chunk>[] analyze(String id) {
		List<Chunk>[] list = null;
		Chunk c;
		List<Word> pros, trgs;
		String tags[], tokens[];
		boolean init = initData(id);
		if (!init) {
			return list;
		} else {
			list = new ArrayList[senpos.length];
		}
		Word prot, trg;
		tagList.clear();
		boolean remove;
		Map<String, KeyData> dic = simp.sharedDic;
		KeyData kdt;
		int e_idx;
		int freq;
		double threshold;
		for (int i = 0; i < senpos.length; i++) {
			if (detectedPro[i].isEmpty() || detectedTrg[i].isEmpty()) {
				list[i] = null;// skip this sentence due to no trigger nor
								// protein
				tagList.add(new String[0]);
			} else {
				tokens = tokenList.get(i);
				tags = parser.POSTag(tokens);
				tagList.add(tags);
				// System.out.println("Parsing: "+id+" send ID: "+i+" "+shortsen[i]);
				parser.old_txt = shortsen[i];
				List<Chunk> chunks = parser.parse(tokens, tags); // parse
																	// sentence
																	// ith;
																	// tokens
																	// obtained
																	// from
																	// initData
				int pidx = 0, tidx = 0, cidx = 0; // protein idx, trigger idx
													// and chunk idx
				pros = detectedPro[i];
				trgs = detectedTrg[i];
				int pcount = 0, tcount = 0;
				while (cidx < chunks.size()) {
					c = chunks.get(cidx);
					// add detected triggers into current chunk
					while (tidx < trgs.size()) {
						trg = trgs.get(tidx);
						kdt = dic.get(trg.word);
						remove = false;
						if (kdt.keytype == 1) {
							e_idx = SenSimplifier.hashType.get(kdt.type);
							threshold = conf[e_idx];
							freq = tgfreq[e_idx];
						} else {
							threshold = 0.15;
							freq = 10;
						}
						if (kdt.score < threshold || kdt.freq < freq) {
							// remove = true;
						}
						if (c.contains(trg) && !trg.word.contains(" ")) {
							if (notTrg.containsKey(trg.word)) {
								if (trg.pos < tokens.length - 1) {
									String key = tokens[trg.pos + 1];
									if (notTrg.get(trg.word).contains(key)) { // skip
																				// this
																				// trg
										remove = true;
									}
								}
							}
							if (!remove) {
								c.addTrigger(trg);
							}
							tcount++;// count number of triggers have been
										// assigned
							tidx++; // next trigger
							trg.pos_tag = tags[trg.pos]; // assign POS
							trg.chunk_type = c.type;
							continue;
						} else if (c.contains(trg) && trg.word.contains(" ")) {
							if (trg.pos + 1 > c.end) { // merge with next chunk
								if (cidx < chunks.size() - 1) {
									Chunk c1 = chunks.get(cidx + 1);
									c.merge(c1);
									chunks.remove(c);
								} else {
									System.out.println(shortsen[i]);
									printChunk(chunks);
									System.out.println(trg.word + " Pos: " + trg.pos);
									remove = true;
								}
							}
							if (!remove) {
								c.addTrigger(trg);
							}
							tcount++;// count number of triggers have been
										// assigned
							tidx++; // next trigger
							trg.pos_tag = tags[trg.pos + 1]; // assign POS
							trg.chunk_type = c.type;
							continue;
						}
						break;
					}
					while (pidx < pros.size()) {
						prot = pros.get(pidx);
						remove = false;
						if (prot.pos >= c.begin && prot.pos <= c.end) {
							if (prot.fullword != null) {
								if (prot.fullword.contains(prot.word + "+") || prot.fullword.endsWith("+") || prot.fullword.startsWith("anti")) {
									remove = true;
								}
							}
							if (!remove) {
								c.addPro(prot);
							}
							pcount++;
							pidx++;
							continue;
						} else {
							break;
						}
					}
					cidx++;
				}
				if (pcount != pros.size() || tcount != trgs.size()) {
					System.out.println("----BUG---> Sen analyzer: protein or trigger is missing");
					if (pcount != pros.size()) {
						System.out.println("Protein missed");
					} else if (tcount != trgs.size()) {
						System.out.println("Trigger missed");
					}
					for (Chunk ch : chunks) {
						System.out.print(ch + " ");
					}
					System.out.println("");
					ecounter++;
				}
				list[i] = chunks;
			}
		}
		return list;
	}

	int ecounter = 0;

	public List<Word>[] splitTrg(List<Word> list, int[] pos) {
		List<Word> dlist[] = new List[pos.length];
		for (int i = 0; i < pos.length; i++) {
			dlist[i] = new ArrayList<Word>();
		}
		int loc, idx;
		for (Word dt : list) {
			loc = dt.locs[0];
			idx = pos2sen(pos, loc);
			dlist[idx].add(dt);
		}
		return dlist;
	}

	private int pos2sen(int[] senpos, int pos) {
		for (int i = senpos.length - 1; i > 0; i--) {
			if (pos >= senpos[i]) {
				return i;
			}
		}
		return 0;
	}

	/**
	 * Split list of Data (Trigger/Protein) based on number of sentences
	 * 
	 * @param list
	 *            : trigger/protein
	 * @return: Array of list
	 */
	public List<TData>[] splitData(List<TData> list, int pos[]) {
		List<TData> dlist[] = new List[pos.length];
		for (int i = 0; i < pos.length; i++) {
			dlist[i] = new ArrayList<TData>();
		}
		int loc, idx;
		for (TData dt : list) {
			loc = dt.list[0];
			idx = pos2sen(pos, loc);
			dlist[idx].add(dt);
		}
		return dlist;
	}

	public void testSentence() {
		init();
		String txt = "This is surprising, since IFN-alpha-inducible signaling cascades are present in A3.01 T cells: we showed that the control plasmid harboring interferon-responsive GAS elements was markedly induced by IFN-alpha treatment.";
		List<Word> ls = detectTrg(txt);
		for (Word w : ls) {
			System.out.println(w.word + " Pos: " + w.pos + " --> " + txt.substring(w.locs[0], w.locs[1]));
		}
	}

	public List<Word> detectTrg(String txt) {
		List<String> tokens[] = utils.SentenceSplitter.wordSpliter(txt);
		String[] words = tokens[0].toArray(new String[0]);
		List<Word> trgList = new ArrayList<Word>();
		Word word;
		String temp, w1, w2 = null;
		int i = 0, len;
		int loc = 0;
		while (i < words.length) {
			loc += tokens[1].get(i).length();
			word = null;
			w1 = words[i];
			len = w1.length();
			temp = w1.toLowerCase();
			if (simpleDic.contains(temp)) {
				if (temp.contains("-")) {
					String[] ww = temp.split("-");
					if (simpleDic.contains(ww[0] + ww[1])) {
						word = new Word(ww[0] + ww[1], i, loc);
					}
				}
				if (word == null) {
					word = new Word(temp, i, loc);
				}
				int pos[] = new int[2];
				pos[0] = loc;
				pos[1] = loc + len;
				word.locs = pos;
				trgList.add(word);
			} else if (temp.contains("-") && temp.length() >= 8) {
				String[] ww = temp.split("-");
				int pt = ww.length - 1;
				String s = ww[pt];
				if (ww.length == 2 && simpleDic.contains(ww[0] + ww[1])) {
					word = new Word(ww[0] + ww[1], i, loc);
					int pos[] = new int[2];
					pos[0] = loc;
					pos[1] = loc + len;
					word.locs = pos;
					trgList.add(word);
				} else if (simpleDic.contains(s)) {
					word = new Word(s, i, loc + temp.indexOf(s));
					int pos[] = new int[2];
					pos[0] = loc + temp.indexOf(s);
					pos[1] = pos[0] + s.length();
					word.locs = pos;
					trgList.add(word);
				}
			}
			i++;
			loc = loc + len;
		}
		return trgList;
	}

	public void processSentence(String txt, String token[], int idx) {
		String[] words = token;
		List<String> list;
		List<Word> trgList = new ArrayList<Word>();
		List<Word> protList = new ArrayList<Word>();
		Word word;
		String temp, w1, w2 = null;
		int i = 0;
		String pid;
		boolean combine = false;
		while (i < words.length) {
			combine = false;
			w1 = words[i];
			temp = w1.toLowerCase();
			word = null;
			if (simpleDic.contains(temp)) {
				if (temp.contains("-")) {
					String[] ww = temp.split("-");
					if (simpleDic.contains(ww[0] + ww[1])) {
						word = new Word(ww[0] + ww[1], i, 0);
					}
				}
				if (word == null) {
					word = new Word(temp, i, 0);
				}
				trgList.add(word);
				i++;
				continue;
			}
			if (proIDMap.containsKey(w1)) {
				word = new Word(w1, i, 0);
				pid = proIDMap.get(w1);
				TData pr = proMap.get(pid);
				word.locs = pr.list;
				protList.add(word);
				i++;
				continue;
			}
			if ((list = simp.getProteins(w1)).size() > 0) { // compound or
															// combined
				if (w1.contains("-") && w1.length() >= 8) {
					String[] ww = w1.split("-");
					int pt = ww.length - 1;
					if (simpleDic.contains(ww[pt].toLowerCase())) {
						word = new Word(ww[pt].toLowerCase(), i, 0);
						word.compound = true;
						if (proIDMap.containsKey(ww[0]) && ww.length == 2) {
							word.combined = true;
							combine = true;
						}
						word.fullword = w1;
						trgList.add(word);
					}
				}
				for (String s : list) {
					word = new Word(s, i, 0);
					word.compound = true;
					word.combined = combine;
					word.fullword = w1;
					pid = proIDMap.get(s);
					TData pr = proMap.get(pid);
					if (pr != null) {
						word.locs = pr.list;
					} else { // remove the last digit due to unwanted pattern
								// e.g. PROXY
						// System.out.println(" ----> " + "PRO: " + word.word +
						// "  not found");
						String s1 = s.substring(0, s.length() - 1);
						// System.out.println("Pro: new name "+s1);
						word.word = s1;
						pid = pid = proIDMap.get(s1);
						pr = proMap.get(pid);
						if (pr != null) {
							word.locs = pr.list;
						} else { // failed!!
							word.locs = new int[2];
						}
					}
					protList.add(word);
				}
				i++;
				continue;
			}
			if (temp.contains("-") && temp.length() >= 8) {
				String[] ww = temp.split("-");
				int pt = ww.length - 1;
				if (ww.length == 2 && simpleDic.contains(ww[0] + ww[1])) {
					word = new Word(ww[0] + ww[1], i, 0);
					trgList.add(word);
				} else if (pt >= 0) {
					if (simpleDic.contains(ww[pt])) {
						word = new Word(ww[pt], i, 0);
						word.compound = true;
						word.fullword = w1;
						trgList.add(word);
					}
				}
			}
			i++;
		}
		detectedPro[idx] = protList;
		detectedTrg[idx] = trgList;
	}

	private void removePro(List<Word> pro, List<String> names) {
		List<Word> rm = new ArrayList<Word>();
		for (String s : names) {
			for (Word w : pro) {
				if (w.word.equals(s)) {
					rm.add(w);
				}
			}
		}
		for (Word w : rm) {
			pro.remove(w);
		}
	}

	/**
	 * DO NOT USE! This method is not correct due to some changes.
	 * 
	 * @param txt
	 * @return
	 */
	public int findEquiv(String txt) {
		int count = 0;
		StringBuilder sb = new StringBuilder(txt);
		int i = 0;
		int[] openP = new int[15];
		int index = -1;
		String sub;
		List<String> alter = new ArrayList<String>();
		List<String> foundList;
		while (i < sb.length()) {
			if (sb.charAt(i) == '(') {
				openP[++index] = i;
			} else {
				if (sb.charAt(i) == ')') {
					int k = i + 1;
					if (index >= 0) {
						sub = sb.substring(openP[index], k);
						alter = simp.getProteins(sub);
						if (alter.isEmpty()) {
							i = openP[index];
						} else if (alter.size() == 1) {
							// have proteins, now create a list
							// check whether this list belongs to the protein
							// closed to this list
							foundList = new ArrayList<String>();
							int pidx = simp.getPIndex(alter.get(0));
							if (pidx > 0) {
								String pr1 = "PRO" + pidx;
								String pro2 = "PRO" + (pidx - 1);
								// if (simp.findDistance(pro, pr1) <= 2) {
								foundList.add(pro2);
								// equiList.put(pr1, foundList);
								count++;
								// }
							}
						}
						index--;
					}
				}
			}
			i++;
		}
		return count;
	}

	public Word findTrigger(int loc[], List<Word> relWord) {
		for (Word w : relWord) {
			if (w.locs[0] == loc[0] || w.locs[1] == loc[1]) {
				return w;
			}
		}
		return null;
	}

	public List<EData>[] splitEvents(List<EData> list) {
		List<EData> dlist[] = new List[longsen.length];
		int pos, idx;
		TData tg;
		for (int i = 0; i < longsen.length; i++) {
			dlist[i] = new ArrayList<EData>();
		}
		for (EData dt : list) {
			tg = dt.trgdata;
			pos = tg.list[0];
			idx = pos2sen(senpos, pos);
			dlist[idx].add(dt);
		}
		return dlist;
	}

	public void testAll() {
		init();
		int total = 0, detected = 0, match = 0, recognized = 0;
		List<String> ids = simp.loadPMIDs();
		System.out.println("Total abstract: " + ids.size());
		Map<String, String> TGmap = new HashMap<String, String>();
		List<EData> elist;
		List<TData> trgList, candidate = new ArrayList<TData>();
		Map<String, TData> map = new HashMap<String, TData>();
		Map<String, Counter> miss = new HashMap<String, Counter>();
		TData tg;
		String key = "";
		for (String id : ids) {
			map.clear();
			elist = simp.loadEvent(id);
			trgList = simp.loadTrigger(id);
			for (TData dt : trgList) { // prepare hash for trigger
				map.put(dt.tid, dt);
			}
			initData(id);
			List<EData> events[] = splitEvents(elist);
			for (int i = 0; i < senpos.length; i++) {
				TGmap.clear();
				for (EData ev : events[i]) {
					if (!TGmap.containsKey(ev.trigID)) {
						TGmap.put(ev.trigID, ev.trigID); // avoiding repetition
					}
				}
				total += TGmap.size();
				candidate.clear();
				for (String s : TGmap.keySet()) {
					tg = map.get(s);
					if (simp.sharedDic.containsKey(tg.name.toLowerCase())) {
						candidate.add(tg);// recognized trigger
					} else {
						Counter c = miss.get(tg.name.toLowerCase());
						if (c == null) {
							c = new Counter(1);
							miss.put(tg.name.toLowerCase(), c);
						} else {
							c.inc();
						}
					}
				}
				recognized += candidate.size();
				for (TData dt : candidate) {
					for (Word w : detectedTrg[i]) {
						if (dt.list[0] == w.locs[0] || dt.list[1] == w.locs[1]) {
							match++;
						}
					}
				}
				if (detectedTrg[i] != null) {
					detected += detectedTrg[i].size();
				}
			}
		}
		System.out.println("Total non-repeat triggers: " + total);
		System.out.println("Recognized triggers: " + recognized + " -> Recall: " + (1f * recognized / total));
		System.out.println("Match trigger: " + match + " -> Recall: " + (1f * match / total));
		System.out.println("Missed triggers: " + (detected - match));
		int recover = 0;
		for (String s : miss.keySet()) {
			Counter c = miss.get(s);
			if (c.count >= 2) {
				System.out.println(s + " " + miss.get(s).count);
				recover += c.count;
			}
		}
		System.out.println("Number of trggers can recovers: " + recover);

	}

	/**
	 * Get list of prepositions of a sentence
	 * 
	 * @param tokens
	 * @return
	 */
	public List<Word> getPreps(String[] tokens) {
		String s;
		List<Word> list = new ArrayList<Word>();
		for (int i = 0; i < tokens.length; i++) {
			s = tokens[i];
			if (SenSimplifier.prepmap.contains(s)) {
				Word w = new Word(s, i, 0);
				list.add(w);
			}
		}
		return list;
	}

	/**
	 * Get list of prepositions of a sentence
	 * 
	 * @param tokens
	 * @return
	 */
	public List<Word> getModifier(String[] tokens) {
		String s;
		List<Word> list = new ArrayList<Word>();
		for (int i = 0; i < tokens.length; i++) {
			s = tokens[i];
			if (SenSimplifier.modifiers.containsKey(s)) {
				Word w = new Word(s, i, 0);
				list.add(w);
			}
		}
		return list;
	}

	public void printChunk(List<Chunk> ls) {
		for (Chunk c : ls) {
			System.out.print("[" + c.type + " " + c.txt + "]");
		}
		System.out.println("");
	}

	private void printChunkValue(List<Chunk> ls) {
		for (Chunk c : ls) {
			System.out.print(c.getValues());
		}
		System.out.println("");
	}

	public void testChunks() {
		init();
		ChunkAnalyzer op = new ChunkAnalyzer();
		List<String> ids = simp.loadPMIDs();
		List<Chunk>[] out;
		int count = 0;
		for (String id : ids) {
			out = analyze(id);
			for (int i = 0; i < out.length; i++) {
				ecount++;
				if (out[i] != null) {
					op.curr_text = shortsen[i];
					op.analyzeChunk(out[i], tagList.get(i), tokenList.get(i));
					if (op.verbList.size() > 0) {
						for (VerbChunk vb : op.verbList) {
							vb.subject.mergeNP();
							vb.object.mergeNP();
						}
					}

				}
			}
		}
		System.out.println("Total sentences: " + ecount);
		System.out.println("Total error due to trigger detection " + ecounter);
		System.out.println("Number of abstract: " + ids.size() + " Number of sentences: " + count);
		System.out.println("---Number of unknown subject cases: " + op.sub_count);

	}

	public void printInChunks() {
		ChunkAnalyzer op = new ChunkAnalyzer();
		List<String> ids = simp.loadPMIDs();
		List<Chunk>[] out;
		int count = 0;
		for (String id : ids) {
			out = analyze(id);
			for (int i = 0; i < out.length; i++) {
				if (out[i] != null) {
					for (Chunk c : out[i]) {
						if (c.trigs.size() == 2) {
							System.out.println(c.getValues());
						}
					}
				}
			}
			count++;
		}
		System.out.println("Total cases: " + ecount);
		System.out.println("Total error due to trigger detection " + ecounter);
		System.out.println("Number of abstract: " + ids.size() + " Number of sentences: " + count);
		System.out.println("---Number of unknown subject cases: " + op.sub_count);

	}

	static final Map<String, Set<String>> notTrg = new HashMap<String, Set<String>>();
	static final String notrigger[] = { "binding->site|sites|domain|element|elements|complexes|protein|proteins|subunit|subunits|complex",
			"transcription->factor|factors|initiation|sites|site|start" };

	static {
		for (String s : notrigger) {
			String st[] = s.split("->");
			String sub[] = st[1].split("\\|");
			Set<String> mapsub = new HashSet<String>();
			mapsub.addAll(Arrays.asList(sub));
			notTrg.put(st[0], mapsub);
		}
	}

	public static void main(String[] args) {
	}
}
