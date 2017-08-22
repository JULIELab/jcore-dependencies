package uk.ac.man.entitytagger.matching.matchers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.matching.Matcher;

public class TaxonGrabMatcher extends Matcher {

	private Set<String> dict;

	public TaxonGrabMatcher(File dictFile){
		System.out.print("Loading TaxonGrab dictionary...");
		dict = loadDict(dictFile);
		System.out.println(" done, loaded " + dict.size() + " entries.");
	}

	private Set<String> loadDict(File dictFile) {
		Set<String> res = new HashSet<String>();
		try{
			BufferedReader inStream = new BufferedReader(new FileReader(dictFile));

			String line = inStream.readLine();

			while (line != null){
				res.add(line);
				line = inStream.readLine();
			}

			inStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
		return res;
	}

	@Override
	public List<Mention> match(String text, uk.ac.man.documentparser.dataholders.Document doc) {
		String docID = doc != null ? doc.getID() : null;

		String orgtext = text;

		String f_word="", s_word="", taxon_name_long = "";
		ArrayList<String> taxon_array = new ArrayList<String>();

		text = text.replace(" -\r", " - ");
		text = text.replace(" -\n", " - ");
		text = text.replace("-\r", "");
		text = text.replace("-\n", "");
		text = text.replace("\r", " ");
		text = text.replace("\t", "");

		String[] lines = text.split("\n");

		for (String line : lines){
			line = line.replace(":", " ");
			line = line.replace(";", " ");
			line = line.replace(".", ". ");

			String[] words = line.split("\\s");

			for (String word : words){
				if (word.matches(".*[\\$\\%\\|\\{\\}\\*\\+\\?\\=\\-\\'\\^\\/\\@\\&]|[0-9].*")){ //drop
					f_word = ""; s_word = ""; taxon_name_long = "";
				}

				if (word.matches(".*[(][\\sa-z]+.*"))
					word = word.replaceAll("[()]", "");

				String word_key = word.toLowerCase();
				word_key = word_key.replace(".", "");
				word_key = word_key.replace(",", "");

				if (!taxon_name_long.equals("") && word.matches(".*^[A-Za-z()]{2,}.*")){
					taxon_array.add(taxon_name_long + " " + word);
					taxon_name_long = "";
				}				

				if (dict.contains(word_key)){
					f_word = "";
					s_word = "";
				} else {
					if (word.matches(".*\\A(?:((^[A-Z][a-z]{1,})|(^[A-Z][a-z]?\\.)))\\z.*") && !word.matches(".*var|subsp.*")){
						f_word = word;
						s_word = "";
					} else if (!f_word.equals("") && s_word.equals("")){
						word = word.replace(",", "");
						if (word.matches(".*^[a-z]{3,}.\\z.*")){
							s_word = word;
							taxon_array.add(f_word + " " + s_word);
						} else if (word.matches(".*\\A\\([A-Z][a-z]{3,}\\)\\z.*")){
							s_word = word;
							taxon_array.add("temporary, should be deleted");
						} else {
							f_word = "";
							s_word = "";
						}
					} else if (!f_word.equals("") && !s_word.equals("") && word.length() > 2){
						word = word.replace(",", "");
						if (word.matches("^[A-Za-z()]{2,}")){
							taxon_array.remove(taxon_array.size()-1);
							if (word.matches(".*var|subsp|subg|ssp.*")) {
								taxon_name_long = f_word + " " + s_word + " " + word;
							} else if (!word.contains(".")){
								taxon_array.add(f_word + " " + s_word + " " + word);
							}
						}

						f_word = ""; s_word = "";
					} else {
						f_word = ""; s_word = "";
					}
				}
			}
		}

		ArrayList<Mention> matches = new ArrayList<Mention>();
		Set<String> processed = new HashSet<String>();

		for (String str : taxon_array){
			if (!processed.contains(str)){
				if (str.endsWith("."))
					str = str.substring(0,str.length()-1);
				if (str.endsWith(")") && str.indexOf("(") == -1)
					str = str.substring(0,str.length()-1);

				if  (str.contains(". ")){
					String pstr = str.replace(". ", ". ?");
					
					pstr = pstr.replace("(", "\\(");
					pstr = pstr.replace(")", "\\)");
					pstr = pstr.replace("[", "\\[");
					pstr = pstr.replace("]", "\\]");
					pstr = pstr.replace("{", "\\{");
					pstr = pstr.replace("}", "\\}");
					pstr = pstr.replace(".", "\\.");
					Pattern p = Pattern.compile(pstr);
					java.util.regex.Matcher matcher = p.matcher(orgtext);

					while (matcher.find()){
						int s = matcher.start();
						int e = matcher.end();
						Mention m = new Mention(new String[]{},s,e,orgtext.substring(s,e));
						m.setComment("taxongrab (" + str + ")");
						m.setDocid(docID);
						if (doc == null || doc.isValid(m.getStart(), m.getEnd()))
							matches.add(m);
					}

				} else {
					int x = orgtext.indexOf(str);
					while (x != -1){
						Mention m = new Mention(new String[]{}, x, x+str.length(), str);
						m.setComment("taxongrab (" + str + ")");
						m.setDocid(docID);
						if (doc == null || doc.isValid(m.getStart(), m.getEnd()))
							matches.add(m);
						x = orgtext.indexOf(str,x+1);
					}
				}

				processed.add(str);
			}
		}

		return matches;
	}
}
