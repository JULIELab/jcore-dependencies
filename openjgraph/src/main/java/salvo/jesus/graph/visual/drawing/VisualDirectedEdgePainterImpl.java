package salvo.jesus.graph.visual.drawing;

import salvo.jesus.graph.visual.VisualEdge;
import salvo.jesus.graph.visual.VisualGraphComponent;

import java.awt.*;

/**
 * An implementation of <tt>VisualDirectedEdgePainter</tt> that draws
 * the <tt>VisualEdge</tt> based on its attributes, then draws the arrowhead
 * indicating the directio of the edge.
 *
 * @author Jesus M. Salvo Jr.
 */


public class VisualDirectedEdgePainterImpl extends VisualEdgePainterImpl implements VisualDirectedEdgePainter
{
    /**
     * Delegate responsible for the actual drawing of the arrowhead.
     */
    VisualDirectedEdgePainterWeakImpl  arrowHeadPainterDelegate = new VisualDirectedEdgePainterWeakImpl();

    public VisualDirectedEdgePainterImpl() {}

    public void paint( VisualGraphComponent component , Graphics2D g2d ) {
        super.paint( component, g2d );
        this.paintArrowHead( (VisualEdge) component, g2d );
    }

    /**
     * Draws the arrow head
    */
    public void paintArrowHead( VisualEdge ve, Graphics2D g2d ) {
        this.arrowHeadPainterDelegate.paintArrowHead( ve, g2d );
    }
}