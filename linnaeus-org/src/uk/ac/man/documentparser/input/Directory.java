package uk.ac.man.documentparser.input;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import uk.ac.man.documentparser.dataholders.Document;

public class Directory implements DocumentIterator {
	private File[] contents;
	private int nextDocIndex = 0;
	private Document nextDocument;
	private String acceptedSuffix;
	private boolean recursive;
	private InputFactory factory;

	private DocumentIterator currentSubDirectory = null;
	private DocumentIterator currentFileIterator = null;

	public Directory(File dir, InputFactory factory, String acceptedSuffix, boolean recursive){
		this.acceptedSuffix = acceptedSuffix;
		this.recursive = recursive;
		this.factory = factory;

		this.contents = dir.listFiles();
		Arrays.sort(this.contents);

		if (dir == null || !dir.isDirectory()){
			System.err.println("You did not specify a valid directory path. This can be caused by:" +
					" 1) no path was specified, or 2) the path you specified does not exist, or" +
					" 3) the path you specified does not represent a directory. The program will now exit.");
			System.exit(-1);
		}
		
		fetchNext();
	}

	private void fetchNext(){
		nextDocument = null;

		if (currentSubDirectory != null){
			nextDocument = currentSubDirectory.next();

			if (!currentSubDirectory.hasNext())
				currentSubDirectory = null;

		} else if (currentFileIterator != null){
			nextDocument = currentFileIterator.next();

			if (!currentFileIterator.hasNext())
				currentFileIterator = null;

		} else if (nextDocIndex < contents.length) {
			File item = contents[nextDocIndex++];

			if (item.isDirectory()){
				if (recursive){
					currentSubDirectory = new Directory(item,factory,acceptedSuffix,recursive);
					if (!currentSubDirectory.hasNext())
						currentSubDirectory = null;

					fetchNext();
				} else {
					fetchNext();
				}
			} else if (item.isFile()){
				if ((acceptedSuffix == null || item.getAbsolutePath().endsWith(acceptedSuffix))){
					this.currentFileIterator = factory.parse(item);
					if (!currentFileIterator.hasNext())
						currentFileIterator = null;

					fetchNext();
				} else {
					fetchNext();
				}
			} 	
		}
	}

	public void skip() {
		if (!hasNext())
			throw new NoSuchElementException();

		fetchNext();
	}

	public boolean hasNext() {
		return nextDocument != null;
	}

	public Document next() {
		if (nextDocument == null)
			throw new NoSuchElementException("no more documents available");

		Document res = nextDocument;
		fetchNext();

		return res;
	}

	public void remove() {
		throw new IllegalStateException("not implemented");
	}

	public Iterator<Document> iterator() {
		return this;
	}
}
