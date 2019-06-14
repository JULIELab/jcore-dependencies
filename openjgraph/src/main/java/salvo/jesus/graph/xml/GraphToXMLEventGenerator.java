package salvo.jesus.graph.xml;

import salvo.jesus.graph.Edge;
import salvo.jesus.graph.Graph;
import salvo.jesus.graph.Vertex;
import salvo.jesus.graph.visual.VisualGraph;

/**
 * An interface to serialize Graphs to XML.
 *
 * @author  Jesus M. Salvo Jr.
 */

public interface GraphToXMLEventGenerator {

    public void addHandler( GraphToXMLHandler handlerToAdd );

    public void removeHandler( GraphToXMLHandler handlerToRemove );

    /**
     * Method to be implemented by subclasses to serialize a Graph
     */
    public void serialize( Graph graph ) throws Exception;

    public void serialize( VisualGraph vGraph ) throws Exception;

    public void notifyStartSerialize( Graph graph ) throws Exception;

    public void notifyStartSerialize( VisualGraph vGraph ) throws Exception;

    public void notifySerializeVertex( Vertex vertex ) throws Exception;

    public void notifySerializeEdge( Edge edge ) throws Exception;

    public void notifyEndSerialize() throws Exception;
}
