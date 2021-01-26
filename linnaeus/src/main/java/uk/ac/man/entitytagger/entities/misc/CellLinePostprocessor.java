package uk.ac.man.entitytagger.entities.misc;

import martin.common.ArgParser;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.matching.Postprocessor;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public class CellLinePostprocessor extends Postprocessor {

	private final int PRE = 50;
	private final int POST = 50;

	public CellLinePostprocessor(File stopTermFiles[], File acronymProbFiles[],
			File entityFrequencyFiles[], Logger logger, Map<String,String> comments) {
		super(stopTermFiles, acronymProbFiles, entityFrequencyFiles, comments, logger);
	}

	@Override
	public List<Mention> postProcess(List<Mention> matches, String text) {
		List<Mention> retres = super.postProcess(matches, text);
		retres = filterForCells(retres, text);
		comment(retres, comments);

		return retres;
	}

	private List<Mention> filterForCells(List<Mention> matches, String text) {
		ArrayList<Mention> retres = new ArrayList<Mention>(matches.size());

		Set<String> ok = new HashSet<String>();

		for (Mention m : matches){

			String t = m.getText();

			if (ok.contains(t))
				retres.add(m);
			else {
				boolean isNumber = true;
				for (int i = 0; i < t.length(); i++)
					if (!Character.isDigit(t.charAt(i))){
						isNumber = false;
						break;
					}
				
				if (!isNumber && t.length() > 1){
					if ((t.length() <= 3) || (t.length() == 4 && t.equals(t.toUpperCase()))){
						String s = text.substring(Math.max(0, m.getStart() - PRE), Math.min(text.length(), m.getEnd() + POST));

						if (s.contains("cell")){
							retres.add(m);
							ok.add(t);
						} else {
							//m.setIds(new String[]{"red"});
							//retres.add(m);
						}
					} else {
						retres.add(m);
						ok.add(t);
					}
				}
			}
		}

		return retres;
	}

	public static Postprocessor getPostprocessor(ArgParser ap, Logger logger, Map<String,String> comments){
		if (ap.containsKey("postProcessing")){
			File[] stop = ap.getFiles("ppStopTerms");
			File[] acr = ap.getFiles("ppAcrProbs");
			File[] spf = ap.getFiles("ppSpeciesFreqs");

			return new CellLinePostprocessor(stop,acr,spf,logger, comments);
		} else {	
			logger.info("Not performing post-processing.\n");
			return null;
		}
	}
}
