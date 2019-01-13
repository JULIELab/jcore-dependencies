package salvo.jesus.graph.visual.drawing;

import salvo.jesus.graph.*;
import salvo.jesus.graph.visual.*;
import salvo.jesus.geom.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * A weak implementation of the VisualDirectedEdgePainter interface.
 *
 * @author Jesus M. Salvo Jr.
 */

class VisualDirectedEdgePainterWeakImpl implements VisualDirectedEdgePainter {

    public VisualDirectedEdgePainterWeakImpl() {}

    /**
     * Empty method implemetation that does nothing. This method should never
     * be called or delegated to for whatever reason.
     */
    public void paint( VisualGraphComponent component , Graphics2D g2d ) {}

    /**
     * Empty method implemetation that does nothing. This method should never
     * be called or delegated to for whatever reason.
     */
    public void paintText( Graphics2D g2d, Font font, Color fontColor,
        String text, float x, float y ) {}

    /**
    * Draws the arrow head
    */
    public void paintArrowHead( VisualEdge ve, Graphics2D g2d ) {
        PathIterator  edgepathiterator;
        Point         intersection;
        Point         arrowbase1, arrowbase2;
        Arrowhead     arrowhead;
        int           arrowx[], arrowy[];
        double        edgesegment[] = new double[6];

        double        previouspoint[] = new double[2];
        double        currentpoint[] = new double[2];
        Line2D.Double edgelastsegment;
        int           segmenttype;

        VisualVertex  visualVertexA = ve.getVisualVertexA();
        VisualVertex  visualVertexB = ve.getVisualVertexB();
        GeneralPath   drawPath = ve.getGeneralPath();

        // Get the intersection between the edge and the vertex
        edgepathiterator = drawPath.getPathIterator( null );
        while( !edgepathiterator.isDone() ){
            previouspoint[0] = currentpoint[0];
            previouspoint[1] = currentpoint[1];

            segmenttype = edgepathiterator.currentSegment( edgesegment );

            currentpoint[0] = edgesegment[0];
            currentpoint[1] = edgesegment[1];

            if( segmenttype == PathIterator.SEG_LINETO ){
                edgelastsegment = new Line2D.Double(
                  previouspoint[0], previouspoint[1],
                  currentpoint[0], currentpoint[1] );

                // Get the intersection point of the edge and the from vertex
                intersection = Intersection.getIntersection( edgelastsegment, visualVertexB.getGeneralPath() );

                if( intersection != null ){
                    // Determine the coordinates of the arrowhead of the edge
                    arrowhead = new Arrowhead( edgelastsegment, intersection );
                    arrowbase1 = arrowhead.getBase1( );
                    arrowbase2 = arrowhead.getBase2( );

                    arrowx = new int[ 3 ];
                    arrowy = new int[ 3 ];

                    arrowx[ 0 ] = (int) intersection.getX( );
                    arrowy[ 0 ] = (int) intersection.getY( );

                    arrowx[ 1 ] = (int) arrowbase1.getX( );
                    arrowy[ 1 ] = (int) arrowbase1.getY( );

                    arrowx[ 2 ] = (int) arrowbase2.getX( );
                    arrowy[ 2 ] = (int) arrowbase2.getY( );

                    // Draw the arrowhead
                    g2d.setColor( ve.getFillcolor() );
                    g2d.fillPolygon( arrowx, arrowy, 3 );
                    g2d.setColor( ve.getOutlinecolor() );
                    g2d.drawPolygon( arrowx, arrowy, 3 );

                    break;
                }
            }

            edgepathiterator.next();
        }
    }

}