package salvo.jesus.graph.visual.drawing;

import salvo.jesus.graph.*;
import salvo.jesus.graph.visual.*;
import salvo.jesus.geom.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * A weak implementation of the VisualWeightedEdgePainter interface.
 *
 * @author Jesus M. Salvo Jr.
 */

class VisualWeightedEdgePainterWeakImpl implements VisualWeightedEdgePainter {

    public VisualWeightedEdgePainterWeakImpl() {}

    /**
     * Empty method implemetation that does nothing. This method should never
     * be called or delegated to for whatever reason.
     */
    public void paint( VisualGraphComponent component, Graphics2D g2d ) {}

    /**
     * Empty method implemetation that does nothing. This method should never
     * be called or delegated to for whatever reason.
     */
    public void paintText( Graphics2D g2d, Font font, Color fontColor,
        String text, float x, float y ) {}

}