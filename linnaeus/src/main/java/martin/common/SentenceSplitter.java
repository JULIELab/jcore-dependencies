package martin.common;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class SentenceSplitter implements Iterator<Pair<Integer>>, Iterable<Pair<Integer>>{
	int c = 1;
	private List<Integer> breaks;

	public SentenceSplitter(String text){
		this(text, true);
	}

	public SentenceSplitter(String text, boolean splitOnLineBreaks){
		BreakIterator bi = BreakIterator.getSentenceInstance();
		bi.setText(text);

		//		this.nextEnd = bi.next();

		this.breaks = new ArrayList<Integer>();
		breaks.add(0);

		while (true){
			int i = bi.next();
			if (i != BreakIterator.DONE)
				breaks.add(i);
			else
				break;			
		}

		int prev = 0;

		//		for (int x : breaks)
		//			System.out.println(x);

		//		System.out.println("-");

		if (splitOnLineBreaks){
			for (int i = 0; i < breaks.size(); i++){
				int x = text.indexOf("\n",prev) + 1;
				int end = breaks.get(i);
				while (x != 0 && x < end){
					if (i > 0 && x - breaks.get(i-1) > 5 && Character.isLetterOrDigit(text.codePointAt(x))){
						breaks.add(i++,x);
					}
					x = text.indexOf("\n",x+1) + 1;
				}
				prev = end;
			}
		}

		//		for (int x : breaks)
		//			System.out.println(x);
	}

	public boolean hasNext() {
		return (c < breaks.size());
	}

	public Pair<Integer> next() {
		if (!hasNext())
			throw new NoSuchElementException();

		return new Pair<Integer>(breaks.get(c-1),breaks.get(c++));
	}

	public void remove() {
		throw new IllegalStateException();
	}

	public Iterator<Pair<Integer>> iterator() {
		return this;
	}

	public static List<Pair<Integer>> toList(String text){
		SentenceSplitter ss = new SentenceSplitter(text);
		List<Pair<Integer>> res = new ArrayList<Pair<Integer>>();
		for (Pair<Integer> p : ss)
			res.add(p);
		return res;
	}

	public static void main(String[] args){
		String text = "When a therapy proves effective , do \n\nclinicians \ntruly know how it works ? Even with a \ntherapy as specific as anti-TNF antibody , it is not clear if the benefit is attributable to simple binding and clearance of TNF-alpha or to binding on the cell surface and subsequent deletion of the activated macrophage . And this is another sentence.";
		SentenceSplitter s = new SentenceSplitter(text);
		for (Pair<Integer> p : s)
			System.out.println(p.toString() + ", '" + text.substring(p.getX(), p.getY()) + "'");
	}
}
