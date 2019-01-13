package com.wcohen.secondstring.expt;

import com.wcohen.secondstring.*;
import java.util.*;
import java.io.*;

/**
 * Holds data for evaluating a distance metric.
 */

public class MatchData 
{
	private Map sourceLists;
	private ArrayList sourceNames;
	private String filename;
	
	/**
	 * Read match data from a file.  Format should be:
	 * sourceRelation TAB instanceID TAB field1 TAB ... fieldn LF
	 */
	public MatchData(String filename) throws InputFormatException
	{
		this.filename = filename;
		sourceNames = new ArrayList();
		sourceLists = new HashMap();
		try {
	    BufferedReader in = new BufferedReader(new FileReader(filename));
	    String line;
	    int lineNum = 0;
	    while ((line = in.readLine())!=null) {
				lineNum++;
				StringTokenizer tok = new StringTokenizer(line,"\t");
				if (!tok.hasMoreTokens()) 
					throw new InputFormatException(filename,lineNum,"no source");
				String src = tok.nextToken();
				if (!tok.hasMoreTokens()) 
					throw new InputFormatException(filename,lineNum,"no id");
				String id = tok.nextToken();
				if (!tok.hasMoreTokens()) 
					throw new InputFormatException(filename,lineNum,"no text fields");
				String text = tok.nextToken();
				addInstance(src,id,text);
	    }
	    in.close();
		} catch (IOException e) {
	    throw new InputFormatException(filename,0,e.toString());
		}
	}
	
	public MatchData() 
	{
		this.filename = "none";
		sourceNames = new ArrayList();
		sourceLists = new HashMap();
	}

	/** Add a single instance, with given src and id, to the datafile */
	public void addInstance(String src,String id,String text) 
	{
		Instance inst = new Instance(src,id,text);
		ArrayList list = (ArrayList)sourceLists.get(src);
		if (list==null) {
			list = new ArrayList();
			sourceLists.put(src,list);
			sourceNames.add(src);
		}
		list.add(inst);
	}

	/** Number of sources in data set */
	public int numSources() { 
		return sourceNames.size(); 
	}

	/** Get string identifier for i-th source */
	public String getSource(int i) { 
		return (String)sourceNames.get(i); 
	}

	/** Number of records for source with given string id */
	public int numInstances(String src) { 
		return ((ArrayList)sourceLists.get(src)).size();
	}

	/** Get the j-th record for the named source. */
	public Instance getInstance(String src, int j) { 
		return (Instance)((ArrayList)sourceLists.get(src)).get(j); 
	}

	/** Prepare every string in the dataset for computation with the 
	 * distance metric.
	 */
	public void prepare(StringDistance distance) 
	{
		// first, let the distance function accumulate any statistics it might need
		distance.accumulateStatistics( new MatchIterator() );
		// now, transform the data
		for (int i=0; i<numSources(); i++) {
	    String src = getSource(i);
	    for (int j=0; j<numInstances(src); j++) {
				Instance inst = getInstance(src,j);
				inst.prepare(distance);
	    } 
		}
	}
	
	public String getFilename()
	{
		return filename;
	}
	public String toString() 
	{
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<numSources(); i++) {
	    String src = getSource(i);
	    for (int j=0; j<numInstances(src); j++) {
				Instance inst = getInstance(src,j);
				buf.append(inst.toString()+"\n");
	    }
		}
		return buf.toString();
	}
	
	
	/** A single item (aka record, string, etc) to match against others.
	 * An item has an id (for evaluating correctness of a match), a
	 * source (which relation its from), and a text field.  Text is
	 * stored as a StringWrapper so that it can be preprocessed, if
	 * necessary.
	 */
	public static class Instance 
	{
		private final String source;
		private final String id;
		private StringWrapper text;
		public Instance(String source, String id, String text) {
	    this.source = source.trim();
	    this.id = id.trim();
	    this.text = new StringWrapper(text.trim());
		}
		public String getSource() { return source; }
		public String getId() { return id; }
		public StringWrapper getText() { return text; }
		public String toString() { return "[src: '"+source+"' id: '"+id+"' text: '"+text+"']"; }
		public void prepare(StringDistance distance) {
	    text = distance.prepare(text.unwrap());
		}
	}
	
	/** Iterates over all stored StringWrappers */
	public class MatchIterator implements Iterator 
	{
		private int sourceCursor,instanceCursor;
		private String src;  // caches getSource(sourceCursor)

		public MatchIterator() { 
			sourceCursor = 0; 
			instanceCursor = 0; 
			src = MatchData.this.getSource(sourceCursor); 
		}

		/** Not implemented. */
		public void remove() { throw new IllegalStateException("remove not implemented"); }

		/** Return the source of the last StringWrapper. */
		public String getSource() { return src; }

		/** Return the next StringWrapper. */
		public StringWrapper nextStringWrapper() { return (StringWrapper)next(); }

		public boolean hasNext() { return sourceCursor<numSources() && instanceCursor<numInstances(src); }

		/** Returns the next StringWrapper as an object. */
		public Object next() {
			Instance inst = getInstance( src, instanceCursor++ );
			if (instanceCursor>numInstances(src)) {
				sourceCursor++;
				if (sourceCursor<numSources()) 
					src = MatchData.this.getSource(sourceCursor);
			}
			return inst.getText();
		}
	}
	

	/** Signals an incorrectly formatted MatchData file.
	 */
	public static class InputFormatException extends Exception {
		public InputFormatException(String file, int line, String msg) {
	    super("line "+line+" of file "+file+": "+msg);
		}
	}
	
	public static void main(String[] argv) 
	{
		try {
	    System.out.println(new MatchData(argv[0]).toString());
		} catch (Exception e) {
	    e.printStackTrace();
		}
	}
}
