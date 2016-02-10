package relations;

import java.util.*;

import utils.BioSemException;

/**
 *
 * @author Chinh
 * @Date: Jun 16, 2011
 */
public class BasicChunk {

	public String type;// NP , VP, PP, ADJP ..
	List<Chunk> chunkList = new ArrayList<Chunk>();
	List<Word> proList = new ArrayList<Word>();
	List<Word> trgList = new ArrayList<Word>();
	int start = 0, end = -1;
	public boolean extracted = false;
	Set<Word> usedPro = new HashSet<Word>(); // used protein
	public boolean is_merged = false;
	public boolean init = false;
	Set<Word> failed = new HashSet<Word>();

	/**
	 * Check whether this basic chunk is empty.
	 * 
	 * @return: true if it is empty
	 */
	public boolean isEmpty() {
		return end == -1 ? true : false;
	}

	public void addChunk(BasicChunk bc) {
		for (Chunk c : bc.chunkList) {
			addChunk(c);
		}
	}

	public void removeTrg(Word tg) {
		Chunk c = getChunk(tg.pos);
		if (c != null) {
			c.trigs.remove(tg);
			if (tg.combined) {
				c.removePro(tg.pos);
			}
			trgList.remove(tg);
			Word pr = null;
			for (Word w : proList) {
				if (w.pos == tg.pos) {
					pr = w;
					break;
				}
			}
			if (pr != null) {
				proList.remove(pr);
			}
		} else {
			String message = "---> BUG: " + tg.word + "  Pos: " + tg.pos;
			// System.out.println(message);
			// printChunk();
			throw new BioSemException(new Exception("There was a word without a chunk: " + message
					+ ". This issue could probably handled more gracefully if someone would look into it."));
		}
	}

	/**
	 * Check whether a given trigger is in_chunk form
	 * 
	 * @param tg
	 * @return: true if in_chunk
	 */
	public boolean inChunkTrg(Word tg, String[] tokens) {
		int pos = getChunkPos(tg.pos);
		if (pos < 0) {
			return false;
		}
		Chunk c = chunkList.get(pos);
		if (c.is_inChunk(tg, tokens)) {
			return true;
		}
		return false;
	}

