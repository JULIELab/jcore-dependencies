/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package relations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.BioSemException;
import utils.DBUtils;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * 
 * @author Chinh Date: 22 - 08 - 2011 Extracting event from sentence
 */
public class EventExtraction {

	private static final Logger log = LoggerFactory.getLogger(EventExtraction.class);
	
	SenAnalyzer analyzer; // use default database
	SenSimplifier simp;
	ChunkAnalyzer op = new ChunkAnalyzer();
	DBUtils db_sr, db;
	Map<String, RuleSet> rules;
	Map<String, KeyData> dic;
	int noun_len = 12;
	int verb_len = 8;
	List<Word> usedPro = new ArrayList<Word>();
	BasicChunk cur_sub = null;
	boolean check_pro = false; // whether protein can be reused (for simple and
								// binding only)
	boolean same_role = false;
	Chunk curr_verb = null;
	Set<Word> sameRole = new HashSet<Word>();
	FileWriter writer = null;
	int curr_verb_type = 0;
	String curr_pmid;
	int curr_senID;
	double ecause = 0.1;
	double escore = 0.1;
	double pscore = 0.3;
	double upperValue = 0.85;
	int etype = 0;
	boolean debug = false;
	Map<String, RuleSets> rset;

	/**
	 * Extract event from the given data
	 * 
	 * @param db
	 */
	public EventExtraction(DBUtils sr, DBUtils db) {
		this.db = db;
		db_sr = sr;
		analyzer = new SenAnalyzer(sr, db);
		simp = analyzer.simp;

	}

	public void init() {
		// if dic is null, we haven't initialized yet
		if (null == dic) {
			analyzer.init();
			dic = simp.sharedDic;
			Map<String, Rules>[] rls = simp.loadPatterns(db_sr);
			rset = groupRules(rls);
		}
	}

