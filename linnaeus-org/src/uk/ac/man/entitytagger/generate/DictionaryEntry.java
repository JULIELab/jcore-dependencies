package uk.ac.man.entitytagger.generate;

import java.sql.PreparedStatement;
import java.util.Set;
import java.util.regex.Pattern;
import dk.brics.automaton.RegExp;

/**
 * A class representing a dictionary entry of some object, consisting of a number of regular expression synonyms and an id.
 * This could for instance be a species with id 9606 and synonyms "human" and "man"
 * @author Martin
 *
 */
public class DictionaryEntry {
	private String id;

	private Pattern pattern = null;
	private String regexp = "";
	private String comment = null;

	public DictionaryEntry(String id){
		this.id = id;
	}

	public DictionaryEntry(String id, String comment){
		this.id = id;
		this.comment = comment;
	}

	public String toString(){
		if (comment != null)
			return id + "\t" + this.getRegexp() + "\t" + comment;
		else
			return id + "\t" + this.getRegexp() + "\t";
	}

	public String getId() {
		return id;
	}

	private void reset(){
		this.pattern = null;
	}

	/**
	 * 
	 * @return a compiled regular expression which matches all added synonyms for this entry
	 */
	public Pattern getPattern(){
		if (pattern != null)
			return pattern;

		pattern = Pattern.compile(this.getRegexp());

		return pattern;
	}

	/**
	 * 
	 * @return the regular expression which matches all added synonyms for this entry
	 */
	String getRegexp() {
		return regexp;
	}

	/**
	 * Adds a regular expression pattern to this entry. Any added patterns are valid matches (full regular expression will be (expr1)|(expr2)|...|(exprn))
	 * @param regexp
	 */
	public void addPattern(String regexp){
		reset();

		if (this.regexp.length() > 0)
			this.regexp += "|(" + regexp +  ")";
		else
			this.regexp += "(" + regexp + ")";
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	public Set<String> convertRegexpToVariants() {
		String r = this.getRegexp();
//		System.out.println(r);
		Set<String> res = new RegExp(r).toAutomaton().getFiniteStrings();
		return res;		
	}

	public void saveVariantsToDB(PreparedStatement pstmt, Set<String> stopList) {
		Set<String> names = convertRegexpToVariants();

		try{
			for (String n : names){
				if (stopList == null || (!stopList.contains(n) && !(stopList.contains(n.toLowerCase()) && n.substring(1).equals(n.substring(1).toLowerCase())))){
					pstmt.setString(1, this.id);
					pstmt.setString(2, n);
					if (comment != null)
						pstmt.setString(3, comment);
					else
						pstmt.setNull(3, java.sql.Types.NULL);

					pstmt.addBatch();
				}
			}

			pstmt.executeBatch();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
