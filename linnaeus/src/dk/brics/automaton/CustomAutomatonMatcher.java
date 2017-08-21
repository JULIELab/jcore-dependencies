/*
 * dk.brics.automaton - AutomatonMatcher
 *
 * Copyright (c) 2008 John Gibson
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

import java.util.ArrayList;
import java.util.regex.MatchResult;

/**
 * A tool that performs match operations on a given character sequence using
 * a compiled automaton.
 * 
 * Modified such that regular expressions can be assigned IDs that are used to differentiate joined automatons.
 *
 * @author John Gibson &lt;<a href="mailto:jgibson@mitre.org">jgibson@mitre.org</a>&gt;, Martin Gerner
 * @see CustomRunAutomaton#newMatcher(java.lang.CharSequence)
 * @see CustomRunAutomaton#newMatcher(java.lang.CharSequence, int, int)
 */
public class CustomAutomatonMatcher implements MatchResult {

	CustomAutomatonMatcher(final CharSequence chars, final CustomRunAutomaton automaton) {
		this.chars = chars;
		this.automaton = automaton;
	}

	private final CustomRunAutomaton automaton;
	private final CharSequence chars;

	private int matchStart = -1;

	private int matchEnd = -1;
	private ArrayList<String> matchIDs;

	private ArrayList<String> getIDs(int state){
		ArrayList<String> res = new ArrayList<String>();

		ArrayList<Character> validCharacters = automaton.getValidChars(state);

		for (char c : validCharacters){
			int step = automaton.step(state,c);
			
			if (step != -1 && step != state){
				if (automaton.accept[step]){
					res.add(""+c);
				}
				
				ArrayList<String> list = getIDs(step);
				
				for (int i = 0; i < list.size(); i++)
					res.add("" + c + list.get(i));
			}
		}
		
		return res;
	}
	
	/**
	 * Equivalent to find(), but for automatons with IDs that have been separated from the regular expression by delimiter
	 * @param delimiter
	 * @return true if a match (delimited using delimiter) can be found, false otherwise.
	 */
	public boolean findWithDelimitedID(char delimiter) {
		int begin;
		if (getMatchStart() == -2) {
			return false;
		} else if (getMatchStart() == -1) {
			begin = 0;
		} else {
			begin = getMatchStart();
		}

		int match_start;
		int match_end;
		ArrayList<String> match_ids;
		
		if (automaton.isAcceptDelimited(automaton.getInitialState(), delimiter)) {
			throw new IllegalStateException("Automaton matched the empty string");
			/*match_start = begin;
			match_end = begin;
			match_ids = getIDs(automaton.step(automaton.getInitialState(),delimiter));
			setMatch(match_start, match_end, match_ids);
			return true;*/
		} else {
			match_start = -1;
			match_end = -1;
			match_ids = null;
		}

		final int l = getChars().length();
		
		if (getMatchEnd() != -1){
			int p = automaton.getInitialState();
			for (int i = begin; i < l; i += 1) {
				final int new_state = automaton.step(p, getChars().charAt(i));
				if (new_state == -1) {
					break;
				} else if (automaton.isAcceptDelimited(new_state, delimiter) && i+1 > getMatchEnd()) {
					if (match_start == -1) {
						match_start = begin;
					}
					match_end = i;
					match_ids = getIDs(automaton.step(new_state,delimiter));
					setMatch(match_start, match_end + 1, match_ids);
					return true;
				}
				p = new_state;
			}
			begin++;
		}
		
		while (begin < l) {
			int p = automaton.getInitialState();
			for (int i = begin; i < l; i += 1) {
				final int new_state = automaton.step(p, getChars().charAt(i));
				if (new_state == -1) {
					break;
				} else if (automaton.isAcceptDelimited(new_state, delimiter)) {
					if (match_start == -1) {
						match_start = begin;
					}
					match_end = i;
					match_ids = getIDs(automaton.step(new_state,delimiter));
					setMatch(match_start, match_end + 1, match_ids);
					return true;
				}
				p = new_state;
			}
			begin += 1;
		}
		
		if (match_start != -1) {
			setMatch(match_start, match_end, match_ids);
			return true;
		} else {
			setMatch(-2, -2, null);
			return false;
		}
	}

	/**
	 * Find the next matching subsequence of the input.
	 * <br />
	 * This also updates the values for the {@code start}, {@code end}, and
	 * {@code group} methods.
	 *
	 * @return {@code true} if there is a matching subsequence.
	 */
	public boolean find() {
		int begin;
		if (getMatchStart() == -2) {
			return false;
		} else if (getMatchStart() == -1) {
			begin = 0;
		} else {
			begin = getMatchEnd();
		}

		int match_start;
		int match_end;
		if (automaton.isAccept(automaton.getInitialState())) {
			match_start = begin;
			match_end = begin;
		} else {
			match_start = -1;
			match_end = -1;
		}
		int l = getChars().length();
		while (begin < l) {
			int p = automaton.getInitialState();
			for (int i = begin; i < l; i += 1) {
				final int new_state = automaton.step(p, getChars().charAt(i));
				if (new_state == -1) {
					break;
				} else if (automaton.isAccept(new_state)) {
					if (match_start == -1) {
						match_start = begin;
					}
					match_end = i;
				}
				p = new_state;
			}
			if (match_start != -1) {
				setMatch(match_start, match_end + 1);
				return true;
			}
			begin += 1;
		}
		if (match_start != -1) {
			setMatch(match_start, match_end + 1);
			return true;
		} else {
			setMatch(-2, -2);
			return false;
		}
	}

