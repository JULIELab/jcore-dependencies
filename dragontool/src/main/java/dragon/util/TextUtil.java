package dragon.util;

/**
 * <p>Text related utilties</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class TextUtil {
	private String text;
	
	public TextUtil(String text){
		this.text =text;
	}
	
	public int countOccurrence(String str){
		return countOccurrence(str,0);
	}
	
	public int countOccurrence(String str, int start){
		int count;
		
		count=0;
		start=text.indexOf(str,start);
		while(start>=0){
			count++;
			start=text.indexOf(str,start+str.length());
		}
		return count;
	}
}
