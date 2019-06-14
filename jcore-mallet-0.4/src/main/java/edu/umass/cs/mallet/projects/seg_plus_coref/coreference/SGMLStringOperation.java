package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
// xxx awc - changed to allow tag attributes
public class SGMLStringOperation
{
	static String sgmlRegex = "</?([^>]*)>";
	
	public static String locateField (String tagName, String string) {
	 	Pattern p = Pattern.compile ("<\\s*"+tagName+"[^>]*>([^<]+)<");
		Matcher m = p.matcher (string);
		return m.find() ? m.group(1) : "";
	}
	
	public static ArrayList locateFields (String tagName, String string) {
	 	Pattern p = Pattern.compile ("<\\s*"+tagName+"[^>]*>([^<]+)<");
		Matcher m = p.matcher (string);
		ArrayList ret = new ArrayList();
		while (m.find()) {
			ret.add (m.group(1));
		}
		return ret;
	}

 	public static String locateAndConcatFields (String tagName, String string) {
		ArrayList l = locateFields (tagName, string);
		String ret = "";
		for (int i=0; i < l.size(); i++) {
			ret += l.get(i).toString() + " ";
		}
		return ret;
	}
	
	/** Returns a hashmap from attribute names to values.
	 *  @param tagName name of tag
	 *  @param string input string
	 *  @return hash mapping attribute names to values
	 */
	public static HashMap locateAttributes (String tagName, String string) {
		HashMap h = new HashMap();
	 	Pattern p = Pattern.compile ("<\\s*"+tagName+"([^>]*)>");
		Matcher m = p.matcher (string);
		if (m.find()) {
			String attributes = m.group(1);
			Pattern ap = Pattern.compile ("(\\S+)=\"(\\S+)\"");
			Matcher am = ap.matcher (attributes);
			while (am.find()) {
				h.put (am.group(1), am.group(2));
			}
		}
		return h;
	}
	
	/** Returns a list of hashmaps from attribute names to values. Each
	 *  list item corresponds to a separate occurrence of the tag.
	 *  @param tagName name of tag
	 *  @param string input string
	 *  @return hash mapping attribute names to values
	 */
	public static ArrayList locateAllAttributes (String tagName, String string) {
		ArrayList hashes = new ArrayList(); 
	 	Pattern p = Pattern.compile ("<\\s*"+tagName+"([^>]*)>");
		Matcher m = p.matcher (string);
		while (m.find()) {
			HashMap h = new HashMap();
			String attributes = m.group(1);
			Pattern ap = Pattern.compile ("(\\S+)=\"(\\S+)\"");
			Matcher am = ap.matcher (attributes);
			while (am.find()) {
				h.put (am.group(1), am.group(2));
			}
			hashes.add (h);
		}
		return hashes;
	}

	/*
    This method will return an array list with ALL field values matching a
    given tag
	*/
	public static ArrayList locateFields(String startTag, String endTag, String string) {
		ArrayList fields = new ArrayList();
		int endIndex = string.length();
		int curEnd = -1;
		int curStart = -1;
		
		while (true) {
			String field = locateField(startTag, endTag, string);
			if (field.length() > 0)
				fields.add(field);
			curStart = string.indexOf(startTag);
			curEnd = string.indexOf(endTag,curStart) + endTag.length()-1;
			if (curEnd > -1 && curEnd < string.length())
				string = string.substring(curEnd);
			else
				break;
		}
		return fields;		
	}
	 
	public static String locateField(String startTag, String endTag, String string)    {
		int indexStart = string.indexOf(startTag);
		int indexEnd   = string.indexOf(endTag, indexStart);
		
		if(indexStart == -1 || indexEnd == -1){
			return "";
		}
		else{
            return string.substring(indexStart+startTag.length(), indexEnd);
		}
	}
	
	public static String removeSGMLTags(String sgmlString)
	{
		return sgmlString.replaceAll(sgmlRegex, "");
	}
	
/*    public static String locateField(String tag, String string)    {
        String startTag = "<" + tag + ">";
				String endTag = "</" + tag + ">";
				return locateAndConcatFields(startTag, endTag, string);
    }
*/
/*
	
    public static String locateAndConcatFields(String startTag, String endTag, String string)    {
        ArrayList alist = locateFields(startTag, endTag, string);
        Iterator iter = alist.iterator();
        String field = null;
        while (iter.hasNext()) {
            String s = (String) iter.next();
            if (field == null)
                field = new String(s);
            else
                field += " " + s;
        }
        return (field == null) ? "" : field;
    }


    public static ArrayList locateFields(String tag, String string) {
        String startTag = "<" + tag + ">";
        String endTag = "</" + tag + ">";
        return locateFields(startTag, endTag, string);
    }

	*/
}
