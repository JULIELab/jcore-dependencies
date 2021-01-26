package uk.ac.man.entitytagger.matching.matchers;

import martin.common.Pair;
import martin.common.SentenceSplitter;
import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.matching.Matcher;

import java.util.LinkedList;
import java.util.List;

public class SentenceMatcher extends Matcher {

	@Override
	public List<Mention> match(String text, Document doc) {
		
		SentenceSplitter sp = new SentenceSplitter(text);
		List<Mention> aux = new LinkedList<Mention>();
		String docID = doc != null ? doc.getID() : null;
		
		int i = 0;
		
		for (Pair<Integer> coords : sp){
		
			int s = coords.getX();
			int e = coords.getY();
			String t = text.substring(s,e);
		
			Mention m = new Mention("sentence:" + i++,s,e,t);
			m.setDocid(docID);
			
			aux.add(m);
		}
		
		return aux;
	}
	

}
