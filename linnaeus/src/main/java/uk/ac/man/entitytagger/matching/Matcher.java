package uk.ac.man.entitytagger.matching;

import martin.common.Tuple;
import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.entitytagger.Mention;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Abstract class describing a matcher, and providing common methods for the available matchers
 * @author Martin
 *
 */
public abstract class Matcher {
	/**
	 * OFF: No disambiguation is performed
	 * ON_EARLIER: Disambiguation is performed by looking at earlier contents in the document
	 * ON_WHOLE: Disambiguation is performed by looking in the whole document
	 */
	public enum Disambiguation {OFF, ON_EARLIER, ON_WHOLE, ON_LONGEST_ONLY}

	/**
	 * Search a given text for mentions
	 * @param text
	 * @param doc the document associated to the text, may be null
	 * @return the mentions found by the matcher in the text
	 */
	public abstract List<Mention> match(String text, Document doc);


	public int size() {
		throw new IllegalStateException("Matcher.size() is not implemented");
	}

	protected static boolean isValidMatch(String text, Mention match){
		int s = match.getStart();
		int e = match.getEnd();

		if (s > 0){
			char c = text.charAt(s-1);
			if (Character.isLetterOrDigit(c) && c != '>' && c != '<'){
				return false; 
			}
		}

		if (e < text.length()-1){
			char c = text.charAt(e);
			if (Character.isLetterOrDigit(c) && c != '>' && c != '<'){
				return false;
			}
		}

		return true;		
	}

	public static List<Mention> disambiguate(String text, List<Mention> matches, Disambiguation mode){
		if (mode == Disambiguation.OFF || matches == null || matches.size() == 0)
			return matches;

		List<Mention> res = new ArrayList<Mention>(matches); 

		//if several matches are overlapping, remove any matches that are smaller than the others
		//(only preserve the ones with maximum length, which are very likely to be the correct hits, e.g. "mice" - "nude mice")
		for (int i = 0; i < res.size(); i++){
			Mention m1 = res.get(i);
			int s1 = m1.getStart();
			int e1 = m1.getEnd();

			for (int j = i+1; j < res.size(); j++){
				Mention m2 = res.get(j);

				int s2 = m2.getStart();
				int e2 = m2.getEnd();

				if (Mention.overlaps(m1, m2)){
					if (e2-s2 < e1-s1){
						res.remove(j);
						j--;
					} else if (e1-s1 < e2-s2){
						res.remove(i);
						i--;
						break;
					}						
				}
			}
		}

		if (mode == Disambiguation.ON_LONGEST_ONLY || matches.size() > 2000)
			return res;

		//		for (Mention m : matches){
		//			if (m.isAmbigous()){
		//				String[] ids = m.getIds();
		//				Set<String> idSet = new HashSet<String>();
		//				for (String s : ids)
		//					if (s != null && !s.equals("0"))
		//						
		//				
		//			}
		//		}


		boolean isClear[] = new boolean[res.size()];

		//determine which matches are overlapping and which are "clear" matches
		for (int i = 0; i < isClear.length; i++)
			isClear[i] = !res.get(i).isAmbigous();

		for (int i = 0; i < isClear.length; i++){
			Mention m1 = res.get(i);

			//determine if m1 overlaps with anything
			for (int j = i+1; j < res.size(); j++){
				Mention m2 = res.get(j);

				if (Mention.overlaps(m1, m2)){
					isClear[i] = false;
					isClear[j] = false;
				}
			}
		}

		//disambiguate by finding clear matches in the document for ambiguous mentions
		for (int i = 0; i < res.size(); i++){
			Mention m1 = res.get(i);
			//if m1 does overlap
			if (m1 != null && !isClear[i]){
				int s1 = m1.getStart();

				for (int j = 0; j < res.size(); j++){
					Mention m2 = res.get(j);

					//find match which does not overlap, has same species, and ((is located earlier) or (located anywhere if we are looking at the whole document)) 
					if (m2 != null && (m2.getStart() < s1 || mode == Disambiguation.ON_WHOLE) && m2.getIds().length > 0 && m1.containsID(m2.getIds()[0]) && isClear[j]){
						isClear[i] = true;
						String id = m2.getIds()[0];
						m1.disambiguate(id);

						//explicit match is found, so remove any matches that overlap with m1
						for (int k = 0; k < res.size(); k++){
							if (res.get(k) != null && Mention.overlaps(m1, res.get(k)) && k != i)
								res.set(k,null);

							//test mode
							if (res.get(k) != null && mode == Disambiguation.ON_EARLIER && m1.getText().equals(res.get(k).getText()) && res.get(k).containsID(id)){
								res.get(k).disambiguate(id);
								isClear[k] = true;
							}
						}
					}
				}
			}
		}

		for (int i = 0; i < res.size(); i++)
			if (res.get(i) == null)
				res.remove(i--);

		return res;
	}

