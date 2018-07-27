package de.julielab.xml;

import de.julielab.xml.util.XMISplitterException;
import org.apache.uima.jcas.JCas;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

public interface XmiSplitter {
    XmiSplitterResult process(byte[] xmiData, JCas aCas, int nextPossibleId, Map<String, Integer> existingSofaIdMap) throws XMISplitterException;
}
