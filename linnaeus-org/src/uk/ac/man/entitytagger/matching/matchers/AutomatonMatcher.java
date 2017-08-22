package uk.ac.man.entitytagger.matching.matchers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import dk.brics.automaton.CustomRunAutomaton;

import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.matching.Matcher;

/**
 * Class for matching using an automaton
 * @author Martin
 */
public class AutomatonMatcher extends Matcher {
	private CustomRunAutomaton[] automatons;
	private boolean ignoreCase;
	
	/**
	 * Will load an automaton binary file, and return a created automaton matcher.
	 * @param file
	 * @param ignoreCase2 
	 * @return the automaton matcher
	 */
	public static AutomatonMatcher loadMatcher(File file){
		try{ 
			ObjectInputStream inStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
			
			int size =  inStream.readInt();
			boolean ignoreCase = inStream.readBoolean();	
			
			CustomRunAutomaton[] automatons = new CustomRunAutomaton[size];

			for (int i = 0;i < size; i++){
				automatons[i] = (CustomRunAutomaton) inStream.readObject();
			}

			inStream.close();

			return new AutomatonMatcher(automatons, ignoreCase);
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}

		return null;
	}

	public static AutomatonMatcher[] loadMatchers(File[] files){
		AutomatonMatcher[] matchers = new AutomatonMatcher[files.length];
		for (int i = 0; i < matchers.length; i++)
			matchers[i] = loadMatcher(files[i]);
		return matchers;
	}

	
	/**
	 * Will create an automaton matcher, given a set of automatons and some settings 
	 * @param automatons the automatons used for matching
	 * @param ignoreCase whether to ignore case or not
	 */
	public AutomatonMatcher(CustomRunAutomaton[] automatons, boolean ignoreCase){
		this.automatons = automatons;
		this.ignoreCase = ignoreCase;
	}

	/**
	 * Will store the automatons in the automaton matcher to a file
	 * @param file
	 */
	public void store(File file){
		try{
			ObjectOutputStream dataStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

			dataStream.writeInt(automatons.length);
			dataStream.writeBoolean(ignoreCase);

			for (int i = 0; i < automatons.length; i++){
				dataStream.writeObject(automatons[i]);
			}

			dataStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);			
		}
	}

	/**
	 * Performs matching
	 */
	public List<Mention> match(String text, Document doc) {
		List<Mention> matches = new ArrayList<Mention>();
		String docID = doc != null ? doc.getID() : null;
		
		String matchText = ignoreCase ? text.toLowerCase() : text;
		
		//run over all automatons
		for (int i = 0; i < automatons.length; i++){
			//create a text-specific matcher
			dk.brics.automaton.CustomAutomatonMatcher matcher = automatons[i].newCustomMatcher(matchText);

			//search
			while (matcher.findWithDelimitedID(CustomRunAutomaton.delimiter)){
				int start = matcher.start();
				int end = matcher.end();
				
				ArrayList<String> ids = matcher.getMatchIDs();
				String matchedText = text.substring(start,end);

				Mention match = new Mention(ids.toArray(new String[0]),start,end,matchedText);

				match.setDocid(docID);

				//add found mention if it's valid
				if (Matcher.isValidMatch(text, match) && (doc == null || doc.isValid(start,end)))
					matches.add(match);
			}
		}
		
		return matches;
	}

	@Override
	public int size() {
		return automatons.length;
	}

	/**
	 * @return the automatons
	 */
	 public CustomRunAutomaton[] getAutomatons() {
		 return automatons;
	 }
}