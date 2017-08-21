package uk.ac.man.documentparser.input;

public interface InputFactory {
	public DocumentIterator parse(String file);
	public DocumentIterator parse(java.io.File file);
	public DocumentIterator parse(StringBuffer data);
}
