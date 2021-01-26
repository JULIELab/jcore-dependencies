package com.uea.stemmer;

/**
 * <p>Title: UEA Lite Stemmer</p>
 *
 * <p>Description: This is a port of the UEAlite Perl stemmer v1.03, authored by Marie-Claire Jenkins and Dr. Dan J Smith.</p>
 *
 * <p>Copyright: Copyright (c) University of East Anglia 2005</p>
 *
 * <p>Company: University of East Anglia</p>
 *
 * <p>Licence:  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * </p>
 *
 * @author (part of UEAlite stemmer port to Java) Richard Churchill
 * @version 1.00
 */
public class Word {
    String word;
    double ruleno;

    public Word( String word, double ruleno ) {
        this.word = word;
        this.ruleno = ruleno;
    }

	public String getWord() {
		// TODO Auto-generated method stub
		return this.word;
	}
}