	private void setMatch(final int matchStart, final int matchEnd, final ArrayList<String> matchIDs) throws IllegalArgumentException {
		if (matchStart > matchEnd) {
			throw new IllegalArgumentException("Start must be less than or equal to end: " + matchStart + ", " + matchEnd);
		}
		if (matchIDs != null && matchIDs.size() == 0)
			throw new IllegalStateException("matchIDs.size() == 0");
		this.matchStart = matchStart;
		this.matchEnd = matchEnd;
		this.matchIDs = matchIDs;
	}

	private void setMatch(final int matchStart, final int matchEnd) throws IllegalArgumentException {
		if (matchStart > matchEnd) {
			throw new IllegalArgumentException("Start must be less than or equal to end: " + matchStart + ", " + matchEnd);
		}
		this.matchStart = matchStart;
		this.matchEnd = matchEnd;
	}

	private int getMatchStart() {
		return matchStart;
	}

	private int getMatchEnd() {
		return matchEnd;
	}

	private CharSequence getChars() {
		return chars;
	}

	/**
	 * Returns the offset after the last character matched.
	 *
	 * @return The offset after the last character matched.
	 * @throws IllegalStateException if there has not been a match attempt or
	 *  if the last attempt yielded no results.
	 */
	public int end() throws IllegalStateException {
		matchGood();
		return matchEnd;
	}

	/**
	 * Returns the offset after the last character matched of the specified
	 * capturing group.
	 * <br />
	 * Note that because the automaton does not support capturing groups the
	 * only valid group is 0 (the entire match).
	 *
	 * @param group the desired capturing group.
	 * @return The offset after the last character matched of the specified
	 *  capturing group.
	 * @throws IllegalStateException if there has not been a match attempt or
	 *  if the last attempt yielded no results.
	 * @throws IndexOutOfBoundsException if the specified capturing group does
	 *  not exist in the underlying automaton.
	 */
	public int end(final int group) throws IndexOutOfBoundsException, IllegalStateException {
		onlyZero(group);
		return end();
	}

	/**
	 * Returns the subsequence of the input found by the previous match.
	 *
	 * @return The subsequence of the input found by the previous match.
	 * @throws IllegalStateException if there has not been a match attempt or
	 *  if the last attempt yielded no results.
	 */
	public String group() throws IllegalStateException {
		matchGood();
		return chars.subSequence(matchStart, matchEnd).toString();
	}

	/**
	 * Returns the subsequence of the input found by the specified capturing
	 * group during the previous match operation.
	 * <br />
	 * Note that because the automaton does not support capturing groups the
	 * only valid group is 0 (the entire match).
	 *
	 * @param group the desired capturing group.
	 * @return The subsequence of the input found by the specified capturing
	 *  group during the previous match operation the previous match. Or
	 *  {@code null} if the given group did match.
	 * @throws IllegalStateException if there has not been a match attempt or
	 *  if the last attempt yielded no results.
	 * @throws IndexOutOfBoundsException if the specified capturing group does
	 *  not exist in the underlying automaton.
	 */
	public String group(final int group) throws IndexOutOfBoundsException, IllegalStateException {
		onlyZero(group);
		return group();
	}

	/**
	 * Returns the number of capturing groups in the underlying automaton.
	 * <br />
	 * Note that because the automaton does not support capturing groups this
	 * method will always return 0.
	 *
	 * @return The number of capturing groups in the underlying automaton.
	 */
	public int groupCount() {
		return 0;
	}

	/**
	 * Returns the offset of the first character matched.
	 *
	 * @return The offset of the first character matched.
	 * @throws IllegalStateException if there has not been a match attempt or
	 *  if the last attempt yielded no results.
	 */
	public int start() throws IllegalStateException {
		matchGood();
		return matchStart;
	}

	/**
	 * Returns the offset of the first character matched of the specified
	 * capturing group.
	 * <br />
	 * Note that because the automaton does not support capturing groups the
	 * only valid group is 0 (the entire match).
	 *
	 * @param group the desired capturing group.
	 * @return The offset of the first character matched of the specified
	 *  capturing group.
	 * @throws IllegalStateException if there has not been a match attempt or
	 *  if the last attempt yielded no results.
	 * @throws IndexOutOfBoundsException if the specified capturing group does
	 *  not exist in the underlying automaton.
	 */
	public int start(int group) throws IndexOutOfBoundsException, IllegalStateException {
		onlyZero(group);
		return start();
	}

	/** Helper method that requires the group argument to be 0. */
	private static void onlyZero(final int group) throws IndexOutOfBoundsException {
		if (group != 0) {
			throw new IndexOutOfBoundsException("The only group supported is 0.");
		}
	}

	/** Helper method to check that the last match attempt was valid. */
	private void matchGood() throws IllegalStateException {
		if ((matchStart < 0) || (matchEnd < 0)) {
			throw new IllegalStateException("There was no available match.");
		}
	}

	/**
	 * @return the IDs of the matches from the last call to find()
	 */
	public ArrayList<String> getMatchIDs() {
		matchGood();
		return matchIDs;
	}
}
