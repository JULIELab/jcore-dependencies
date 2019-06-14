package dragon.ml.seqmodel.feature;

import dragon.ml.seqmodel.data.DataSequence;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * This FeatureType can not be used independently because the label is not set.
 * This feature type should be wrapped by FeatureTypeStateLoop.
 */

public class FeatureTypeRegexCount extends AbstractFeatureType {
    private String patternString[][] = {
            {"isInitCapitalWord",     		"[A-Z][a-z]+"        },
            {"isAllCapitalWord",      		"[A-Z][A-Z]+"                },
            {"isAllSmallCase",      	"[a-z]+"                },
            {"isWord",           		"[a-zA-Z][a-zA-Z]+"     },
            {"isAlphaNumeric",      	"[a-zA-Z0-9]+"          },
            {"singleCapLetter",  		"[A-Z]"  				},
            {"isSpecialCharacter",		"[#;:\\-/<>'\"()&]"},
            {"singlePunctuation", 		"\\p{Punct}"			},
            {"singleDot", 				"[.]"			},
            {"singleComma", 			"[,]"			},
            {"containsDigit", 			".*\\d+.*"		},
            {"isDigits", 				"\\d+"			},
        };
    private Pattern p[];
    private int patternOccurence[], index, maxSegmentLength;

    public FeatureTypeRegexCount(int maxSegmentLength, String patternFile) {
        super(false);
        this.maxSegmentLength = maxSegmentLength;
        patternString=getPatterns(patternFile);
        p = new Pattern[patternString.length];
        for (int i = 0; i < patternString.length; i++) {
            p[i] = Pattern.compile(patternString[i][1]);
        }
        patternOccurence = new int[patternString.length];
    }

    public FeatureTypeRegexCount(int maxSegmentLength, String[][] patternString) {
        super(false);
        this.maxSegmentLength = maxSegmentLength;
        this.patternString=patternString;
        p = new Pattern[patternString.length];
        for (int i = 0; i < patternString.length; i++) {
            p[i] = Pattern.compile(patternString[i][1]);
        }
        patternOccurence = new int[patternString.length];
    }

    public FeatureTypeRegexCount(int maxSegmentLength) {
        super(false);
        this.maxSegmentLength = maxSegmentLength;
        p = new Pattern[patternString.length];
        for (int i = 0; i < patternString.length; i++) {
            p[i] = Pattern.compile(patternString[i][1]);
        }
        patternOccurence = new int[patternString.length];
    }

    public boolean startScanFeaturesAt(DataSequence data, int pos) {
        return startScanFeaturesAt(data, pos, pos);
    }

    public boolean startScanFeaturesAt(DataSequence data, int startPos, int endPos) {
        int i, j;

        for (j = 0; j < patternOccurence.length; j++) {
            patternOccurence[j] = 0;
        }
        for (i = startPos; i <= endPos; i++) {
            for (j = 0; j < p.length; j++) {
                if (p[j].matcher(data.getToken(i).getContent()).matches()) {
                    patternOccurence[j]++;
                }
            }
        }
        index = -1;
        return advance();
    }

    private boolean advance() {
        while (++index < (patternOccurence.length) && patternOccurence[index] <= 0);
        return index < patternOccurence.length;
    }

    public boolean hasNext() {
        return index < patternOccurence.length;
    }

    public Feature next() {
        BasicFeature f;
        String name;
        FeatureIdentifier id;
        int curLabel;

        curLabel = -1; // the label is not set.
        patternOccurence[index] = Math.min(maxSegmentLength, patternOccurence[index]);
        name = patternString[index][0] + "_Count_" + patternOccurence[index];
        id = new FeatureIdentifier(name, maxSegmentLength * (index + 1) + patternOccurence[index], curLabel);
        f = new BasicFeature(id, curLabel, 1);
        advance();
        return f;
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

}
