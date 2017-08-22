package uk.ac.man.entitytagger.matching;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import uk.ac.man.entitytagger.Mention;

public class Postprocessor {

	private Map<String,List<Pattern>> stopTerms;
	private Map<String,HashMap<String,Double>> acronymProbabilities;
	private Map<String,Integer> entityFrequencies;
	protected Map<String, String> comments;

	public Postprocessor(File stopTermFile[], File[] acronymProbFile, File[] entityFrequencyFile, Map<String,String> comments, Logger logger){

		if (logger != null)
			logger.info("Loading postprocessing data files... ");

		this.comments = comments;
		this.stopTerms = stopTermFile != null ? loadStopTerms(stopTermFile) : null;
		this.acronymProbabilities = acronymProbFile != null ? loadAcronymProbabilities(acronymProbFile) : null;
		this.entityFrequencies = entityFrequencyFile != null ? loadEntityFrequencies(entityFrequencyFile) : null;

		if (logger != null)
			logger.info(" done (s: " +
					(stopTerms != null ? stopTerms.size() : 0) + ", a: " + 
					(acronymProbabilities != null ? acronymProbabilities.size() : 0) + ", f: " + 
					(entityFrequencies != null ? entityFrequencies.size() : 0) + ", c: " + 
					(comments != null ? comments.size() : 0)+ 
			").\n");
	}

	public Postprocessor(InputStream[] stopTermStreams, InputStream[] acronymProbStreams, InputStream[] entityFrequencyStreams, Map<String,String> comments, Logger logger){

		if (logger != null)
			logger.info("Loading postprocessing data files... ");

		this.comments = comments;
		this.stopTerms = stopTermStreams != null ? loadStopTerms(stopTermStreams) : null;
		this.acronymProbabilities = acronymProbStreams != null ? loadAcronymProbabilities(acronymProbStreams) : null;
		this.entityFrequencies = entityFrequencyStreams != null ? loadEntityFrequencies(entityFrequencyStreams) : null;

		if (logger != null)
			logger.info(" done (s: " +
					(stopTerms != null ? stopTerms.size() : 0) + ", a: " + 
					(acronymProbabilities != null ? acronymProbabilities.size() : 0) + ", f: " + 
					(entityFrequencies != null ? entityFrequencies.size() : 0) + ", c: " + 
					(comments != null ? comments.size() : 0)+ 
			").\n");
	}
	
	private Map<String, Integer> loadEntityFrequencies(File[] entityFreqFiles) {
		try{
			InputStream[] streams = new InputStream[entityFreqFiles.length];
			
			for (int i = 0; i < streams.length; i++)
				streams[i] = new FileInputStream(entityFreqFiles[i]);
			
			Map<String, Integer> res = loadEntityFrequencies(streams);
			
			for (int i = 0; i < streams.length; i++)
				streams[i].close();
			
			return res;			
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(0);
		}
		
		return null;	
	}

	private Map<String, Integer> loadEntityFrequencies(InputStream[] entityFrequencyStreams) {
		if (entityFrequencyStreams == null || entityFrequencyStreams.length == 0)
			return null;

		Map<String, Integer> retres = new HashMap<String, Integer>();

		try{
			for (InputStream entityFrequencyFile : entityFrequencyStreams){
				BufferedReader inStream = new BufferedReader(new InputStreamReader(entityFrequencyFile));

				String line = inStream.readLine();
				while (line != null){

					if (!line.startsWith("#")){
						String[] fields = line.split("\\t");
						retres.put(fields[0], Integer.parseInt(fields[1]));
					}

					line = inStream.readLine();
				}

				inStream.close();
			}
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}

		return retres;
	}

	
	private HashMap<String, HashMap<String, Double>> loadAcronymProbabilities(
			File[] acronymProbFiles) {
		try{
			InputStream[] streams = new InputStream[acronymProbFiles.length];
			
			for (int i = 0; i < streams.length; i++)
				streams[i] = new FileInputStream(acronymProbFiles[i]);
			
			HashMap<String,HashMap<String,Double>> res = loadAcronymProbabilities(streams);
			
			for (int i = 0; i < streams.length; i++)
				streams[i].close();
			
			return res;			
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(0);
		}
		
		return null;	
	}
		
	private HashMap<String, HashMap<String, Double>> loadAcronymProbabilities(
			InputStream[] acronymProbStreams) {

		if (acronymProbStreams == null || acronymProbStreams.length == 0)
			return null;

		HashMap<String, HashMap<String, Double>> retres = new HashMap<String, HashMap<String,Double>>();

		try{
			for (InputStream acronymProbFile : acronymProbStreams){
				BufferedReader inStream = new BufferedReader(new InputStreamReader(acronymProbFile));

				String line = inStream.readLine();
				while (line != null){

					if (!line.startsWith("#")){
						String[] fields = line.split("\\t");

						double d = Double.parseDouble(fields[2]);

						if (!retres.containsKey(fields[0]))
							retres.put(fields[0], new HashMap<String, Double>());

						retres.get(fields[0]).put(fields[1], d);
					}

					line = inStream.readLine();
				}

				inStream.close();
			}
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}

		return retres;
	}