	private boolean hasIts(int pos, String tokens[]) {
		if (pos > 0) {
			if (tokens[pos - 1].equals("its")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Evaluating candidate triggers whether they can form events
	 * 
	 * @param bs
	 *            : BasicChunk contains trgs
	 * @param preps
	 *            : preposition list
	 * @param mods
	 *            : modifier words
	 * @param tokens
	 *            : list of words/tokens
	 * @param Trgs
	 *            : detected TG (used to determine trg type)
	 */
	public void evaluateTrg(BasicChunk bs, List<Word> preps, List<Word> mods,
			String[] tokens, List<Word> Trgs) {
		bs.mergeNP();
		if (bs.trgList.isEmpty()) {
			return;
		}
		int i = 0;
		Word tg;
		Set<Word> checked = new HashSet<Word>();
		RuleSets rule;
		KeyData kdt;
		int idx;
		String key;
		double score = 0.15;
		boolean has_its;
		while (i < bs.trgList.size()) { // check in-chunk: PRO - TG
			tg = bs.trgList.get(i);
			has_its = hasIts(tg.pos, tokens);
			kdt = dic.get(tg.word);
			if (kdt.keytype > 1) {
				if (kdt.score < score) {
					bs.removeTrg(tg);
					continue;
				}
				kdt = kdt.getDefault();
			}

			if (kdt != null && kdt.type != null) {
				idx = SenSimplifier.hashType.get(kdt.type);
			} else {
				System.out.println("--> BUG: " + tg.word + " --> no type");
				bs.removeTrg(tg);
				continue;
			}
			key = tg.word + idx + tg.pos_tag + "NP";
			if (bs.inChunkTrg(tg, tokens)
					|| (has_its && tg.pos_tag.equals("NN"))) {
				rule = rset.get(key);
				if (rule == null) {
					bs.removeTrg(tg);
					continue;
				}
				boolean found = false;
				if (rule != null
						&& (rule.inChunk() || rule.getFrontScore() > 0.01)
						&& !rule.isSkipped(idx)) {
					found = true;
				}
				if (!found) { // no in-chunk pattern, remove this trigger
					bs.removeTrg(tg);
					continue;
				} else {
					boolean has_type = false;
					kdt = dic.get(tg.word);
					if (kdt.keytype == 1 && (!kdt.required || has_its)) {
						tg.type = kdt.type;
						has_type = true;
						tg.keytype = 1;
					} else if (kdt.keytype > 1) {
						List<Word> mod = getMod(bs, tg, mods);
						String evt_type = kdt.getType(mod);
						if (evt_type != null) {
							tg.type = evt_type;
							tg.keytype = kdt.keytype == 2 ? 2 : 1;
							has_type = true;
						}
					} else if (kdt.keytype == 1 && kdt.required) {
						Word wd = findTrg(tg.pos, Trgs);
						if (wd != null && kdt.parent != null) {
							if (kdt.parent.contains(wd.word)) {
								has_type = true;
								tg.type = kdt.type;
								tg.keytype = 1;
							}
						} else if (cur_sub == null && curr_verb_type == 1
								&& kdt.parent != null) {
							if (curr_verb.trigs.size() > 0) {
								wd = curr_verb.trigs.get(0);
								if (kdt.parent.contains(wd.word)) {
									has_type = true;
									tg.type = kdt.type;
									tg.keytype = 1;
								}
							}
						}
					}
					if (!has_type) {
						bs.removeTrg(tg);
						continue;
					} else {
						tg.inchunk = true;
						checked.add(tg);
					}
				}
			}
			i++;
		}
		i = 0;
		while (i < bs.trgList.size()) { // check TG-prep-PRO/EVT
			tg = bs.trgList.get(i);
			if (checked.contains(tg)) {
				i++;
				continue;
			}
			kdt = dic.get(tg.word);
			if (kdt.keytype > 1) {
				if (kdt.score < score) {
					bs.removeTrg(tg);
					continue;
				}
				kdt = kdt.getDefault();
			}
			idx = SenSimplifier.hashType.get(kdt.type);
			key = tg.word + idx + tg.pos_tag + "NP";
			rule = rset.get(key);
			if (rule == null || rule.isSkipped(idx)) {
				bs.removeTrg(tg);
				continue;
			}
			String prep;
			List<Word> pros = null;
			if (tg.pos_tag.startsWith("NN")) { // find
				if (i + 1 < bs.trgList.size()) {
					Word tg2 = bs.trgList.get(i + 1);
					if (bs.isSameRole(tg, tg2, tokens)) {
						int tgpos1 = bs.getChunkPos(tg.pos);
						int tgpos2 = bs.getChunkPos(tg2.pos);
						if (tgpos2 - tgpos1 == 2) {
							Chunk c = bs.chunkList.get(tgpos1);
							c.merge(bs.chunkList.get(tgpos1 + 1));
							c.merge(bs.chunkList.get(tgpos2));
							bs.chunkList.remove(tgpos1 + 1);
							bs.chunkList.remove(tgpos1 + 1);
						}
					}
				}

			}
			prep = getPrep(tg, bs, rule.prep);
			if (tg.pos_tag.startsWith("NN") && !prep.isEmpty()) {
				pros = findPro(bs, tg, prep, rule.dist1);
			} else if (!tg.pos_tag.startsWith("NN")) {
				pros = findPro(bs, tg, "", rule.dist1);
			}
			if (idx == 5 && pros != null && pros.isEmpty()
					&& rule.getFrontScore() > 0.15) {
				pros = findFrontPro(bs, tg, 5);
			}
			// now determine trg type
			kdt = dic.get(tg.word);
			if (kdt.keytype > 1 || kdt.required) { // mix/share or tg requires
													// modifiers
				boolean has_type = false;
				if (pros != null && pros.size() > 0) {
					// now determine trg type for share/mix case
					List<Word> mod = getMod(bs, tg, pros, mods);
					String evt_type = kdt.getType(mod);
					if (evt_type != null) {
						tg.type = evt_type;
						tg.keytype = kdt.keytype == 2 ? 2 : 1;
						has_type = true;
					} else if (kdt.keytype == 1 && kdt.required) {
						Word wd = findTrg(tg.pos, Trgs);
						if (wd != null && kdt.parent != null) {
							if (kdt.parent.contains(wd.word)) {
								has_type = true;
								tg.type = kdt.type;
								tg.keytype = 1;
							}
						}
					}
				} else if (idx > 5) { // for regulatory event
					if (i < bs.trgList.size() - 1) {
						Word tg2 = bs.trgList.get(i + 1);
						int pos2 = bs.getChunkPos(tg2.pos);
						int pos1 = bs.getChunkPos(tg.pos);
						kdt = kdt.getDefault();
						if (kdt.child.contains(tg2.word)
								|| (pos2 - pos1) <= rule.dist1 || true) {
							tg.type = kdt.type;
							tg.keytype = 1;
							has_type = true;
						}
					}
				}
				if (!has_type) {
					bs.removeTrg(tg);
					continue;
				}
			} else { // normal trigger
				boolean has_type = false;
				if (pros != null && pros.size() > 0) {
					tg.type = kdt.type;
					tg.keytype = 1;
					has_type = true;
				} else if (idx > 5) {
					if (i < bs.trgList.size() - 1) {
						Word tg2 = bs.trgList.get(i + 1);
						int pos2 = bs.getChunkPos(tg2.pos);
						int pos1 = bs.getChunkPos(tg.pos);
						if (kdt.child.contains(tg2.word)
								|| (pos2 - pos1) <= rule.dist1 || true) {
							tg.type = kdt.type;
							tg.keytype = 1;
							has_type = true;
						}
					}
				}
				if (!has_type) {
					bs.removeTrg(tg);
					continue;
				}
			}
			i++;
		}
	}

	private Word findAnaphora(BasicChunk bs, int dist, Word tg) {
		if (bs.proList.size() == 1) {
			Word pr = bs.proList.get(0);
			if (pr.pos < tg.pos) {
				if (bs.getChunkPos(tg.pos) - bs.getChunkPos(pr.pos) <= dist) {
					return pr;
				}
			}
		} else if (cur_sub != null && cur_sub.proList.size() == 1) {
			Word pr = cur_sub.proList.get(0);
			if (cur_sub.getChunkPos(pr.pos) < dist) {
				return pr;
			}
		}
		return null;
	}

	public Map<String, RuleSets> groupRules(Map<String, Rules>[] map) {
		Map<String, RuleSets> ls = new HashMap<String, RuleSets>();
		Map<String, Rules> rule;
		Rules r;
		RuleSets rs = new RuleSets();
		List<RuleData> list;
		for (int i = 0; i < SenSimplifier.trigger_type.length; i++) {
			rule = map[i];
			for (String s : rule.keySet()) {
				if (!dic.containsKey(s)) {
					continue;
				}
				r = rule.get(s);
				r.initMap();
				for (String key : r.data.keySet()) {
					list = r.data.get(key);
					RuleSets rlset = rs.createRule(list);
					if (rlset.total >= 1) {
						ls.put(s + i + key, rlset); // trg + type + pos +
													// chunk_type
					}
				}
			}
		}
		return ls;
	}

	private Word findTrg(int pos, List<Word> ls) {
		if (ls == null || ls.isEmpty()) {
			return null;
		}
		int i = ls.size() - 1;
		Word w;
		while (i >= 0) {
			w = ls.get(i);
			if (w.pos < pos && Math.abs(w.pos - pos) <= 10) {
				return w;
			}
			i--;
		}
		return null;
	}

	/**
	 * Find a trg within a given range
	 * 
	 * @param start
	 *            : start position
	 * @param ls
	 *            : list of tg;
	 * @param end
	 *            : end position
	 * @return
	 */
	private Word findTrg(int start, int end, BasicChunk bs) {
		if (bs.trgList == null || bs.trgList.isEmpty()) {
			return null;
		}
		int pos;
		for (Word w : bs.trgList) {
			pos = bs.getChunkPos(w.pos);
			if (pos > start && pos < end) {
				return w;
			} else if (pos > end) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Extracting simple event from BasicChunk with a given trigger (+type)
	 * 
	 * @param tg
	 * @param bc
	 * @param preps
	 * @param mods
	 * @return
	 */
	public List<PData> extractSimpleNP(Word tg, BasicChunk bc, List<Word> preps) {
		List<PData> list = new ArrayList<PData>();
		String ev_type = tg.type;
		// There are 3 cases: in-chunk, NN with prep, and JJ/VB with/out prep
		// Check in-chunk first
		int idx = SenSimplifier.hashType.get(tg.type);
		String key = tg.word + idx + tg.pos_tag + "NP";
		RuleSets rule = rset.get(key);
		if (rule == null) {
			return list;
		}
		List<Word> pros = null;
		if ((rule.inChunk() || rule.getFrontScore() > 0.01) && tg.inchunk) { // in
																				// chunk
			// find pros in the same chunk with trg
			pros = getInChunkPro(bc, tg); // 11.10.2011-> Checked!
			if (pros.isEmpty() && hasIts(tg.pos, tokens)
					&& rule.getFrontScore() > 0.01) {
				Word pr = findAnaphora(bc, 5, tg);
				if (pr != null) {
					pros = new ArrayList<Word>();
					pros.add(pr);
				}
			}
		}
		if (pros == null || pros.isEmpty()) { // pro follows trg
			if (tg.pos_tag.startsWith("NN")) { // find
				String prep = getPrep(tg, bc, rule.prep);
				if (!prep.isEmpty()) {
					pros = findPro(bc, tg, prep, rule.dist1);
				} else {
					pros = findPro(bc, tg, "", 3);
				}
			} else {
				pros = findPro(bc, tg, "", rule.dist1);
				if (pros.isEmpty() && rule.getFrontScore() > 0.2) { // 13.12.2011
					pros = findFrontPro(bc, tg, rule.fdist);
				}
			}
		}
		if (pros != null && pros.size() > 0) {
			Word tg2 = null;
			if (tg.keytype == 20) { // SKIP <--- shared trigger, clone one trg
									// for Genexpression
				tg2 = tg; // keep the original for Positive_regulation
				tg = new Word(tg.word, tg.pos, tg.loc);
				tg.locs = tg2.locs;
				tg.type = "Gene_expression";
				tg.keytype = 1;
				tg2.type = "Positive_regulation"; // change type into Pos_Reg
				tg2.keytype = 1;
			}
			tg.TID = getTrgID(); // generate TrgID;
			for (Word w : pros) {
				PData p = new PData(tg, w, ev_type);
				p.PID = getEventID(); // Event ID
				extractedSet.add(p);
				list.add(p);
				bc.usedPro.add(w);
			}
			if (tg2 != null) {
				extractedMap.put(tg, list); // put Gene_expression into
											// extractedMap
				key = tg2.word + 6 + tg2.pos_tag + "NP";
				rule = rset.get(key);
				List<PData> list1 = new ArrayList<PData>();
				tg2.TID = getTrgID();
				tg2.used = true;
				boolean form_event = true;
				if (tg2.inchunk) {
					if (rule.getEvtScore(0) < 0.5) {
						form_event = false;
					}
				} else {
					if (rule.getEvtScore(2) < 0.5) {
						form_event = false;
					}
				}
				if (form_event) {
					for (PData pdt : list) {
						PData p = new PData(tg2, pdt, tg2.type);
						p.PID = getEventID();
						extractedSet.add(p);
						list1.add(p);
					}
				} else {
					for (Word w : pros) {
						PData p = new PData(tg2, w, tg2.type);
						p.PID = getEventID(); // Event ID
						extractedSet.add(p);
						list1.add(p);
						bc.usedPro.add(w);
					}
				}
				return list1;
			}

		}
		return list;

	}

	public List<PData> extractRegNP(Word tg, BasicChunk bc, List<Word> preps) {
		List<PData> list = new ArrayList<PData>();
		List pro1 = null, pro2 = null;
		KeyData kdt = dic.get(tg.word).getKeyData(tg.type);
		int idx = SenSimplifier.hashType.get(tg.type);
		if ((kdt.score < 0.15 && idx >= 7) || (kdt.score < 0.2 && idx == 6)) {
			// return list;
		}
		Set<String> childSet = kdt.child;
		Set<String> parentSet = kdt.parent;
		String key = tg.word + idx + tg.pos_tag + "NP";
		RuleSets rule = rset.get(key);
		String prep1;
		if (rule == null) {
			return list;
		}
		int len = Math.min(rule.dist1, 0);
		if ((rule.inChunk() || rule.getFrontScore() > 0.01) && tg.inchunk) {
			prep1 = getPrep(tg, bc, rule.prep);
			if (rule.getInchunkCause() > 0.05) {
				// find (theme)
				if ((pro1 == null || pro1.isEmpty())
						&& rule.getInChunkEvtScore() < upperValue) { // theme as
																		// pro
					if (tg.pos_tag.startsWith("NN")) {
						pro1 = findPro(bc, tg, prep1, rule.dist1);
					} else {
						pro1 = findRegPro(bc, tg, bc.getChunkPos(tg.pos),
								rule.dist1);
					}
				}
				if ((pro1 == null || pro1.isEmpty())
						&& rule.getInChunkEvtScore() > escore) {// PRO-TG - EVT
					if (tg.pos_tag.startsWith("NN")) { // NNx -> requires prep
						if (curr_tg < bc.trgList.size() - 1) {
							Word tg2 = bc.trgList.get(curr_tg + 1);
							prep1 = getPrep(tg, bc, rule.prep);
							if (prep1 != null
									&& !prep1.isEmpty()
									&& ((childSet.contains(tg2.word) || Math
											.abs(bc.getChunkPos(tg2.pos)
													- bc.getChunkPos(tg.pos)) <= len))) {
								curr_tg++;
								List temp = extractNP(curr_tg, bc, preps);
								if (temp != null && !temp.isEmpty()) {
									pro1 = temp;
								}
							}
						}
					} else if (curr_tg < bc.trgList.size() - 1) { // VB/JJ do
																	// not
																	// require
																	// prep
						Word tg2 = bc.trgList.get(curr_tg + 1);
						if (childSet.contains(tg2.word)) {
							curr_tg++;
							List temp = extractNP(curr_tg, bc, preps);
							if (temp != null && !temp.isEmpty()) {
								pro1 = temp;
							}
						}
					}
				}
			}
			List<Word> pcause = findCausePro(bc, tg);
			if (pcause.size() > 0) {
				pro1 = getInChunkPro(bc, tg);
				pro2 = pcause;
			}
			if ((pro1 == null || pro1.isEmpty())
					&& rule.count[0] * 1f / (rule.count[0] + rule.count[1]) > escore) { // PRO-TG
																						// form
				pro1 = getInChunkPro(bc, tg);
				if (pro1 != null && pro1.size() > 0) {
					bc.usedPro.addAll(pro1);
				}
			} else if (pro1 != null && pro1.size() > 0 && pro2 == null) { // PRO-TG
																			// theme
																			// (PRO/EVT)
				pro2 = getInChunkPro(bc, tg); // cause , assume all causes are
												// pros
				if (pro2 != null && pro2.size() > 0) {
					bc.usedPro.addAll(pro2);
				}
				if (rule.causePrep.contains(prep1)) {
					List<Word> temp = pro1;
					pro1 = pro2;
					pro2 = temp;
				}
			}
			if (pro1 != null && pro1.isEmpty() && hasIts(tg.pos, tokens)
					&& rule.getFrontScore() > 0.01) {
				Word pr = findAnaphora(bc, 5, tg);
				if (pr != null) {
					pro1 = new ArrayList<Word>();
					pro1.add(pr);
				}
			}
		} else { // TG - PRO
			// First attempt; find event
			prep1 = getPrep(tg, bc, rule.prep);
			if (tg.pos_tag.startsWith("NN")) {
				if (!prep1.isEmpty()) {
					if (rule.prepPair.get(prep1) != null) {
						int pairs[] = getPrepPairs(tg.pos, prep1, bc,
								rule.prepPair.get(prep1),
								Math.max(rule.dist1, rule.dist2));
						if (pairs != null) {
							boolean order = false;
							String txt = bc.chunkList.get(pairs[0]).txt
									+ bc.chunkList.get(pairs[1]).txt;
							if (rule.order.contains(txt)) {
								order = true;
							}
							if (order) { // TG - cause - theme
								// find pro/event after prep2
								Word tg2 = findTrg(pairs[0], pairs[1], bc); // check
																			// whether
																			// any
																			// trg
																			// between
																			// prep1
																			// and
																			// prep2
								if (tg2 != null && parentSet.contains(tg2.word)) {
									curr_tg++;
									pro2 = extractNP(curr_tg, bc, preps);
								}
								if ((pro2 == null || pro2.isEmpty())
										&& rule.getEvtCause(3) < upperValue
										&& rule.getProCause(3) > pscore) {
									pro2 = findPro(bc, pairs[0], pairs[1]
											- pairs[0] - 1);
								}
								if (rule.getEvtScore(3) < upperValue
										&& rule.getProScore(3) > pscore) {// try
																			// to
																			// find
																			// pro
																			// first,
																			// if
																			// no
																			// pro
																			// found,then
																			// find
																			// event
									pro1 = findRegPro(bc, tg, pairs[1],
											rule.dist2);
								}
								if (pro1 == null || pro1.isEmpty()) { // find
																		// event
									if (curr_tg < bc.trgList.size() - 1) {
										tg2 = bc.trgList.get(curr_tg + 1);
										if (childSet.contains(tg2.word)) {
											curr_tg++;
											List temp = extractNP(curr_tg, bc,
													preps);
											if (temp != null && !temp.isEmpty()) {
												pro1 = temp;
											}
										}
									}
								}
								if (pro1 == null || pro1.isEmpty()) {
									return list;
								}
							} else { // TG - theme - cause
								// find pro/event after prep1
								Word tg2 = findTrg(pairs[0], pairs[1], bc); // check
																			// whether
																			// any
																			// trg
																			// between
																			// prep1
																			// and
																			// prep2
								if (tg2 != null && childSet.contains(tg2.word)) {
									curr_tg++;
									pro1 = extractNP(curr_tg, bc, preps);
								}
								if ((pro1 == null || pro1.isEmpty())
										&& rule.getEvtScore(3) < upperValue
										&& rule.getProScore(3) > pscore) {
									pro1 = findPro(bc, pairs[0], pairs[1]
											- pairs[0] - 1);
								}
								// Find cause
								if (rule.getEvtCause(3) < upperValue
										&& rule.getProCause(3) > pscore) {// try
																			// to
																			// find
																			// pro
																			// first,
																			// if
																			// no
																			// pro
																			// found,then
																			// find
																			// event
									pro2 = findRegPro(bc, tg, pairs[1],
											rule.dist2);
								}
								if (pro2 == null || pro2.isEmpty()) { // find
																		// event
									if (curr_tg < bc.trgList.size() - 1) {
										tg2 = bc.trgList.get(curr_tg + 1);
										if (parentSet.contains(tg2.word)) {
											curr_tg++;
											List temp = extractNP(curr_tg, bc,
													preps);
											if (temp != null && !temp.isEmpty()) {
												pro2 = temp;
											}
										}
									}
								}
								if (pro2 != null && pro2.isEmpty()) {
									pro2 = null;
								}
							}
						}
					}
					// no prep pairs or cannot find pro2
					if (pro1 == null || pro1.isEmpty()) {
						if (rule.getEvtScore(2) < upperValue
								&& rule.getProScore(2) > pscore) { // find pro
																	// as theme
							pro1 = findPro(bc, tg, prep1, rule.dist1);
						}
						if (pro1 == null || pro1.isEmpty()) {
							if (curr_tg < bc.trgList.size() - 1) {
								Word tg2 = bc.trgList.get(curr_tg + 1);
								if (childSet.contains(tg2.word)) {
									curr_tg++;
									List temp = extractNP(curr_tg, bc, preps);
									if (temp != null && !temp.isEmpty()) {
										pro1 = temp;
									}
								}
							}
						}
					}
					List<Word> pcause = findCausePro(bc, tg);
					if (pcause.size() > 0 && pro1 != null && pro1.size() > 0) {
						pro2 = pcause;
					}
					// Check group 6: cause - tg - theme
					Word frontPrep = findFrontPrep(tg, bc);
					if (pro1 != null && pro1.size() > 0 && frontPrep != null
							&& cur_sub != null) {
						key = tg.word + idx + tg.pos_tag + "CP";
						RuleSets rule2 = rset.get(key);
						if (rule2 != null
								&& rule2.frontPrep.contains(frontPrep.word)) {
							pro2 = findFrontPro(cur_sub, tg, rule2.fdist);
						}
					}
					// Check group 5: theme - tg - cause
					if (pro1 != null && pro1.size() > 0 && pro2 == null) {
						if (rule.causePrep.contains(prep1)
								&& rule.getEvtCause(5) < upperValue
								&& rule.getProCause(5) > pscore) {
							List<Word> temp = findFrontPro(bc, tg, rule.fdist);
							if (temp.size() > 0) {
								pro2 = pro1;
								pro1 = temp;
							}
						}
					}

				} else { // has no prep
					if (rule.getBehindScore() > rule.getFrontScore()) {
						// TG - PRO
					} else {
						// PRO - TG
					}
				}
			} else { // TG is VBx or JJ
				if (rule.getBehindScore() > rule.getFrontScore()) {
					if (rule.getEvtScore(2) < upperValue
							&& rule.getProScore(3) > pscore) { // find pro as
																// theme
						pro1 = findRegPro(bc, tg, bc.getChunkPos(tg.pos),
								rule.dist1);
					}
					if (pro1 == null || pro1.isEmpty()) {
						if (curr_tg < bc.trgList.size() - 1) {
							Word tg2 = bc.trgList.get(curr_tg + 1);
							if (childSet.contains(tg2.word)) {
								curr_tg++;
								List temp = extractNP(curr_tg, bc, preps);
								if (temp != null && !temp.isEmpty()) {
									pro1 = temp;
								}
							}
						}
					}
					// Check group 6: cause - tg - theme
					Word frontPrep = findFrontPrep(tg, bc);
					if (pro1 != null && pro1.size() > 0 && frontPrep != null) {
						if (rule.frontPrep.contains(frontPrep.word)) {
							List<Word> pros2 = findFrontPro(bc, tg, rule.fdist);
							if (pros2.size() > 0) {
								pro2 = pro1;
								pro1 = pros2;
							}
						}
					}
					// Check group 5: theme - tg - cause
					if (pro1 != null && pro1.size() > 0 && pro2 == null) {
						if (rule.causePrep.contains(prep1)
								&& rule.getEvtCause(5) < upperValue) {
							List<Word> temp = findFrontPro(bc, tg, rule.fdist);
							if (temp.size() > 0) {
								pro2 = pro1;
								pro1 = temp;
							}
						}
					}
				}
				if ((pro1 == null || pro1.isEmpty())
						&& rule.getBehindScore() > 0.1) {
					if (rule.getEvtCause(5) < upperValue) {
						pro1 = findFrontPro(bc, tg, rule.fdist);
					}
					if ((pro1 == null || pro1.isEmpty())
							&& rule.getEvtCause(4) > ecause) {
						if (curr_tg >= 1) {
							Word tg2 = bc.trgList.get(curr_tg - 1);
							if (childSet.contains(tg2.word)
									&& extractedMap.containsKey(tg2)) {
								pro1 = extractedMap.get(tg2);
							}
						}
					}
				}
			}
		}
		// forming event pairs
		if (pro1 != null && pro1.size() > 0 && pro2 != null && pro2.size() > 0) {
			removeDuplicate(pro1, pro2);
		}
		if (pro1 != null && pro1.size() > 0 && pro2 != null && pro2.size() > 0) {

			tg.TID = getTrgID();
			for (Object obj1 : pro1) {
				for (Object obj2 : pro2) {
					PData pair = new PData(tg, obj1, obj2, tg.type);
					pair.PID = getEventID();
					extractedSet.add(pair);
					list.add(pair);
				}
			}
			return list;
		} else if (pro1 != null && pro1.size() > 0) {
			tg.TID = getTrgID();
			for (Object obj1 : pro1) {
				PData pair = new PData(tg, obj1, tg.type);
				pair.PID = getEventID();
				extractedSet.add(pair);
				list.add(pair);
			}
			return list;
		}
		return list;
	}

	public List<PData> extractRegVP(Word tg, VerbChunk vc, List<Word> preps) {
		List<PData> list = new ArrayList<PData>();
		List pro1 = null, pro2 = null;
		int idx = SenSimplifier.hashType.get(tg.type);
		KeyData kdt = dic.get(tg.word).getKeyData(tg.type);
		Set<String> childSet = kdt.child;
		Set<String> parentSet = kdt.parent;
		String key = tg.word + idx + tg.pos_tag + "VP";
		RuleSets rule = rset.get(key);
		if (rule == null || rule.isSkipped()) {
			return list;
		}
		// find theme
		String prep1 = getPrep(tg, vc.object, rule.prep);
		if ((vc.verb_type == 1 && tg.pos_tag.equals("VBN") && rule.passive_order)
				|| (prep1 != null && prep1.equals("by"))) { // Inverse case:
															// Theme - TG -
															// Cause
			// find theme
			if (rule.passive_order) { // normal case -> theme -tg - cause
				double cause_score = rule.count[5] * 1f
						/ (rule.count[5] + rule.count[4]);
				if (rule.causePrep.contains(prep1) || cause_score > ecause) { // has
																				// cause,
																				// check
																				// group
																				// 5
					// find theme
					if (rule.getEvtScore(4) < upperValue) { // try to find pro
						pro1 = findFrontPro(vc.subject, tg, rule.fdist);
					}
					if ((pro1 == null || pro1.isEmpty())
							&& rule.getEvtScore(4) > escore) { // no pro, find
																// event
						pro1 = findEvent(vc.subject, rule.fdist);
					}
					// has theme, find cause
					if (pro1 != null && pro1.size() > 0 && cause_score > ecause) { // find
																					// cause
						if (rule.getEvtCause(5) < upperValue) {
							pro2 = findRegPro(vc.object, tg, -1, rule.dist1);
						}
						if ((pro2 == null || pro2.isEmpty())
								&& rule.getEvtCause(5) > ecause) {
							pro2 = findEvent(vc.object, rule.dist1);
							if (pro2 == null || pro2.isEmpty()) {
								return list;
							}
						}
					}
				} else { // no cause : theme - TG
					if (rule.getEvtScore(4) < upperValue) { // try to find pro
						pro1 = findFrontPro(vc.subject, tg, rule.fdist);
					}
					if ((pro1 == null || pro1.isEmpty())
							&& rule.getEvtScore(4) > escore) { // no pro, find
																// event
						pro1 = findEvent(vc.subject, rule.fdist);
					}
				}
			}
		} else { // normal case: Cause - TG - Theme
			double fcause = rule.count[5] * 1f
					/ (rule.count[4] + rule.count[5]);
			double bcause = rule.count[6] * 1f
					/ (rule.count[2] + rule.count[6]);

			if (rule.getEvtScore(2) < upperValue) {
				pro1 = findRegPro(vc.object, tg, -1, rule.dist1);
			}
			if ((pro1 == null || pro1.isEmpty())
					&& rule.getEvtScore(2) > escore) {
				pro1 = findEvent(vc.object, rule.dist1);
			}
			if (pro1 != null && pro1.size() > 0 && bcause > ecause) {
				if (rule.getEvtCause(6) < upperValue) { // try to find pro
					pro2 = findFrontPro(vc.subject, tg, rule.fdist);
				}
				if ((pro2 == null || pro2.isEmpty())
						&& rule.getEvtCause(6) > ecause) { // no pro, find event
					pro2 = findEvent(vc.subject, rule.fdist);
				}
			}
			if ((pro1 == null || pro1.isEmpty())
					&& (rule.count[4] + rule.count[5]) * 1f / rule.total > escore) {
				if (rule.getEvtScore(4) < upperValue) { // try to find pro
					pro1 = findFrontPro(vc.subject, tg, rule.fdist);
				}
				if ((pro1 == null || pro1.isEmpty())
						&& rule.getEvtScore(4) > escore) { // no pro, find event
					pro1 = findEvent(vc.subject, rule.fdist);
				}
				if (pro1 != null && pro1.size() > 0 && fcause > ecause) { // find
																			// cause
					if (rule.getEvtCause(5) < upperValue) {
						pro2 = findRegPro(vc.object, tg, -1, rule.dist1);
					}
					if ((pro2 == null || pro2.isEmpty())
							&& rule.getEvtCause(5) > ecause) {
						pro2 = findEvent(vc.object, rule.dist1);
						if (pro2 == null || pro2.isEmpty()) {
							return list;
						}
					}
				}
			}

		}
		// forming event pairs
		if (pro1 != null && pro1.size() > 0 && pro2 != null && pro2.size() > 0) {
			tg.TID = getTrgID();
			for (Object obj1 : pro1) {
				for (Object obj2 : pro2) {
					PData pair = new PData(tg, obj1, obj2, tg.type);
					pair.PID = getEventID();
					extractedSet.add(pair);
					list.add(pair);
				}
			}
			return list;
		} else if (pro1 != null && pro1.size() > 0) {
			tg.TID = getTrgID();
			for (Object obj1 : pro1) {
				PData pair = new PData(tg, obj1, tg.type);
				pair.PID = getEventID();
				extractedSet.add(pair);
				list.add(pair);
			}
			return list;
		}
		return list;
	}

	/**
	 * Split list of proteins using 'and' break
	 * 
	 * @param ls
	 *            : protein
	 * @param tokens
	 *            : tokens from sentence
	 * @return: two lists of proteins if break exists
	 */
	private List<Word>[] splitProByAnd(List<Word> ls, String[] tokens) {
		List<Word> rs[] = new ArrayList[2];
		rs[0] = new ArrayList<Word>();
		rs[1] = new ArrayList<Word>();
		int idx = 0;
		if (ls.size() <= 1) {// cannot split
			rs[0] = ls;
			return rs;
		} else {
			int pos1 = ls.get(0).pos; // first pro
			int pos2 = ls.get(ls.size() - 1).pos; // last pro
			for (int i = pos1 + 1; i < pos2; i++) {
				if (tokens[i].equals("and")) { // has break
					idx = i;
					break;
				}
			}
			if (idx > pos1 && idx < pos2) { // found 'and' break
				for (Word w : ls) {
					if (w.pos < idx) {
						rs[0].add(w);
					} else {
						rs[1].add(w);
					}
				}
			}
		}
		return rs;
	}

	/**
	 * Split compound pros into two list
	 * 
	 * @param pro1
	 * @return
	 */
	private List<Word[]> splitProPair(List<Word> pros) {
		List<Word[]> ls = new ArrayList<Word[]>();
		int i, j;
		Word pr, pr2;
		for (i = 0; i < pros.size() - 1; i++) {
			pr = pros.get(i);
			for (j = i + 1; j < pros.size(); j++) {
				pr2 = pros.get(j);
				if (pr2.compound && pr2.pos == pr.pos) {
					Word[] pair = new Word[2];
					pair[0] = pr;
					pair[1] = pr2;
					ls.add(pair);
					break;
				} else if (pr2.pos > pr.pos) {
					break;
				}
			}
		}
		return ls;
	}

	/**
	 * 12.10.2011 Extracting binding events
	 * 
	 * @param bc
	 * @param preps
	 * @param mods
	 * @return
	 */
	public List<PData> extractBindNP(Word tg, BasicChunk bc, List<Word> preps) {
		List<PData> list = new ArrayList<PData>();
		String ev_type = tg.type;
		RuleSets rule;
		String prep;
		Word frontPrep;
		String key = tg.word + 5 + tg.pos_tag + "NP";
		rule = rset.get(key);
		List<Word> pro1 = null, pro2 = null; // theme1 and theme2
		// Finding pro1 and pro2 based on patterns
		boolean inchunk = false;
		if (rule == null) {
			return list;
		}
		if ((rule.getFrontScore() > 0.01 || rule.inChunk()) && tg.inchunk) { // in
																				// chunk:
																				// PRO
																				// -
																				// TG
			// find pros in the same chunk with trg
			pro1 = getInChunkPro(bc, tg);
			if (pro1 != null && pro1.size() > 0 && rule.getInchunkCause() > 0.1) {
				prep = getPrep(tg, bc, rule.prep);
				if (!prep.isEmpty()) {
					pro2 = findPro(bc, tg, prep, rule.dist1);
				}
			}
			if (pro1 != null && pro1.size() > 0) {
				inchunk = true;
			}
			if (pro1.isEmpty() && hasIts(tg.pos, tokens)
					&& rule.getFrontScore() > 0.01) {
				Word pr = findAnaphora(bc, 5, tg);
				if (pr != null) {
					pro1 = new ArrayList<Word>();
					pro1.add(pr);
				}
			}
		}
		if (pro1 == null || pro1.isEmpty()) { // pro follows trg: TG-PRO
			prep = getPrep(tg, bc, rule.prep);
			// check prep-> has prep or empty
			// // find pro in chunk with maximun distance
			if (tg.pos_tag.startsWith("NN")) { // tg is noun
				if (!prep.isEmpty()) {
					if (rule.prepPair.get(prep) != null) {
						int pairs[] = getPrepPairs(tg.pos, prep, bc,
								rule.prepPair.get(prep), rule.dist2);
						if (pairs != null) {
							List<Word> pros1, pros2;
							int p1 = pairs[0];
							int p2 = pairs[1];
							pros1 = findPro(bc, p1, p2 - p1 - 1);
							pros2 = findPro(bc, p2, rule.dist2);
							if (!pros1.isEmpty() && !pros2.isEmpty()) {
								pro1 = pros1;
								pro2 = pros2;
							} else if (!pros1.isEmpty()) {
								pro1 = pros1;
							}
						}
					}
					if (pro1 == null || pro1.isEmpty()) { // failed to find
															// prep2 /or no pro
															// for theme2
						pro1 = findPro(bc, tg, prep, rule.dist1);
						frontPrep = findFrontPrep(tg, bc);
						if (pro1.size() > 0) {
							if (prep.equals("between")
									|| (tg.word.startsWith("interaction") && prep
											.equals("of"))) { // hardwire
																// code
																// since
																// 'and'
																// is
																// not
																// prep
								List<Word> prs[] = splitProByAnd(pro1, tokens);
								if (prs != null) {
									pro1 = prs[0];
									pro2 = prs[1];
								}
							} else if (frontPrep != null) {
								if (cur_sub != null) { // try CP pattern
									key = tg.word + 5 + tg.pos_tag + "CP";
									RuleSets rule2 = rset.get(key);
									if (rule2 != null
											&& rule2.frontPrep
													.contains(frontPrep.word)) {
										String prep_2 = getPrep(tg, bc,
												rule2.prep);
										if (!prep_2.isEmpty()) {
											pro1 = findFrontPro(cur_sub, tg,
													rule2.fdist);
											pro2 = findPro(bc, tg, prep_2,
													rule2.dist1);
										}
									}
								}
							} else if (rule.count[3] * 1f / rule.total > 0.2) { // split
																				// protein
								// split by and
								// split pair
							}
						}
					}
				} else { // no prep1, use front and behind score to determine
							// theme
					if (rule.getFrontScore() < rule.getBehindScore()) {
						pro1 = findPro(bc, tg, prep, rule.dist1);
					}
					if ((pro1 == null || pro1.isEmpty())
							&& rule.getFrontScore() > 0.1) {
						pro1 = findPro(bc, tg, prep, rule.fdist);
					}
				}

			} else { // trg is JJ or VB
				List<Word> pros2;
				pro1 = findPro(bc, tg, prep, rule.dist1);
				frontPrep = findFrontPrep(tg, bc);
				if (frontPrep != null && cur_sub != null) { // try CP pattern
					key = tg.word + 5 + tg.pos_tag + "CP";
					RuleSets rule2 = rset.get(key);
					if (rule2 != null
							&& rule2.frontPrep.contains(frontPrep.word)) {
						String prep_2 = getPrep(tg, bc, rule2.prep);
						if (!prep_2.isEmpty()) {
							pro1 = findFrontPro(cur_sub, tg, rule2.fdist);
							pro2 = findPro(bc, tg, prep_2, rule2.dist1);
						}
					}
				} else if (pro1.size() > 0 && prep != null) { // try to expand
																// by adding
																// theme2
					if (rule.count[5] * 1f / rule.total > 0.1) { //
						pros2 = findFrontPro(bc, tg, rule.fdist);
						if (pros2.size() > 0) {
							pro2 = pro1;
							pro1 = pros2;
						}
					}
				} else if (pro1.isEmpty() && rule.getFrontScore() > 0.1) {
					pro1 = findFrontPro(bc, tg, rule.fdist);
				}
			}
		}
		// forming pairs based on pro1 and pro2
		if (pro1 != null && pro1.size() > 0 && pro2 != null && pro2.size() > 0) {
			removeDuplicate(pro1, pro2);
		}
		if (pro1 != null && pro1.size() > 0) { // check case has_theme2 first
			bc.usedPro.addAll(pro1);
			if (pro2 != null && pro2.size() > 0) { // has pro2 list
				bc.usedPro.addAll(pro2);
				tg.TID = getTrgID();
				for (Word pr1 : pro1) {
					for (Word pr2 : pro2) {
						PData p = new PData(tg, pr1, pr2, ev_type);
						p.PID = getEventID();
						extractedSet.add(p);
						list.add(p);
					}
				}
				return list;
			} else { // no theme 2
				// TODO: add condition in case this trg always requires theme2;
				// loop the rest for any pattern satisfies
				if (inchunk && pro1.size() == 2 && rule.getInchunkCause() > 0.2) { // compound
																					// case
					List<Word[]> pair = splitProPair(pro1);
					if (pair.size() > 0) {
						tg.TID = getTrgID();
						for (Word[] pw : pair) {
							PData p = new PData(tg, pw[0], pw[1], ev_type);
							p.PID = getEventID();
							extractedSet.add(p);
							list.add(p);
						}
						return list;
					}
				}
				tg.TID = getTrgID();
				for (Word w : pro1) {
					PData p = new PData(tg, w, ev_type);
					p.PID = getEventID();
					extractedSet.add(p);
					list.add(p);
				}
				return list;
			}
		}
		return list;
	}

	/**
	 * get prep pairs from basic chunk
	 * 
	 * @param pos
	 *            : tg pos
	 * @param prep1
	 *            : first prep
	 * @param bs
	 *            : basic chunk
	 * @param set
	 *            : prep 2 list
	 * @param dist
	 *            : distance to search
	 * @return : prep pair if found, null otherwise.
	 */
	private int[] getPrepPairs(int pos, String prep1, BasicChunk bs,
			Set<String> set, int dist) {
		int pair[] = new int[2];
		int idx = bs.getChunkPos(pos);
		int stop = Math.min(idx + dist, bs.chunkList.size() - 1);
		int prepcount = 0;
		for (int i = idx + 1; i < stop; i++) {
			Chunk c = bs.chunkList.get(i);
			if (c.txt.equals(prep1)) {
				for (int j = i + 1; j < stop; j++) {
					Chunk c2 = bs.chunkList.get(j);
					if (set.contains(c2.txt)) {
						pair[0] = i;
						pair[1] = j;
						return pair;
					}
					if (c2.type.equals("PP")) {
						prepcount++;
					}
					if (prepcount > 1) {
						break;
					}
				}
			}
		}
		return null;
	}

	private Word findFrontPrep(Word tg, BasicChunk bc) {
		int tg_pos = bc.getChunkPos(tg.pos);
		int idx;
		if (tg_pos > 0) {
			for (int i = 1; i <= 4; i++) {
				idx = tg_pos - i;
				if (idx >= 0) {
					Chunk c = bc.chunkList.get(idx);
					if (prepSet.contains(c.txt)) {
						return new Word(c.txt, c.begin, 0);
					}
				}
			}
		}
		return null;
	}

	private void removeDuplicate(List l1, List l2) {
		if (l1.equals(l2)) {
			l2.clear();
		} else {
			for (Object ob : l2) {
				if (l1.contains(ob)) {
					l1.remove(ob);
				}
			}
		}
	}

	/**
	 * Get position of second prep follows PRO list
	 * 
	 * @param pro
	 * @param bs
	 * @param set
	 * @return chunk position, -1 otherwise
	 */
	private int getPrep(List<Word> pro, BasicChunk bs, Set<String> set,
			int limit) {
		int pos = 0;
		for (Word w : pro) {
			pos = Math.max(pos, w.pos);
		}
		pos = bs.getChunkPos(pos);
		if (pos >= 0) {
			return getPrep(pos, bs, set, limit);
		}

		return -1;
	}

	private List findEvent(BasicChunk bc, int dist) {
		int pos;
		for (Word w : bc.trgList) {
			pos = bc.getChunkPos(w.pos);
			if (bc.failed.contains(w)) {
				continue;
			} else if (extractedMap.containsKey(w) && pos <= dist) {
				return extractedMap.get(w);
			}
		}
		return null;
	}

	/**
	 * Get prep for trg from list of preps
	 * 
	 * @param pos
	 *            : trigger position
	 * @param preps
	 *            : list of preposition
	 * @return: prep (Word) if found / null otherwise
	 */
	private int getPrep(int pos, BasicChunk bs, Set<String> prep2, int limit) {
		int stop = Math.min(bs.chunkList.size() - 1, limit);
		int i = bs.getChunkPos(pos) + 1;
		for (; i <= stop; i++) {
			Chunk c = bs.chunkList.get(i);
			if (prep2.contains(c.txt)) {
				return i;
			}
		}
		return -1;
	}

	public void extractNP(BasicChunk bs, List<Word> preps) {
		curr_tg = 0;// index of trgs belong to current NP, reset when start a
					// new NP
		sameRole.clear();
		if (bs.extracted) {
			return;
		}
		while (curr_tg < bs.trgList.size()) {
			extractNP(curr_tg, bs, preps);
			curr_tg++;
		}
		bs.extracted = true;
	}

	public List<PData> extractNP(int idx, BasicChunk bs, List<Word> preps) {
		List<PData> temp = null;
		if (idx < bs.trgList.size()) {
			Word tg = bs.trgList.get(idx);
			if (usedTrg.contains(tg) || tg.used) {
				return temp;
			}
			if (idx + 1 < bs.trgList.size()) {
				Word tg2 = bs.trgList.get(idx + 1);
				if (bs.isSameRole(tg, tg2, tokens)) {
					sameRole.add(tg);
					sameRole.add(tg2);
				}

			}
			same_role = false;
			if (sameRole.contains(tg)) {
				same_role = true;
			}
			int ev_type = SenSimplifier.hashType.get(tg.type);
			if (ev_type < 5) {
				temp = extractSimpleNP(tg, bs, preps);
			} else if (ev_type == 5) {
				temp = extractBindNP(tg, bs, preps);
			} else {
				temp = extractRegNP(tg, bs, preps);
			}
			if (temp != null && temp.size() > 0) {
				extractedMap.put(tg, temp);
				usedTrg.add(tg);
			} else if (temp != null && temp.isEmpty()) {
				bs.failed.add(tg);
			}
		}
		return temp;
	}

	/**
	 * Get modifier for trigger in order to determine trigger type
	 * 
	 * @param c1
	 * @param c2
	 * @param ls
	 * @return
	 */
	public List<Word> getMod(Chunk c1, Chunk c2, List<Word> ls) {
		List<Word> list = new ArrayList<Word>();
		int pos1, pos2;
		pos1 = Math.min(c1.begin, c2.begin);
		pos2 = Math.max(c1.end, c2.end);
		for (Word w : ls) {
			if (w.pos >= pos1 && w.pos <= pos2) {
				list.add(w);
			}
		}
		return list;

	}

	/**
	 * Get modifier for trigger in order to determine trigger type
	 * 
	 * @param ls
	 *            : list of modifiers
	 * @return: a modifier for tg/pro
	 */
	public List<Word> getMod(int pos1, int pos2, List<Word> ls) {
		List<Word> list = new ArrayList<Word>();
		for (Word w : ls) {
			if (w.pos >= pos1 && w.pos <= pos2) {
				list.add(w);
			}
		}
		return list;
	}

	/**
	 * Get modifier for trigger in order to determine trigger type
	 * 
	 * @param bs
	 *            : BasicChunk
	 * @param tg
	 *            : trg
	 * @param ls
	 *            : list of modifiers
	 * @return: a modifier for tg/pro
	 */
	public List<Word> getMod(BasicChunk bs, Word tg, List<Word> ls) {
		List<Word> list = new ArrayList<Word>();
		Chunk c = bs.getChunk(tg.pos);
		int pos1 = c.begin, pos2 = c.end;
		for (Word w : ls) {
			if (w.pos >= pos1 && w.pos <= pos2) {
				list.add(w);
			}
		}
		return list;
	}

	/**
	 * Get modifier for trigger in order to determine trigger type
	 * 
	 * @param bs
	 *            : BasicChunk
	 * @param tg
	 *            : trg
	 * @param pros
	 *            :list of pro
	 * @param ls
	 *            : modifiers
	 * @return: a modifier for tg/pro
	 */
	public List<Word> getMod(BasicChunk bs, Word tg, List<Word> pros,
			List<Word> ls) {
		Chunk c1 = bs.getChunk(tg.pos);
		int max = 0;
		int min = 1000000;
		for (Word pr : pros) {
			if (pr.pos > max) {
				max = pr.pos;
			}
			if (pr.pos < min) {
				min = pr.pos;
			}
		}
		if (c1 == null) {
			c1 = bs.getChunk(min);
		}
		Chunk c2 = bs.getChunk(max);
		return getMod(c1, c2, ls);
	}

	/**
	 * Find pros for binding event which has pattern as: Pro1 - Trg - Pro2
	 * 
	 * @param tg
	 *            : trigger
	 * @param ls
	 *            : Basic Chunk
	 * @return : list of pros
	 */
	public List<PData> extractSimpleVP(Word tg, VerbChunk vc, List<Word> mods) {
		List<PData> list = new ArrayList<PData>();
		String ev_type;
		KeyData kdt;
		int idx;
		List<Word> mod;
		RuleSets rule;
		BasicChunk bc;
		kdt = dic.get(tg.word);
		if (kdt != null) {
			ev_type = kdt.getDefault().type;
			idx = SenSimplifier.hashType.get(ev_type);
			if (kdt.keytype == 2) { // shared trigger, set to Gene_expression
				idx = 0;
			}
			String key = tg.word + idx + tg.pos_tag + "VP";
			rule = rset.get(key);
			if (rule == null || rule.isSkipped()) {
				return list;
			}
			List<Word> pros;
			if (vc.verb_type == 1 && tg.pos_tag.equals("VBN")) { // passive
				pros = null;
				bc = vc.object;
				if (bc.chunkList.size() > 0) {
					String prep = getPrepVerb(bc, rule.prep); // give tg with
																// prep higher
																// priority
					if (!prep.isEmpty()) {
						pros = findPro(bc, tg, prep, rule.dist1);
					}
					if (pros == null || pros.isEmpty()) {
						bc = vc.subject;
						pros = findFrontPro(bc, tg, rule.fdist);
					}
				}
			} else { // pro in front of trg
				// pro follows trg
				// check prep-> has prep or empty
				// // find pro in chunk with maximun distance
				bc = vc.object;
				pros = findPro(bc, tg, "", rule.dist1);
				if (pros.isEmpty() && tg.pos_tag.equals("VBN")
						&& rule.getFrontScore() > 0.1) { // this tg migh has
															// passive form
					pros = findFrontPro(bc, tg, rule.fdist);
				}
			}
			if (pros != null && pros.size() > 0) {
				Word tg2 = null;
				if (kdt.keytype == 3 || (kdt.required && kdt.keytype == 1)) { // determine
																				// type
					mod = getMod(bc, tg, pros, mods);
					ev_type = kdt.getType(mod);
					if (ev_type == null) {
						return list;
					}
					tg.keytype = kdt.keytype == 2 ? 2 : 1;
				}
				tg.type = ev_type;
				if (tg.keytype == 20) { // shared trigger, clone one trg for
										// Genexpression
					tg2 = tg; // keep the original for Positive_regulation
					tg = new Word(tg.word, tg.pos, tg.loc);
					tg.locs = tg2.locs;
					tg.type = "Gene_expression";
					tg.keytype = 1;
					tg2.type = "Positive_regulation"; // change type into
														// Pos_Reg
					tg2.keytype = 1;
				}
				tg.TID = getTrgID();
				for (Word w : pros) {
					PData p = new PData(tg, w, ev_type);
					p.PID = getEventID();
					extractedSet.add(p);
					list.add(p);
					bc.usedPro.add(w);
				}
				if (tg2 != null) {
					extractedMap.put(tg, list); // put Gene_expression into
												// extractedMap
					List<PData> list1 = new ArrayList<PData>();
					tg2.TID = getTrgID();
					for (PData pdt : list) {
						PData p = new PData(tg2, pdt, tg2.type);
						p.PID = getEventID();
						extractedSet.add(p);
						list1.add(p);
					}
					return list1;
				}
			}
		}

		return list;
	}

	public List<PData> extractBindVP(Word tg, VerbChunk vc, List<Word> preps) {
		List<PData> list = new ArrayList();
		String ev_type = tg.type;
		String key = tg.word + 5 + tg.pos_tag + "VP";
		RuleSets rule = rset.get(key);
		if (rule == null || rule.isSkipped()) {
			return list;
		}
		List<Word> pro1 = null, pro2 = null;
		String prep = getPrepVerb(vc.object, rule.prep);
		// find theme1 and theme2 for binding events
		pro1 = findPro(vc.object, tg, prep, rule.dist1);
		if (pro1.size() > 0) {
			if ((rule.count[5] * 1f) / rule.total > 0.1) {
				List<Word> pros2 = findFrontPro(vc.subject, tg, rule.fdist);
				if (pros2.size() > 0) {
					pro2 = pro1;
					pro1 = pros2;
				}
			}
		}
		if (pro1 == null || pro1.isEmpty()) {
			pro1 = findFrontPro(vc.subject, tg, rule.dist1);
		}
		// forming event from pro1 and pro2 list
		if (pro1 != null && pro1.size() > 0) { // PRO1 - TG - PRO2
			if (pro2 != null && pro2.size() > 0) {
				tg.TID = getTrgID();
				for (Word pr1 : pro1) {
					for (Word pr2 : pro2) {
						PData p = new PData(tg, pr1, pr2, ev_type);
						p.PID = getEventID();
						extractedSet.add(p);
						list.add(p);
					}
				}
			} else {
				tg.TID = getTrgID();
				for (Word w : pro1) {
					PData p = new PData(tg, w, ev_type);
					p.PID = getEventID();
					extractedSet.add(p);
					list.add(p);
				}
			}
		}
		return list;
	}

	/**
	 * 11.10.2011. Checked!
	 * 
	 * @param bs
	 *            : BasicChunk
	 * @param tg
	 *            : trigger
	 * @return : list of protein that can use to form event
	 */
	public List<Word> getInChunkPro(BasicChunk bs, Word tg) {
		Chunk chunk = bs.getChunk(tg.pos);
		if (chunk != null) {
			return chunk.getInChunkPro(tg, tokens);
		} else {
			return new ArrayList<Word>();
		}
	}

	/**
	 * 11.10.2011. Checked! Find pro for noun phrase patterns: TG - PRO
	 * 
	 * @param bs
	 *            : BasicChunk
	 * @param tg
	 *            : trig
	 * @param prep
	 *            :prep
	 * @param len
	 *            : number of chunks
	 * @return: list of pros
	 */
	public List<Word> findPro(BasicChunk bs, Word tg, String prep, int len) {
		List<Word> ls = new ArrayList<Word>();
		boolean found_pro = false;
		len = Math.min(len, noun_len);
		Chunk chunk = bs.getChunk(tg.pos);
		int pos = 0; // for verb phrase ; tg not belong to this chunk ; so start
						// from 0
		Chunk c;
		if (chunk != null) { // noun phrase
			pos = bs.chunkList.indexOf(chunk);
		} else {
			if (!bs.proList.isEmpty()) {
				if (!bs.trgList.isEmpty()) {
					Word p1 = bs.proList.get(0);
					Word t1 = bs.trgList.get(0);
					if (p1.pos > t1.pos) {
						return ls;
					}
				}
			}
		}
		int stop = Math.min(len + pos, bs.chunkList.size() - 1);// offset
		// stop = Math.min(stop, getStop(bs,tg));
		int idx = pos;
		if (!prep.isEmpty() && chunk != null) {
			idx = pos + 2;
		} else if (chunk != null) { // no prep && chunk!=null
			// noun phrase contain trg
			List<Word> temp = chunk.getPro(tg, tokens); // for tg with JJ and
														// VBz tag
			if (temp.size() > 0) { //
				for (Word w : temp) {
					if (w.pos > tg.pos
							&& (!bs.usedPro.contains(w) || same_role)) {
						ls.add(w);
					}
				}
			}
			if (ls.size() > 0) {
				found_pro = true;
			}
			idx++; // TO DO: check whether to skip this chunk?
		}
		for (int i = idx; i <= stop; i++) {
			c = bs.chunkList.get(i);
			if (c.type.equals("VP") || (c.type.equals("PP") && found_pro)) {
				break;
			} else {
				List<Word> temp = c.getPro(tokens);
				if (temp.size() > 0) { //
					if (temp.size() > 0) { //
						for (Word w : temp) {
							if (!bs.usedPro.contains(w) || same_role) {
								ls.add(w);
							}
						}
						if (!ls.isEmpty()) {
							found_pro = true;
						}
					}
				}
			}
			if (c.trigs.size() > 0 && !same_role) {
				break;
			} else if (c.trigs.size() > 0 && same_role) {
				same_role = false;
			}

		}
		return ls;
	}

	private int getStop(BasicChunk bs, Word tg) {
		if (bs.trgList.isEmpty()) {
			return bs.chunkList.size() - 1;
		} else {
			int i = 0;
			while (i < bs.trgList.size()) {
				Word c = bs.trgList.get(i);
				if (c.pos < tg.pos) {
					i++;
				} else if (bs.isSameRole(tg, c, tokens)) {
					i++;
				} else {
					return bs.getChunkPos(c.pos);
				}
			}
		}
		return bs.chunkList.size() - 1;

	}

	/**
	 * Get Pro as cause for : PRO-xx TG PRO/EVENT
	 * 
	 * @param bs
	 * @param tg
	 * @return
	 */
	public List<Word> findCausePro(BasicChunk bs, Word tg) {
		List<Word> ls = new ArrayList<Word>();
		int pos = bs.getChunkPos(tg.pos);
		if (pos >= 0) {
			Chunk c = bs.chunkList.get(pos);
			return c.cause;
		}
		return ls;
	}

	/**
	 * Find pro after a given position (for use with tg- prep1 - prep2) Only
	 * looks for pro after prep2
	 * 
	 * @param bs
	 *            : BasicChunk
	 * @param start
	 *            : start position
	 * @param len
	 *            : distance to look for
	 * @return : list of pros if found
	 */
	public List<Word> findPro(BasicChunk bs, int start, int len) {
		List<Word> ls = new ArrayList<Word>();
		int stop = Math.min(len + start, bs.chunkList.size() - 1);// offset
		int idx = start;
		boolean found_pro = false;
		Chunk c;
		for (int i = idx; i <= stop; i++) {
			c = bs.chunkList.get(i);
			List<Word> temp = c.getPro(tokens);
			if (temp.size() > 0) { //
				if (temp.size() > 0) { //
					for (Word w : temp) {
						if (!bs.usedPro.contains(w) || same_role) {
							ls.add(w);
						}
					}
					if (!ls.isEmpty()) {
						found_pro = true;
					}
				}
			}
			if (c.type.equals("VP") || (c.type.equals("PP") && found_pro)) {
				break;
			} else if (c.trigs.size() > 0 && !same_role) {
				break;
			} else if (c.trigs.size() > 0 && same_role) {
				same_role = false;
			}
		}
		return ls;
	}

	/**
	 * Find pro after a given position (for use with tg- prep1 - prep2) Only
	 * looks for pro after prep2
	 * 
	 * @param bs
	 *            : BasicChunk
	 * @param start
	 *            : start position
	 * @param len
	 *            : distance to look for
	 * @return : list of pros if found
	 */
	public List<Word> findRegPro(BasicChunk bs, Word tg, int start, int len) {
		List<Word> ls = new ArrayList<Word>();
		int pos = start;
		int tg_pos = -1;
		int prep_count = 0;
		boolean found_pro = false;
		int limit = bs.chunkList.size() - 1;
		if (pos >= 0) { // NP chunk
			int pos1 = bs.trgList.indexOf(tg);
			if (pos1 < bs.trgList.size() - 1) {
				Word tg2 = bs.trgList.get(pos1 + 1);
				if (sameRole.contains(tg2)) {
					if (pos1 < bs.trgList.size() - 2) {
						tg_pos = bs.trgList.get(pos1 + 2).pos;
					}
				} else {
					tg_pos = tg2.pos;
				}
			} else { // no more trg
			}
			Chunk chunk = bs.chunkList.get(pos);
			List<Word> temp = chunk.getPro(tg, tokens); // for tg with JJ and
														// VBz tag
			if (temp.size() > 0) { //
				for (Word w : temp) {
					if (w.pos > tg.pos && !bs.usedPro.contains(w) || same_role) {
						ls.add(w);
					}
				}
			}
			if (ls.size() > 0) {
				found_pro = true;
			}
		} else { // VP chunk
			if (bs.trgList.size() > 0) {
				tg_pos = bs.trgList.get(0).pos;
			}
			pos = 0;
		}
		if (tg_pos > 0) { // has_tg
			limit = bs.getChunkPos(tg_pos);
		}
		int idx = pos;
		int stop = Math.min(len + pos, limit);// offset
		Chunk c;
		for (int i = idx; i <= stop; i++) {
			c = bs.chunkList.get(i);
			List<Word> temp = c.getPro(tokens);
			if (temp.size() > 0) { //
				if (temp.size() > 0) { //
					for (Word w : temp) {
						if (!bs.usedPro.contains(w) || same_role) {
							ls.add(w);
						}
					}
					if (!ls.isEmpty()) {
						found_pro = true;
					}
				}
			}
			if (c.type.equals("VP") || (c.type.equals("PP") && found_pro)) {
				break;
			} else if (c.trigs.size() > 0 && !same_role) {
				break;
			} else if (c.trigs.size() > 0 && same_role) {
				same_role = false;
			}
		}
		return ls;
	}

	/**
	 * 12.10.2011 Find pro in front of tg for noun phrase/subject patterns: PRO
	 * - TG
	 * 
	 * @param bs
	 *            : BasicChunk
	 * @param tg
	 *            : trig
	 * @param prep
	 *            :prep
	 * @param len
	 *            : number of chunks
	 * @return: list of pros
	 */
	public List<Word> findFrontPro(BasicChunk bs, Word tg, int len) {
		List<Word> ls = new ArrayList<Word>();
		boolean found_pro = false;
		len = Math.min(len, noun_len);
		// for subject case, chunk do not contains trg, therefore use last
		// position
		int pos, idx;
		Chunk chunk = bs.getChunk(tg.pos);
		int start = 0;
		Chunk c;
		if (chunk == null) { // subject case, start from begining, search to the
								// right
			if (!bs.proList.isEmpty()) {
				if (!bs.trgList.isEmpty()) {
					Word p1 = bs.proList.get(0);
					Word t1 = bs.trgList.get(0);
					if (p1.pos > t1.pos) {
						return ls;
					}
				}
			}
			pos = bs.chunkList.size() - 1;
			idx = Math.min(len, pos);
			for (int i = start; i <= idx; i++) {
				c = bs.chunkList.get(i);
				List<Word> temp = c.getPro(tokens);
				if (temp.size() > 0) { //
					for (Word w : temp) {
						if (!bs.usedPro.contains(w) || check_pro) {
							ls.add(w);
						}
					}
					if (!ls.isEmpty()) {
						found_pro = true;
					}
				}
				if ((c.type.equals("VP") || c.type.equals("PP")) && found_pro) {
					break;
				} else if (c.pros.isEmpty() && c.trigs.size() > 0) {
					break;
				}
			}
			return ls;
		} else { // noun phrase case, start from tg position, search to the left
			pos = bs.chunkList.indexOf(chunk); // noun phrase case
			int end = Math.max(0, pos - len);
			idx = pos;
			List<Word> temp1 = bs.chunkList.get(idx).getInChunkPro(tg, tokens); // pro
																				// in
																				// the
																				// same
																				// chunk
																				// with
																				// trg
			if (temp1.size() > 0) { //
				for (Word w : temp1) {
					if (!bs.usedPro.contains(w) || check_pro || same_role) {
						ls.add(w);
					}
				}
				if (!ls.isEmpty()) {
					found_pro = true;
				}
			} else {
				temp1 = chunk.getPro(tokens);
				for (Word w : temp1) {
					if (!bs.usedPro.contains(w) || check_pro || same_role) {
						ls.add(w);
					}
				}
				if (!ls.isEmpty()) {
					found_pro = true;
				}
			}
			for (int i = idx - 1; i >= end; i--) {
				c = bs.chunkList.get(i);
				List<Word> temp = c.getProFront();
				if (temp.size() > 0) { //
					if (temp.size() > 0) { //
						for (Word w : temp) {
							if (!bs.usedPro.contains(w) || check_pro
									|| same_role) {
								ls.add(w);
							}
						}
						if (!ls.isEmpty()) {
							found_pro = true;
						}
					}
				}
				if (c.type.equals("VP") || (c.type.equals("PP") && found_pro)) {
					break;
				} else if (c.pros.isEmpty() && c.trigs.size() > 0) {
					break;
				}
			}
		}
		return ls;
	}

	/**
	 * Get preposition follows a given trg
	 * 
	 * @param tg
	 *            : Tg
	 * @param bs
	 *            : basic chunk
	 * @param prep
	 *            : list of allowed prep
	 * @return: prep if found, null otherwise
	 */
	private String getPrep(Word tg, BasicChunk bs, Set<String> prep) {
		int tg_pos = bs.getChunkPos(tg.pos);
		if (tg_pos >= 0 && tg_pos < bs.chunkList.size() - 1) { // NP
			int pos = bs.trgList.indexOf(tg);
			if (pos < bs.trgList.size() - 1) {
				Word tg2 = bs.trgList.get(pos + 1);
				int pos2 = bs.getChunkPos(tg2.pos);
				if (tg_pos == pos2 && tg2.pos - tg.pos == 1) {
					return "";
				}
			}
			Chunk c = bs.chunkList.get(tg_pos + 1);
			if (prep.contains(c.txt)) {
				return c.txt;
			}
		} else if (bs.chunkList.size() > 0) { // VP
			Chunk c = bs.chunkList.get(0);
			if (prep.contains(c.txt)) {
				return c.txt;
			}
		}
		return "";
	}

	/**
	 * Get preposition follows a given trg
	 * 
	 * @param tg
	 *            : Tg
	 * @param bs
	 *            : basic chunk
	 * @param prep
	 *            : list of allowed prep
	 * @return: prep if found, null otherwise
	 */
	private String getPrepVerb(BasicChunk bs, Set<String> prep) {
		if (bs.chunkList.isEmpty()) {
			return "";
		}
		for (Chunk c : bs.chunkList) {
			if (prep.contains(c.txt)) {
				return c.txt;
			} else if (c.type.startsWith("AD")) {
				continue;
			} else {
				return "";
			}
		}
		return "";
	}

	public void initSentence(String id) {
		out = analyzer.analyze(id);
		trg_ID = analyzer.proList.size(); // trg ID follows number of protein +1
											// ;
		// In case of predicted proteins that have been merged with gold genes, the 1:1 relation between the number
		// of proteins and their IDs gets lost. Obtain the highest protein ID to avoid ID clashes.
		OptionalInt maxGeneIdNumber = analyzer.proList.stream().map(prot -> prot.tid).mapToInt(tid -> Integer.parseInt(tid.substring(1))).max();
		if (maxGeneIdNumber.isPresent())
			trg_ID = maxGeneIdNumber.getAsInt() + 1;
		evt_ID = 0;// reset new Event list
	}

	public void extractSentence(int i) {
		List<Word> prep, mods;
		if (out[i] == null) {
			return;
		}
		curr_senID = i;
		tokens = analyzer.tokenList.get(i);
		prep = analyzer.getPreps(tokens);
		mods = analyzer.getModifier(tokens);
		if (debug) {
			System.out.print("----Text: ");
			System.out.println(analyzer.shortsen[i]);
			analyzer.printChunk(out[i]);
		}
		op.curr_text = analyzer.shortsen[i];
		tags = analyzer.tagList.get(i);
		op.analyzeChunk(out[i], tags, tokens); // split sentence into chunks
												// (BasicChunk/VerbChunk)
		// Extract from BasicChunk and VerbChunk
		// Evaluating trg from BasicChunk
		for (BasicChunk bc : op.bsList) {
			evaluateTrg(bc, prep, mods, tokens, analyzer.detectedTrg[i]);
			extractNP(bc, prep);
		}
		// evaluating candidate trg from clause
		usedTrg.clear();
		for (VerbChunk vc : op.verbList) {
			cur_sub = null;
			curr_verb = vc.verb;
			curr_verb_type = vc.verb_type;
			check_pro = vc.subject_type == 1 ? true : false;
			evaluateTrg(vc.subject, prep, mods, tokens, analyzer.detectedTrg[i]);
			extractNP(vc.subject, prep);
			cur_sub = vc.subject;
			evaluateTrg(vc.object, prep, mods, tokens, analyzer.detectedTrg[i]);
			extractNP(vc.object, prep);
			extractVP(vc, prep, mods);
		}
	}

	public void printChunkList() {
		for (BasicChunk bs : op.bsList) {
			bs.printChunk();
			System.out.println("");
		}
		for (VerbChunk vc : op.verbList) {
			if (!vc.isQualify()) {
				// continue;
			}
			vc.print();
			System.out.println("");
		}
	}

	public void extractVP(VerbChunk vc, List<Word> preps, List<Word> mods) {
		int i = 0;
		while (i < vc.verb.trigs.size()) {
			Word tg = vc.verb.trigs.get(i); // last tg
			if (i + 1 < vc.verb.trigs.size()) {
				Word tg2 = vc.verb.trigs.get(i + 1);
				if (vc.verb.isSameRole(tg, tg2, tokens)) {
					sameRole.add(tg2);
				}
			}
			same_role = false;
			if (sameRole.contains(tg)) {
				same_role = true;
				sameRole.clear();
			}
			KeyData kdt = dic.get(tg.word);
			String ev_type;
			int idx;
			List<PData> ls = null;
			if (kdt != null) {
				if (kdt.keytype > 1) {
					ev_type = kdt.getDefault().type;
					idx = SenSimplifier.hashType.get(ev_type);
					if (idx >= 5) {
						tg.type = ev_type;
					}
				} else {
					tg.type = kdt.type;
					idx = SenSimplifier.hashType.get(tg.type);
				}
				if (idx < 5) {
					ls = extractSimpleVP(tg, vc, mods);
				} else if (idx == 5) {
					ls = extractBindVP(tg, vc, preps);
				} else {
					ls = extractRegVP(tg, vc, preps);
				}
				if (ls != null && !ls.isEmpty()) {
					extractedMap.put(tg, ls);
				}
			}
			i++;
		}
	}

	private String getTrgID() {
		trg_ID++;
		return "T" + trg_ID;
	}

	private String getEventID() {
		evt_ID++;
		return "E" + evt_ID;
	}

	public void Test(String outPath) {
		try {
			init();
			removeOldFiles(outPath);
			List<String> ids = simp.loadPMIDs();
			// ids.clear();
			// ids.add("PMC-1134658-01-Background");
//			System.out.println("Loading abstracts: " + ids.size());
			log.debug("Loading abstracts: {}", ids.size());
			for (String id : ids) {
				// System.out.println(" Extracting ... " + id);
				curr_pmid = id;
				extractEvents(id);
				writeResult(id, outPath);
			}
		} catch (Exception ex) {
//			ex.printStackTrace();
//			System.out.println(ex.getLocalizedMessage());
//			log.error("Caught exception, recognition of events is skipped for current document(s). Error occurred in document " + curr_pmid + ":", ex);
			throw new BioSemException(ex);
		}
		// RuleLearner learner = new RuleLearner();
		// learner.storeRuleSet(rules, db_sr);
	}

	public void Test() {
		try {
			init();
			List<String> ids = simp.loadPMIDs();
			// ids.clear();
			// ids.add("PMC-1134658-01-Background");
//			System.out.println("Loading abstracts: " + ids.size());
			log.debug("Loading abstracts: {}", ids.size());
			for (String id : ids) {
				// System.out.println(" Extracting ... " + id);
				curr_pmid = id;
				extractEvents(id);
			}
		} catch (Exception ex) {
//			ex.printStackTrace();
//			System.out.println(ex.getLocalizedMessage());
			log.debug("Caught exception, recognition of events is skipped for current document(s). Error occurred in document " + curr_pmid + ":", ex);
		}
		// RuleLearner learner = new RuleLearner();
		// learner.storeRuleSet(rules, db_sr);
	}

	/**
	 * Extracting events from abstract/paragraph.
	 * 
	 * @param id
	 *            : PMID (abstract/paragraph)
	 * @return List of events
	 */
	public void extractEvents(String id) {
		extractedMap.clear();// clear all events
		extractedSet.clear();
		initSentence(id);
		for (int i = 0; i < analyzer.senpos.length; i++) { // loop over
															// sentences
			extractSentence(i);
		}
		unifyEvents(id);
	}

	private void unifyEvents(String id) {
		boolean duplicatesFound;
		do {
			Set<PData> uniqueEvents = new HashSet<>();
			Set<PData> duplicates = new HashSet<>();
			List<PData> lst = new ArrayList<>(extractedSet);

			duplicatesFound = false;
			Map<PData, List<PData>> eqClasses = new HashMap<>();
			for (int i = 0; i < lst.size(); ++i) {
				PData pdt = lst.get(i);
				// skip events that have already been declared to equal another
				// event occurring earlier in the list
				if (duplicates.contains(pdt))
					continue;
				List<PData> equals = new ArrayList<>();
				for (int j = i + 1; j < lst.size(); ++j) {
					PData pdt2 = lst.get(j);
					boolean equal = true;
					if (!pdt.evt_type.equals(pdt2.evt_type))
						equal = false;
					else if (pdt.trg == null ^ pdt2.trg == null)
						equal = false;
					else if (pdt.pro1 == null ^ pdt2.pro1 == null)
						equal = false;
					else if (pdt.pro2 == null ^ pdt2.pro2 == null)
						equal = false;
					else if (pdt.pdata1 == null ^ pdt2.pdata1 == null)
						equal = false;
					else if (pdt.pdata2 == null ^ pdt2.pdata2 == null)
						equal = false;
					else if (pdt.trg != null && pdt2.trg != null
							&& !pdt.trg.TID.equals(pdt2.trg.TID))
						equal = false;
					else if (pdt.pro1 != null && pdt2.pro1 != null
							&& !pdt.pro1.TID.equals(pdt2.pro1.TID))
						equal = false;
					else if (pdt.pro2 != null && pdt2.pro2 != null
							&& !pdt.pro2.TID.equals(pdt2.pro2.TID))
						equal = false;
					else if (pdt.pdata1 != null && pdt2.pdata1 != null
							&& !pdt.pdata1.PID.equals(pdt2.pdata1.PID))
						equal = false;
					else if (pdt.pdata2 != null && pdt2.pdata2 != null
							&& !pdt.pdata2.PID.equals(pdt2.pdata2.PID))
						equal = false;
					if (equal) {// && id.equals("PMC-2222968-06-Results"))
						// System.out.println(pdt.PID + ", " + pdt2.PID +": " +
						// pdt.getWriteID() + " " +
						// pdt2.getWriteID() +
						// " " + id);
						equals.add(pdt2);
						duplicates.add(pdt2);
						duplicatesFound = true;
					}
				}
				eqClasses.put(pdt, equals);
			}
			for (PData pdt : eqClasses.keySet()) {
				List<PData> equals = eqClasses.get(pdt);
				// for each event, check whether it is referring to an element
				// of the current equivalence class
				// if so, replace the reference with the canonical element
				for (PData pdt3 : lst) {
					for (PData pdt4 : equals) {
						// replace duplicate events with the canonical event
						if (pdt3.pdata1 != null
								&& pdt3.pdata1.PID.equals(pdt4.PID))
							pdt3.pdata1 = pdt;
						if (pdt3.pdata2 != null
								&& pdt3.pdata2.PID.equals(pdt4.PID))
							pdt3.pdata2 = pdt;
					}
				}
			}
			// create a new set of events, filtering out the duplicates
			for (PData pdt5 : lst) {
				if (!duplicates.contains(pdt5))
					uniqueEvents.add(pdt5);
			}
			extractedSet = uniqueEvents;
			// it is possible that by replacing references to duplicate events,
			// some referring events have actually
			// become equal; repeat until no duplicates are found any more
		} while (duplicatesFound);

	}

	private void writeResult(String id, String path) {
		try {
			File dPath = new File(path);
			if (!dPath.exists()) {
				dPath.mkdirs();
			}
			writer = new FileWriter(path + "/" + id + ".a2");
			for (Word w : extractedMap.keySet()) {
				writer.append(w.toString());
			}
			// for (Word w : extractedMap.keySet()) {
			// List<PData> ls = extractedMap.get(w);
			// for(PData pdt:ls){
			// writer.append(pdt.toString());
			// }
			// }
			writeEvent.clear();
			String e_id;
			for (PData pdt : extractedSet) {
				e_id = pdt.getWriteID();
				// if (id.equals("PMC-2222968-06-Results"))
				// System.out.println(e_id + " ---> " + pdt.toString());
				if (!writeEvent.contains(e_id)) {
					writer.append(pdt.toString());
					writeEvent.add(e_id);
				}
			}
			closeFile();
		} catch (Exception ex) {
			System.out.println("Loi roi :-(");
			System.out.println(ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Remove old output files
	 * 
	 * @param filename
	 */
	private void removeOldFiles(String filename) {
		try {
			File file = new File(filename);
			if (file.isDirectory()) {
				File[] list = file.listFiles();
				for (File f : list) {
					f.delete();
				}
			}
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
	}

	private void closeFile() {
		try {
			if (writer != null) {
				writer.close();
			}
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
	}

	/**
	 * Available after {@link #Test()} or {@link #Test(String)} has been run.
	 * Contains the words identified as being triggers.
	 * 
	 * @return
	 */
	public Set<Word> getExtractedTriggers() {
		return extractedMap.keySet();
	}

	/**
	 * Available after {@link #Test()} or {@link #Test(String)} has been run.
	 * Contains identified events mentions.
	 * 
	 * @return
	 */
	public Set<PData> getExtractedEvents() {
		return extractedSet;
	}

	public static void main(String[] args) {
		String sr_path = null;
		String dest_path = null;
		String outPath = null;
		if (args.length == 3) {
			sr_path = args[0];
			dest_path = args[1];
			outPath = args[2];
		}
		else {
			System.out.println("Declare the trained database, the test database and the output path.");
			System.exit(1);
		}
//		String sr_path = "D:/DataNLP/Mix2011/Data";
//		String dest_path = "D:/DataNLP/Test2011/Data";
//		String outPath = "d:/Output/test";
//		String sr_path = "data_deleteme/db-2011/mix";
//		String dest_path = "D:/DataNLP/Data2011TestPrepared/Data";
//		String outPath = "data_deleteme/annotation_output_test";
		DBUtils sr = new DBUtils();
		sr.openDB(sr_path);
		DBUtils dest = new DBUtils();
		dest.openDB(dest_path);
		EventExtraction xtr = new EventExtraction(sr, dest);
		xtr.Test(outPath);
		sr.closeDB();
	}

	/**
	 * Global variables for extracting events
	 */
	List<Chunk>[] out;
	String tokens[]; // tokens of current sentence
	String tags[]; // POS tags of current sentence
	Map<String, String> skipTrg = new HashMap<String, String>();
	Map<Word, List<PData>> extractedMap = new HashMap<Word, List<PData>>();
	Set<PData> extractedSet = new HashSet<PData>();
	Set<String> writeEvent = new HashSet<String>();
	Set<Word> usedTrg = new HashSet<Word>();
	int curr_tg;
	int trg_ID; // trigger ID
	int evt_ID; // Event_ID
	public final static Set<String> prepSet = new HashSet<String>();
	public static final String ccList[] = { "and", "or", "but", "as well as",
			"but not" };
	public final static String prepList[] = { "by", "after", "through", "via",
			"upon" };
	public static final Set<String> ccSet = new HashSet<String>();

	static {
		prepSet.addAll(Arrays.asList(prepList));
		ccSet.addAll(Arrays.asList(ccList));
	}

	public void setDb(DBUtils docDb) {
		analyzer.setDB(docDb);
	}

}
