package salvo.jesus.graph.xml;

import org.xml.sax.Attributes;
import salvo.jesus.graph.*;
import salvo.jesus.graph.algorithm.GraphTraversal;
import salvo.jesus.graph.visual.VisualEdge;
import salvo.jesus.graph.visual.VisualGraph;
import salvo.jesus.graph.visual.VisualVertex;
import salvo.jesus.graph.visual.drawing.Painter;
import salvo.jesus.graph.visual.layout.GraphLayoutManager;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.lang.reflect.Constructor;


/**
 * Custom XMLToGraphHandler for parsing an XGMML input source using SAX2.
 *
 * @author      Jesus M. Salvo Jr.
 */

public class XGMMLContentHandler extends XMLToGraphHandler {

    Vertex  vertex;
    Edge    edge;
    Vertex  vertexA;
    Vertex  vertexB;

    boolean     graphic = false;
    boolean     directed = false;
    boolean     directedAcyclic = false;
    boolean     weighted = false;
    String      graphFactoryClassName;
    String      traversalClassName;
    public String      className;

    String      graphicsClassName;
    String      painterClassName;
    String      layoutManagerClassName;

    double  width;
    double  height;
    String  fontName;
    Color   fillColor;
    Color   outlineColor;

    double  centerX;
    double  centerY;

    GeneralPath path;
    boolean firstPoint;


    public XGMMLContentHandler() {
        super();
    }


    public void instantiateGraph() throws Exception {
        if( this.directedAcyclic )
            this.graph = new DirectedAcyclicGraphImpl();
        else if( this.weighted )
            this.graph = new WeightedGraphImpl();
        else if( this.directed )
            this.graph = new DirectedGraphImpl();
        else
            this.graph = new GraphImpl();

        // Set the GraphFactory used by the Graph
        if( this.graphFactoryClassName != null ) {
            GraphFactory factory = ( GraphFactory ) Class.forName( this.graphFactoryClassName ).newInstance();
            this.graph.setGraphFactory( factory );
        }

        if( this.traversalClassName != null ) {
            // Set the GraphTraversal used by the Graph
            Class graphClass;
            if( this.directed ) {
                graphClass = Class.forName( "salvo.jesus.graph.DirectedGraph" );
            }
            else {
                graphClass = Class.forName( "salvo.jesus.graph.Graph" );
            }
            Class traversalClass = Class.forName( this.traversalClassName );
            Class[] argTypes = { graphClass };
            Constructor traversalConstructor = traversalClass.getConstructor( argTypes );

            Object[] constructorArgs = { this.graph };
            GraphTraversal traversal = ( GraphTraversal ) traversalConstructor.newInstance( constructorArgs );
            this.graph.setTraversal( traversal );
        }

        // Create a VisualGraph anyway even if the XGMML input source is really
        // not a "graphic" ( visual ) graph
        this.vGraph = new VisualGraph();
        this.vGraph.setGraph( this.graph );

        if( this.graphic ) {
            Class layoutClass = Class.forName( this.layoutManagerClassName );
            Class visualGraphClass = Class.forName( "salvo.jesus.graph.visual.VisualGraph" );
            Class[] argTypes = { visualGraphClass };
            Constructor layoutConstructor = layoutClass.getConstructor( argTypes );

            Object[] constructorArgs = { this.vGraph };
            GraphLayoutManager layoutManager = ( GraphLayoutManager )
                layoutConstructor.newInstance( constructorArgs );
            this.vGraph.setGraphLayoutManager( layoutManager );
        }
    }

