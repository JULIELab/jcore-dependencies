package com.uea.stemmer;

import java.io.Serializable;

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
 * @author Marie-Claire Jenkins, Dr. Dan J Smith, this port to Java by Richard Churchill
 * @version 1.03
 */
public class UEALite implements Serializable {

    /**
	 * added by JULIE lab for the work with JNET
	 */
	private static final long serialVersionUID = 2960712918243165711L;
	private int maxWordLength = "deoxyribonucleicacid".length(); // or some other suitable value, e.g antidisestablishmentarianism
    private int maxAcronymLength = "CAVASSOO".length(); // or some other suitable value

    /************************************************************************
     * Constructor
    ************************************************************************/
    public UEALite() { }

    public UEALite( int wordLength, int acronymLength ) {
        maxWordLength = wordLength;
        maxAcronymLength = acronymLength;
    }
    /***********************************************************************/

    public int getMaxWordLength() {
        return maxWordLength;
    }

    public int getMaxAcronymLength() {
        return maxAcronymLength;
    }

    public boolean setMaxWordLength( int length ) {
        maxWordLength = length;
        return true;
    }

    public boolean setMaxAcronymLength( int length ) {
        maxAcronymLength = length;
        return true;
    }

    public Word stem( String word ) {
        String stemmed_word = word;
        double ruleno = 0;

        if( this.isProblemWord( word ) ) return new Word( word, 90 );
        if( word.length() > maxWordLength ) return new Word( word, 95 );
        if ( word.indexOf( "'" ) != -1 ) {			                              // word had apostrophe(s) - remove and continue 94
            if( word.matches( "^.*'[sS]$" ) ) stemmed_word = this.remove( word, "'s" );       // remove possessive singular
            if( word.matches( "^.*'$")  ) stemmed_word = this.remove( word, "'" );            // remove possessive plural
            stemmed_word = stemmed_word.replaceAll( "n't", "not" );                           // expand contraction n't
            stemmed_word = stemmed_word.replaceAll( "'ve", "have" );                          // expand contraction 've
            stemmed_word = stemmed_word.replaceAll( "'re", "are" );                           // expand contraction 're
            stemmed_word = stemmed_word.replaceAll( "'m", "am" );                             // expand contraction I'm

            return new Word( stemmed_word, 94 );
	}

        if( word.matches( "^\\d+$" ) ) { return new Word( word, 90.3 ); }
        else if( word.matches( "^\\w+-\\w+$" ) ) { return new Word( word, 90.2 ); }
        else if( word.matches( "^.*-.*$" ) ) { return new Word( word, 90.1 ); }
        else if( word.matches( "^.*_.*$" ) ) { return new Word( word, 90 ); }
        else if( word.matches( "^\\p{Upper}+s$" ) ) { return new Word( this.remove( word, "s" ), 91.1 ); }
        else if( word.matches( "^\\p{Upper}+$" ) ) { return new Word( word, 91 ); }
        else if( word.matches( "^\\p{Upper}+$" ) ) { return new Word( word, 91 ); }
        else if( word.matches( "^.*\\p{Upper}.*\\p{Upper}.*$" ) ) { return new Word( word, 92 ); }
        else if( word.matches( "^\\p{Upper}{1}.*$" ) ) { return new Word( word, 92 ); }

        // should word be stemmed followed by call to private method with text to stem
        if (word.endsWith("aceous")) { stemmed_word = this.remove(word, "aceous"); ruleno = 1; } // 1
        else if (word.endsWith("ces")) { stemmed_word = this.remove(word, "s"); ruleno = 2; }  // 2
        else if (word.endsWith("cs")) { ruleno = 3; } // 3
        else if (word.endsWith("sis")) { ruleno = 4; } // 4
        else if (word.endsWith("tis")) { ruleno = 5; } // 5
        else if (word.endsWith("ss")) { ruleno = 6; } // 6


        /***********************************************************************************************************/
        // plural change - this differs from Perl v1.03
        else if (word.endsWith("eed")) { ruleno = 7; } // 7
        else if (word.endsWith("eeds")) { stemmed_word = this.remove( word, "s" ); ruleno = 7; } // 7
        /***********************************************************************************************************/

        else if (word.endsWith("ued")) { stemmed_word = this.remove(word, "d"); ruleno = 8; } // 8
        else if (word.endsWith("ues")) { stemmed_word = this.remove(word, "s"); ruleno = 9; } // 9
        else if (word.endsWith("ees")) { stemmed_word = this.remove(word, "s"); ruleno = 10; } // 10
        else if (word.endsWith("iases")) { stemmed_word = this.remove(word, "es"); ruleno = 11.4; } // 11.4
        else if (word.endsWith("uses")) { stemmed_word = this.remove(word, "s"); ruleno = 11.3; } // 11.3
        else if (word.endsWith("sses")) { stemmed_word = this.remove(word, "es"); ruleno = 11.2; } // 11.2
        else if (word.endsWith("eses")) { stemmed_word = this.remove(word, "es"); stemmed_word += "is"; ruleno = 11.1; } //11.1
        else if (word.endsWith("ses")) { stemmed_word = this.remove(word, "s"); ruleno = 11; } // 11
        else if (word.endsWith("tled")) { stemmed_word = this.remove(word, "d"); ruleno = 12.5; } // 12.5
        else if (word.endsWith("pled")) { stemmed_word = this.remove(word, "d"); ruleno = 12.4; } // 12.4
        else if (word.endsWith("bled")) { stemmed_word = this.remove(word, "d"); ruleno = 12.3; } // 12.3
        else if (word.endsWith("eled")) { stemmed_word = this.remove(word, "ed"); ruleno = 12.2; } // 12.2
        else if (word.endsWith("lled")) { stemmed_word = this.remove(word, "ed"); ruleno = 12.1; } // 12.1
        else if (word.endsWith("led")) { stemmed_word = this.remove(word, "ed"); ruleno = 12; } // 12
        else if (word.endsWith( "ened" )) { stemmed_word = this.remove( word, "ed" ); ruleno = 13.7; }       // 13.7
        else if (word.endsWith( "ained" )) { stemmed_word = this.remove( word, "ed" ); ruleno = 13.6; }       // 13.6
        else if (word.endsWith( "erned" )) { stemmed_word = this.remove( word, "ed" ); ruleno = 13.5; }       // 13.5
        else if (word.endsWith( "rned" )) { stemmed_word = this.remove( word, "ed" ); ruleno = 13.4; }       // 13.4
        else if (word.endsWith( "nned" )) { stemmed_word = this.remove( word, "ned" ); ruleno = 13.3; }       // 13.3
        else if (word.endsWith( "oned" )) { stemmed_word = this.remove( word, "ed" ); ruleno = 13.2; }       // 13.2
        else if (word.endsWith( "gned" )) { stemmed_word = this.remove( word, "ed" ); ruleno = 13.1; }       // 13.1
        else if (word.endsWith( "ned" )) { stemmed_word = this.remove( word, "d" ); ruleno = 13; }       // 13
        else if (word.endsWith( "ifted" )) { stemmed_word = this.remove( word, "ed" ); ruleno = 14; }       // 14
        else if (word.endsWith( "ected" )) { stemmed_word = this.remove( word, "ed" ); ruleno = 15; }       // 15
        else if (word.endsWith( "vided" )) { stemmed_word = this.remove( word, "d" ); ruleno = 16; }       // 16
        else if (word.endsWith( "ved" )) { stemmed_word = this.remove( word, "d" ); ruleno = 17; }       // 17
        else if (word.endsWith( "ced" )) { stemmed_word = this.remove( word, "d" ); ruleno = 18; }       // 18
        else if (word.endsWith( "erred" )) { stemmed_word = this.remove( word, "red" ); ruleno = 19; }       // 19
        else if (word.endsWith( "urred" )) { stemmed_word = this.remove( word, "red" ); ruleno = 20.5; }       // 20.5
        else if (word.endsWith( "lored" )) { stemmed_word = this.remove( word, "ed" ); ruleno = 20.4; }       // 20.4
        else if (word.endsWith( "eared" )) { stemmed_word = this.remove( word, "ed" ); ruleno = 20.3; }       // 20.3
        else if (word.endsWith( "tored" )) { stemmed_word = word.replaceFirst("ed", "e"); ruleno = 20.2; }
        else if (word.endsWith( "ered" )) { stemmed_word = this.remove( word, "ed" ); ruleno = 20.1; }       // 20.1

        /***********************************************************************************************************/
        // plural change - this differs from Perl v1.03
        else if (word.endsWith( "red" )) { stemmed_word = this.remove( word, "d" ); ruleno = 20; }       // 20
        else if (word.endsWith( "reds" )) { stemmed_word = this.remove( word, "ds" ); ruleno = 20; }       // 20
        /***********************************************************************************************************/

        else if (word.endsWith( "tted" )) { stemmed_word = this.remove( word, "ted" ); ruleno = 21; }       // 21
        else if (word.endsWith( "noted" )) { stemmed_word = this.remove( word, "d" ); ruleno = 22.4; }       // 22.4
        else if (word.endsWith( "leted" )) { stemmed_word = this.remove( word, "d" ); ruleno = 22.3; }       // 22.3
        else if (word.endsWith( "uted" )) { stemmed_word = this.remove( word, "d" ); ruleno = 22.2; }       // 22.2
        else if (word.endsWith( "ated" )) { stemmed_word = this.remove( word, "d" ); ruleno = 22.1; }       // 22.1
        else if (word.endsWith( "ted" )) { stemmed_word = this.remove( word, "ed" ); ruleno = 22; }       // 22
        else if (word.endsWith( "anges" )) { stemmed_word = this.remove( word, "s" ); ruleno = 23; }       // 23
        else if (word.endsWith( "aining" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 24; }       // 24
        else if (word.endsWith( "acting" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 25; }       // 25

        /***********************************************************************************************************/
        // plural change - this differs from Perl v1.03
        else if (word.endsWith( "tting" )) { stemmed_word = this.remove( word, "ting" ); ruleno = 26; }       // 26
        else if (word.endsWith( "ttings" )) { stemmed_word = this.remove( word, "tings" ); ruleno = 26; }       // 26
        /***********************************************************************************************************/

        else if (word.endsWith( "viding" )) { stemmed_word = word.replaceFirst("ing", "e"); ruleno = 27; }       // 27
        else if (word.endsWith( "ssed" )) { stemmed_word = this.remove( word, "ed" ); ruleno = 28; }       // 28
        else if (word.endsWith( "sed" )) { stemmed_word = this.remove( word, "d" ); ruleno = 29; }       // 29
        else if (word.endsWith( "titudes" )) { stemmed_word = this.remove( word, "s" ); ruleno = 30; }       // 30
        else if (word.endsWith( "umed" )) { stemmed_word = this.remove( word, "d" ); ruleno = 31; }       // 31
        else if (word.endsWith( "ulted" )) { stemmed_word = this.remove( word, "ed" ); ruleno = 32; }       // 32
        else if (word.endsWith( "uming" )) { stemmed_word = word.replaceFirst("ing", "e"); ruleno = 33; }       // 33
        else if (word.endsWith( "fulness" )) { stemmed_word = this.remove( word, "ness" ); ruleno = 34; }       // 34
        else if (word.endsWith( "ousness" )) { stemmed_word = this.remove( word, "ness" ); ruleno = 35; }       // 35

        /***********************************************************************************************************/
        // in the perl version these are all in one regrex ( r[aeiou]bed$ )
        else if (word.endsWith( "rabed" )) { stemmed_word = this.remove( word, "d" ); ruleno = 36.1; }       // 36.1
        else if (word.endsWith( "rebed" )) { stemmed_word = this.remove( word, "d" ); ruleno = 36.1; }       // 36.1
        else if (word.endsWith( "ribed" )) { stemmed_word = this.remove( word, "d" ); ruleno = 36.1; }       // 36.1
        else if (word.endsWith( "robed" )) { stemmed_word = this.remove( word, "d" ); ruleno = 36.1; }       // 36.1
        else if (word.endsWith( "rubed" )) { stemmed_word = this.remove( word, "d" ); ruleno = 36.1; }       // 36.1
        /***********************************************************************************************************/

        /***********************************************************************************************************/
        // plural change - this differs from Perl v1.03
        else if (word.endsWith( "bed" )) { stemmed_word = this.remove( word, "ed" ); ruleno = 36; }       // 36
        else if (word.endsWith( "beds" )) { stemmed_word = this.remove( word, "eds" ); ruleno = 36; }       // 36

        else if (word.endsWith( "ssing" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 37; }       // 37
        else if (word.endsWith( "ssings" )) { stemmed_word = this.remove( word, "ings" ); ruleno = 37; }       // 37
        /***********************************************************************************************************/

        else if (word.endsWith( "ulting" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 38; }       // 38

        /***********************************************************************************************************/
        // plural change - this differs from Perl v1.03
        else if (word.endsWith( "ving" )) { stemmed_word = word.replaceFirst("ing", "e");  ruleno = 39; }       // 39
        else if (word.endsWith( "vings" )) { stemmed_word = word.replaceFirst("ings", "e");  ruleno = 39; }       // 39
        /***********************************************************************************************************/

        else if (word.endsWith( "eading" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 40.7; }       // 40.7
        else if (word.endsWith( "eadings" )) { stemmed_word = this.remove( word, "ings" ); ruleno = 40.7; }     // 40.7
        else if (word.endsWith( "oading" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 40.6; }       // 40.6
        else if (word.endsWith( "oadings" )) { stemmed_word = this.remove( word, "ings" ); ruleno = 40.6; }       // 40.6
        else if (word.endsWith( "eding" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 40.5; }       // 40.5
        else if (word.endsWith( "edings" )) { stemmed_word = this.remove( word, "ings" ); ruleno = 40.5; }       // 40.5
        else if (word.endsWith( "dding" )) { stemmed_word = this.remove( word, "ding" ); ruleno = 40.4; }       // 40.4
        else if (word.endsWith( "ddings" )) { stemmed_word = this.remove( word, "dings" ); ruleno = 40.4; }       // 40.4
        else if (word.endsWith( "lding" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 40.3; }       // 40.3
        else if (word.endsWith( "ldings" )) { stemmed_word = this.remove( word, "ings" ); ruleno = 40.3; }       // 40.3
        else if (word.endsWith( "rding" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 40.2; }       // 40.2
        else if (word.endsWith( "rdings" )) { stemmed_word = this.remove( word, "ings" ); ruleno = 40.2; }       // 40.2
        else if (word.endsWith( "nding" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 40.1; }       // 40.1
        else if (word.endsWith( "ndings" )) { stemmed_word = this.remove( word, "ings" ); ruleno = 40.1; }       // 40.1

        /***********************************************************************************************************/
        // plural change - this differs from Perl v1.03
        else if (word.endsWith( "ding" )) { stemmed_word = word.replaceFirst("ing", "e"); ruleno = 40; }       // 40
        else if (word.endsWith( "dings" )) { stemmed_word = word.replaceFirst("ings", "e"); ruleno = 40; }     // 40

        else if (word.endsWith( "lling" )) { stemmed_word = this.remove( word, "ling" ); ruleno = 41; } 		// word ends in -lling 41
        else if (word.endsWith( "llings" )) { stemmed_word = this.remove( word, "lings" ); ruleno = 41; } 		// word ends in -lling 41

        else if (word.endsWith( "ealing" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 42.4; } 		// word ends in -ealing 42.4
        else if (word.endsWith( "ealings" )) { stemmed_word = this.remove( word, "ings" ); ruleno = 42.4; } 		// word ends in -ealing 42.4

        else if (word.endsWith( "oling" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 42.3; } 		// word ends in -oling 42.3
        else if (word.endsWith( "olings" )) { stemmed_word = this.remove( word, "ings" ); ruleno = 42.3; } 		// word ends in -oling 42.3

        else if (word.endsWith( "ailing" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 42.2; } 		// word ends in -ailing 42.2
        else if (word.endsWith( "ailings" )) { stemmed_word = this.remove( word, "ings" ); ruleno = 42.2; } 		// word ends in -ailing 42.2

        else if (word.endsWith( "eling" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 42.1; } 		// word ends in -ling 42.1
        else if (word.endsWith( "elings" )) { stemmed_word = this.remove( word, "ings" ); ruleno = 42.1; } 		// word ends in -ling 42.1

        else if (word.endsWith( "ling" )) { stemmed_word = this.remove( word, "ing" ); stemmed_word = stemmed_word.concat( "e" ); ruleno = 42; }			// word ends in -ting 48
        else if (word.endsWith( "lings" )) { stemmed_word = this.remove( word, "ings" ); stemmed_word = stemmed_word.concat( "e" ); ruleno = 42; }			// word ends in -ting 48
        /***********************************************************************************************************/

        else if (word.endsWith( "nged" )) { stemmed_word = this.remove( word, "d" ); ruleno = 43.2; } 		// word ends in -nged  43.2
        else if (word.endsWith( "gged" )) { stemmed_word = this.remove( word, "ged" ); ruleno = 43.1; } 		// word ends in -gged  43.1
        else if (word.endsWith( "ged" )) { stemmed_word = this.remove( word, "d" ); ruleno = 43; } 			// word ends in -ged  43

        /***********************************************************************************************************/
        // plural change - this differs from Perl v1.03
        else if (word.endsWith( "mming" )) { stemmed_word = this.remove( word, "ming" ); ruleno = 44.3; } 		// word ends in -mming  44.3
        else if (word.endsWith( "mmings" )) { stemmed_word = this.remove( word, "mings" ); ruleno = 44.3; } 		// word ends in -mming  44.3
        /***********************************************************************************************************/

        else if (word.endsWith( "rming" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 44.2; } 		// word ends in -rming  44.2
        else if (word.endsWith( "lming" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 44.1; } 		// word ends in -lming  44.1

        /***********************************************************************************************************/
        // plural change - this differs from Perl v1.03
        else if (word.endsWith( "ming" )) { stemmed_word = this.remove( word, "ing" ); stemmed_word = stemmed_word.concat( "e" ); ruleno = 44; } 			// word ends in -ting 48
        else if (word.endsWith( "mings" )) { stemmed_word = this.remove( word, "ings" ); stemmed_word = stemmed_word.concat( "e" ); ruleno = 44; } 			// word ends in -ting 48

        else if (word.endsWith( "nging" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 45.2; } 		// word ends in -ging 45.2
        else if (word.endsWith( "ngings" )) { stemmed_word = this.remove( word, "ings" ); ruleno = 45.2; } 		// word ends in -ging 45.2

        else if (word.endsWith( "gging" )) { stemmed_word = this.remove( word, "ging" ); ruleno = 45.1; } 		// word ends in -ging 45.1
        else if (word.endsWith( "ggings" )) { stemmed_word = this.remove( word, "gings" ); ruleno = 45.1; } 		// word ends in -ging 45.1

        else if (word.endsWith( "ging" )) { stemmed_word = this.remove( word, "ing" ); stemmed_word = stemmed_word.concat( "e" ); ruleno = 45; }
        else if (word.endsWith( "gings" )) { stemmed_word = this.remove( word, "ings" ); stemmed_word = stemmed_word.concat( "e" ); ruleno = 45; }
        /***********************************************************************************************************/

        else if (word.endsWith( "aning" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 46.6; } 		// word ends in -aning 46.6
        else if (word.endsWith( "ening" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 46.5; } 		// word ends in -ening 46.5
        else if (word.endsWith( "gning" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 46.4; } 		// word ends in -gning 46.4
        else if (word.endsWith( "nning" )) { stemmed_word = this.remove( word, "ning" ); ruleno = 46.3; } 		// word ends in -nning 46.3
        else if (word.endsWith( "oning" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 46.2; } 		// word ends in -oning 46.2
        else if (word.endsWith( "rning" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 46.1; } 		// word ends in -rning 46.1
        else if (word.endsWith( "ning" )) { stemmed_word = this.remove( word, "ing" ); stemmed_word = stemmed_word.concat( "e" ); ruleno = 46; } 			// word ends in -ting 46

        /***********************************************************************************************************/
        // plural change - this differs from Perl v1.03
        else if (word.endsWith( "sting" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 47; } 		// word ends in -sting 47
        else if (word.endsWith( "stings" )) { stemmed_word = this.remove( word, "ings" ); ruleno = 47; } 		// word ends in -sting 47
        /***********************************************************************************************************/

        /***********************************************************************************************************/
        // plural change - this differs from Perl v1.03
        else if (word.endsWith( "eting" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 48.4; } 		// word ends in -pting 48.4
        else if (word.endsWith( "etings" )) { stemmed_word = this.remove( word, "ings" ); ruleno = 48.4; } 		// word ends in -pting 48.4
        /***********************************************************************************************************/

        else if (word.endsWith( "pting" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 48.3; } 		// word ends in -pting 48.3

        /***********************************************************************************************************/
        // plural change - this differs from Perl v1.03
        else if (word.endsWith( "nting" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 48.2; } 		// word ends in -nting 48.2
        else if (word.endsWith( "ntings" )) { stemmed_word = this.remove( word, "ings" ); ruleno = 48.2; } 		// word ends in -nting 48.2
        /***********************************************************************************************************/

        else if (word.endsWith( "cting" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 48.1; } 		// word ends in -cting 48.1

        /***********************************************************************************************************/
        // plural change - this differs from Perl v1.03
        else if (word.endsWith( "ting" )) { stemmed_word = this.remove( word, "ing" ); stemmed_word = stemmed_word.concat( "e" ); ruleno = 48; } 			// word ends in -ting 48
        else if (word.endsWith( "tings" )) { stemmed_word = this.remove( word, "ings" ); stemmed_word = stemmed_word.concat( "e" ); ruleno = 48; } 			// word ends in -ting 48
        /***********************************************************************************************************/

        else if (word.endsWith( "ssed" )) { stemmed_word = this.remove( word, "ed" ); ruleno = 49; } 			// word ends in -ssed 49
        else if (word.endsWith( "les" )) { stemmed_word = this.remove( word, "s" ); ruleno = 50; } 			// word ends in -les 50
        else if (word.endsWith( "tes" )) { stemmed_word = this.remove( word, "s" ); ruleno = 51; } 			// word ends in -tes 51
        else if (word.endsWith( "zed" )) { stemmed_word = this.remove( word, "d" ); ruleno = 52; } 			// word ends in -zed 52
        else if (word.endsWith( "lled" )) { stemmed_word = this.remove( word, "ed" ); ruleno = 53; } 			// word ends in -lled 53

        /***********************************************************************************************************/
        // plural change - this differs from Perl v1.03
        else if (word.endsWith( "iring" )) { stemmed_word = word.replaceFirst("ing", "e"); ruleno = 54.4; }
        else if (word.endsWith( "irings" )) { stemmed_word = word.replaceFirst("ings", "e"); ruleno = 54.4; }

        else if (word.endsWith( "uring" )) { stemmed_word = word.replaceFirst("ing", "e"); ruleno = 54.3; }
        else if (word.endsWith( "urings" )) { stemmed_word = word.replaceFirst("ings", "e"); ruleno = 54.3; }

        else if (word.endsWith( "ncing" )) { stemmed_word = word.replaceFirst("ing", "e"); ruleno = 54.2; }
        else if (word.endsWith( "ncings" )) { stemmed_word = word.replaceFirst("ings", "e"); ruleno = 54.2; }
        /***********************************************************************************************************/

        else if (word.endsWith( "zing" )) { stemmed_word = word.replaceFirst("ing", "e"); ruleno = 54.1; }

        /***********************************************************************************************************/
        // plural change - this differs from Perl v1.03
        else if (word.endsWith( "sing" )) { stemmed_word = word.replaceFirst("ing", "e"); ruleno = 54; }
        else if (word.endsWith( "sings" )) { stemmed_word = word.replaceFirst("ings", "e"); ruleno = 54; }
        /***********************************************************************************************************/

        else if (word.endsWith( "lling" )) { stemmed_word = this.remove( word, "ing" ); ruleno = 55; }
        else if (word.endsWith( "ied" )) { stemmed_word = word.replaceFirst("ied", "y"); ruleno = 56; }
        else if (word.endsWith( "ating" )) { stemmed_word = word.replaceFirst("ing", "e"); ruleno = 57; }


        /***********************************************************************************************************/
        // plural change - this differs from Perl v1.03
        else if (word.endsWith( "thing" )) { ruleno = 58.1; }

        // the word "things" was being caught by 58.1 so have added this rule, this should really have been caught by 68 but that wasn't happening
        else if (word.endsWith( "things" )) { stemmed_word = this.remove( word, "s" ); ruleno = 58.1; }

        else if (word.matches( ".*\\w\\wings?$" ) ) { stemmed_word = rule58( word ); ruleno = 58; }
        /***********************************************************************************************************/

        else if (word.endsWith( "ies" )) { stemmed_word = word.replaceFirst("ies", "y"); ruleno = 59; }
        else if (word.endsWith( "lves" )) { stemmed_word = word.replaceFirst("ves", "f"); ruleno = 60.1; }
        else if (word.endsWith( "ves" )) { stemmed_word = this.remove( word, "s" ); ruleno = 60; }
        else if (word.endsWith( "aped" )) { stemmed_word = this.remove( word, "d" ); ruleno = 61.3; }
        else if (word.endsWith( "uded" )) { stemmed_word = this.remove( word, "d" ); ruleno = 61.2; }
        else if (word.endsWith( "oded" )) { stemmed_word = this.remove( word, "d" ); ruleno = 61.1; }
        else if (word.endsWith( "ated" )) { stemmed_word = this.remove( word, "d" ); ruleno = 61; }
        else if (word.matches( ".*\\w\\weds?$" )) { stemmed_word = rule62( word ); ruleno = 62; }
        else if (word.endsWith( "pes" )) { stemmed_word = this.remove( word, "s" ); ruleno = 63.8; }
        else if (word.endsWith( "mes" )) { stemmed_word = this.remove( word, "s" ); ruleno = 63.7; }
        else if (word.endsWith( "ones" )) { stemmed_word = this.remove( word, "s" ); ruleno = 63.6; }
        else if (word.endsWith( "izes" )) { stemmed_word = this.remove( word, "s" ); ruleno = 63.5; }
        else if (word.endsWith( "ures" )) { stemmed_word = this.remove( word, "s" ); ruleno = 63.4; }
        else if (word.endsWith( "ines" )) { stemmed_word = this.remove( word, "s" ); ruleno = 63.3; }
        else if (word.endsWith( "ides" )) { stemmed_word = this.remove( word, "s" ); ruleno = 63.2; }
        else if (word.endsWith( "ges" )) { stemmed_word = this.remove( word, "s" ); ruleno = 63.1; }
        else if (word.endsWith( "es" )) { stemmed_word = this.remove( word, "es" ); ruleno = 63; }
        else if (word.endsWith( "is" )) { stemmed_word = word.replaceFirst("is", "e"); ruleno = 64; }
        else if (word.endsWith( "ous" )) { ruleno = 65; }
        else if (word.endsWith( "ums" )) { ruleno = 66; }
        else if (word.endsWith( "us" )) { ruleno = 67; }
        else if (word.endsWith( "s" )) { stemmed_word = this.remove( word, "s" ); ruleno = 68; }

        return new Word( stemmed_word, ruleno );
    }

    private String rule58( String word ) {
        String remove = "ing";
        return this.stemWithDuplicateCharacterCheck( word, remove );
    }

    private String rule62( String word ) {
        String remove = "ed";
        return this.stemWithDuplicateCharacterCheck( word, remove );
    }

    private String stemWithDuplicateCharacterCheck( String word, String remove ) {
        if( word.endsWith( "s" ) ) remove = remove.concat( "s");
        String stemmed_word = this.remove( word, remove );
        if( stemmed_word.matches( ".*(\\w)\\1$" ) ) {
            stemmed_word = this.remove( stemmed_word, "." );
        }
        return stemmed_word;
    }

    private String remove( String word, String suffix ) {
        return word.substring( 0, word.length() - suffix.length() );
    }

    private boolean isProblemWord( String word ) {
        if( word.equals("is") || word.equals("as") || word.equals("this") || word.equals("has") || word.equals("was") || word.equals("during") ) {
            return true;
        }

        return false;
    }

}
