package uk.ac.man.entitytagger.matching.matchers;

import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.matching.Matcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RegexpMatcher extends Matcher {
	private Map<String,Pattern> hashmap;
	
	public RegexpMatcher(Map<String,Pattern> hashmap){
		this.hashmap = hashmap;
	}
	
	public List<Mention> match(String text, Document doc) {
		List<Mention> matches = new ArrayList<Mention>();
		Iterator<String> keys = hashmap.keySet().iterator();
		
		while (keys.hasNext()){
			String key = keys.next();
			java.util.regex.Matcher m = hashmap.get(key).matcher(text);
			
			while (m.find()){
				Mention match = new Mention(new String[]{key},m.start(),m.end(),text.substring(m.start(),m.end()));
				if (doc != null)
					match.setDocid(doc.getID());
				if (Matcher.isValidMatch(text, match) && (doc == null || doc.isValid(m.start(),m.end())))
					matches.add(match);
			}
		}
		
		return matches;
	}
	
	@Override
	public int size() {
		return hashmap.size();
	}
}