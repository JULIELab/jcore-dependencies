package dragon.ml.seqmodel.feature;

import dragon.ml.seqmodel.data.DataSequence;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * ConcatRegexFeatures generates features by matching the token with the character patterns.
 * Character patterns are regular expressions for checking whether the token is capitalized word,
 * a number, small case word, whether the token contains any special characters and like.
 * It uses regular expression to match a sequence of character pattern and generates features
 * accordingly.
 * <P>
 * The feature generated here is whether a sequence of tokens has a particular sequence of given pattern or not.
 * For example, if a pattern is to mathc a capital word, then for two token context window, various features
 * generated are weither two token (bigram) sequence is having any of the following pattern or not:
 * 	(1) Capital, Capital
 *	(2) Capital, Non-Capital
 *	(3) Non-capital, Capital.
 *
 * You can use any window around the current token (segment) for creating regular expression based features.
 * Also, you can define your own patterns, by writing down the regular expression in a file,
 * whose format is specified below.
 * </p>

 * A token in a token sequence has a index relative to the current token index, which is described below:
 * <pre>
 	x0 x1 x2 x3 x4 x5 x6 x7 .... xn
	-4 -3 -2 -1 0  0  0  1 2 ...
 * </pre>
 * <p>
 * In above example, the current segment is from postion 4 to 6 with value of pos = 6 and prevPos = 3 in
 * startScanFeaturesAt() call of FeatureGenerator.
 * You can refer to any of the token relative to current position by using the index below the token sequence.
 * Thus, you can create a pattern concat features for any token sequence in the neighbourhood of the current token,
 * using relSegmentStart and relSegmentEnd.
 * For, example to create pattern for two tokens to the left of the current token, following is the parameters
 * to be passed to the constructor of the class:
 * </p>
 *
 * @author 	Imran Mansuri
 *
 * This FeatureType can not be used independently because the label is not set.
 * This feature type should be wrapped by FeatureTypeStateLoop.
 */

public class FeatureTypeConcatRegex extends AbstractFeatureType {
	/**
	 *      Various patterns are defined here.
	 *      First dimension of this two dimensional array is feature name and second value is the
	 *      regular expression pattern to be matched against a token. You can add your own patterns
	 *      in this array.
	 */
	private String patternString[][] = {
	    {"isWord",           		"[a-zA-Z][a-zA-Z]+"     },
	    {"singleCapLetterWithDot",  "[A-Z]\\."  			},
		{"singleCapLetter",  		"[A-Z]"  				},
		{"isDigits", 				"\\d+"					},
		{"singleDot", 		"[.]"			},
		{"singleComma", 		"[,]"			},
		{"isSpecialCharacter",		"[#;:\\-/<>'\"()&]"},
		{"containsSpecialCharacters",".*[#;:\\-/<>'\"()&].*"},
		{"isInitCapital",     		"[A-Z][a-z]+"        },
		{"isAllCapital",      		"[A-Z]+"                },
		{"isAllSmallCase",      	"[a-z]+"                },
		{"isAlpha",           		"[a-zA-Z]+"             },
		{"isAlphaNumeric",      	"[a-zA-Z0-9]+"          },
		{"endsWithDot",             "\\p{Alnum}+\\."        },
		{"endsWithComma",       	"\\w+[,]"              },
		{"endsWithPunctuation",     "\\w+[;:,.?!]"		    },
		{"singlePunctuation", 		"\\p{Punct}"			},
		{"singleAmp", 		"[&]"			},
		{"containsDigit", 			".*\\d+.*"				},
		{"singleDigit", 				"\\s*\\d\\s*"					},
		{"twoDigits", 				"\\s*\\d{2}\\s*"					},
		{"threeDigits", 				"\\s*\\d{3}\\s*"					},
		{"fourDigits", 				"\\s*\\(*\\d{4}\\)*\\s*"	},
		{"isNumberRange", 			"\\d+\\s*([-]{1,2}\\s*\\d+)?"},
		{"isDashSeparatedWords", 		"(\\w[-])+\\w"},
		{"isDashSeparatedSeq", 			"((\\p{Alpha}+|\\p{Digit}+)[-])+(\\p{Alpha}+|\\p{Digit}+)"},
		{"isURL", 					"\\p{Alpha}+://(\\w+\\.)\\w+(:(\\d{2}|\\d{4}))?(/\\w+)*(/|(/\\w+\\.\\w+))?"	},
		{"isEmailId", 				"\\w+@(\\w+\\.)+\\w+"	},
		{"containsDashes",			".*--.*"}
	};
	private Pattern p[];
	protected transient DataSequence data;
	protected int index, idbase, curId, window;
	protected int relSegmentStart, relSegmentEnd;
	protected int maxSegmentLength;
	protected int left, right;

	/**
	 * Constructs an object of ConcatRegexFeatures to be used to generate features for the token
	 * sequence as specified.
	 * You can specify the sequence of tokens on which the pattern has to be applied using relSegmentStart
	 * and relSegmentEnd, which denotes segment boundries.
	 * The maxSegmentLength denotes the maximum segment size, for normal CRF the value of maxSegmentLength is 1.
	 * There are certain default patterns defined in the class. You can specify your own pattern in a file, and pass
	 * the name of the file in this constructor. The file should begin with integer value for number of pattern in the
	 * file. This should be follwoed by one pattern definition on each line. The first word is the name of the pattern
	 * and second word is regular expression for the pattern.
	 *
	 * @param fgen			a {@link Model} object
	 * @param relSegmentStart	index of the reltive position for left boundary
	 * @param relSegmentEnd		index of the reltive position for right boundary
	 * @param maxSegmentLength		maximum size of a segment
	 * @param patternFile		file which contains the pattern definition
	 */

