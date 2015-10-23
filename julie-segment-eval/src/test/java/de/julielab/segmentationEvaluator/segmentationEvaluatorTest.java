package de.julielab.segmentationEvaluator;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

public class segmentationEvaluatorTest extends TestCase {
	
  private static final Logger LOGGER = LoggerFactory.getLogger(segmentationEvaluatorTest.class);
  
	final String RESOURCES = "src/test/resources/";
	
	final String PENNBIO_IO = RESOURCES+"pennbio.io";
	
	final String PENNBIO_IO_ERROR = RESOURCES+"pennbio_error.io";
	
	final String PENNBIO_IOB = RESOURCES+"pennbio.iob";
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testTextToIOTokens() throws Exception {
      
        LOGGER.info("Testing method \"textToIOTokens\" of class Converter");
      
		IOToken[] ioTokens;
		
		// testing whether conversion to IOTokens works correctly
		
		File pennbioIO = new File(PENNBIO_IO);
		Converter conv = new Converter(Converter.TYPE_IO);
		ioTokens = conv.textToIOTokens(pennbioIO);
        LOGGER.debug(ioTokens[7].getText());
		assertTrue (ioTokens[0].getText().equals("Small")
				&& ioTokens[0].getLabel().equals("malignancy"));
		
		// testing whether conversion to IOBTokens works correctly
		
		File pennbioIOB = new File(PENNBIO_IOB);
		conv = new Converter(Converter.TYPE_IOB);
		ioTokens = conv.textToIOTokens(pennbioIOB);
		
        boolean allOK = ioTokens[0].getText().equals("Small")
          && ioTokens[0].getIobMark().equals("B")
          && ioTokens[0].getLabel().equals("malignancy");
        
        if (allOK) {
          LOGGER.info("Everything okay");
        }
        else {
          LOGGER.error("Error with method \"textToIOTokens\" of class Converter");
        }
          
		assertTrue (allOK);
	}
	
