package salvo.jesus.graph.xml;

import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import salvo.jesus.graph.*;

/**
 * This class is responsible for parsing an XGMML input using SAX2 and
 * returning a Graph instance. An implementation of <tt>ContentHandler</tt>,
 * <tt>XGMMLLContentHandler</tt> is used to process SAX events.
 *
 * @author  Jesus M. Salvo Jr.
 */

public class XGMMLReader {

    private InputSource     source;
    private EntityResolver  resolver;
    private XGMMLContentHandler handler;

    public XGMMLReader( InputStream in, XGMMLContentHandler handler ) {
        this.source = new InputSource( in );
        this.handler = handler;
    }

    public XGMMLReader( Reader reader, XGMMLContentHandler handler ) {
        this.source = new InputSource( reader );
        this.handler = handler;
    }

    public Graph parse() throws IOException, SAXException {
        XMLReader   reader = XMLReaderFactory.createXMLReader( "org.apache.xerces.parsers.SAXParser" );

        resolver = new DTDResolver();
        reader.setContentHandler( this.handler );
        reader.setEntityResolver( resolver );
        reader.parse( source );
        return handler.getGraph();
    }
}
