package uk.ac.man.documentparser.dataholders;

public class Journal {
	private String ISSN;
	private String title;
	private String abbrev;

	public Journal(String ISSN, String title, String abbrev){
		this.ISSN = ISSN;
		this.title = title;
		this.abbrev = abbrev;
	}

	/**
	 * @return the iSSN
	 */
	public String getISSN() {
		return ISSN;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the abbrev
	 */
	public String getAbbrev() {
		return abbrev;
	}
}
