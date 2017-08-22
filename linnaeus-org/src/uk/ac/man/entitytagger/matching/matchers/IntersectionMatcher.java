package uk.ac.man.entitytagger.matching.matchers;

import java.util.ArrayList;
import java.util.List;

import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.doc.TaggedDocument;
import uk.ac.man.entitytagger.matching.Matcher;

public class IntersectionMatcher extends Matcher{
	private List<Matcher> matchers;
	
	public IntersectionMatcher(List<Matcher> matchers){
		this.matchers = matchers;
	}

	public List<Mention> match(String text, Document doc){
		List<Mention> mentions = new ArrayList<Mention>();

		List<List<Mention>> allMentions = new ArrayList<List<Mention>>();

		for (Matcher m : matchers)
			allMentions.add(m.match(text, doc));
		
		List<Mention> firstMentions = allMentions.remove(0);
				
		for (Mention m : firstMentions){
			boolean retain = true;
			for (List<Mention> l : allMentions){
				boolean found = false;
				if (retain)
					for (Mention n : l)
						if (m.overlaps(n))
							found=true;
				
				retain = retain && found;
			}
			
			if (retain)
				mentions.add(m);					
		}
		
		return mentions;
	}

	public TaggedDocument matchDocument(Document doc){
		String text = doc.toString();
	
		List<Mention> matches = match(text, doc);
	
		return new TaggedDocument(doc,null,null,matches,text.toString());
	}

	@Override
	public int size() {
		return matchers.size();
	}
}
