package uk.ac.man.entitytagger.matching.matchers;

import martin.common.Tuple;
import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.matching.Matcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class ACIDMatcher extends Matcher {

	private HashMap<String, Pattern> patterns;
	private HashMap<String, Integer> groups;

	public ACIDMatcher(File patternFile){
		Tuple<HashMap<String, Pattern>, HashMap<String, Integer>> tuple = loadPatterns(patternFile);
		this.patterns = tuple.getA();
		this.groups = tuple.getB();
	}

	@Override
	public List<Mention> match(String text, Document doc) {
		List<Mention> matches = new ArrayList<Mention>();
		String docid = doc != null ? doc.getID() : null;

		for (String key : patterns.keySet()){
			Pattern p = patterns.get(key);
			java.util.regex.Matcher m = p.matcher(text);

			Integer group = this.groups.containsKey(key) ? this.groups.get(key) : null;
			
			while (m.find()){

				int s,e;
				String comment=null;
				
				if (group != null){
					s = m.start(group);
					e = m.end(group);
					comment = text.substring(m.start(), m.end());
				} else {
					s = m.start();
					e = m.end();
				}
				
				String matchtext = text.substring(s,e);
				String id = key + matchtext;
				Mention match = new Mention(new String[]{id},s,e,matchtext);
				match.setDocid(docid);
				match.setComment(comment);
				matches.add(match);
			}
		}

		return matches;
	}

	public static Tuple<HashMap<String, Pattern>,HashMap<String,Integer>> loadPatterns(File patternFile) {
		if (patternFile == null)
			return null;

		HashMap<String,Pattern> patterns = new HashMap<String, Pattern>();
		HashMap<String,Integer> group = new HashMap<String,Integer>();

		try{
			BufferedReader inStream = new BufferedReader(new FileReader(patternFile));

			String line = inStream.readLine();
			while (line != null){
				if (!line.startsWith("#") && line.length() > 0){
					String[] fields = line.split("\\t");
					patterns.put(fields[0], Pattern.compile("\\b" + fields[1] + "\\b"));
					if (fields.length > 2 && fields[2].length() > 0)
						group.put(fields[0],Integer.parseInt(fields[2]));
				}
				line = inStream.readLine();
			}

			inStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}

		return new Tuple<HashMap<String,Pattern>, HashMap<String,Integer>>(patterns,group);
	}
}