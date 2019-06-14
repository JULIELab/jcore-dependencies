package uk.ac.man.entitytagger.generate;

import martin.common.Tuple;
import martin.common.compthreads.Problem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class ConvertToVariantsProblemIterator implements Iterator<Problem<Tuple<DictionaryEntry,Set<String>>>> {
	private Iterator<DictionaryEntry> iterator;

	public ConvertToVariantsProblemIterator(
			HashMap<String, DictionaryEntry> dict) {
		this.iterator = dict.values().iterator();
	}

	private class ConvertToVariantsProblem implements Problem<Tuple<DictionaryEntry,Set<String>>> { 
		private DictionaryEntry de;

		public ConvertToVariantsProblem(DictionaryEntry de){
			this.de = de;		
		}

		public Tuple<DictionaryEntry,Set<String>> compute() {
			Set<String> variants = de.convertRegexpToVariants();

			if (variants == null)
				throw new IllegalStateException("Detected that regular expression '" + de.getRegexp() + "' is non-finite. Cannot convert this expression to variants."); 

			return new Tuple<DictionaryEntry,Set<String>>(de, variants);
		}
	}

	public boolean hasNext() {
		return iterator.hasNext();
	}

	public Problem<Tuple<DictionaryEntry,Set<String>>> next() {
		return new ConvertToVariantsProblem(iterator.next());
	}

	public void remove() {
		iterator.remove();		
	}
}
