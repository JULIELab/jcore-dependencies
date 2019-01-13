package salvo.jesus.graph.xml;

import salvo.jesus.graph.*;
import salvo.jesus.graph.visual.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import org.apache.xml.serialize.*;
import org.apache.xerces.dom.*;
import java.io.*;
import java.util.*;

/**
 * An implmentation of GraphToXMLHandler that serializes a Graph and/or
 * VisualGraph to XGMML via DOM.
 * <p>
 * This class simply delegates handling of events to either the inner class
 * <tt>GraphToXGMMLDOMHandler.GraphHandler</tt> or
 * <tt>GraphToXGMMLDOMHandler.VisualGraphHandler</tt>, depending on which
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

public class GraphToXGMMLDOMHandler implements GraphToXMLHandler {

    protected XMLSerializer             serializer;
    private GraphToXMLEventGenerator    eventGenerator;

    /**
     * Delegate that performs the actual serialization
     */
    private GraphToXGMMLDOMHandler      delegate;

    public GraphToXGMMLDOMHandler( GraphToXMLEventGenerator eventGenerator, XMLSerializer serializer  ){
        this.eventGenerator = eventGenerator;
        this.serializer = serializer;
    }

    /**
     * Initializes the delegate to be the inner class <tt>GraphHandler</tt>,
     * and calls the <tt>startSerialize()</tt> method of the delegate.
     */
    public void startSerialize( Graph graph ) throws IOException {
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
     * Calls the delegate's <tt>serializerVertex()</tt> method
     */
    public void serializeVertex( Vertex vertex ) {
        this.delegate.serializeVertex( vertex );
    }

    /**
     * Calls the delegate's <tt>endSerializerVertex()</tt> method
     */
    public void endSerializeVertex( Vertex vertex ) {
        this.delegate.endSerializeVertex( vertex );
    }

    /**
     * Calls the delegate's <tt>serializerEdge()</tt> method
     */
    public void serializeEdge( Edge edge ) {
        this.delegate.serializeEdge( edge );
    }

    /**
     * Calls the delegate's <tt>serializerEdge()</tt> method
     */
    public void endSerializeEdge( Edge edge ) {
        this.delegate.endSerializeEdge( edge );
    }

    /**
     * Calls the delegate's <tt>endSerialize()</tt> method
     */
    public void endSerialize() throws IOException {
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
    class CommonHandler extends GraphToXGMMLDOMHandler {
        private Graph                   graph;
        private Document                doc;
        private Element                 rootElement;
        private TreeMap                 vertexIDMap;
        private long                    idAttribute;

        public CommonHandler( GraphToXMLEventGenerator eventGenerator, XMLSerializer serializer ) {
            super( eventGenerator, serializer );
        }

        /**
         * Creates the Document object with the necessary root node, <graph>, already
         * added.
         */
        private Document createDocument( Graph graph ) {
            Document doc = new DocumentImpl();
            this.rootElement = doc.createElement( XGMML.GRAPH_ELEMENT_LITERAL );
            this.rootElement.setAttribute( XGMML.VENDOR_ATTRIBUTE_LITERAL, "OpenJGraph" );
            this.rootElement.setAttribute( XGMML.DIRECTED_ATTRIBUTE_LITERAL, graph instanceof DirectedGraph ? "1": "0" );

            if( graph instanceof DirectedAcyclicGraph ) {
                Element attDAGElement = doc.createElement( XGMML.ATT_ELEMENT_LITERAL );
                attDAGElement.setAttribute( XGMML.TYPE_ATTRIBUTE_LITERAL, "integer" );
                attDAGElement.setAttribute( XGMML.NAME_ATTRIBUTE_LITERAL,
                    XGMML.ATT_ELEMENT_NAME_ATTRIBUTE_VALUE_DAG );
                attDAGElement.setAttribute( XGMML.VALUE_ATTRIBUTE_LITERAL, "1" );
                this.rootElement.appendChild( attDAGElement );
            }
            if( graph instanceof WeightedGraph ) {
                Element attDAGElement = doc.createElement( XGMML.ATT_ELEMENT_LITERAL );
                attDAGElement.setAttribute( XGMML.TYPE_ATTRIBUTE_LITERAL, "integer" );
                attDAGElement.setAttribute( XGMML.NAME_ATTRIBUTE_LITERAL,
                    XGMML.ATT_ELEMENT_NAME_ATTRIBUTE_VALUE_WEIGHTED );
                attDAGElement.setAttribute( XGMML.VALUE_ATTRIBUTE_LITERAL, "1" );
                this.rootElement.appendChild( attDAGElement );
            }
            doc.appendChild( this.rootElement );

            // Add the graphFactory
            Element attElement = doc.createElement( XGMML.ATT_ELEMENT_LITERAL );
            attElement.setAttribute( XGMML.NAME_ATTRIBUTE_LITERAL,
                XGMML.ATT_ELEMENT_NAME_ATTRIBUTE_VALUE_GRAPHFACTORY );
            attElement.setAttribute( XGMML.VALUE_ATTRIBUTE_LITERAL,
                graph.getGraphFactory().getClass().getName() );
            attElement.setAttribute( XGMML.TYPE_ATTRIBUTE_LITERAL, "string" );
            this.rootElement.appendChild( attElement );

            // Add the traversal
            attElement = doc.createElement( XGMML.ATT_ELEMENT_LITERAL );
            attElement.setAttribute( XGMML.NAME_ATTRIBUTE_LITERAL,
                XGMML.ATT_ELEMENT_NAME_ATTRIBUTE_VALUE_TRAVERSAL );
            attElement.setAttribute( XGMML.VALUE_ATTRIBUTE_LITERAL,
                graph.getTraversal().getClass().getName() );
            attElement.setAttribute( XGMML.TYPE_ATTRIBUTE_LITERAL, "string" );
            this.rootElement.appendChild( attElement );

            return doc;
        }

        /**
         * Initialized the DOM's Document object by calling <tt>createDocument()</tt>.
         * Also initialized the <tt>TreeMap</tt> that stores the mapping between
         * a <tt>Vertex</tt> and its corresponding <tt>id</tt> attribute value.
         */
        public void startSerialize( Graph graph ) {
            this.doc = this.createDocument( graph );
            this.vertexIDMap = new TreeMap( new VertexComparator() );
            this.idAttribute = 1;
        }

        /**
         * Initialized the DOM's Document object by calling <tt>createDocument()</tt>.
         * Also initialized the <tt>TreeMap</tt> that stores the mapping between
         * a <tt>Vertex</tt> and its corresponding <tt>id</tt> attribute value.
         */
        public void startSerialize( VisualGraph vGraph ) {
            this.doc = this.createDocument( vGraph.getGraph() );
            this.vertexIDMap = new TreeMap( new VertexComparator() );
            this.idAttribute = 1;
        }

        /**
         * Writes out the <tt>Document</tt> via DOM serialization.
         */
        public void endSerialize() throws IOException {
            OutputFormat format = new OutputFormat();

            format.setOmitXMLDeclaration( false );
            format.setEncoding( OutputFormat.Defaults.Encoding );
            format.setIndenting( true );
            format.setDoctype( XGMML.PUBLIC_ID, XGMML.SYSTEM_ID );

            this.serializer.setOutputFormat( format );
            this.serializer.serialize( doc );
        }

        /**
         * Empty method implementation
         */
        public void serializeVertex( Vertex vertex ) {}

        /**
         * Puts the mapping between the <tt>Vertex </tt> and its corresponding
         * <tt>id</tt> attribute value into the internal <tt>TreeMap</tt>, and
         * increments the <tt>id</tt> attribute value for the next <tt>Vertex</tt>.
         */
        public void endSerializeVertex( Vertex vertex ) {
            this.vertexIDMap.put( vertex, new Long( this.idAttribute ));
            this.idAttribute++;
        }

        /**
         * Empty method implementation
         */
        public void serializeEdge( Edge edge ) {}

        /**
         * Empty method implementation
         */
        public void endSerializeEdge( Edge edge ) {}

        protected Document getDocument() {
            return this.doc;
        }

        protected Element getRootElement() {
            return this.rootElement;
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

    }

    /**
     * Inner class delegate of <tt>GraphToXGMMLDOMHandler</tt> for
     * serializing <tt>Graph</tt> objects and not <tt>VisualGraph</tt> objects.
     *
     * @author  Jesus M. Salvo Jr.
     */
    class GraphHandler extends GraphToXGMMLDOMHandler {

        private CommonHandler   commonHandler;

        public GraphHandler( GraphToXMLEventGenerator eventGenerator,
                XMLSerializer serializer, CommonHandler commonHandler )
        {
            super( eventGenerator, serializer );
            this.commonHandler = commonHandler;
        }

        /**
         * Calls <tt>CommonHandler.startSerialize()</tt>, but also sets
         * the <tt>&lt;graph&gt;</tt> element's <tt>Graphic</tt> attribute to <tt>0</tt>
         */
        public void startSerialize( Graph graph ) throws IOException {
            this.commonHandler.startSerialize( graph );

            Element rootElement = this.commonHandler.getRootElement();
            rootElement.setAttribute( XGMML.GRAPHIC_ATTRIBUTE_LITERAL, "0" );
        }

        /**
         * Create the <tt>&lt;node&gt;</tt> element for the specified <tt>Vertex</tt>,
         * with the addition of an <tt>&lt;att&gt;</tt> element as a child node
         * of the <tt>&lt;node&gt;</tt> element to identify the name of the
         * class implementing the <tt>Vertex</tt> interface. The created
         * <tt>&lt;node&gt;</tt> element is then appended to the <tt>Document</tt>'s
         * root element.
         */
        public void serializeVertex( Vertex vertex ) {
            Document        doc = this.commonHandler.getDocument();
            Element         rootElement = this.commonHandler.getRootElement();
            TreeMap         vertexIDMap = this.commonHandler.getVertexIDMap();

            Element vertexElement = doc.createElement( XGMML.NODE_ELEMENT_LITERAL );
            vertexElement.setAttribute( XGMML.LABEL_ATTRIBUTE_LITERAL, vertex.toString() );
            vertexElement.setAttribute( XGMML.ID_ATTRIBUTE_LITERAL,
                Long.toString( this.commonHandler.getCurrentIDAttribute() ) );

            Element attElement = doc.createElement( XGMML.ATT_ELEMENT_LITERAL );
            attElement.setAttribute( XGMML.NAME_ATTRIBUTE_LITERAL,
                "className" );
            attElement.setAttribute( XGMML.VALUE_ATTRIBUTE_LITERAL,
                vertex.getClass().getName() );
            attElement.setAttribute( XGMML.TYPE_ATTRIBUTE_LITERAL, "string" );

            vertexElement.appendChild( attElement );
            rootElement.appendChild( vertexElement );
        }

        /**
         * Calls <tt>CommonHandler.endSerializeVertex()</tt>
         */
        public void endSerializeVertex( Vertex vertex ) {
            this.commonHandler.endSerializeVertex( vertex );
        }

        /**
         * Create the <tt>&lt;edge&gt;</tt> element for the specified <tt>Edge</tt>,
         * with the addition of an <tt>&lt;att&gt;</tt> element as a child node
         * of the <tt>&lt;edge&gt;</tt> element to identify the name of the
         * class implementing the <tt>Edge</tt> interface. The created
         * <tt>&lt;edge&gt;</tt> element is then appended to the <tt>Document</tt>'s
         * root element.
         */
        public void serializeEdge( Edge edge ) {
            Document        doc = this.commonHandler.getDocument();
            Element         rootElement = this.commonHandler.getRootElement();
            TreeMap         vertexIDMap = this.commonHandler.getVertexIDMap();

            Element edgeElement = doc.createElement( XGMML.EDGE_ELEMENT_LITERAL );
            if( edge instanceof DirectedEdgeImpl ) {
                DirectedEdge    dEdge = (DirectedEdge) edge;
                edgeElement.setAttribute( XGMML.SOURCE_ATTRIBUTE_LITERAL,
                    ((Long) vertexIDMap.get( dEdge.getSource() )).toString() );
                edgeElement.setAttribute( XGMML.TARGET_ATTRIBUTE_LITERAL,
                    ((Long) vertexIDMap.get( dEdge.getSink() )).toString() );
            }
            else {
                edgeElement.setAttribute( XGMML.SOURCE_ATTRIBUTE_LITERAL,
                    ((Long) vertexIDMap.get( edge.getVertexA() )).toString() );
                edgeElement.setAttribute( XGMML.TARGET_ATTRIBUTE_LITERAL,
                    ((Long) vertexIDMap.get( edge.getVertexB() )).toString() );
            }
            edgeElement.setAttribute( XGMML.LABEL_ATTRIBUTE_LITERAL, edge.toString() );
            if( edge instanceof WeightedEdgeImpl ) {
                edgeElement.setAttribute( XGMML.WEIGHT_ATTRIBUTE_LITERAL,
                    Double.toString( ((WeightedEdge) edge).getWeight()) );
            }

            Element attElement = doc.createElement( XGMML.ATT_ELEMENT_LITERAL );
            attElement.setAttribute( XGMML.NAME_ATTRIBUTE_LITERAL,
                "className" );
            attElement.setAttribute( XGMML.VALUE_ATTRIBUTE_LITERAL,
                edge.getClass().getName() );
            attElement.setAttribute( XGMML.TYPE_ATTRIBUTE_LITERAL, "string" );

            edgeElement.appendChild( attElement );
            rootElement.appendChild( edgeElement );
        }

        /**
         * Calls <tt>CommonHandler.endSerializeEdge()</tt>
         */
        public void endSerializeEdge( Edge edge ) {
            this.commonHandler.endSerializeEdge( edge );
        }

        /**
         * Calls <tt>CommonHandler.endSerialize()</tt>
         */
        public void endSerialize() throws IOException {
            this.commonHandler.endSerialize();
        }
    }

    /**
     * Inner class delegate of <tt>GraphToXGMMLDOMHandler</tt> for
     * serializing <tt>VisualGraph</tt> objects.
     *
     * @author  Jesus M. Salvo Jr.
     */
    class VisualGraphHandler extends GraphToXGMMLDOMHandler {

        private VisualGraph     vGraph;
        private GraphHandler    graphHandler;
        private CommonHandler   commonHandler;

        public VisualGraphHandler( GraphToXMLEventGenerator eventGenerator,
                XMLSerializer serializer, CommonHandler commonHandler )
        {
            super( eventGenerator, serializer );
            this.commonHandler = commonHandler;
            this.graphHandler = new GraphHandler( eventGenerator, serializer, this.commonHandler );
        }

        /**
         * Calls <tt>CommonHandler.startSerialize()</tt>, but also sets
         * the <tt>&lt;graph&gt;</tt> element's <tt>Graphic</tt> attribute to <tt>1</tt>
         */
        public void startSerialize( VisualGraph vGraph ) throws Exception {
            this.vGraph = vGraph;

            this.commonHandler.startSerialize( vGraph );

            Element rootElement = this.commonHandler.getRootElement();
            rootElement.setAttribute( XGMML.GRAPHIC_ATTRIBUTE_LITERAL, "1" );

            if( vGraph.getVisualVertexPainterFactory() != null ) {
                Element attElement = this.commonHandler.getDocument().createElement( XGMML.ATT_ELEMENT_LITERAL );
                attElement.setAttribute( XGMML.NAME_ATTRIBUTE_LITERAL, "vertexPainterFactory" );
                attElement.setAttribute( XGMML.TYPE_ATTRIBUTE_LITERAL, "string" );
                attElement.setAttribute( XGMML.VALUE_ATTRIBUTE_LITERAL,
                    vGraph.getVisualVertexPainterFactory().getClass().getName() );
                rootElement.appendChild( attElement );
            }

            if( vGraph.getVisualEdgePainterFactory() != null ) {
                Element attElement = this.commonHandler.getDocument().createElement( XGMML.ATT_ELEMENT_LITERAL );
                attElement.setAttribute( XGMML.NAME_ATTRIBUTE_LITERAL, "edgePainterFactory" );
                attElement.setAttribute( XGMML.TYPE_ATTRIBUTE_LITERAL, "string" );
                attElement.setAttribute( XGMML.VALUE_ATTRIBUTE_LITERAL,
                    vGraph.getVisualEdgePainterFactory().getClass().getName() );
                rootElement.appendChild( attElement );
            }

            if( vGraph.getVisualGraphComponentEditorFactory() != null ) {
                Element attElement = this.commonHandler.getDocument().createElement( XGMML.ATT_ELEMENT_LITERAL );
                attElement.setAttribute( XGMML.NAME_ATTRIBUTE_LITERAL, "componentEditorFactory" );
                attElement.setAttribute( XGMML.TYPE_ATTRIBUTE_LITERAL, "string" );
                attElement.setAttribute( XGMML.VALUE_ATTRIBUTE_LITERAL,
                    vGraph.getVisualGraphComponentEditorFactory().getClass().getName() );
                rootElement.appendChild( attElement );
            }

            if( vGraph.getGraphLayoutManager() != null ) {
                Element attElement = this.commonHandler.getDocument().createElement( XGMML.ATT_ELEMENT_LITERAL );
                attElement.setAttribute( XGMML.NAME_ATTRIBUTE_LITERAL, "layoutManager" );
                attElement.setAttribute( XGMML.TYPE_ATTRIBUTE_LITERAL, "string" );
                attElement.setAttribute( XGMML.VALUE_ATTRIBUTE_LITERAL,
                    vGraph.getGraphLayoutManager().getClass().getName() );
                rootElement.appendChild( attElement );
            }

        }

        /**
         * Calls <tt>GraphHandler.serializeVertex()</tt>, then adds a
         * <tt>&ltgraph&gt;</tt> element describing the visual properties
         * of the <tt>VisualVertex</tt> as allowed by XGMML.
         * <p>
         * The corresponding <tt>VisualVertex</tt> of the specified <tt>Vertex</tt>
         * is obtained via tt>VisualGraph.getVisualVertex()</tt>.
         */
        public void serializeVertex( Vertex vertex ) {
            Document        doc = this.commonHandler.getDocument();
            Element         rootElement = this.commonHandler.getRootElement();
            TreeMap         vertexIDMap = this.commonHandler.getVertexIDMap();

            this.graphHandler.serializeVertex( vertex );

            VisualVertex vVertex = this.vGraph.getVisualVertex( vertex );
            java.awt.geom.Rectangle2D bounds = vVertex.getBounds2D();

            Element graphicElement = doc.createElement( XGMML.GRAPHICS_ELEMENT_LITERAL );
            graphicElement.setAttribute(
                XGMML.WIDTH_ATTRIBUTE_LITERAL, Double.toString(bounds.getWidth()) );
            graphicElement.setAttribute(
                XGMML.HEIGHT_ATTRIBUTE_LITERAL, Double.toString(bounds.getHeight()) );
            graphicElement.setAttribute(
                XGMML.FONT_ATTRIBUTE_LITERAL, vVertex.getFont().getName() );
            graphicElement.setAttribute(
                XGMML.VISIBLE_ATTRIBUTE_LITERAL, "true" );
            graphicElement.setAttribute(
                XGMML.FILL_ATTRIBUTE_LITERAL, Integer.toHexString( vVertex.getFillcolor().getRGB()) );
            graphicElement.setAttribute(
                XGMML.OUTLINE_ATTRIBUTE_LITERAL, Integer.toHexString( vVertex.getOutlinecolor().getRGB()) );

            Element centerElement = doc.createElement( XGMML.CENTER_ELEMENT_LITERAL );
            centerElement.setAttribute(
                XGMML.X_ATTRIBUTE_LITERAL, Double.toString(bounds.getCenterX()) );
            centerElement.setAttribute(
                XGMML.Y_ATTRIBUTE_LITERAL, Double.toString(bounds.getCenterY()) );
            graphicElement.appendChild( centerElement );

            Element attElement = doc.createElement( XGMML.ATT_ELEMENT_LITERAL );
            attElement.setAttribute( XGMML.NAME_ATTRIBUTE_LITERAL, "className" );
            attElement.setAttribute( XGMML.TYPE_ATTRIBUTE_LITERAL, "string" );
            attElement.setAttribute( XGMML.VALUE_ATTRIBUTE_LITERAL, vVertex.getClass().getName() );
            graphicElement.appendChild( attElement );

            attElement = doc.createElement( XGMML.ATT_ELEMENT_LITERAL );
            attElement.setAttribute( XGMML.NAME_ATTRIBUTE_LITERAL, "painter" );
            attElement.setAttribute( XGMML.TYPE_ATTRIBUTE_LITERAL, "string" );
            attElement.setAttribute( XGMML.VALUE_ATTRIBUTE_LITERAL, vVertex.getPainter().getClass().getName() );
            graphicElement.appendChild( attElement );

            Node vertexNode = rootElement.getLastChild();
            vertexNode.appendChild( graphicElement );
        }

        public void endSerializeVertex( Vertex vertex ) {
            this.commonHandler.endSerializeVertex( vertex );
        }

        /**
         * Calls <tt>GraphHandler.serializeEdge()</tt>, then adds a
         * <tt>&ltgraph&gt;</tt> element describing the visual properties
         * of the <tt>VisualEdge</tt> as allowed by XGMML.
         * <p>
         * The corresponding <tt>VisualEdge</tt> of the specified <tt>Edge</tt>
         * is obtained via tt>VisualGraph.getVisualEdge()</tt>.
         */
        public void serializeEdge( Edge edge ) {
            Document        doc = this.commonHandler.getDocument();
            Element         rootElement = this.commonHandler.getRootElement();

            this.graphHandler.serializeEdge( edge );
            VisualEdge vEdge = this.vGraph.getVisualEdge( edge );

            Element graphicElement = doc.createElement( XGMML.GRAPHICS_ELEMENT_LITERAL );
            graphicElement.setAttribute(
                XGMML.FONT_ATTRIBUTE_LITERAL, vEdge.getFont().getFontName() );
            graphicElement.setAttribute(
                XGMML.VISIBLE_ATTRIBUTE_LITERAL, "true" );
            graphicElement.setAttribute(
                XGMML.FILL_ATTRIBUTE_LITERAL, Integer.toHexString( vEdge.getFillcolor().getRGB()) );
            graphicElement.setAttribute(
                XGMML.OUTLINE_ATTRIBUTE_LITERAL, Integer.toHexString( vEdge.getOutlinecolor().getRGB()) );

            Element lineElement = doc.createElement( XGMML.LINE_ELEMENT_LITERAL );
            Element pointElement;
            double edgeSegment[] = new double[6];

            java.awt.geom.PathIterator pathIterator =
                vEdge.getGeneralPath().getPathIterator( null );
            while( !pathIterator.isDone() ) {
                int segmentType = pathIterator.currentSegment( edgeSegment );
                if( segmentType != java.awt.geom.PathIterator.SEG_CLOSE ) {
                    pointElement = doc.createElement( XGMML.POINT_ELEMENT_LITERAL );
                    pointElement.setAttribute(
                        XGMML.X_ATTRIBUTE_LITERAL, Double.toString( edgeSegment[0] ));
                    pointElement.setAttribute(
                        XGMML.Y_ATTRIBUTE_LITERAL, Double.toString( edgeSegment[1] ));
                    lineElement.appendChild( pointElement );
                }
                pathIterator.next();
            }
            graphicElement.appendChild( lineElement );

            Element attElement = doc.createElement( XGMML.ATT_ELEMENT_LITERAL );
            attElement.setAttribute( XGMML.NAME_ATTRIBUTE_LITERAL, "className" );
            attElement.setAttribute( XGMML.TYPE_ATTRIBUTE_LITERAL, "string" );
            attElement.setAttribute( XGMML.VALUE_ATTRIBUTE_LITERAL, vEdge.getClass().getName() );
            graphicElement.appendChild( attElement );

            attElement = doc.createElement( XGMML.ATT_ELEMENT_LITERAL );
            attElement.setAttribute( XGMML.NAME_ATTRIBUTE_LITERAL, "painter" );
            attElement.setAttribute( XGMML.TYPE_ATTRIBUTE_LITERAL, "string" );
            attElement.setAttribute( XGMML.VALUE_ATTRIBUTE_LITERAL, vEdge.getPainter().getClass().getName() );
            graphicElement.appendChild( attElement );

            Node edgeNode = rootElement.getLastChild();
            edgeNode.appendChild( graphicElement );
        }

        public void endSerializeEdge( Edge edge ) {
            this.commonHandler.endSerializeEdge( edge );
        }

        public void endSerialize() throws IOException {
            this.commonHandler.endSerialize();
        }

    }

}
