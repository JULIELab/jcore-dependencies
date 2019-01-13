package salvo.jesus.graph.xml;

import salvo.jesus.graph.*;
import salvo.jesus.graph.visual.*;
import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.apache.xml.serialize.*;
import org.apache.xerces.parsers.*;
import org.apache.xerces.utils.*;
import org.apache.xerces.framework.*;
import java.util.*;

/**
 * An implmentation of GraphToXMLHandler that serializes a Graph and/or
 * VisualGraph to XGMML via SAX.
 * <p>
 * This class simply delegates handling of events to either the inner class
 * <tt>GraphToXGMMLSAXHandler.GraphHandler</tt> or
 * <tt>GraphToXGMMLSAXHandler.VisualGraphHandler</tt>, depending on which
 * method between <tt>startSerialize()</tt> is called. Users should simply create
 * an instance of this class and should not be concerned of the inner classes,
 * and call the appropriate overloaded <tt>startSerialize()</tt> method.
 * <p>
 *
 * See <a href="http://www.cs.rpi.edu/~puninj/XGMML/draft-xgmml-20001006.html">
 * XGMML 1.0 Draft Specification</a>
 *
 * @author  Jesus M. Salvo Jr.
 */

public class GraphToXGMMLSAXHandler implements GraphToXMLHandler {

    private GraphToXMLEventGenerator    eventGenerator;
    protected XMLSerializer             serializer;

    /**
     * Delegate that performs the actual serialization
     */
    private GraphToXGMMLSAXHandler      delegate;

    public GraphToXGMMLSAXHandler( GraphToXMLEventGenerator eventGenerator, XMLSerializer serializer ) {
        this.eventGenerator = eventGenerator;
        this.serializer = serializer;
    }

    /**
     * Initializes the delegate to be the inner class <tt>GraphHandler</tt>,
     * and calls the <tt>startSerialize()</tt> method of the delegate.
     */
    public void startSerialize( Graph graph ) throws Exception
    {
        this.delegate = new GraphHandler( this.eventGenerator, this.serializer,
            new CommonHandler( this.eventGenerator, this.serializer ) );
        this.delegate.startSerialize( graph );
    }

    /**
     * Initializes the delegate to be the inner class <tt>VisualGraphHandler</tt>,
     * and calls the <tt>startSerialize()</tt> method of the delegate.
     */
    public void startSerialize( VisualGraph vGraph ) throws Exception {
        this.delegate = new VisualGraphHandler( this.eventGenerator, this.serializer,
            new CommonHandler( this.eventGenerator, this.serializer ) );
        this.delegate.startSerialize( vGraph );
    }

    /**
     * Calls the delegate's <tt>serializerEdge()</tt> method
     */
    public void serializeEdge( Edge edge ) throws Exception
    {
        this.delegate.serializeEdge( edge );
    }

    /**
     * Calls the delegate's <tt>endSerializerEdge()</tt> method
     */
    public void endSerializeEdge( Edge edge ) throws Exception {
        this.delegate.endSerializeEdge( edge );
    }

    /**
     * Calls the delegate's <tt>serializerVertex()</tt> method
     */
    public void serializeVertex( Vertex vertex ) throws Exception
    {
        this.delegate.serializeVertex( vertex );
    }

    /**
     * Calls the delegate's <tt>endSerializerVertex()</tt> method
     */
    public void endSerializeVertex( Vertex vertex ) throws Exception {
        this.delegate.endSerializeVertex( vertex );
    }

    /**
     * Calls the delegate's <tt>serializerVertex()</tt> method
     */
    public void endSerialize() throws Exception {
        this.delegate.endSerialize();
    }

    public GraphToXMLEventGenerator getEventGenerator() {
        return this.eventGenerator;
    }

    /**
     * Methods and instance variables common to either of the two inner class delegates
     * <tt>GraphHandler</tt> and <tt>VisualGraphHandler</tt>.
     *
     * @author  Jesus M. Salvo Jr.
     */
    class CommonHandler extends GraphToXGMMLSAXHandler {

        /**
         * Index of the DTD's name in the pool of Strings
         */
        protected int    DOCTYPE_NAME_STRINGPOOL_INDEX;

        /**
         * Index of the Public ID in the pool of Strings
         */
        protected int    PUBLIC_ID_STRINGPOOL_INDEX;

        /**
         * Index of the System ID in the pool of Strings
         */
        protected int    SYSTEM_ID_STRINGPOOL_INDEX;

        /**
         * Index of the "OpenJGraph" literal in the pool of Strings
         */
        protected int    OPENJGRAPH_LITERAL_STRINGPOOL_INDEX;

        /**
         * Index of the "graph" literal in the pool of Strings
         */
        protected int    GRAPH_ELEMENT_LITERAL_STRINGPOOL_INDEX;
        /**
         * Index of the "Vendor" literal in the pool of Strings
         */
        protected int    VENDOR_ATTRIBUTE_LITERAL_STRINGPOOL_INDEX;
        /**
         * Index of the "directed" literal in the pool of Strings
         */
        protected int    DIRECTED_ATTRIBUTE_LITERAL_STRINGPOOL_INDEX;
        /**
         * Index of the "Graphic" literal in the pool of Strings
         */
        protected int    GRAPHIC_ATTRIBUTE_LITERAL_STRINGPOOL_INDEX;