	public void testGetAnnotationSpans() throws Exception {
      
        LOGGER.info("Testing method \"getAnnotationSpans\" of class Evaluator");
        
		File pennbioIO = new File(PENNBIO_IO);
		Converter conv = new Converter(Converter.TYPE_IO);
		IOToken[] ioTokens = conv.textToIOTokens(pennbioIO);
		
		Method getAnnotationSpans = null;
		HashMap<String, HashMap<String,String>> ret = null;
		Object[] actualParams = {ioTokens};
		
		Class[] formalParams = {IOToken[].class};
		try {
			getAnnotationSpans = Evaluator.class.getDeclaredMethod("getAnnotationSpans", formalParams);
			getAnnotationSpans.setAccessible(true);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			ret = (HashMap<String, HashMap<String,String>>)getAnnotationSpans.invoke(Evaluator.class, actualParams);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        boolean allOK = ret != null
          && ret.get("malignancy") != null
          && ret.get("malignancy").get("0,5") != null
          && ret.get("malignancy").get("0,5").equals("malignancy#malignancy#malignancy#malignancy#malignancy#malignancy");
        
        if (allOK) {
          LOGGER.info("Everything okay");
        }
        else {
          LOGGER.error("Error with method \"getAnnotationSpans\" of class Evaluator");
        }
        
		assertTrue (allOK);
	}
	
	public void testEvaluateSingle() throws Exception {
		
        LOGGER.info("Testing method \"evaluateSingle\" of class Evaluator");
      
		File pennbioIO = new File(PENNBIO_IO);
		/* In this file, two malignancy-sequences were split by an 0.
		 * Thus, two sequences were not recognised (false negative) and
		 * four sequences (two split sequences == four false sequences)
		 * were recognised wrongly.
		 */
		File pennbioIOError = new File(PENNBIO_IO_ERROR);
		Converter conv = new Converter(Converter.TYPE_IO);
		IOToken[] ioTokens = conv.textToIOTokens(pennbioIO);
		IOToken[] ioTokensPred = conv.textToIOTokens(pennbioIOError);
		
		HashMap<String, HashMap<String,String>> predSpans = null;
		HashMap<String, HashMap<String,String>> goldSpans = null;
		
		Method evaluateSingle = null;
		Method getAnnotationSpans = null;
		int[] ret = null;
		Object[] actualAnnoParams = {ioTokens};
		Object[] actualEvalParams = new Object[2];
		
		Class[] formalParamsEval = {HashMap.class,HashMap.class};
		Class[] formalParamsAnno = {IOToken[].class};
		try {
			evaluateSingle = Evaluator.class.getDeclaredMethod("evaluateSingle", formalParamsEval);
			getAnnotationSpans = Evaluator.class.getDeclaredMethod("getAnnotationSpans", formalParamsAnno);
			evaluateSingle.setAccessible(true);
			getAnnotationSpans.setAccessible(true);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			goldSpans = (HashMap<String, HashMap<String,String>>)getAnnotationSpans.invoke(Evaluator.class, actualAnnoParams);
			actualAnnoParams[0] = ioTokensPred;
			predSpans = (HashMap<String, HashMap<String,String>>)getAnnotationSpans.invoke(Evaluator.class, actualAnnoParams);
			actualEvalParams[0] = goldSpans.get("malignancy");
			actualEvalParams[1] = predSpans.get("malignancy");
			ret = (int[])evaluateSingle.invoke(Evaluator.class, actualEvalParams);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//                 tp              fn              fp
        boolean allOK = ret[0] == 40 && ret[1] == 2 && ret[2] == 4;
        
        if (allOK) {
          LOGGER.info("Everything okay");
        }
        else {
          LOGGER.error("Error with method \"evaluateSingle\" of class Evaluator");
        }
		
		assertTrue (allOK);
	}
	
	public void testEvaluate() throws Exception {
		
	    LOGGER.info("Testing method \"evaluate\" of class Evaluator");
      
		File pennbioIO = new File(PENNBIO_IO);
		/* In this file, two malignancy-sequences were split by an 0.
		 * Thus, two sequences were not recognised (false negative) and
		 * four sequences (two split sequences == four false sequences)
		 * were recognised wrongly.
		 */
		File pennbioIOError = new File(PENNBIO_IO_ERROR);
		Converter conv = new Converter(Converter.TYPE_IO);
		IOToken[] ioTokensGold = conv.textToIOTokens(pennbioIO);
		IOToken[] ioTokensPred = conv.textToIOTokens(pennbioIOError);
		
		EvaluationResult[] results = null;
		EvaluationResult result = null;
		String[] labels = {"malignancy", "gene-protein"};
		String label = "variation-location";
		
		results = Evaluator.evaluate(ioTokensGold, ioTokensPred);
		
		int counter = 0;
		
		for (int i=0; i<results.length; i++) {
			if (results[i].getFscore() == 1.0) {
				++counter;
			}
		}
        
        boolean allOK = counter == 8;
        
        if (allOK) {
          LOGGER.info("General method \"evaluate\": No error");
        }
        else {
          LOGGER.error("Error with general method \"evaluate\" of class Evaluator");
        }
        
		assertTrue (allOK);
		
		results = Evaluator.evaluate(ioTokensGold, ioTokensPred, labels);
		
        allOK = results.length == 2
          && results[0].getEvalLabel().equals(labels[0])
          && results[1].getEvalLabel().equals(labels[1]);
        
        if (allOK) {
          LOGGER.info("Method \"evaluate\" with array of labels: No error");
        }
        else {
          LOGGER.error("Error with method \"evaluate\" that evaluates an array of labels of class Evaluator");
        }
        
		assertTrue (allOK);
		
		result = Evaluator.evaluate(ioTokensGold, ioTokensPred, label);
		
        allOK = result.getEvalLabel().equals(label);
        
        if (allOK) {
          LOGGER.info("Method \"evaluate\" with one labels: No error");
        }
        else {
          LOGGER.error("Error with method \"evaluate\" that evaluates one label of class Evaluator");
        }
        
		assertTrue (allOK);
	}
}
