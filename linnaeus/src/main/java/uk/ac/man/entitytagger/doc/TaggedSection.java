package uk.ac.man.entitytagger.doc;

import uk.ac.man.documentparser.dataholders.Section;
import uk.ac.man.entitytagger.Mention;

import java.util.ArrayList;

public class TaggedSection {
	private Section original;
	private TaggedSection[] children;
	private ArrayList<Mention> matches;

	public TaggedSection(Section original, TaggedSection[] children, ArrayList<Mention> matches){
		this.original = original;
		this.children = children;
		this.matches = matches;
	}
	
	/**
	 * @return the original
	 */
	public Section getOriginal() {
		return original;
	}

	/**
	 * @return the children
	 */
	public TaggedSection[] getChildren() {
		return children;
	}

	/**
	 * @return the matches
	 */
	public ArrayList<Mention> getMatches() {
		return matches;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Mention> getAllMatches() {
		ArrayList<Mention> res = (ArrayList<Mention>) matches.clone();
		
		if (children != null)
			for (int i = 0; i < children.length; i++)
				if (children[i] != null)
					res.addAll(children[i].getAllMatches());

		return res;
	}

	public Object toHTML() {
		// TODO add code here
		throw new IllegalStateException("not implemented");
	}
}