        /**
         * Index of the "att" literal in the pool of Strings
         */
        protected int    ATT_ELEMENT_LITERAL_STRINGPOOL_INDEX;
        /**
         * Index of the "weighted" literal in the pool of Strings
         */
        protected int    ATT_ELEMENT_NAME_ATTRIBUTE_VALUE_WEIGHTED_STRINGPOOL_INDEX;
        /**
         * Index of the "dag" literal in the pool of Strings
         */
        protected int    ATT_ELEMENT_NAME_ATTRIBUTE_VALUE_DAG_STRINGPOOL_INDEX;
        /**
         * Index of the "type" literal in the pool of Strings
         */
        protected int    TYPE_ATTRIBUTE_LITERAL_STRINGPOOL_INDEX;
        /**
         * Index of the "name" literal in the pool of Strings
         */
        protected int    NAME_ATTRIBUTE_LITERAL_STRINGPOOL_INDEX;
        /**
         * Index of the "value" literal in the pool of Strings
         */
        protected int    VALUE_ATTRIBUTE_LITERAL_STRINGPOOL_INDEX;

        /**
         * Index of the "node" literal in the pool of Strings
         */
        protected int    NODE_ELEMENT_LITERAL_STRINGPOOL_INDEX;

        /**
         * Index of the "edge" literal in the pool of Strings
         */
        protected int    EDGE_ELEMENT_LITERAL_STRINGPOOL_INDEX;
        /**
         * Index of the "source" literal in the pool of Strings
         */
        protected int    SOURCE_ATTRIBUTE_LITERAL_STRINGPOOL_INDEX;
        /**
         * Index of the "target" literal in the pool of Strings
         */
        protected int    TARGET_ATTRIBUTE_LITERAL_STRINGPOOL_INDEX;
        /**
         * Index of the "weight" literal in the pool of Strings
         */
        protected int    WEIGHT_ATTRIBUTE_LITERAL_STRINGPOOL_INDEX;

        /**
         * Index of the "id" literal in the pool of Strings
         */
        protected int    ID_ATTRIBUTE_LITERAL_STRINGPOOL_INDEX;
        /**
         * Index of the "label" literal in the pool of Strings
         */
        protected int    LABEL_ATTRIBUTE_LITERAL_STRINGPOOL_INDEX;

        /**
         * Index of the "string" literal in the pool of Strings
         */
        protected int    STRING_TYPE_STRINGPOOL_INDEX;
        /**
         * Index of the "integer" literal in the pool of Strings
         */
        protected int    INTEGER_TYPE_STRINGPOOL_INDEX;
        /**
         * Index of the "double" literal in the pool of Strings
         */
        protected int    DOUBLE_TYPE_STRINGPOOL_INDEX;
        /**
         * Index of the "boolean" literal in the pool of Strings
         */
        protected int    BOOLEAN_TYPE_STRINGPOOL_INDEX;

        /**
         * Index of the "1" literal in the pool of Strings
         */
        protected int    ZERO_LITERAL_STRINGPOOL_INDEX;
        /**
         * Index of the "0" literal in the pool of Strings
         */
        protected int    ONE_LITERAL_STRINGPOOL_INDEX;

        private XMLReaderAdapter    readerAdapter;
        private Graph       graph;
        private SAXParser   parser;
        // Wrap the parser with a XMLReaderAdapter so that we can call
        // startElement() and endElement() methods.
        private OutputFormat        format;
        private TreeMap     vertexIDMap;
        private long        idAttribute;

        /**
         * A pool of Strings that we will use often throughout the serialization.
         * These Strings include the element's name, attributes names, and possibly
         * values as well.
         */
        private StringPool  sPool;


        public CommonHandler( GraphToXMLEventGenerator eventGenerator, XMLSerializer serializer ) {
            super( eventGenerator, serializer );
            this.sPool = this.initStringPool();
        }

        /**
         * Initialized the SAXParser, XMLReaderAdapter and OutputFormat.
         * Serializes the DTD declaration.
         */
        public void startSerialize( Graph graph ) throws Exception {
            this.graph = graph;
            // Create a SAX2 parser: org.apache.xerces.parsers.SAXParser
            this.parser = new SAXParser();
            // Wrap the parser with a XMLReaderAdapter so that we can call
            // startElement() and endElement() methods.
            this.readerAdapter = new XMLReaderAdapter( parser );
            this.format = new OutputFormat();
            // Reference to the mappings between a vertex and its ID attribute
            this.vertexIDMap = new TreeMap( new VertexComparator() );
            // The id attribute that we assign to all elements
            this.idAttribute = 1;

            // Initialise the OutputFormat
            this.format.setOmitXMLDeclaration( false );
            this.format.setEncoding( OutputFormat.Defaults.Encoding );
            this.format.setIndenting( true );
            this.format.setDoctype( XGMML.PUBLIC_ID, XGMML.SYSTEM_ID );

            // Tell the serializer the OutputFormat to use
            this.serializer.setOutputFormat( format );

            // Tell the parser the ContentHander to use
            this.parser.setContentHandler( this.serializer );

            this.parser.startDocument();

            // Serialize the DTD declaration
            this.parser.startDTD( new QName( -1,
                    this.DOCTYPE_NAME_STRINGPOOL_INDEX, this.DOCTYPE_NAME_STRINGPOOL_INDEX ),
                this.PUBLIC_ID_STRINGPOOL_INDEX,
                this.SYSTEM_ID_STRINGPOOL_INDEX );
            this.parser.endDTD();

            // Tell the XMLReaderAdapter the DocumentHandler to use
            // Even though we have told the parser the ContentHandler to use,
            // remember that the XMLReadeAdapter is a wrapper around the SAXParser.
            // Therefore we need to indepedently tell the this adapter to DocumentHandler
            this.readerAdapter.setDocumentHandler( this.serializer );
        }

