package de.julielab.xml;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

public class TestAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		System.out.println("This Test AE does nothing (" + TestAnnotator.class.getName() + ")");
	}

}
