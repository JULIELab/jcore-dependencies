package uk.ac.man.documentparser.dataholders;

public class ExternalID {
	public enum Source {MEDLINE, PMC, TEXT, ELSEVIER, OTHER}

	private String ID;
	private Source source;
	
	public ExternalID(String ID, Source source){
		this.ID = ID;
		this.source = source;
	}

	/**
	 * @return the iD
	 */
	public String getID() {
		return ID;
	}

	/**
	 * @return the source
	 */
	public Source getSource() {
		return source;
	}
}