        /**
         * Empty method implementation
         */
        public void startSerialize( VisualGraph vGraph ) throws Exception {}

        /**
         * Empty method implementation
         */
        public void serializeVertex( Vertex vertex ) throws Exception {}

        /**
         * Serializes the end of the Vertex's element and maps
         * the Vertex to the unique ID attribute.
         */
        public void endSerializeVertex( Vertex vertex ) throws Exception {
            this.readerAdapter.endElement( "",
                XGMML.NODE_ELEMENT_LITERAL, XGMML.NODE_ELEMENT_LITERAL );

            // Store the mapping between the vertex and the ID attribute
            // This is later used when writing out the edges
            this.vertexIDMap.put( vertex, new Long( idAttribute ));
            this.incremenetIDAttribute();
        }

        /**
         * Empty method implementation
         */
        public void serializeEdge( Edge edge ) throws Exception {}

        /**
         * Serializes the end of the Edge's element.
         */
        public void endSerializeEdge( Edge edge ) throws Exception {
            this.readerAdapter.endElement( "",
                XGMML.EDGE_ELEMENT_LITERAL, XGMML.EDGE_ELEMENT_LITERAL );
        }

        /**
         * Serializes the end of the Graph's element.
         */
        public void endSerialize() throws Exception {
            this.readerAdapter.endElement( "",
                XGMML.GRAPH_ELEMENT_LITERAL, XGMML.GRAPH_ELEMENT_LITERAL );
            this.parser.endDocument();
        }

        protected XMLReaderAdapter getReaderAdapter() {
            return this.readerAdapter;
        }

        protected TreeMap getVertexIDMap() {
            return this.vertexIDMap;
        }

        protected long getCurrentIDAttribute() {
            return this.idAttribute;
        }

        protected void incremenetIDAttribute() {
            this.idAttribute++;
        }

        /**
         * Initialise the pool of Strings that we shall be using
         */
        private StringPool initStringPool() {
            StringPool  sPool = new StringPool();

            this.DOCTYPE_NAME_STRINGPOOL_INDEX = sPool.addString( XGMML.DOCTYPE_NAME );
            this.PUBLIC_ID_STRINGPOOL_INDEX = sPool.addString( XGMML.PUBLIC_ID );
            this.SYSTEM_ID_STRINGPOOL_INDEX = sPool.addString( XGMML.SYSTEM_ID );

            this.GRAPH_ELEMENT_LITERAL_STRINGPOOL_INDEX = sPool.addString( XGMML.GRAPH_ELEMENT_LITERAL );
            this.VENDOR_ATTRIBUTE_LITERAL_STRINGPOOL_INDEX = sPool.addString( XGMML.VENDOR_ATTRIBUTE_LITERAL );
            this.OPENJGRAPH_LITERAL_STRINGPOOL_INDEX = sPool.addString( "OpenJGraph" );
            this.GRAPHIC_ATTRIBUTE_LITERAL_STRINGPOOL_INDEX = sPool.addString( XGMML.GRAPHIC_ATTRIBUTE_LITERAL );
            this.DIRECTED_ATTRIBUTE_LITERAL_STRINGPOOL_INDEX = sPool.addString( XGMML.DIRECTED_ATTRIBUTE_LITERAL );

            this.STRING_TYPE_STRINGPOOL_INDEX = sPool.addString( "string" );
            this.INTEGER_TYPE_STRINGPOOL_INDEX = sPool.addString( "integer" );
            this.DOUBLE_TYPE_STRINGPOOL_INDEX = sPool.addString( "double" );
            this.BOOLEAN_TYPE_STRINGPOOL_INDEX = sPool.addString( "boolean" );

            this.ZERO_LITERAL_STRINGPOOL_INDEX = sPool.addString( "0" );
            this.ONE_LITERAL_STRINGPOOL_INDEX = sPool.addString( "1" );

            return sPool;
        }

