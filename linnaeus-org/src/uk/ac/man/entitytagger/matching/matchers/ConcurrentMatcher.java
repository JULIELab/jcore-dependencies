package uk.ac.man.entitytagger.matching.matchers;

import java.util.Iterator;

import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.documentparser.input.DocumentIterator;
import uk.ac.man.entitytagger.doc.TaggedDocument;
import uk.ac.man.entitytagger.matching.MatchOperations;
import uk.ac.man.entitytagger.matching.Matcher;

import martin.common.compthreads.Problem;

/**
 * Class for facilitating concurrent text matching. 
 * This class should be used together with martin.common.compthreads.IteratorBasedMaster.
 * A IteratorBasedMaster object is created based on an object of this class, a thread is created from the master object, and match results can be retrieved from the master object. 
 * Note that results returned from the master will be TaggedDocument if using the single-matcher constructor, and ArrayList<TaggedDocument> if using the multi-matcher constructor.
 * 
 * Sample code:
 * ThreadMatcher tm = new ThreadMatcher(somematcher, somedocuments);
 * IteratorBasedMaster master = new IteratorBasedMaster(tm, somenumberofthreads);
 * new Thread(master).start();
 * while (master.hasNext()){
 *   TaggedDocument td = (TaggedDocument) master.next();
 *   //do something with the tagged document here
 * }
 * 
 * If an array of matchers had been given to the ThreadMatcher constructor, results would have been on the form ArrayList<TaggedDocument> tds = (ArrayList<TaggedDocument> master.next();. 
 * 
 * @author Martin
 */
public class ConcurrentMatcher implements Iterator<Problem<TaggedDocument>> {
	public class MatchProblem implements Problem<TaggedDocument> {
		private Matcher matcher;
		private Document doc;
		public MatchProblem(Matcher matcher, Document doc){
			this.matcher = matcher;
			this.doc = doc;
		}
		public TaggedDocument compute() {
			
			if (doc == null)
				return null;

			return MatchOperations.matchDocument(matcher, doc);
		}
	}

	private Matcher matcher;
	private DocumentIterator documents;
	
	/**
	 * Create an object which will do matching using a single matcher over a number of documents.
	 * @param matcher
	 * @param documents
	 */
	public ConcurrentMatcher(Matcher matcher, DocumentIterator documents){
		this.matcher = matcher;
		this.documents = documents;
	}

	public boolean hasNext() {
		return documents.hasNext();
	}

	public ConcurrentMatcher.MatchProblem next() {
		return new MatchProblem(matcher, documents.next());
	}

	public void remove() {
		throw new IllegalStateException("Not implemented.");		
	}
}
