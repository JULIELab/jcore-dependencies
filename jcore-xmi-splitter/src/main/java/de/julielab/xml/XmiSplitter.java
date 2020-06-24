package de.julielab.xml;

import de.julielab.xml.util.XMISplitterException;
import org.apache.uima.cas.TypeSystem;

import java.util.Map;

public interface XmiSplitter {
    String DOCUMENT_MODULE_LABEL = "DOCUMENT-MODULE";
    XmiSplitterResult process(byte[] xmiData, TypeSystem ts, int nextPossibleId, Map<String, Integer> existingSofaIdMap) throws XMISplitterException;
}