    public void instantiateVertex() throws Exception {
        StackEntry  entry = (StackEntry) this.elementStack.peek();
        Attributes  attribs = entry.getElementAttributes();
        String label = attribs.getValue( XGMML.LABEL_ATTRIBUTE_LITERAL );
        this.vertex = new VertexImpl( label );

        this.graph.add( this.vertex );

        if( this.graphic ) {
            VisualVertex vVertex = new VisualVertex( this.vertex, this.vGraph );
            vVertex.setLocation( this.centerX, this.centerY );
            vVertex.setOutlinecolor( this.outlineColor );
            vVertex.setFillcolor( this.fillColor );
            // Setting the font currently does not completely work.
            // Also, there is no option in XGMML to save the font type and font size
            vVertex.setFont( new Font( this.fontName, Font.PLAIN, 10 ));
            // There is also no direct option to store the line segments
            // making up the boundaries of the vertex
            /*
            vVertex.setGeneralPath( new Rectangle2D.Double().getPathIterator( null ) );
            */

            Class painterClass = Class.forName( this.painterClassName );
            vVertex.setPainter( (Painter) painterClass.newInstance() );

            this.vGraph.setVisualVertex( this.vertex, vVertex );
        }

        this.mapIDToVertex( attribs.getValue( XGMML.ID_ATTRIBUTE_LITERAL ), this.vertex );
    }

    /**
     * Detects if the graph is directed or not. The Graph instance
     * is not instantiated here as we need to check if there
     * are &lt;att&gt; elements of &lt;graph&lt; that further defines
     * the graph's type, such as if it is weighted or a dag.
     */
    public void startGraphElement( Attributes attribs ) {
        String directed = attribs.getValue( XGMML.DIRECTED_ATTRIBUTE_LITERAL );
        if( directed != null && directed.equals( "1" ))
            this.directed = true;
        String graphic = attribs.getValue( XGMML.GRAPHIC_ATTRIBUTE_LITERAL );
        if( graphic != null && graphic.equals( "1" ))
            this.graphic = true;
    }

    /**
     * Process &lt;att&gt; element. Internal processing within this method
     * only occur if the top item on the stack is a result of the &lt;graph&gt;
     * element. Therefore, internal processing only occurs if the parent element
     * is &lt;graph&gt;
     */
    public void startAttElement( Attributes attribs )
    {
        StackEntry  entry = (StackEntry) this.elementStack.peek();

        if( entry.getElementName().equals( XGMML.GRAPH_ELEMENT_LITERAL )) {
            if( attribs.getValue( XGMML.NAME_ATTRIBUTE_LITERAL ).equals( XGMML.ATT_ELEMENT_NAME_ATTRIBUTE_VALUE_WEIGHTED ) &&
                attribs.getValue( XGMML.VALUE_ATTRIBUTE_LITERAL ).equals( "1" ))
            {
                this.weighted = true;
            }

            if( attribs.getValue( XGMML.NAME_ATTRIBUTE_LITERAL ).equals( XGMML.ATT_ELEMENT_NAME_ATTRIBUTE_VALUE_DAG ) &&
                attribs.getValue( XGMML.VALUE_ATTRIBUTE_LITERAL ).equals( "1" ))
            {
                this.directedAcyclic = true;
            }

            if( attribs.getValue( XGMML.NAME_ATTRIBUTE_LITERAL ).equals( XGMML.ATT_ELEMENT_NAME_ATTRIBUTE_VALUE_GRAPHFACTORY )) {
                this.graphFactoryClassName = attribs.getValue( XGMML.VALUE_ATTRIBUTE_LITERAL );
            }

            if( attribs.getValue( XGMML.NAME_ATTRIBUTE_LITERAL ).equals( XGMML.ATT_ELEMENT_NAME_ATTRIBUTE_VALUE_TRAVERSAL )) {
                this.traversalClassName = attribs.getValue( XGMML.VALUE_ATTRIBUTE_LITERAL );
            }

            if( attribs.getValue( XGMML.NAME_ATTRIBUTE_LITERAL).equals( "layoutManager" )) {
                this.layoutManagerClassName = attribs.getValue( XGMML.VALUE_ATTRIBUTE_LITERAL );
            }
        }

        if( entry.getElementName().equals( XGMML.NODE_ELEMENT_LITERAL )) {
            this.processNodeAttElement( attribs );
        }

        if( entry.getElementName().equals( XGMML.EDGE_ELEMENT_LITERAL )) {
            this.processEdgeAttElement( attribs );
        }

        if( entry.getElementName().equals( XGMML.GRAPHICS_ELEMENT_LITERAL )) {
            this.processGraphicsAttElement( attribs );
        }


    }

