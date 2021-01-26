package examples;


import is2.data.InstancesTagger;
import is2.data.SentenceData09;
import is2.io.CONLLReader09;
import is2.lemmatizer.Lemmatizer;
import is2.lemmatizer.MFO;
import is2.parser.Parser;
import is2.tag.Tagger;

import java.io.File;
import java.util.Arrays;

//import org.apache.log4j.Logger;

/**
 * Dependency parsing
 *
 * @author B. Piwowarski <benjamin@bpiwowar.net>
 * @date 10/10/12
 */
//@TaskDescription(name = "dependency-parser", project = "mate-tools")
public class DependencyParser  {
 //   final static private Logger LOGGER = Logger.getLogger(DependencyParser.class);
    //@Argument(name = "lemmatizer", required = true, checkers = IOChecker.Readable.class)
    File lemmatizerFile;

    //@Argument(name = "tagger", required = true)
    File taggerFile;

    //@Argument(name = "parser", required = true)
    File parserFile;

    //@Override
    public int execute() throws Throwable {

        // Load lemmatizer
        //LOGGER.info("Loading lemmatizer");
    	// true = do uppercase lemmatization
        Lemmatizer lemmatizer = new Lemmatizer(lemmatizerFile.getAbsolutePath()); 

        // Load tagger
        //LOGGER.info("Loading tagger");
        Tagger tagger = new Tagger(taggerFile.getAbsolutePath());

        // Load parser
        //LOGGER.info("Loading parser");
        Parser parser = new Parser(parserFile.getAbsolutePath());


        // Sentences to parse
        String sentences[] = new String[]{
                "Airfields have been constructed on a number of the islands .",
                "Private investment has even made an increasingly modern ferry fleet possible .",
                "Politically , the 1990s have been relatively quite times for the islands ."
        };

        CONLLReader09 reader = new CONLLReader09(CONLLReader09.NO_NORMALIZE);

        for (String sentence : sentences) {
            // Prepare the sentence
            InstancesTagger instanceTagger = new InstancesTagger();
            instanceTagger.init(1, new MFO());

            String[] split = sentence.split("\\s+");
            String[] splitRoot = new String[split.length+1];
            System.arraycopy(split, 0, splitRoot, 1, split.length);
            splitRoot[0] = CONLLReader09.ROOT;

            SentenceData09 instance = new SentenceData09();
            instance.init(splitRoot);

            reader.insert(instanceTagger, instance);

            SentenceData09 result = lemmatizer.apply(instance);
            tagger.apply(result);
            result = parser.parse(result, parser.params, false, parser.options);


            // Output
            System.out.println(Arrays.toString(result.forms));
            System.out.println(Arrays.toString(result.plemmas));
            System.out.println(Arrays.toString(result.ppos));
            System.out.println(Arrays.toString(result.pheads));
            System.out.println(Arrays.toString(result.plabels));
            System.out.println();

        }

        return 0;
    }
}
