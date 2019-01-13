package salvo.jesus.graph.visual.drawing;

import java.awt.*;
import salvo.jesus.graph.*;
import salvo.jesus.graph.visual.*;

/**
 * A <tt>VisualEdgePainter</tt> that does nothing. It therefore does not paint
 * at all. This maybe useful to "hide" the edges of a graph, like when emphasizing
 * a particular path and showing only the important edges.
 *
 * @author  Jesus M. Salvo Jr.
 */

public class VisualEdgeNullPainter implements VisualEdgePainter {

    public void paint( VisualGraphComponent component, Graphics2D g2d ){}
    public void paintText( Graphics2D g2d, Font font, Color fontColor,
        String text, float x, float y ) {}

}