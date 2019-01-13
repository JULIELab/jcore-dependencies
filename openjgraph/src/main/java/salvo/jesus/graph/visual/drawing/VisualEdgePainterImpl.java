package salvo.jesus.graph.visual.drawing;

import salvo.jesus.graph.*;
import salvo.jesus.graph.visual.*;
import salvo.jesus.graph.visual.layout.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

/**
 * An implementation of <tt>VisualEdgePainter</tt> that draws
 * the <tt>VisualEdge</tt> based on its attributes.
 *
 * @author Jesus M. Salvo Jr.
 */

public class VisualEdgePainterImpl implements VisualEdgePainter {

    public VisualEdgePainterImpl() {}

    /**
    * Paints the <tt>visualEdge</tt>. No arrowhead is drawn.
    */
    public void paint( VisualGraphComponent component, Graphics2D g2d ) {
        VisualEdge    vEdge = ( VisualEdge ) component;
        Rectangle     fromvertexBounds;
        Rectangle     tovertexBounds;
        GeneralPath   drawPath;
        VisualVertex  visualVertexA = vEdge.getVisualVertexA();
        VisualVertex  visualVertexB = vEdge.getVisualVertexB();
        GraphLayoutManager  layoutmanager = vEdge.getVisualGraph().getGraphLayoutManager();

        drawPath = vEdge.getGeneralPath();

        // If there is no layoutmanager or there is one but the layout has not
        // been initialised, by default, let us route edges as straight lines.
        if( layoutmanager == null || (layoutmanager != null && !layoutmanager.isInitialized()) ) {

            fromvertexBounds = visualVertexA.getBounds();
            tovertexBounds = visualVertexB.getBounds();

            // Make sure to clear the GeneralPath() first. Otherwise, the edge's previous
            // path will be redrawn as well.
            drawPath.reset();

            // Start the line from the center of the vEdgertex
            drawPath.moveTo( (float)fromvertexBounds.getCenterX(), (float)fromvertexBounds.getCenterY() );
            drawPath.lineTo( (float)tovertexBounds.getCenterX(), (float)tovertexBounds.getCenterY() );
        }
        else {
            // Let the layout manager determine how the edge will be routed.
            layoutmanager.routeEdge( g2d, vEdge );
        }

        // Draw the line
        g2d.setColor( vEdge.getOutlinecolor() );
        g2d.draw( drawPath );

        // Draw the edge label
        this.paintText( vEdge, g2d );
    }

    /**
     *  Wrapper method around the <tt>paintText()</tt> method of the
     *  <tt>VisualEdgePainter</tt> interface. This method performs the
     *  calculation to determine the position where the text will
     *  be drawn.
     */
    private void paintText( VisualEdge vEdge, Graphics2D g2d ) {
        Point       fromPoint = new Point();
        Point       toPoint = new Point();
        GeneralPath gPath = vEdge.getGeneralPath();
        PathIterator iterator = gPath.getPathIterator( null );
        FontMetrics fontMetrics;
        float   edgeSegment[] = new float[6];
        double  currentLength = 0;
        float   cumulativeLength = 0;
        float   x1 = 0, y1 = 0, x2 = 0, y2 = 0;
        int     segmentType;
        boolean firstPointInitialized = false;

        // Get the total length of the edge
        float  edgeLength = vEdge.getEdgeLength( vEdge, fromPoint, toPoint );

        while( !iterator.isDone() ){
            segmentType = iterator.currentSegment( edgeSegment );

            switch( segmentType ){
            case PathIterator.SEG_LINETO:
            case PathIterator.SEG_MOVETO:
                x2 = edgeSegment[0];
                y2 = edgeSegment[1];
                break;
            case PathIterator.SEG_QUADTO:
                x2 = edgeSegment[2];
                y2 = edgeSegment[3];
                break;
            case PathIterator.SEG_CUBICTO:
                x2 = edgeSegment[4];
                y2 = edgeSegment[5];
            }

            if( firstPointInitialized ) {
                currentLength = Point2D.distance( x1, y1, x2, y2 );
                cumulativeLength += currentLength;
            }

            iterator.next();

            // If we are halfway through the length of the edge,
            // then paint the text
            if( cumulativeLength >= ( edgeLength / 2 ) || cumulativeLength >= edgeLength ) {
                // Ratio of the remaining half-length over the length of the current edge
                double  ratio = (( edgeLength / 2 ) - ( cumulativeLength - currentLength )) / currentLength;
                fontMetrics = vEdge.getFontMetrics();

                // Take into account the text's length
                this.paintText(g2d, vEdge.getFont(), vEdge.getFontcolor(), vEdge.getLabel(),
                    (float) (fromPoint.getX() < toPoint.getX() ?
                        (x1 + ( Math.abs( x2 - x1 ) * ratio )):
                        (x1 - ( Math.abs( x2 - x1 ) * ratio )))
                            - fontMetrics.stringWidth( vEdge.getLabel() ) / 2,
                    (float) (fromPoint.getY() < toPoint.getY() ?
                        (y1 + ( Math.abs( y2 - y1 ) * ratio )):
                        (y1 - ( Math.abs( y2 - y1 ) * ratio )))
                 );
                break;
            }

            x1 = x2;
            y1 = y2;

            if( !firstPointInitialized ) {
                firstPointInitialized = true;
            }
        }
    }

    /**
     * Paints the text of the <tt>VisualEdge</tt>.
     */
    public void paintText( Graphics2D g2d, Font font, Color fontColor,
        String text, float x, float y )
    {
        g2d.setFont( font );
        g2d.setColor( fontColor );
        g2d.drawString( text, x, y );
    }

}