package martin.common.xml;

import martin.common.Pair;

import java.util.ArrayList;

public class Misc {
	public static ArrayList<Pair<Integer>> getSectionCoordinates(String data, String validXPathSections, int start, int end) {
		ArrayList<Pair<Integer>> res = new ArrayList<Pair<Integer>>();

		String[] levels = validXPathSections.split("/");
		String level = levels[0];

		int s2 = data.indexOf("<" + level + ">",start);

		while (s2 != -1){
			int e2 = data.indexOf("</" + level + ">",s2);
			int s2e = data.indexOf(">",s2) + 1;

			if (e2 != -1 && e2 <= end){
				if (levels.length == 1)
					res.add(new Pair<Integer>(s2e,e2));
				else
					res.addAll(getSectionCoordinates(data, validXPathSections.substring(level.length()+1),s2e,e2));
			}

			s2 = data.indexOf("<" + level + ">", e2);
		}

		s2 = data.indexOf("<" + level + " ",start);

		while (s2 != -1){
			int e2 = data.indexOf("</" + level + ">",s2);
			int s2e = data.indexOf(">",s2) + 1;

			if (e2 != -1 && e2 <= end){
				if (levels.length == 1)
					res.add(new Pair<Integer>(s2e,e2));
				else
					res.addAll(getSectionCoordinates(data, validXPathSections.substring(level.length()+1),s2e,e2));
			}

			s2 = data.indexOf("<" + level + " ", e2);
		}

		return res ;
	}

	public static ArrayList<Pair<Integer>> getSectionCoordinates(String data, String[] validXPathSections) {
		ArrayList<Pair<Integer>> res = new ArrayList<Pair<Integer>>();

		for (String loc : validXPathSections)
			res.addAll(getSectionCoordinates(data, loc, 0, data.length()));			

		return res ;
	}
}