	/**
	 * Check whether two trigger has the same role (i.e have the same arguments)
	 * 
	 * @param tg1
	 * @param tg2
	 * @param tokens
	 * @return
	 */
	public boolean isSameRole(Word tg1, Word tg2, String[] tokens) {
		int pos1, pos2;
		if (tg1.inchunk || tg2.inchunk) {
			return false;
		}
		if (tg1.combined || tg2.combined || !tg1.pos_tag.equals(tg2.pos_tag)) {
			return false;
		}
		pos1 = getChunkPos(tg1.pos);
		pos2 = getChunkPos(tg2.pos);
		if (pos1 < 0 || pos2 < 0) {
			return false;
		}
		if (pos2 == pos1 + 2) {
			if (pos1 > 0 && chunkList.get(pos1 - 1).txt.equals("of")) {
				return false;
			}
			if (!ccMap.contains(chunkList.get(pos1 + 1).txt)) {
				return false;
			}
		} else if (pos1 != pos2) {
			return false;
		}
		pos1 = Math.min(tg1.pos, tg2.pos);
		pos2 = Math.max(tg1.pos, tg2.pos);
		for (int i = pos1 + 1; i < pos2; i++) {
			if (ccMap.contains(tokens[i])) {
				for (Word pr : proList) {
					if (pr.pos > pos1 && pr.pos < pos2) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether in the same chunk but not in_chunk form
	 * 
	 * @param tg
	 * @param pr
	 * @return
	 */
	public boolean isSameChunk(Word tg, Word pr) {
		int pos1 = getChunkPos(tg.pos);
		int pos2 = getChunkPos(pr.pos);
		if (pos1 == pos2 && pos1 >= 0 && tg.pos < pr.pos) {
			return true;
		}
		return false;
	}

	public List<Word> getPro() {
		List<Word> ls = new ArrayList<Word>();
		for (Word w : proList) {
			if (!usedPro.contains(w)) {
				ls.add(w);
			}
		}
		return ls;
	}

	public void mergeNP() {
		if (init) {
			return;
		}
		int i = 1;
		Chunk curr, next, prev = null;
		if (chunkList.isEmpty()) {
			return;
		}
		prev = chunkList.get(0);
		while (i < chunkList.size()) {
			curr = chunkList.get(i);
			if (prev != null && prev.type.equals("NP") && curr.type.equals("NP")) {
				prev.merge(curr);
				chunkList.remove(curr);
				continue;
			} else if (curr.type.equals("ADVP") && curr.pros.isEmpty() && curr.trigs.isEmpty()
					&& i < chunkList.size() - 1) {
				next = chunkList.get(i + 1);
				if (prev != null && prev.txt.equals(",") && next.txt.equals(",")) {
					// System.out.println("---> before remove advp:
					// "+toString());
					chunkList.remove(prev);
					chunkList.remove(curr);
					chunkList.remove(next);
					prev = null;
					// System.out.println("---> after remove advp:
					// "+toString());
					continue;
				}
			}
			i++;
			prev = curr;
		}
		findCoordinator(chunkList);
		init = true;
	}

	public String findTrigger(Word tg, Word pro) {
		String txt = null;
		int p1, p2;
		p1 = Math.min(tg.pos, pro.pos);
		p2 = Math.max(tg.pos, pro.pos);
		for (Word w : trgList) {
			if (w.pos > p1 && w.pos < p2) {
				if (txt == null) {
					txt = w.word;
				} else {
					txt += "," + w.word;
				}
			}
		}
		if (txt != null) {
			return txt;
		} else {
			return "";
		}
	}

	public void findCoordinator(List<Chunk> ls) {
		int i = 0, idx = 0;
		Chunk curr, next1, next;
		boolean found, in_loop;
		while (i < ls.size() - 2) {
			curr = ls.get(i);
			found = false;
			in_loop = false;
			if (curr.type.equals("NP")) {
				int j = i + 1;
				if (curr.txt.contains(",")) { // 14.10.2011 : add this condition
												// since in some cases, the
												// parser has already merged
												// co-ordinative chunks
					in_loop = true;
				}
				while (j < ls.size() - 1) {
					next = ls.get(j);
					next1 = ls.get(j + 1);
					if (next.type.equals("VP") && next.txt.equals("containing") && next1.type.equals("NP")) {
						found = true; // can break here
						idx = j + 1;
						break;
					}
					if (next.txt.equals(",") && next1.type.equals("NP")) {// [,]
																			// [NP]
						j += 2;
						in_loop = true;
						continue;
					} else if (next.txt.equals(",")) {// [,] -> [CC]
														// [NP]|[appo][NP]
						if (next1.type.equals("O") && ccMap.contains(next1.txt) && in_loop) { // expect
																								// NP
							if (j < ls.size() - 2) {
								Chunk tmp = ls.get(j + 2);
								if (tmp.type.equals("NP")) {
									found = true; // can break here
									idx = j + 2;
									break;
								}
							} else {
								System.out
										.println("BasicChunk: findCoordinator -----> Unkown case: ---> co-ordination");
								System.out.println(toString());
								// System.exit(1);
								break;
							}
						} else if (appoMap.contains(next1.txt)) { // not in
																	// loop,
																	// check for
																	// apposition
							if (j < ls.size() - 2) {
								Chunk tmp = ls.get(j + 2);
								if (tmp.type.equals("NP")) {
									idx = j + 2;
									j += 3;
									in_loop = true;
									found = true;
									continue;
								} else {
									break;
								}
							} else {
								System.out
										.println("BasicChunk: findCoordinator -----> Unkown case: --> apposition ---");
								System.out.println(toString());
								// System.exit(1);
								break;
							}
						} else if (in_loop && (next1.txt.contains(" and ") || next1.txt.contains(" or "))) {
							found = true;
							idx = j + 2;
							break;
						} else {
							break;
						}
					} else if (next.type.equals("O") && ccMap.contains(next.txt) && next1.type.equals("NP")) {
						// NP and NP
						found = true;
						idx = j + 1;
						if (!in_loop) {
							if (j < ls.size() - 2) {
								Chunk tmp = ls.get(j + 2);
								if (tmp.txt.equals("of")) { // ignore this case,
															// second NP might
															// related to 'of'
									found = false;
								}
							}
						}
						break;
					} else if (next.type.equals("CONJP") && next1.type.equals("NP")) {
						idx = j + 1;
						found = true;
						if (!in_loop) {
							if (j < ls.size() - 2) {
								Chunk tmp = ls.get(j + 2);
								if (tmp.txt.equals("of")) { // ignore this case,
															// second NP might
															// related to 'of'
									found = false;
								}
							}
						}
						break;
					} else if (appoMap.contains(next.txt) && next1.type.equals("NP")) {
						if (j + 3 < ls.size()) {
							Chunk tmp = ls.get(j + 2);
							if (tmp.txt.equals("of")) { // ignore this case,
														// second NP might
														// related to 'of'
								found = false;
								break;
							} else {
								j += 3;
								idx = j;
								found = true;
								in_loop = true;
								continue;
							}
						} else {
							found = true;
							idx = j + 1;
							break;
						}
					}
					break;
				} // inner loop
				if (found) {// [NP],[NP],...,[and][NP]
					in_loop = false;
					List<Chunk> rm = new ArrayList<Chunk>();
					for (int k = i + 1; k <= idx; k++) {
						next = ls.get(k);
						curr.merge(next);
						rm.add(next);
					}
					for (Chunk c : rm) {
						ls.remove(c);
					}
					curr.is_merged = true;
					continue;// keep the same chunk, migh be has another
								// co-ordination
				}
			}
			i++;
		}
	}

	public int proCount() {
		return proList.size();
	}

	public int trgCount() {
		return trgList.size();
	}

	public boolean containsKey(Word key) {
		if (key.pos >= start && key.pos <= end) {
			return true;
		}
		return false;
	}

	public boolean belongTO(Word trg, Word pr) {
		if (containsKey(pr) && containsKey(trg)) {
			return true;
		}
		return false;
	}

	public boolean belongTO(Word trg, Word pr, Word pr2) {
		if (containsKey(pr) && containsKey(trg)) {
			if (pr2 == null) {
				return true;
			} else if (containsKey(pr2)) {
				return true;
			}
		}
		return false;
	}

	// count number of chunk between two Words tg/pro or tg/tg
	// used to learning rules
	public int countChunks(Word w1, Word w2) {
		int pos1 = getChunkPos(w1.pos);
		int pos2 = getChunkPos(w2.pos);
		if (pos1 < 0 || pos2 < 0) {
			System.out.println("---> not belong to BasicChunk ----> start: " + start + " end: " + end);
			// printChunk();
			System.out.println("V1: " + w1.word + " pos: " + w1.pos + " V2: " + w2.word + " pos: " + w2.pos);
			return -1;
		}
		return Math.abs(pos1 - pos2);
	}

	public Chunk getChunk(int pos) {
		for (Chunk c : chunkList) {
			if (pos >= c.begin && pos <= c.end) {
				return c;
			}
		}
		return null;
	}

	public String getBy_Through(Word tg, Word pr, String tokens[]) {
		int st1, st2;
		st1 = Math.min(tg.pos, pr.pos);
		st2 = Math.max(tg.pos, pr.pos);
		for (int i = st1; i < st2; i++) {
			if (tokens[i].equals("through")) {
				return tokens[i];
			}
		}
		return null;
	}

	public boolean isQualify() {
		return proList.size() > 0 && trgList.size() > 0;
	}

	public String getPrep(Word tg, Word pr) {

		return "";
	}

	public int getChunkPos(int pos) {
		Chunk c;
		if (pos >= start && pos <= end) {
			for (int i = 0; i < chunkList.size(); i++) {
				c = chunkList.get(i);
				if ((pos >= c.begin) && (pos <= c.end)) {
					return i;
				}
			}
		}
		return -1; // not in chunk
	}

	public boolean inChunk(Word trg, Word pro) {
		if (belongTO(trg, pro)) {
			for (Chunk c : chunkList) {
				if (c.inChunk(trg, pro)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Two tg in the same chunk ; for learning rule only
	 * 
	 * @param trg1
	 * @param trg2
	 * @return:true if in the same chunk
	 */
	public boolean inChunkTG(Word trg1, Word trg2) {
		if (belongTO(trg1, trg2)) {
			for (Chunk c : chunkList) {
				if (c.inChunkTG(trg1, trg2)) {
					return true;
				}
			}
		}
		return false;
	}

	public void addChunk(Chunk c) {
		if (chunkList.isEmpty()) {
			start = c.begin;
		}
		end = c.end;
		chunkList.add(c);
		if (c.pros.size() > 0) {
			proList.addAll(c.pros);
		}
		if (c.trigs.size() > 0) {
			trgList.addAll(c.trigs);
		}
	}

	public void addPro(Word pro) {
		proList.add(pro);
	}

	public void addTrg(Word trg) {
		trgList.add(trg);
	}

	public void printChunk() {
		for (Chunk ch : chunkList) {
			System.out.print("[" + ch.type + " " + ch.txt + "]");
		}
		System.out.print(" | ");
	}

	private void splitAND() {
		Chunk c, next, prev;
		int i = 1;
		if (chunkList.size() <= 3) {
			return;
		}
		prev = chunkList.get(0);
		while (i < chunkList.size() - 1) {
			c = chunkList.get(i);
			if (prev.txt.equals("of")) {
				next = chunkList.get(i + 1);
				if (c.txt.contains("and") && next.txt.equals("of")) {
					// split into chunk contains AND
					String s[] = c.txt.split(" ");
					int count = 0;
					while (!s[count].equals("and")) {
						prev.txt += " " + s[count];
						count++;
					}
					prev.end += count;
					Chunk and = new Chunk("O");
					and.txt = "and";
					and.begin = c.begin + count;
					and.end = c.begin + count;
					c.begin += count + 1;
					int idx = c.txt.indexOf("and");
					c.txt = c.txt.substring(idx + 4);
					chunkList.add(i, and);
				}
			}
			prev = c;
			i++;
		}
	}

	/**
	 * Some known patterns/rules: 1. [PRO1 TRIG] of [PRO2]: >> if trg is a reg
	 * event: -> TRG|PRO2|PRO1 (reg event) >> else -> TRG|PRO1 (simple event)
	 * 
	 * 2. [TRG1 TRG2 ] of [PRO] if TRG1 is a reg event then: Event: TRG2|PRO and
	 * TRG1|[TRG2|PRO]
	 * 
	 * 3. [PRO1-TRG1 TRG2] of [PRO2] if TRG1 is a reg event then: Event:
	 * TRG2|PRO and TRG1|[TRG2|PRO2]|PRO1 4. [TRG1] of [TRG2] of [PRO] if TRG1
	 * is reg event then: Event: TRG2|PRO and TRG1|[TRG2|PRO]
	 */
	/**
	 * Operation: 1. Check in_chunk: trg and pro in the same chunk, pro preceeds
	 * trg, trig's POS is a NN if trg's POS is VBx and trg is a reg event -> PRO
	 * is used as cause 2. Find connection: NP prep NP : if trig is NP and not
	 * (in_chunk) use known list of preps to connect with another NP 3. Check
	 * role: if there is more than one trig in chunk, check relationship
	 * (equals, dependent) between trgs two trig are equal if they connected by
	 * CC (i.e. and,or) and has the same POS 4. Divide into simple/binding/reg
	 * event to make extraction process simpler 5. How to store extracted
	 * events? How to assign ID?-> List of extracted event, reference via object
	 * ID
	 */
	@Override
	public String toString() {
		String result = "";
		for (Chunk c : chunkList) {
			result += c.toString();
		}
		return result;
	}

	public static final String ccList[] = { "and", "or", "but not", "as well as" };
	public static final String appoList[] = { "like", "such as", "including" };
	public static final Set<String> appoMap = new HashSet<String>(5);
	public static final Set<String> ccMap = new HashSet<String>(10);

	static {
		BasicChunk.ccMap.addAll(Arrays.asList(BasicChunk.ccList));
		BasicChunk.appoMap.addAll(Arrays.asList(BasicChunk.appoList));
	}
}
