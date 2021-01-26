package uk.ac.man.documentparser.input;

import uk.ac.man.documentparser.dataholders.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;

public class PMCIndex implements DocumentIterator {
	private File pmcBaseDir;
	private BufferedReader indexStream;
	private String nextEntry;
	private String[] dtds;

	public PMCIndex(File pmcBaseDir, File indexFile, String[] dtds){
		this.pmcBaseDir = pmcBaseDir;
		this.dtds = dtds;

		try{
			this.indexStream = new BufferedReader(new FileReader(indexFile));
		} catch (FileNotFoundException e){
			System.err.println("The file " + indexFile.getAbsolutePath() + " could not be found. Exiting.");
			System.exit(-1);
		}
		fetchNextLine();
	}

	public Iterator<Document> iterator() {
		return this;
	}

	private void fetchNextLine(){
		try {

			boolean read = true;

			while (read){
				nextEntry = indexStream.readLine();

				read = false;

				if (nextEntry != null && nextEntry.startsWith("#")){
					read = true;
				}
			}
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public Document next() {
		Document doc = null;
		try {
			if (nextEntry != null){
				String[] fields = nextEntry.split(",");

				String basePath = new File(pmcBaseDir,fields[2]).getAbsolutePath();
				
				boolean hasXML = fields[3].equals("1");
				boolean hasXMLBody = fields[4].equals("1");
				boolean hasOCR = fields[5].equals("1");
				boolean hasPTT = fields[6].equals("1");

				doc = new PMC(basePath, dtds, fields[0], hasXML, hasXMLBody, hasOCR, hasPTT).next();
				
				fetchNextLine();

			} else {
				indexStream.close();
			}
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}

		return doc;
	}

	public boolean hasNext() {
		return nextEntry != null;
	}

	public void remove() {
		throw new IllegalStateException("Not implemented.");
	}

	public void skip() {
		fetchNextLine();
	}
}