	public FeatureTypeConcatRegex(int relSegmentStart, int relSegmentEnd, int maxSegmentLength, String patternFile){
		super(false);
		this.relSegmentStart = relSegmentStart;
		this.relSegmentEnd = relSegmentEnd;
		this.maxSegmentLength = maxSegmentLength;

		window = getWindowSize(relSegmentStart, relSegmentEnd);
		idbase = (int) Math.pow(2, window);
		patternString=getPatterns(patternFile);
		p = new Pattern[patternString.length];
		for(int i = 0; i < patternString.length; i++){
			p[i] = Pattern.compile(patternString[i][1]);
		}
	}

    public FeatureTypeConcatRegex(int relSegmentStart, int relSegmentEnd, int maxSegmentLength, String[][] patternString){
        super(false);
        this.relSegmentStart = relSegmentStart;
        this.relSegmentEnd = relSegmentEnd;
        this.maxSegmentLength = maxSegmentLength;
        window = getWindowSize(relSegmentStart, relSegmentEnd);
        idbase = (int) Math.pow(2, window);
        this.patternString=patternString;
        p = new Pattern[patternString.length];
        for(int i = 0; i < patternString.length; i++){
            p[i] = Pattern.compile(patternString[i][1]);
        }
    }

	public FeatureTypeConcatRegex(int relSegmentStart, int relSegmentEnd, int maxSegmentLength){
		super(false);
		this.relSegmentStart = relSegmentStart;
		this.relSegmentEnd = relSegmentEnd;
		this.maxSegmentLength = maxSegmentLength;
		window = getWindowSize(relSegmentStart, relSegmentEnd);
		idbase = (int) Math.pow(2, window);
		p = new Pattern[patternString.length];
		for(int i = 0; i < patternString.length; i++){
			p[i] = Pattern.compile(patternString[i][1]);
		}
	}

	public FeatureTypeConcatRegex(int relSegmentStart, int relSegmentEnd){
		this(relSegmentStart, relSegmentEnd, 1);
	}

	public FeatureTypeConcatRegex(int relSegmentStart, int relSegmentEnd, String patternFile){
		this(relSegmentStart, relSegmentEnd, 1, patternFile);
	}

    private int getWindowSize(int relSegmentStart, int relSegmentEnd) {
        if ( (sign(relSegmentEnd) == sign(relSegmentStart)) && relSegmentStart != 0)
            return relSegmentEnd - relSegmentStart + 1;
        else
            return relSegmentEnd - relSegmentStart + maxSegmentLength;
    }

	private int sign(int boundary){
		if(boundary == 0)
			return 0;
		else if(boundary < 0)
			return -1;
		else
			return 1;
	}

	public boolean startScanFeaturesAt(DataSequence data, int startPos, int endPos){
		this.data = data;
		index = 0;
		if (relSegmentStart <= 0) {
			left = startPos + relSegmentStart;
		} else {
			left = endPos + relSegmentStart;
		}

		if (relSegmentEnd < 0) {
			right = startPos + relSegmentEnd;
		} else {
			right = endPos + relSegmentEnd;
		}

		if(!(left >= 0 && left < data.length() && right >= 0 && right < data.length()))
			index = patternString.length;
		advance();
		return true;
	}

	public boolean hasNext() {
		return index < patternString.length;
	}

	public Feature next() {
        Feature f;
        String name;
        FeatureIdentifier id;
        int curState;

        curState=-1;
        name = patternString[index][0] + "_" + window + "_" + Integer.toBinaryString(curId);
        id=new FeatureIdentifier(name, curId + idbase * index++,curState);
        f=new BasicFeature(id,curState,1);
		advance();
        return f;
	}

	private void advance(){
        boolean match;
        int base;

		curId = 0;
		while(curId <= 0 && index < patternString.length){
			base = 1;
			for(int k = left; k <= right; k++){
                match= p[index].matcher(data.getToken(k).getContent()).matches();
				curId += base * (match? 1:0);
				base = base * 2;
			}
			if(curId > 0)
				break;
			index++;
		}
	}

    /**
     * Reads patterns to be matched from the file.
     * The format of the file is as follows:
     * The first line of the file is number of patterns, followed by a list of patterns one per line.
     * Each line describes a pattern's name and pattern string itself.
     *
     * @param patternFile		name of the pattern file
     */
    private String[][] getPatterns(String patternFile) {
        String patterns[][];
        try {
            BufferedReader in = new BufferedReader(new FileReader(patternFile));
            int len = Integer.parseInt(in.readLine());
            patterns = new String[len][2];

            for (int k = 0; k < len; k++) {
                StringTokenizer strTokenizer = new StringTokenizer(in.readLine());
                patterns[k][0] = strTokenizer.nextToken();
                patterns[k][1] = strTokenizer.nextToken();
            }
            return patterns;
        }
        catch (IOException ioe) {
            System.err.println("Could not read pattern file : " + patternFile);
            ioe.printStackTrace();
            return null;
        }
    }
};