    public void startNodeElement( Attributes attribs ) {
        this.className = null;
    }

    public void endNodeElement() throws Exception {

    }

    public void startEdgeElement( Attributes attribs ) {
        this.className = null;
    }

    /**
     * Processes &lt;edge&gt; elements.
     * <p>
     * Besides checking for the <tt>weight</tt> attribute to determine
     * if it is a weighted edge, it also checks the type of graph
     * that was created. Hence, if the graph is an instance of
     * <tt>DirectedGraph</tt>, then the type of Edge created is
     * either a <tt>DirectedWeightedEdgeImpl</tt> or a <tt>DirectedEdgeImpl</tt>.
     */
    public void endEdgeElement() throws Exception {
        StackEntry  entry = (StackEntry) this.elementStack.peek();
        Attributes attribs = entry.getElementAttributes();

        String  source = attribs.getValue( XGMML.SOURCE_ATTRIBUTE_LITERAL );
        String  target = attribs.getValue( XGMML.TARGET_ATTRIBUTE_LITERAL );

        this.vertexA = (Vertex) this.vertexIDMap.get( source );
        this.vertexB = (Vertex) this.vertexIDMap.get( target );
    }



    public void startGraphicsElement( Attributes attribs ) throws Exception {
        if( attribs.getValue( XGMML.WIDTH_ATTRIBUTE_LITERAL ) != null )
            this.width = Double.parseDouble( attribs.getValue( XGMML.WIDTH_ATTRIBUTE_LITERAL ));
        if( attribs.getValue( XGMML.HEIGHT_ATTRIBUTE_LITERAL ) != null )
            this.height = Double.parseDouble( attribs.getValue( XGMML.HEIGHT_ATTRIBUTE_LITERAL ));

        this.fontName = attribs.getValue( XGMML.FONT_ATTRIBUTE_LITERAL );
        // We can't parse using Integer.parseInt() because the alpha is from bits 24 to 31,
        // and in the case where bit 31 is 1, it is beyond Intetger.MAX_VALUE ( or
        // should have been interpreted as the minus sign, but no luck.
        this.fillColor = new Color(
            Long.valueOf( attribs.getValue( XGMML.FILL_ATTRIBUTE_LITERAL ), 16 ).intValue(), true);
        this.outlineColor = new Color(
            Long.valueOf( attribs.getValue( XGMML.OUTLINE_ATTRIBUTE_LITERAL ), 16 ).intValue(), true);
    }

    public void startCenterElement( Attributes attribs ) throws Exception {
        this.centerX = Double.parseDouble( attribs.getValue( XGMML.X_ATTRIBUTE_LITERAL ));
        this.centerY = Double.parseDouble( attribs.getValue( XGMML.Y_ATTRIBUTE_LITERAL ));
    }

    public void startLineElement( Attributes attribs ) throws Exception {
        this.path = new GeneralPath();
        this.firstPoint = true;
    }

    public void startPointElement( Attributes attribs ) throws Exception {
        if( this.firstPoint ) {
            this.path.moveTo(
                Float.parseFloat( attribs.getValue( XGMML.X_ATTRIBUTE_LITERAL )),
                Float.parseFloat( attribs.getValue( XGMML.Y_ATTRIBUTE_LITERAL )));
            this.firstPoint = false;
        }
        else
            this.path.lineTo(
                Float.parseFloat( attribs.getValue( XGMML.X_ATTRIBUTE_LITERAL )),
                Float.parseFloat( attribs.getValue( XGMML.Y_ATTRIBUTE_LITERAL )));
    }


