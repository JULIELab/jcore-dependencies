package uk.ac.man.entitytagger.matching.matchers;

import martin.common.Misc;
import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.matching.Matcher;
import uk.ac.man.entitytagger.matching.Postprocessor;

import java.io.File;
import java.util.List;
import java.util.Map;

public class MatchPostProcessor extends Matcher {

	private Matcher matcher;
	private Disambiguation mode;
	private boolean abbrevResolution;
	private Postprocessor postProcessor;
	private Map<String,String> mapConvertIDs;

	public MatchPostProcessor(Matcher matcher, Disambiguation mode, boolean abbrevResolution, File ppConvertIDs, Postprocessor postProcessor){
		this.matcher = matcher;
		this.mode = mode;
		this.abbrevResolution = abbrevResolution;
		this.postProcessor = postProcessor;
		this.mapConvertIDs = Misc.loadMap(ppConvertIDs);
	}
	
	@Override
	public List<Mention> match(String text, Document doc) {
		List<Mention> mentions = matcher.match(text,doc);
		
		if (doc == null)
			for (Mention m : mentions)
				m.setDocid("none");
		
		mentions = Matcher.combineMatches(mentions);
		mentions = Matcher.disambiguate(text, mentions, mode);
		
		if (mapConvertIDs != null)
			convertIDs(mentions,mapConvertIDs);

		if (abbrevResolution)
			Matcher.performAcronymResolution(doc, text, mentions);
		
		if (postProcessor != null)
			mentions = postProcessor.postProcess(mentions, text);

		mentions = Misc.sort(mentions);
		Matcher.detectEnumerations(mentions, text);

		if (doc == null)
			for (Mention m : mentions)
				m.setDocid(null);

		return mentions;
	}

	private void convertIDs(List<Mention> mentions, Map<String, String> mapConvertIDs2) {
		for (Mention m : mentions){
			String[] ids = m.getIds();
			for (int i =0; i < ids.length; i++)
				while (mapConvertIDs2.containsKey(ids[i]))
					ids[i] = mapConvertIDs2.get(ids[i]);
		}
	}
}