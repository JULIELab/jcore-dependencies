package de.julielab.xml;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

public class Temp {
    public static void main(String[] args) throws XMLStreamException {
        String xmls[] = {
               "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rt><title1>PI3K-AKT-GSK3β</title1><title2>abcd efgh &amp; ijkl &#10;mnop</title2><title3>abcd efgh &quot; ijkl mnop</title3></rt>"
        };
        for (String xml : xmls) {
            parseTextAsXML(xml);
        }
    }

    private static Map<String, String> parseTextAsXML(String xml)
            throws XMLStreamException {

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        final byte[] bytes = xml.getBytes();
        InputStream in = new ByteArrayInputStream(bytes);

        XMLStreamReader eventReader = inputFactory.createXMLStreamReader(in,
                "UTF-8");

        int lastStartOffset = -1;
        while (eventReader.hasNext()) {
            final int next = eventReader.next();
            if (next == START_ELEMENT) {
                lastStartOffset = eventReader.getLocation().getCharacterOffset();
                System.out.println("Start string: " + xml.charAt(lastStartOffset));
                System.out.println("Start byte: " + new String(Arrays.copyOfRange(bytes, lastStartOffset, lastStartOffset+1)));
            }
            if (next == END_ELEMENT) {
                System.out.println("End string: " + new String(Arrays.copyOfRange(bytes, lastStartOffset-1, lastStartOffset)));
                System.out.println("End byte : " + new String(Arrays.copyOfRange(bytes, lastStartOffset-1, lastStartOffset)));
            }
            System.out.println();
        }
        return null;
    }
}
