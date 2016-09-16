package examples;


import is2.data.InstancesTagger;
import is2.data.SentenceData09;
import is2.io.CONLLReader09;
import is2.io.CONLLWriter09;
import is2.lemmatizer.Lemmatizer;
import is2.lemmatizer.MFO;
import is2.parser.Parser;
import is2.tag.Tagger;
//import org.apache.log4j.Logger;

import java.io.File;
import java.util.Arrays;

/**
 * Dependency parsing
 *
 * @author B. Piwowarski <benjamin@bpiwowar.net>
 * @date 10/10/12
 */
//@TaskDescription(name = "dependency-parser", project = "mate-tools")
public class FullPipelineTest  {
 //   final static private Logger LOGGER = Logger.getLogger(DependencyParser.class);
    //@Argument(name = "lemmatizer", required = true, checkers = IOChecker.Readable.class)
    public File lemmatizerFile;

    //@Argument(name = "tagger", required = true)
    public File taggerFile;

    public File mtaggerFile;

    //@Argument(name = "parser", required = true)
    public File parserFile;

    //@Override
    public int execute(String source, String target) throws Throwable {

        // Load lemmatizer
        //LOGGER.info("Loading lemmatizer");
    	// true = do uppercase lemmatization
        Lemmatizer lemmatizer = new Lemmatizer(lemmatizerFile.getAbsolutePath()); 

        // Load tagger
        //LOGGER.info("Loading tagger");
        Tagger tagger = new Tagger(taggerFile.getAbsolutePath());
        
        is2.mtag.Tagger mtagger = new is2.mtag.Tagger(mtaggerFile.getAbsolutePath());

        // Load parser
        //LOGGER.info("Loading parser");
        Parser parser = new Parser(parserFile.getAbsolutePath());


        CONLLReader09 reader = new CONLLReader09(source);
        CONLLWriter09 writer = new CONLLWriter09(target);

        int count=0;
        while (true) {
            // Prepare the sentence
        	InstancesTagger is = new InstancesTagger();
            is.init(1, new MFO());
            
            SentenceData09 instance=  reader.getNext(is);
        	if (instance ==null) break;
        	SentenceData09 result = null;
try {
	
           	System.out.print("\b\b\b\b"+count);
           	result= lemmatizer.apply(instance);
          
            result = tagger.apply(result);
            result= mtagger.apply(result);
            result = parser.apply(result);

            count++;
} catch(Exception e) {
	
	System.out.println("error"+result);
	System.out.println("error"+instance);
	e.printStackTrace();
	break;
}

            // Output
            writer.write(result);

        }
        writer.finishWriting();
        return 0;
    }
    
    public static void main(String args[]) throws Throwable {
    	
    	if (args.length<3) {
    		System.out.println("lemmatizer-model tagger-model parser-model source target");
    		System.exit(0);
    	}
    	FullPipelineTest p = new FullPipelineTest();
    	p.lemmatizerFile = new File(args[0]);
    	p.taggerFile = new File(args[1]);
    	p.mtaggerFile = new File(args[2]);
    	p.parserFile = new File(args[3]);
    	
    	p.execute(args[4], args[5]);
    	
    }
    
}