	public static List<Mention> combineMatches(List<Mention> matches){
		ArrayList<Mention> res = new ArrayList<Mention>();

		boolean[] removed = new boolean[matches.size()];

		for (int i = 0; i < matches.size(); i++){
			if (!removed[i]){
				Mention m1 = matches.get(i);
				List<Tuple<String,Double>> sp = new ArrayList<Tuple<String,Double>>();

				String[] ids_ = m1.getIdsWithLineNumbers();
				Double[] probs_ = m1.getProbabilities();

				for (int j = 0;j < ids_.length; j++){
					double d = probs_ != null && probs_[j] != null ? probs_[j] : 0; 
					sp.add(new Tuple<String,Double>(ids_[j],d));
				}				

				for (int j = i+1; j < matches.size(); j++){
					if (matches.get(j).getStart() == m1.getStart() && matches.get(j).getEnd() == m1.getEnd()){

						ids_ = matches.get(j).getIdsWithLineNumbers();
						probs_ = matches.get(j).getProbabilities();

						for (int k = 0;k < ids_.length; k++){
							double d = probs_ != null && probs_[k] != null ? probs_[k] : 0; 
							sp.add(new Tuple<String,Double>(ids_[k],d));
						}				

						removed[j] = true;
					}
				}

				String[] ids = new String[sp.size()];
				Double[] probs = new Double[sp.size()];
				double total = 0;

				for (Tuple<String,Double> t : sp)
					total += t.getB();

				for (int j = 0; j < sp.size(); j++){
					ids[j] = sp.get(j).getA();
					probs[j] = total > 0 ? sp.get(j).getB() / total : 0;
				}

				Mention m = new Mention(ids,m1.getStart(),m1.getEnd(),m1.getText());

				m.setDocid(m1.getDocid());
				m.setComment(m1.getComment());
				m.setProbabilities(probs);
				m.sortIDsByProbabilities();

				res.add(m);
			}
		}

		return res;
	}

	public List<Mention> match(String text) {
		return match(text, (Document)null);
	}

	public List<Mention> match(String text, String documentID){
		if (documentID == null)
			return match(text,(Document)null);
		else {
			Document d = new Document(documentID,null,null,null,text,null,null,null,null,null,null,null,null,null,null);
			return match(text, d);
		}
	}

	protected static void performAcronymResolution(Document doc, String text, List<Mention> matches){
		HashMap<String,Mention> acronyms = new HashMap<String,Mention>();

		String regexp = "[ABCDEFGHIJKLMNOPQRSTUVWXYZ][ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789\\-\\.]+";
		String docID = doc != null ? doc.getID() : null;

		for (int i = 0; i < matches.size(); i++){
			Mention m = matches.get(i);
			int s = m.getStart();
			int e = m.getEnd();

			if (s != -1 && e != -1 && e+1 < text.length()){
				if (text.charAt(e) == ' ' && text.charAt(e+1) == '('){
					int ncp = text.indexOf(')',e+2);

					if (ncp > -1 && ncp < text.length()){
						String abb = text.substring(e+2,ncp);

						if (abb.matches(regexp) && abb.length() < 5){
							Mention a = new Mention(m.getIds(),e+2,ncp,abb);
							a.setComment("abbrev main (" + m.getText() + ")");
							a.setDocid(m.getDocid());

							boolean overlaps = false;

							for (int j = 0; j < matches.size(); j++)
								if (matches.get(j).overlaps(a))
									overlaps = true;

							if (!acronyms.containsKey(abb) && !overlaps)
								acronyms.put(abb,a);
						}
					}
				}
			}
		}

		for (Mention m : acronyms.values()){
			Pattern p = Pattern.compile("\\b" + m.getText() + "\\b");
			java.util.regex.Matcher matcher = p.matcher(text);
			int numMatched = 0;
			while (matcher.find()){
				int s = matcher.start();
				int e = matcher.end();

				if (s > m.getEnd()){
					Mention nm = new Mention(m.getIds(),s,e,text.substring(s,e));
					nm.setComment("abbrev sec");
					nm.setDocid(m.getDocid());
					boolean add = true;

					for (int i = 0; i < matches.size(); i++){
						if (matches.get(i).overlaps(nm)){
							add = false;
							break;
						}
					}

					if (add){
						nm.setDocid(docID);
						matches.add(nm);
						numMatched++;
					}
				}
			}

			if (numMatched > 0){
				m.setDocid(docID);
				matches.add(m);
			}
		}
	}

	public static void detectEnumerations(List<Mention> mentions, String text) {
		int nextGroup = 0;

		Mention.sort(mentions);
		
		Map<Integer,Integer> matchToGroupMap = new HashMap<Integer,Integer>();
		Set<Integer> validGroups = new HashSet<Integer>();

		Pattern cP = Pattern.compile("^[,/] ?$");
		Pattern aP = Pattern.compile("^,? and $");

		for (int i = 0; i < mentions.size() - 1; i++){
			Mention m = mentions.get(i);
			Mention nm = mentions.get(i+1);
			Integer currentGroup = matchToGroupMap.containsKey(i) ? matchToGroupMap.get(i) : null;

			if (m.getEnd() < nm.getStart()){
				String t = text.substring(m.getEnd(), nm.getStart());

				if (cP.matcher(t).matches()){
					if (currentGroup == null)
						currentGroup = nextGroup++;
					matchToGroupMap.put(i, currentGroup);
					matchToGroupMap.put(i+1, currentGroup);
					validGroups.add(currentGroup);
				}

				if (aP.matcher(t).matches()){
					if (currentGroup == null)
						currentGroup = nextGroup++;
					matchToGroupMap.put(i, currentGroup);
					matchToGroupMap.put(i+1, currentGroup);
					validGroups.add(currentGroup);
				}
			} else if (m.getStart() > nm.getEnd()) {
				throw new IllegalStateException("Mentions were not sorted!");
			}
		}

		for (int k : matchToGroupMap.keySet())
			mentions.get(k).setComment("group: " + matchToGroupMap.get(k) + ", valid: " + validGroups.contains(matchToGroupMap.get(k)));
	}


	public List<Mention> match(Document d) {
		return match(d.toString(), d);
	}
}
