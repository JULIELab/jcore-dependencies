package com.wcohen.secondstring.tokens;

/**
 * An interned version of a string.    
 *
 */

public class Token implements Comparable
{
	private final int index;
	private final String value;
	
	Token(int index,String value) {
		this.index = index;
		this.value = value;
	}
	public String getValue() { return value; }
	public int getIndex() { return index; }
	public int compareTo(Object o) {
		Token t = (Token)o;
		return index - t.index;
	} 
	public int hashCode() { return value.hashCode(); }
	public String toString() { return "[tok "+getIndex()+":"+getValue()+"]"; }
}