    public void instantiateEdge() throws Exception {

        if ( ( this.directed || this.directedAcyclic ) && !this.weighted )
            this.edge = new DirectedEdgeImpl( this.vertexA, this.vertexB );
        else if( this.weighted && !this.directed && !this.directedAcyclic ) {
            StackEntry  entry = (StackEntry) this.elementStack.peek();
            Attributes  attribs = entry.getElementAttributes();
            String      weight = attribs.getValue( XGMML.WEIGHT_ATTRIBUTE_LITERAL );

            this.edge = new WeightedEdgeImpl( this.vertexA, this.vertexB,
                new Double( weight ).doubleValue() );
        }
        else if ( ( this.directed || this.directedAcyclic ) && this.weighted ) {
            StackEntry  entry = (StackEntry) this.elementStack.peek();
            Attributes  attribs = entry.getElementAttributes();
            String      weight = attribs.getValue( XGMML.WEIGHT_ATTRIBUTE_LITERAL );

            this.edge = new DirectedWeightedEdgeImpl( this.vertexA, this.vertexB,
                new Double( weight ).doubleValue() );
        }
        else {
            this.edge = new EdgeImpl( this.vertexA, this.vertexB );
        }

        this.graph.addEdge( this.edge );

        if( this.graphic ) {
            VisualEdge vEdge = new VisualEdge( this.edge, this.vGraph );
            vEdge.setFillcolor( this.fillColor );
            vEdge.setOutlinecolor( this.outlineColor );
            // There is no option in XGMML to save the font type and font size
            vEdge.setFont( new Font( this.fontName, Font.PLAIN, 10 ));
            vEdge.setGeneralPath( this.path );

            Class painterClass = Class.forName( this.painterClassName );
            vEdge.setPainter( (Painter) painterClass.newInstance() );


            this.vGraph.setVisualEdge( this.edge, vEdge );
        }
    }

    /**
     * Classes extending this abstract class may need to override this method.
     * All this method do is read in the value of the <tt>value</tt> attribute
     * of the <tt><att/></tt> element where the <tt>name</tt> attribute is
     * <tt>className</tt>, and reading that value into an internal variable.
     * <p>
     * When overriding this method, ensure that you call this method via
     * <p>
     * <tt>super.processNodeElement( attribs );</tt>
     * <p>
     * so that the internal variable is set.
     */
    public void processNodeAttElement( Attributes attribs ) {
        if( attribs.getValue( XGMML.NAME_ATTRIBUTE_LITERAL).equals( "className" ))
            this.className = attribs.getValue( XGMML.VALUE_ATTRIBUTE_LITERAL );
    }

    /**
     * Classes extending this abstract class may need to override this method.
     * All this method do is read in the value of the <tt>value</tt> attribute
     * of the <tt><att/></tt> element where the <tt>name</tt> attribute is
     * <tt>className</tt>, and reading that value into an internal variable.
     * <p>
     * When overriding this method, ensure that you call this method via
     * <p>
     * <tt>super.processNodeElement( attribs );</tt>
     * <p>
     * so that the internal variable is set.
     */
    public void processEdgeAttElement( Attributes attribs ) {
        if( attribs.getValue( XGMML.NAME_ATTRIBUTE_LITERAL).equals( "className" ))
            this.className = attribs.getValue( XGMML.VALUE_ATTRIBUTE_LITERAL );
    }

    public void processGraphicsAttElement( Attributes attribs ) {
        if( attribs.getValue( XGMML.NAME_ATTRIBUTE_LITERAL).equals( "className" ))
            this.graphicsClassName = attribs.getValue( XGMML.VALUE_ATTRIBUTE_LITERAL );
        if( attribs.getValue( XGMML.NAME_ATTRIBUTE_LITERAL).equals( "painter" ))
            this.painterClassName = attribs.getValue( XGMML.VALUE_ATTRIBUTE_LITERAL );
    }


    public String getVertexElementName() { return XGMML.NODE_ELEMENT_LITERAL; }
    public String getEdgeElementName() { return XGMML.EDGE_ELEMENT_LITERAL; }

}