        /**
         * Serialise other meta information about the graph into an <tt>att</tt> element
         * under the <tt>graph</tt> element.
         */
        protected void serializeOtherGraphMetaInformation()
                throws Exception
        {
            AttributesImpl attrs = new AttributesImpl();

            if( graph instanceof DirectedAcyclicGraph ) {
                // Add the name attribute whose value is "dag"
                attrs.addAttribute( "",
                    XGMML.NAME_ATTRIBUTE_LITERAL, XGMML.NAME_ATTRIBUTE_LITERAL,
                    this.sPool.toString( this.STRING_TYPE_STRINGPOOL_INDEX ),
                    XGMML.ATT_ELEMENT_NAME_ATTRIBUTE_VALUE_DAG );
                attrs.addAttribute( "",
                    XGMML.TYPE_ATTRIBUTE_LITERAL, XGMML.TYPE_ATTRIBUTE_LITERAL,
                    this.sPool.toString( this.STRING_TYPE_STRINGPOOL_INDEX ),
                    "integer" );
                attrs.addAttribute( "",
                    XGMML.VALUE_ATTRIBUTE_LITERAL, XGMML.VALUE_ATTRIBUTE_LITERAL,
                    this.sPool.toString( this.STRING_TYPE_STRINGPOOL_INDEX ),
                    this.sPool.toString( this.ONE_LITERAL_STRINGPOOL_INDEX ));

                readerAdapter.startElement( "",
                    XGMML.ATT_ELEMENT_LITERAL, XGMML.ATT_ELEMENT_LITERAL, attrs );
                readerAdapter.endElement( "",
                    XGMML.ATT_ELEMENT_LITERAL, XGMML.ATT_ELEMENT_LITERAL );
            }
            else if( graph instanceof WeightedGraph ) {
                // Add the name attribute whose value is "weighted"
                attrs.addAttribute( "",
                    XGMML.NAME_ATTRIBUTE_LITERAL, XGMML.NAME_ATTRIBUTE_LITERAL,
                    this.sPool.toString( this.STRING_TYPE_STRINGPOOL_INDEX ),
                    XGMML.ATT_ELEMENT_NAME_ATTRIBUTE_VALUE_WEIGHTED );
                attrs.addAttribute( "",
                    XGMML.TYPE_ATTRIBUTE_LITERAL, XGMML.TYPE_ATTRIBUTE_LITERAL,
                    this.sPool.toString( this.STRING_TYPE_STRINGPOOL_INDEX ),
                    "integer" );
                attrs.addAttribute( "",
                    XGMML.VALUE_ATTRIBUTE_LITERAL, XGMML.VALUE_ATTRIBUTE_LITERAL,
                    this.sPool.toString( this.STRING_TYPE_STRINGPOOL_INDEX ),
                    this.sPool.toString( this.ONE_LITERAL_STRINGPOOL_INDEX ));

                readerAdapter.startElement( "",
                    XGMML.ATT_ELEMENT_LITERAL, XGMML.ATT_ELEMENT_LITERAL, attrs );
                readerAdapter.endElement( "",
                    XGMML.ATT_ELEMENT_LITERAL, XGMML.ATT_ELEMENT_LITERAL );
            }

            // Add the graphFactory
            attrs = new AttributesImpl();
            attrs.addAttribute( "",
                XGMML.NAME_ATTRIBUTE_LITERAL, XGMML.NAME_ATTRIBUTE_LITERAL,
                this.sPool.toString( this.STRING_TYPE_STRINGPOOL_INDEX ),
                XGMML.ATT_ELEMENT_NAME_ATTRIBUTE_VALUE_GRAPHFACTORY );
            attrs.addAttribute( "",
                XGMML.TYPE_ATTRIBUTE_LITERAL, XGMML.TYPE_ATTRIBUTE_LITERAL,
                this.sPool.toString( this.STRING_TYPE_STRINGPOOL_INDEX ),
                "string" );
            attrs.addAttribute( "",
                XGMML.VALUE_ATTRIBUTE_LITERAL, XGMML.VALUE_ATTRIBUTE_LITERAL,
                this.sPool.toString( this.STRING_TYPE_STRINGPOOL_INDEX ),
                graph.getGraphFactory().getClass().getName() );
            readerAdapter.startElement( "",
                XGMML.ATT_ELEMENT_LITERAL, XGMML.ATT_ELEMENT_LITERAL, attrs );
            readerAdapter.endElement( "",
                XGMML.ATT_ELEMENT_LITERAL, XGMML.ATT_ELEMENT_LITERAL );

            // Add the traversal
            attrs = new AttributesImpl();
            attrs.addAttribute( "",
                XGMML.NAME_ATTRIBUTE_LITERAL, XGMML.NAME_ATTRIBUTE_LITERAL,
                this.sPool.toString( this.STRING_TYPE_STRINGPOOL_INDEX ),
                XGMML.ATT_ELEMENT_NAME_ATTRIBUTE_VALUE_TRAVERSAL );
            attrs.addAttribute( "",
                XGMML.TYPE_ATTRIBUTE_LITERAL, XGMML.TYPE_ATTRIBUTE_LITERAL,
                this.sPool.toString( this.STRING_TYPE_STRINGPOOL_INDEX ),
                "string" );
            attrs.addAttribute( "",
                XGMML.VALUE_ATTRIBUTE_LITERAL, XGMML.VALUE_ATTRIBUTE_LITERAL,
                this.sPool.toString( this.STRING_TYPE_STRINGPOOL_INDEX ),
                graph.getTraversal().getClass().getName() );
            readerAdapter.startElement( "",
                XGMML.ATT_ELEMENT_LITERAL, XGMML.ATT_ELEMENT_LITERAL, attrs );
            readerAdapter.endElement( "",
                XGMML.ATT_ELEMENT_LITERAL, XGMML.ATT_ELEMENT_LITERAL );

        }

    }

    /**
     * Inner class delegate of <tt>GraphToXGMMLSAXHandler</tt> for
     * serializing <tt>Graph</tt> objects and not <tt>VisualGraph</tt> objects.
     *
     * @author  Jesus M. Salvo Jr.
     */
    class GraphHandler extends GraphToXGMMLSAXHandler {

        private CommonHandler   commonHandler;

        public GraphHandler( GraphToXMLEventGenerator eventGenerator,
                XMLSerializer serializer, CommonHandler commonHandler ) {
            super( eventGenerator, serializer );
            this.commonHandler = commonHandler;
        }

