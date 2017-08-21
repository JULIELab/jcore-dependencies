package uk.ac.man.entitytagger.matching.matchers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.generate.GenerateMatchers;
import uk.ac.man.entitytagger.matching.Matcher;

public class DuplicationMatcher extends Matcher {
	private Matcher matcher;

	public DuplicationMatcher(Matcher matcher){
		this.matcher = matcher;
	}

	@Override
	public List<Mention> match(String text, Document doc) {
		List<Mention> mentions = matcher.match(text,doc);
		mentions = Matcher.combineMatches(mentions);
		
		List<Mention> aux = new ArrayList<Mention>();
		
		Map<String,Set<String>> termToMention = new HashMap<String,Set<String>>();
		
		for (Mention m : mentions){
			if  (!termToMention.containsKey(m.getText()))
				termToMention.put(m.getText(),new HashSet<String>());
			for (String id : m.getIds()){
				termToMention.get(m.getText()).add(id);
			}
		}
		
		for (String term : termToMention.keySet()){
			Pattern p = Pattern.compile("\\b" + GenerateMatchers.escapeRegexp(term) + "\\b");
			java.util.regex.Matcher matcher = p.matcher(text);
			
			while (matcher.find()){
				int s = matcher.start();
				
				Mention m = new Mention(termToMention.get(term).toArray(new String[0]),s,s+term.length(),term);
				m.setDocid(doc.getID() != null ? doc.getID() : null);
				
				Mention n = Mention.findClosestMention(mentions, s);
				if (m.overlaps(n)){
					n = n.clone();
					n.setStart(s);
					n.setEnd(s+term.length());
					aux.add(n);
				} else {
					aux.add(m);
				}
			}
		}
		
		return aux;
	}
}