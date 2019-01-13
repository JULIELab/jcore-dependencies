package salvo.jesus.graph.xml;

import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import salvo.jesus.graph.*;

/**
 * @author  Jesus M. Salvo Jr.
 */

public class XMLToGraphReader {

    private InputSource     source;
    private EntityResolver  resolver;
    private XMLToGraphDelegatorHandler  handler;

    public XMLToGraphReader( InputStream in, XMLToGraphHandler delegateHandler ) {
        this.source = new InputSource( in );
        this.handler = new XMLToGraphDelegatorHandler( delegateHandler );
    }

    public XMLToGraphReader( Reader reader, XMLToGraphHandler delegateHandler ) {
        this.source = new InputSource( reader );
        this.handler = new XMLToGraphDelegatorHandler( delegateHandler );
    }

    public void parse() throws IOException, SAXException {
        XMLReader   reader = XMLReaderFactory.createXMLReader( "org.apache.xerces.parsers.SAXParser" );

        resolver = new DTDResolver();
        reader.setContentHandler( this.handler );
        reader.setEntityResolver( resolver );
        reader.parse( source );
    }
}