        /**
         * Serializes the <tt>&lt;graph&gt;</tt> element, including setting the
         * <tt>Graphic</tt> attribute to <tt>0</tt>.
         */
        public void startSerialize( Graph graph ) throws Exception {
            this.commonHandler.startSerialize( graph );

            // Serialize the beginning tag of the graph element
            AttributesImpl attrs = new AttributesImpl();

            // Add the Graphic attribute
            attrs.addAttribute( "",
                XGMML.GRAPHIC_ATTRIBUTE_LITERAL, XGMML.GRAPHIC_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.BOOLEAN_TYPE_STRINGPOOL_INDEX ),
                this.commonHandler.sPool.toString( this.commonHandler.ZERO_LITERAL_STRINGPOOL_INDEX ) );
            // Add the Vendor attribute
            attrs.addAttribute( "",
                XGMML.VENDOR_ATTRIBUTE_LITERAL, XGMML.VENDOR_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                this.commonHandler.sPool.toString( this.commonHandler.OPENJGRAPH_LITERAL_STRINGPOOL_INDEX ));
            // Add the directed attribute
            attrs.addAttribute( "",
                XGMML.DIRECTED_ATTRIBUTE_LITERAL, XGMML.DIRECTED_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.BOOLEAN_TYPE_STRINGPOOL_INDEX ),
                graph instanceof DirectedGraph ?
                    this.commonHandler.sPool.toString( this.commonHandler.ONE_LITERAL_STRINGPOOL_INDEX ) :
                    this.commonHandler.sPool.toString( this.commonHandler.ZERO_LITERAL_STRINGPOOL_INDEX ));

            this.commonHandler.getReaderAdapter().startElement( "",
                XGMML.GRAPH_ELEMENT_LITERAL, XGMML.GRAPH_ELEMENT_LITERAL, attrs );

