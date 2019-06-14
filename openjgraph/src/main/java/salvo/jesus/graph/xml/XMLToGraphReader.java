package salvo.jesus.graph.xml;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

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