	private HashMap<String, List<Pattern>> loadStopTerms(File[] entityFreqFiles) {
		try{
			InputStream[] streams = new InputStream[entityFreqFiles.length];
			
			for (int i = 0; i < streams.length; i++)
				streams[i] = new FileInputStream(entityFreqFiles[i]);
			
			HashMap<String, List<Pattern>> res = loadStopTerms(streams);
			
			for (int i = 0; i < streams.length; i++)
				streams[i].close();
			
			return res;			
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(0);
		}
		
		return null;	
	}
	
	private HashMap<String, List<Pattern>> loadStopTerms(InputStream[] stopTermStreams) {
		if (stopTermStreams == null || stopTermStreams.length == 0)
			return null;

		HashMap<String, List<Pattern>> retres = new HashMap<String, List<Pattern>>();
		try{
			for (InputStream stopTermFile : stopTermStreams){
				BufferedReader inStream = new BufferedReader(new InputStreamReader(stopTermFile));

				String line = inStream.readLine();
				while (line != null){
					if (!line.startsWith("#") && line.length() > 0){
						String[] fields = line.split("\\t");

						String id = fields[0];

						if (!retres.containsKey(id)){
							retres.put(id,new ArrayList<Pattern>());
						}

						if (fields.length > 1)
							retres.get(fields[0]).add(Pattern.compile("^" + fields[1] + "$"));
						else{
							System.err.println("Stop-term line \"" + line + "\" does not contain enough fields.");
							System.exit(-1);
						}
					}

					line = inStream.readLine();
				}

				inStream.close();
			}
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}

		return retres;
	}

	public List<Mention> postProcess(List<Mention> matches, String text){
		List<Mention> retres = filterByStopTerms(matches);

		if (comments != null)
			comment(retres, comments);

		removeLineNumbers(matches);

		setProbs(retres);

		return retres;
	}

	private void removeLineNumbers(List<Mention> matches) {
		for (Mention m : matches){
			String[] ids = m.getIds();
			
			Set<String> idSet = new HashSet<String>();

			for (int i = 0; i < ids.length; i++){
				/*String[] fields = ids[i].split(":");
				if (fields.length > 3)
					ids[i] = fields[0] + ":" + fields[1] + ":" + fields[2];*/
				String[] fields = ids[i].split("\\" + Mention.COMMENT_SEPARATOR);
				idSet.add(fields[0]);					
			}

			m.setIds(idSet.toArray(new String[0]));			
		}		
	}

	public void comment(List<Mention> matches, Map<String, String> comments2) {
		if (comments2 == null)
			return;

		for (Mention m : matches){
			String id = m.getMostProbableIDWithIdLine();
			if (comments2.containsKey(id)){
				String c = m.getComment();
				if (c == null || c.length() == 0)
					c = comments2.get(id);
				else
					c += ", " + comments2.get(id);

				m.setComment(c);
			}			
		}		
	}

	public void setProbs(List<Mention> matches) {
		if (entityFrequencies == null && acronymProbabilities == null)
			return;

		for (Mention m : matches){
			String[] ids = m.getIds();
			String term = m.getText();

			Double[] probabilities = new Double[ids.length];
			int freqSum = 0;

			if (ids.length > 1){
				for (String s : ids)
					if (entityFrequencies != null && entityFrequencies.containsKey(s))
						freqSum += entityFrequencies.get(s);
			}

			for (int i = 0; i < ids.length; i++){
				String s = ids[i];
				if (acronymProbabilities != null && acronymProbabilities.containsKey(s) && acronymProbabilities.get(s).containsKey(term))
					probabilities[i] = acronymProbabilities.get(s).get(term);
				else
					if (entityFrequencies != null && ids.length > 1 && entityFrequencies.containsKey(s)){
						probabilities[i] = ((double)entityFrequencies.get(s))/((double)freqSum);
					}
			}
			
			m.setProbabilities(probabilities);
			m.sortIDsByProbabilities();
		}		
	}

	private List<Mention> filterByStopTerms(List<Mention> matches) {
		List<Mention> retres = new ArrayList<Mention>(matches.size());
		List<Pattern> generalPatterns = (stopTerms != null && stopTerms.containsKey("*")) ? stopTerms.get("*") : null;

		for (Mention m : matches){
			String[] ids = m.getIds();
			String term = m.getText();
			boolean remove = false;

			for (String s : ids){
				if (stopTerms != null && stopTerms.containsKey(s)){
					List<Pattern> patterns = stopTerms.get(s);
					for (Pattern p : patterns)
						if (p.matcher(term).matches()){
							remove = true;
							break;
						}
				}
				if (generalPatterns != null)
					for (Pattern p : generalPatterns){
						if (p.matcher(term).matches()){
							remove = true; 
							break;
						}
					}


				if (remove)
					break;
			}

			if (!remove)
				retres.add(m);
		}

		return retres;
	}
}
