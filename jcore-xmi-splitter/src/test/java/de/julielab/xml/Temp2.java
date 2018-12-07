package de.julielab.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Temp2 {
    public static void main(String args[]) throws XMLStreamException {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();

        Map<String, String> namespacesAndXmiVersion = new HashMap<>();
        namespacesAndXmiVersion.put("ext","http:///de/julielab/jcore/types/ext.ecore");
        namespacesAndXmiVersion.put("medical","http:///de/julielab/jcore/types/medical.ecore");
        namespacesAndXmiVersion.put("types", "http:///de/julielab/jcore/types.ecore");
        namespacesAndXmiVersion.put("ace", "http:///de/julielab/jcore/types/ace.ecore");
        namespacesAndXmiVersion.put("mantra", "http:///de/julielab/jcore/types/mantra.ecore");
        namespacesAndXmiVersion.put("mmax", "http:///de/julielab/jcore/types/mmax.ecore");
        namespacesAndXmiVersion.put("noNamespace", "http:///uima/noNamespace.ecore");
        namespacesAndXmiVersion.put("jcore", "http:///de/julielab/jcore.ecore");
        namespacesAndXmiVersion.put("muc7", "http:///de/julielab/jcore/types/muc7.ecore");
        namespacesAndXmiVersion.put("bootstrep", "http:///de/julielab/jcore/types/bootstrep.ecore");
        namespacesAndXmiVersion.put("dta", "http:///de/julielab/jcore/types/extensions/dta.ecore");
        namespacesAndXmiVersion.put("pubmed", "http:///de/julielab/jcore/types/pubmed.ecore");
        namespacesAndXmiVersion.put("cas", "http:///uima/cas.ecore");
        namespacesAndXmiVersion.put("xmi", "http://www.omg.org/XMI");
        namespacesAndXmiVersion.put("xmi:version", "2.0");
        namespacesAndXmiVersion.put("stemnet", "http:///de/julielab/jcore/types/stemnet.ecore");
        namespacesAndXmiVersion.put("tcas", "http:///uima/tcas.ecore");
        namespacesAndXmiVersion.put("wikipedia", "http:///de/julielab/jcore/types/wikipedia.ecore");
        List<Attribute> xmiAtt = new ArrayList<>();
        List<Namespace> xmiNS = new ArrayList<>();
        for (Map.Entry<String, String> nsEntry : namespacesAndXmiVersion.entrySet()) {
            if (nsEntry.getKey().equals("xmi:version")) {
                QName qName = new QName("http://www.omg.org/XMI", "version");
                xmiAtt.add(eventFactory.createAttribute(qName, nsEntry.getValue()));
            } else
                xmiNS.add(eventFactory.createNamespace(nsEntry.getKey(), nsEntry.getValue()));
        }
        StartElement xmiStart = eventFactory.createStartElement("xmi", "http://www.omg.org/XMI", "XMI",
                xmiAtt.iterator(), xmiNS.iterator());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEventWriter w = outputFactory.createXMLEventWriter(baos);
        w.add(xmiStart);
        w.add(eventFactory.createCharacters("dummy"));
        w.add(eventFactory.createEndElement("xmi", "http://www.omg.org/XMI", "XMI"));
        w.close();
    }
}
