package de.julielab.xml;

import de.julielab.xml.util.XMISplitterException;
import org.apache.uima.jcas.JCas;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

public abstract class XmiSplitter {
    public abstract XmiSplitterResult process(byte[] xmiData, JCas aCas, int nextPossibleId, Map<String, Integer> existingSofaIdMap) throws XMISplitterException;
}
