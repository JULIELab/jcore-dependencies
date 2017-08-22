package uk.ac.man.documentparser.dataholders;

import java.io.Serializable;

public class Section implements Serializable {
	private static final long serialVersionUID = -5798559603277004111L;
	private String title, content;
	private Section[] subSections;

	public Section(String title, String content, Section[] subSections){
		this.title = title;
		this.content = content;
		this.subSections = subSections;
	}

	public static String toString(Section[] sections){
		if (sections == null)
			return "";
		
		String res = "";
		for (Section s : sections)
			if (s != null)
				res += s.toString() + "\n";
		return res;
	}
	
	public String toString(){
		StringBuffer res = new StringBuffer(title + "\n" + content);

		if (subSections != null)
			for (Section s : subSections)
				if (s != null)
					res.append(s.toString() + "\n");

		return res.toString();
	}
	
	public String toHTML(int level){
		StringBuffer sb = new StringBuffer();
		
		String title = this.title != null ? this.title : "[Section title missing]";
		String content = this.content != null ? this.content : "[Section content missing]";
		
		if (level == 0)
			sb.append("<br><b>" + title + "</b><br>\n" + content.replace("\n", "<br>\n"));
		if (level == 1)
			sb.append("<br><i>" + title + "</i><br>\n" + content.replace("\n", "<br>\n"));
		if (level > 1)
			sb.append("<br>" + title + "<br>\n" + content.replace("\n", "<br>\n"));
		
		if (subSections != null)
			for (int i = 0; i < subSections.length; i++)
				sb.append(subSections[i].toHTML(level+1) + "\n");
		
		return sb.toString();
	}
	
	public int getLength() {
		int c = 0, s = 0;
		
		if (content != null)
			c = content.length();
		
		if (subSections != null)
			for (int i = 0; i < subSections.length; i++)
				s  += subSections[i].getLength();
		
		return c + s;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public Section[] getSubSections() {
		return subSections;
	}
	
	public String getContentsRecursive(){
		if (subSections == null || subSections.length == 0){
			return content + "\n";
		}

		String res = content;
		
		for (int i = 0; i < subSections.length; i++)
			res += "\n" + subSections[i].getContentsRecursive();
		
		return res;
	}
}
