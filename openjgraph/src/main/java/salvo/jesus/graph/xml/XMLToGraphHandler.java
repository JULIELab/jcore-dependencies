package salvo.jesus.graph.xml;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import java.util.*;
import salvo.jesus.graph.*;
import salvo.jesus.graph.visual.*;
import salvo.jesus.util.StringComparator;
import org.apache.log4j.Category;


/**
 * @author  Jesus M. Salvo Jr.
 */

public abstract class XMLToGraphHandler extends DefaultHandler {

    Stack       elementStack;
    TreeMap     vertexIDMap;

    Graph       graph;
    VisualGraph vGraph;

    Vertex      vertex;
    Edge        edge;

    /**
     * Log4J Category. The name of the category is the fully qualified name of the
     * enclosing class.
     */
    static        Category    logger;

    static {
        logger = Category.getInstance( XMLToGraphHandler.class.getName());
    }

    public XMLToGraphHandler() {}

    public void startDocument() {
        this.elementStack = new Stack();
        this.vertexIDMap = new TreeMap( new StringComparator() );
        this.graph = null;
        this.vGraph = null;
    }

    public void startElement( String uri, String localName,
            String qualifiedName, Attributes attribs )
    {
        // Important!!!! Do not store attribs in the stack. Instead, create a new
        // AttributesImpl since there is only ever 1 instance of attribs
        // created by Xerces. Each call to this method reuses the instance of Attributes.
        // If we therefore just store attribs in the stack, all Attributes in the stack
        // will actually just point to one single instance.
        StackEntry newEntry = new StackEntry( localName, new AttributesImpl( attribs ) );
        this.elementStack.push( newEntry );
    }

    /**
     * Pops the last item of the internal stack
     */
    public void endElement( String uri, String localName,
            String qualifiedName )
    {
        StackEntry entry = ( StackEntry ) this.elementStack.pop();
        Attributes attribs = entry.getElementAttributes();
    }

    public Graph getGraph() {
        return this.graph;
    }

    public VisualGraph getVisualGraph() {
        return this.vGraph;
    }

    public abstract void instantiateVertex() throws Exception;

    public abstract void instantiateEdge() throws Exception;

    public abstract void instantiateGraph() throws Exception;

    public void mapIDToVertex( String idValue, Vertex aVertex ) {
        this.vertexIDMap.put( idValue, aVertex );
    }

    public abstract String getVertexElementName();
    public abstract String getEdgeElementName();

}