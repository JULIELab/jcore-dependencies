package uk.ac.man.entitytagger.matching.matchers;

import java.util.ArrayList;
import java.util.List;

import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.doc.TaggedDocument;
import uk.ac.man.entitytagger.matching.Matcher;
import uk.ac.man.entitytagger.matching.Postprocessor;

public class UnionMatcher extends Matcher{
	private List<Matcher> matchers;
	private boolean equalMatchers;
	
	public UnionMatcher(List<Matcher> matchers, boolean equalMatchers){
		this.matchers = matchers;
		this.equalMatchers = equalMatchers;
	}

	public List<Mention> match(String text, Document doc){
		List<Mention> matches = new ArrayList<Mention>();
	
		for (Matcher m : matchers){
			List<Mention> localMatches = m.match(text, doc);
			int matcheslength = matches.size();
	
			//only add matches that do not overlap with previously found matches
			//(i.e. earlier matchers are prioritized over later matchers)
			for (int j = 0; j < localMatches.size(); j++){
				boolean overlapping = false;
				if (!equalMatchers){
					for (int k = 0; k < matcheslength; k++)
						if (localMatches.get(j).overlaps(matches.get(k))){
							overlapping = true;
							break;
						}
				}
				if (!overlapping)
					matches.add(localMatches.get(j));
			}
		}
		
		return matches;
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