            // Serialize the att element for other meta information about the graph
            this.commonHandler.serializeOtherGraphMetaInformation();
        }

        /**
         * Empty method implementation
         */
        public void startSerialize( VisualGraph vGraph ) throws Exception {}

        /**
         * Serializes the <tt>&lt;node&gt;</tt> element
         */
        public void serializeVertex( Vertex vertex ) throws Exception {
            XMLReaderAdapter    readerAdapter = this.commonHandler.getReaderAdapter();
            TreeMap             vertexIDMap = this.commonHandler.getVertexIDMap();
            long                idAttribute = this.commonHandler.getCurrentIDAttribute();
            AttributesImpl      attrs = new AttributesImpl();

            // Add the id attribute
            attrs.addAttribute( "",
                XGMML.ID_ATTRIBUTE_LITERAL, XGMML.ID_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                Long.toString( idAttribute ) );
            // Add the label attribute
            attrs.addAttribute( "",
                XGMML.LABEL_ATTRIBUTE_LITERAL, XGMML.LABEL_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                vertex.toString() );

            readerAdapter.startElement( "", XGMML.NODE_ELEMENT_LITERAL, XGMML.NODE_ELEMENT_LITERAL, attrs);

            attrs.clear();
            // Add the name attribute
            attrs.addAttribute( "",
                XGMML.NAME_ATTRIBUTE_LITERAL, XGMML.NAME_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                "className" );
            // Add the type attribute
            attrs.addAttribute( "",
                XGMML.TYPE_ATTRIBUTE_LITERAL, XGMML.TYPE_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                "string" );
            // Add the value attribute
            attrs.addAttribute( "",
                XGMML.VALUE_ATTRIBUTE_LITERAL, XGMML.VALUE_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                vertex.getClass().getName() );

            readerAdapter.startElement( "", XGMML.ATT_ELEMENT_LITERAL, XGMML.ATT_ELEMENT_LITERAL, attrs);
            readerAdapter.endElement( "", XGMML.ATT_ELEMENT_LITERAL, XGMML.ATT_ELEMENT_LITERAL );

        }

        /**
         * Calls <tt>CommonHandler.endSerializeVertex()</tt>
         */
        public void endSerializeVertex( Vertex vertex ) throws Exception {
            this.commonHandler.endSerializeVertex( vertex );
        }

        /**
         * Serializes the <tt>&lt;edge&gt;</tt> element
         */
        public void serializeEdge( Edge edge ) throws Exception {
            XMLReaderAdapter    readerAdapter = this.commonHandler.getReaderAdapter();
            TreeMap         vertexIDMap = this.commonHandler.getVertexIDMap();
            AttributesImpl  attrs = new AttributesImpl();

            // Add the label attribute
            attrs.addAttribute( "",
                XGMML.LABEL_ATTRIBUTE_LITERAL, XGMML.LABEL_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                edge.toString() );

            if( edge instanceof DirectedEdgeImpl ) {
                DirectedEdge    dEdge = (DirectedEdge) edge;

                // Get the source and target attribute values via
                // getSource() and getSink() methods
                attrs.addAttribute( "",
                    XGMML.SOURCE_ATTRIBUTE_LITERAL, XGMML.SOURCE_ATTRIBUTE_LITERAL,
                    this.commonHandler.sPool.toString( this.commonHandler.INTEGER_TYPE_STRINGPOOL_INDEX ),
                    (( Long ) vertexIDMap.get( dEdge.getSource() )).toString() );
                attrs.addAttribute( "",
                    XGMML.TARGET_ATTRIBUTE_LITERAL, XGMML.TARGET_ATTRIBUTE_LITERAL,
                    this.commonHandler.sPool.toString( this.commonHandler.INTEGER_TYPE_STRINGPOOL_INDEX ),
                    (( Long ) vertexIDMap.get( dEdge.getSink() )).toString() );
            }
            else {
                // Get the source and target attribute values via
                // getVertexA() and getVertexB() methods
                attrs.addAttribute( "",
                    XGMML.SOURCE_ATTRIBUTE_LITERAL, XGMML.SOURCE_ATTRIBUTE_LITERAL,
                    this.commonHandler.sPool.toString( this.commonHandler.INTEGER_TYPE_STRINGPOOL_INDEX ),
                    (( Long ) vertexIDMap.get( edge.getVertexA() )).toString() );
                attrs.addAttribute( "",
                    XGMML.TARGET_ATTRIBUTE_LITERAL, XGMML.TARGET_ATTRIBUTE_LITERAL,
                    this.commonHandler.sPool.toString( this.commonHandler.INTEGER_TYPE_STRINGPOOL_INDEX ),
                    (( Long ) vertexIDMap.get( edge.getVertexB() )).toString() );
            }
            if( edge instanceof WeightedEdgeImpl ) {
                // Add the weight attribute
                attrs.addAttribute( "",
                    XGMML.WEIGHT_ATTRIBUTE_LITERAL, XGMML.WEIGHT_ATTRIBUTE_LITERAL,
                    this.commonHandler.sPool.toString( this.commonHandler.DOUBLE_TYPE_STRINGPOOL_INDEX ),
                    Double.toString( ((WeightedEdge) edge).getWeight()) );
            }
            readerAdapter.startElement( "",
                XGMML.EDGE_ELEMENT_LITERAL, XGMML.EDGE_ELEMENT_LITERAL, attrs );

            attrs.clear();
            // Add the name attribute
            attrs.addAttribute( "",
                XGMML.NAME_ATTRIBUTE_LITERAL, XGMML.NAME_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                "className" );
            // Add the type attribute
            attrs.addAttribute( "",
                XGMML.TYPE_ATTRIBUTE_LITERAL, XGMML.TYPE_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                "string" );
            // Add the value attribute
            attrs.addAttribute( "",
                XGMML.VALUE_ATTRIBUTE_LITERAL, XGMML.VALUE_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                edge.getClass().getName() );

            readerAdapter.startElement( "", XGMML.ATT_ELEMENT_LITERAL, XGMML.ATT_ELEMENT_LITERAL, attrs);
            readerAdapter.endElement( "", XGMML.ATT_ELEMENT_LITERAL, XGMML.ATT_ELEMENT_LITERAL );

        }

        /**
         * Calls <tt>CommonHandler.endSerializeEdge()</tt>
         */
        public void endSerializeEdge( Edge edge ) throws Exception {
            this.commonHandler.endSerializeEdge( edge );
        }

        /**
         * Calls <tt>CommonHandler.endSerialize()</tt>
         */
        public void endSerialize() throws Exception {
            this.commonHandler.endSerialize();
        }
    }

    /**
     * Inner class delegate of <tt>GraphToXGMMLSAXHandler</tt> for
     * serializing <tt>VisualGraph</tt> objects and not <tt>Graph</tt> objects.
     *
     * @author  Jesus M. Salvo Jr.
     */
    class VisualGraphHandler extends GraphToXGMMLSAXHandler {

        private VisualGraph     vGraph;
        private CommonHandler   commonHandler;
        private GraphHandler    graphHandler;

        public VisualGraphHandler( GraphToXMLEventGenerator eventGenerator,
                XMLSerializer serializer, CommonHandler commonHandler ) {
            super( eventGenerator, serializer );
            this.commonHandler = commonHandler;
            this.graphHandler = new GraphHandler( eventGenerator, serializer, this.commonHandler );
        }

        /**
         * Empty method implementation
         */
        public void startSerialize( Graph graph ) throws Exception {}

        /**
         * Serializes the <tt>&lt;graph&gt;</tt> element, including setting the
         * <tt>Graphic</tt> attribute to <tt>1</tt>.
         */
        public void startSerialize( VisualGraph vGraph ) throws Exception {
            this.vGraph = vGraph;
            this.commonHandler.startSerialize( vGraph.getGraph() );

            // Serialize the beginning tag of the graph element
            AttributesImpl attrs = new AttributesImpl();

            // Add the Graphic attribute
            attrs.addAttribute( "",
                XGMML.GRAPHIC_ATTRIBUTE_LITERAL, XGMML.GRAPHIC_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.BOOLEAN_TYPE_STRINGPOOL_INDEX ),
                this.commonHandler.sPool.toString( this.commonHandler.ONE_LITERAL_STRINGPOOL_INDEX ));
            // Add the Vendor attribute
            attrs.addAttribute( "",
                XGMML.VENDOR_ATTRIBUTE_LITERAL, XGMML.VENDOR_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                this.commonHandler.sPool.toString( this.commonHandler.OPENJGRAPH_LITERAL_STRINGPOOL_INDEX ));
            // Add the directed attribute
            attrs.addAttribute( "",
                XGMML.DIRECTED_ATTRIBUTE_LITERAL, XGMML.DIRECTED_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.BOOLEAN_TYPE_STRINGPOOL_INDEX ),
                vGraph.getGraph() instanceof DirectedGraph ?
                    this.commonHandler.sPool.toString( this.commonHandler.ONE_LITERAL_STRINGPOOL_INDEX ) :
                    this.commonHandler.sPool.toString( this.commonHandler.ZERO_LITERAL_STRINGPOOL_INDEX ));

            this.commonHandler.getReaderAdapter().startElement( "",
                XGMML.GRAPH_ELEMENT_LITERAL, XGMML.GRAPH_ELEMENT_LITERAL, attrs );

            // Serialize the att element for other meta information about the graph
            this.commonHandler.serializeOtherGraphMetaInformation();

            // =============NOTE: Need to also serialize vertexPainterFactory,
            // ============= EdgePainterFactory, and componentEditorFactory
            // Serialize the layoutmanager
            attrs.clear();
            // Add the name attribute
            attrs.addAttribute( "",
                XGMML.NAME_ATTRIBUTE_LITERAL, XGMML.NAME_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                "layoutManager" );
            // Add the type attribute
            attrs.addAttribute( "",
                XGMML.TYPE_ATTRIBUTE_LITERAL, XGMML.TYPE_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                "string" );
            // Add the type attribute
            attrs.addAttribute( "",
                XGMML.VALUE_ATTRIBUTE_LITERAL, XGMML.VALUE_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                this.vGraph.getGraphLayoutManager().getClass().getName() );

            this.commonHandler.getReaderAdapter().startElement( "",
                XGMML.ATT_ELEMENT_LITERAL, XGMML.ATT_ELEMENT_LITERAL, attrs );
            this.commonHandler.getReaderAdapter().endElement( "",
                XGMML.ATT_ELEMENT_LITERAL, XGMML.ATT_ELEMENT_LITERAL);
        }

        /**
         * Serializes the <tt>&lt;node&gt;</tt> element, including the
         * &ltgraphics&gt; element that describes the visual properties of the
         * <tt>VisualVertex</tt> as allowed by XGMML.
         * <p>
         * The corresponding <tt>VisualVertex</tt> of the specified <tt>Vertex</tt>
         * is obtained via tt>VisualGraph.getVisualVertex()</tt>.
         */
        public void serializeVertex( Vertex vertex ) throws Exception {
            XMLReaderAdapter    readerAdapter = this.commonHandler.getReaderAdapter();
            AttributesImpl  attrs = new AttributesImpl();

            this.graphHandler.serializeVertex( vertex );

            VisualVertex vVertex = this.vGraph.getVisualVertex( vertex );
            java.awt.geom.Rectangle2D bounds = vVertex.getBounds2D();

            attrs.addAttribute( "",
                XGMML.WIDTH_ATTRIBUTE_LITERAL, XGMML.WIDTH_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                Double.toString(bounds.getWidth()) );
            attrs.addAttribute( "",
                XGMML.HEIGHT_ATTRIBUTE_LITERAL, XGMML.HEIGHT_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                Double.toString(bounds.getHeight()) );
            attrs.addAttribute( "",
                XGMML.FONT_ATTRIBUTE_LITERAL, XGMML.FONT_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                vVertex.getFont().getName() );
            attrs.addAttribute( "",
                XGMML.VISIBLE_ATTRIBUTE_LITERAL, XGMML.VISIBLE_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                "true" );
            attrs.addAttribute( "",
                XGMML.FILL_ATTRIBUTE_LITERAL, XGMML.FILL_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                Integer.toHexString( vVertex.getFillcolor().getRGB()) );
            attrs.addAttribute( "",
                XGMML.OUTLINE_ATTRIBUTE_LITERAL, XGMML.OUTLINE_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                Integer.toHexString( vVertex.getOutlinecolor().getRGB()) );

            readerAdapter.startElement( "",
                XGMML.GRAPHICS_ELEMENT_LITERAL, XGMML.GRAPHICS_ELEMENT_LITERAL, attrs );

            attrs.clear();
            attrs.addAttribute( "",
                XGMML.X_ATTRIBUTE_LITERAL, XGMML.X_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                Double.toString(bounds.getCenterX()) );
            attrs.addAttribute( "",
                XGMML.Y_ATTRIBUTE_LITERAL, XGMML.Y_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                Double.toString(bounds.getCenterY()) );
            readerAdapter.startElement( "",
                XGMML.CENTER_ELEMENT_LITERAL, XGMML.CENTER_ELEMENT_LITERAL, attrs );
            readerAdapter.endElement( "",
                XGMML.CENTER_ELEMENT_LITERAL, XGMML.CENTER_ELEMENT_LITERAL );

            attrs.clear();
            attrs.addAttribute( "",
                XGMML.NAME_ATTRIBUTE_LITERAL, XGMML.NAME_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                "className" );
            attrs.addAttribute( "",
                XGMML.TYPE_ATTRIBUTE_LITERAL, XGMML.TYPE_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ));
            attrs.addAttribute( "",
                XGMML.VALUE_ATTRIBUTE_LITERAL, XGMML.VALUE_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                vVertex.getClass().getName() );
            readerAdapter.startElement( "",
                XGMML.ATT_ELEMENT_LITERAL, XGMML.ATT_ELEMENT_LITERAL, attrs );
            readerAdapter.endElement( "",
                XGMML.ATT_ELEMENT_LITERAL, XGMML.ATT_ELEMENT_LITERAL );

            attrs.clear();
            attrs.addAttribute( "",
                XGMML.NAME_ATTRIBUTE_LITERAL, XGMML.NAME_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                "painter" );
            attrs.addAttribute( "",
                XGMML.TYPE_ATTRIBUTE_LITERAL, XGMML.TYPE_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ));
            attrs.addAttribute( "",
                XGMML.VALUE_ATTRIBUTE_LITERAL, XGMML.VALUE_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                vVertex.getPainter().getClass().getName());
            readerAdapter.startElement( "",
                XGMML.ATT_ELEMENT_LITERAL, XGMML.ATT_ELEMENT_LITERAL, attrs );
            readerAdapter.endElement( "",
                XGMML.ATT_ELEMENT_LITERAL, XGMML.ATT_ELEMENT_LITERAL );

            readerAdapter.endElement( "",
                XGMML.GRAPHIC_ATTRIBUTE_LITERAL, XGMML.GRAPHIC_ATTRIBUTE_LITERAL);
        }

        /**
         * Calls <tt>CommonHandler.endSerializeVertex()</tt>
         */
        public void endSerializeVertex( Vertex vertex ) throws Exception {
            this.commonHandler.endSerializeVertex( vertex );
        }

        /**
         * Serializes the <tt>&lt;edge&gt;</tt> element, including the
         * &ltgraphics&gt; element that describes the visual properties of the
         * <tt>VisualEdge</tt> as allowed by XGMML.
         * <p>
         * The corresponding <tt>VisualEdge</tt> of the specified <tt>Edge</tt>
         * is obtained via tt>VisualGraph.getVisualEdge()</tt>.
         */
        public void serializeEdge( Edge edge ) throws Exception {
            XMLReaderAdapter    readerAdapter = this.commonHandler.getReaderAdapter();
            AttributesImpl      attrs = new AttributesImpl();

            this.graphHandler.serializeEdge( edge );

            VisualEdge vEdge = this.vGraph.getVisualEdge( edge );

            attrs.addAttribute( "",
                XGMML.FONT_ATTRIBUTE_LITERAL, XGMML.FONT_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                vEdge.getFont().getName() );
            attrs.addAttribute( "",
                XGMML.VISIBLE_ATTRIBUTE_LITERAL, XGMML.VISIBLE_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                "true" );
            attrs.addAttribute( "",
                XGMML.FILL_ATTRIBUTE_LITERAL, XGMML.FILL_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                Integer.toHexString( vEdge.getFillcolor().getRGB()) );
            attrs.addAttribute( "",
                XGMML.OUTLINE_ATTRIBUTE_LITERAL, XGMML.OUTLINE_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                Integer.toHexString( vEdge.getOutlinecolor().getRGB()) );

            readerAdapter.startElement( "",
                XGMML.GRAPHICS_ELEMENT_LITERAL, XGMML.GRAPHICS_ELEMENT_LITERAL, attrs );

            readerAdapter.startElement( "",
                XGMML.LINE_ELEMENT_LITERAL, XGMML.LINE_ELEMENT_LITERAL,
                new AttributesImpl());


            double edgeSegment[] = new double[6];

            java.awt.geom.PathIterator pathIterator =
                vEdge.getGeneralPath().getPathIterator( null );
            while( !pathIterator.isDone() ) {
                int segmentType = pathIterator.currentSegment( edgeSegment );
                if( segmentType != java.awt.geom.PathIterator.SEG_CLOSE ) {
                    attrs.clear();
                    attrs.addAttribute( "",
                        XGMML.X_ATTRIBUTE_LITERAL, XGMML.X_ATTRIBUTE_LITERAL,
                        this.commonHandler.sPool.toString( this.commonHandler.DOUBLE_TYPE_STRINGPOOL_INDEX ),
                        Double.toString( edgeSegment[0] ));
                    attrs.addAttribute( "",
                        XGMML.Y_ATTRIBUTE_LITERAL, XGMML.Y_ATTRIBUTE_LITERAL,
                        this.commonHandler.sPool.toString( this.commonHandler.DOUBLE_TYPE_STRINGPOOL_INDEX ),
                        Double.toString( edgeSegment[1] ));
                    readerAdapter.startElement( "",
                        XGMML.POINT_ELEMENT_LITERAL, XGMML.POINT_ELEMENT_LITERAL,
                        attrs );
                    readerAdapter.endElement( "",
                        XGMML.POINT_ELEMENT_LITERAL, XGMML.POINT_ELEMENT_LITERAL );
                }
                pathIterator.next();
            }

            readerAdapter.endElement( "",
                XGMML.LINE_ELEMENT_LITERAL, XGMML.LINE_ELEMENT_LITERAL );

            attrs.clear();
            attrs.addAttribute( "",
                XGMML.NAME_ATTRIBUTE_LITERAL, XGMML.NAME_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                "className" );
            attrs.addAttribute( "",
                XGMML.TYPE_ATTRIBUTE_LITERAL, XGMML.TYPE_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ));
            attrs.addAttribute( "",
                XGMML.VALUE_ATTRIBUTE_LITERAL, XGMML.VALUE_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                vEdge.getClass().getName() );
            readerAdapter.startElement( "",
                XGMML.ATT_ELEMENT_LITERAL, XGMML.ATT_ELEMENT_LITERAL, attrs );
            readerAdapter.endElement( "",
                XGMML.ATT_ELEMENT_LITERAL, XGMML.ATT_ELEMENT_LITERAL );

            attrs.clear();
            attrs.addAttribute( "",
                XGMML.NAME_ATTRIBUTE_LITERAL, XGMML.NAME_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                "painter" );
            attrs.addAttribute( "",
                XGMML.TYPE_ATTRIBUTE_LITERAL, XGMML.TYPE_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ));
            attrs.addAttribute( "",
                XGMML.VALUE_ATTRIBUTE_LITERAL, XGMML.VALUE_ATTRIBUTE_LITERAL,
                this.commonHandler.sPool.toString( this.commonHandler.STRING_TYPE_STRINGPOOL_INDEX ),
                vEdge.getPainter().getClass().getName());
            readerAdapter.startElement( "",
                XGMML.ATT_ELEMENT_LITERAL, XGMML.ATT_ELEMENT_LITERAL, attrs );
            readerAdapter.endElement( "",
                XGMML.ATT_ELEMENT_LITERAL, XGMML.ATT_ELEMENT_LITERAL );

            readerAdapter.endElement( "",
                XGMML.GRAPHIC_ATTRIBUTE_LITERAL, XGMML.GRAPHIC_ATTRIBUTE_LITERAL );

        }

        /**
         * Calls <tt>CommonHandler.endSerializeEdge()</tt>
         */
        public void endSerializeEdge( Edge edge ) throws Exception {
            this.commonHandler.endSerializeEdge( edge );
        }

        /**
         * Calls <tt>CommonHandler.endSerialize</tt>
         */
        public void endSerialize() throws Exception {
            this.commonHandler.endSerialize();
        }

    }
}
