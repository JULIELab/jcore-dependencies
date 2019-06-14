package salvo.jesus.graph.visual;

import salvo.jesus.graph.Vertex;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * State object that represents the vertex mode in a GraphPanel.
 * Vertex mode being the ability to interactively add a vertex into a graph.
 *
 * @author  Jesus M. Salvo Jr.
 */
public class GraphPanelVertexState extends GraphPanelState {

    /**
     * Creates a GraphPanelVertexState object for the specified GraphPanel object.
     */
    public GraphPanelVertexState( GraphPanel gpanel ) {
        super( gpanel );
    }

    /**
     * Creates a new vertex on the specified coordinate.
     */
    public GraphPanelState mousePressed( MouseEvent e ) {
        // Create a new vertex and set its location
        // to the coordinates of the mouse
        VisualGraph         vg;
        Vertex              newvertex;

        // Create a new vertex
        vg = gpanel.getVisualGraph();
        newvertex = vg.getGraph().getGraphFactory().createVertex();

        // Add the vertex to the graph
        try {
            vg.add( newvertex );
        }
        catch( Exception ex ) {
            ex.printStackTrace();
        }

        // Notify the VisualGraphComponent of the event
        // Do this before adding the new vertex onto the graph
        informTargetVisualGraphComponentOfMouseEvent(e);

        // Set the location of the visual representation of the vertex
        // to the coordinates of the mouse
        vg.getVisualVertex( newvertex ).setLocation( e.getX(), e.getY());
        return this;
    }

    /**
     * Just call VisualGraph().paint()
     */
    public void paint( Graphics2D g2d ){
        this.gpanel.getVisualGraph().paint( g2d );
    }

}

