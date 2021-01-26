/*
 * dk.brics.automaton
 * 
 * Copyright (c) 2001-2008 Anders Moeller
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package dk.brics.automaton;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

/**
 * Finite-state automaton with fast run operation. This is a modified version of RunAutomaton from the BRICS automaton package.
 * Modified such that regular expressions can be assigned IDs that are used to differentiate joined automatons.
 * @author Anders M&oslash;ller &lt;<a href="mailto:amoeller@brics.dk">amoeller@brics.dk</a>&gt, Martin Gerner;
 */
public class CustomRunAutomaton extends RunAutomaton implements Serializable {
	private static final long serialVersionUID = -6099832471357070405L;


	public ArrayList<Character> getValidChars(int state){
		ArrayList<Character> list = new ArrayList<Character>();
		int mult = state * points.length;
		
		for (int i = mult; i < mult + points.length; i++){
			if (transitions[i] != -1)
				list.add(points[i - mult]);
		}
		
		return list;
	}

	/**
	 * Retrieves a serialized <code>RunAutomaton</code> located by a URL.
	 * @param url URL of serialized automaton
	 * @exception IOException if input/output related exception occurs
	 * @exception OptionalDataException if the data is not a serialized object
	 * @exception InvalidClassException if the class serial number does not match
	 * @exception ClassCastException if the data is not a serialized <code>RunAutomaton</code>
	 * @exception ClassNotFoundException if the class of the serialized object cannot be found
	 */
	public static CustomRunAutomaton load(URL url) throws IOException, OptionalDataException, ClassCastException, 
													ClassNotFoundException, InvalidClassException {
		return load(url.openStream());
	}

	/**
	 * Retrieves a serialized <code>RunAutomaton</code> from a stream.
	 * @param stream input stream with serialized automaton
	 * @exception IOException if input/output related exception occurs
	 * @exception OptionalDataException if the data is not a serialized object
	 * @exception InvalidClassException if the class serial number does not match
	 * @exception ClassCastException if the data is not a serialized <code>RunAutomaton</code>
	 * @exception ClassNotFoundException if the class of the serialized object cannot be found
	 */
	public static CustomRunAutomaton load(InputStream stream) throws IOException, OptionalDataException, ClassCastException, 
															   ClassNotFoundException, InvalidClassException {
		ObjectInputStream s = new ObjectInputStream(stream);
		return (CustomRunAutomaton) s.readObject();
	}

	/**
	 * This delimiter char is used to separate regular expressions from their IDs. A regular expression on the form
	 * something-delimiter-id will match "something", and will be returned together with the associated id.
	 */
	public static final char delimiter = '\u25B2';

	/**
	 * Constructs a new <code>RunAutomaton</code> from a deterministic
	 * <code>Automaton</code>. If the given automaton is not deterministic,
	 * it is determinized first.
	 * @param a an automaton
	 * @param tableize if true, a transition table is created which makes the <code>run</code> 
	 *                 method faster in return of a higher memory usage
	 */
	public CustomRunAutomaton(Automaton a, boolean tableize) {
		super(a,tableize);
	}

	/**
	 * Creates a new automaton matcher for the given input.
	 * @param s the CharSequence to search
	 * @return A new automaton matcher for the given input
	 */
	public CustomAutomatonMatcher newCustomMatcher(CharSequence s)  {
		return new CustomAutomatonMatcher(s, this);
	}

	/**
	 * Creates a new automaton matcher for the given input.
	 * @param s the CharSequence to search
	 * @param startOffset the starting offset of the given character sequence
	 * @param endOffset the ending offset of the given character sequence
	 * @return A new automaton matcher for the given input
	 */
	public CustomAutomatonMatcher newCustomMatcher(CharSequence s, int startOffset, int endOffset)  {
		return new CustomAutomatonMatcher(s.subSequence(startOffset, endOffset), this);
	}

	/**
	 * @param state
	 * @param delimiter
	 * @return whether a given state is an accept state, if delimiter is used to separate regular expressions and IDs
	 */
	public boolean isAcceptDelimited(int state, char delimiter) {
		return step(state, delimiter) != -1;
	}
}
