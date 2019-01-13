package salvo.jesus.graph.visual.layout;

import salvo.jesus.graph.*;
import salvo.jesus.graph.visual.*;
import java.awt.*;
import java.awt.geom.*;

public class StraightLineLayout extends OrthogonalLineLayout {

  /**
   * Creates a StraightLineLayout object used to layout the VisualGraph object
   * specified by vgraph.
   *
   * Because no translation parameters are specified, position 0,0
   * of the internal Grid object will be drawn on 0,0 of the Container;
   * and the distance between grid lines is 100.
   *
   * @param   vgraph    The VisualGraph object to be laid out.
   */
  public StraightLineLayout( VisualGraph vgraph ) {
    super( vgraph );
  }

  /**
   * Creates a StraightLineLayout object used to layout the VisualGraph object
   * specified by vgraph.
   *
   * Because no translation parameters are specified, position 0,0
   * of the internal Grid object will be drawn on 0,0 of the Container;
   * and the distance between grid lines is 100.
   *
   * @param   gpane   A GraphScrollPane object encapsulating the VisualGraph
   * object to be laid out.
   */
  public StraightLineLayout( GraphScrollPane gpane ){
    super( gpane.getVisualGraph() );
  }

  /**
   * Creates a StraightLineLayout object used to layout the VisualGraph object
   * specified by vgraph.
   *
   * Because no translation parameters are specified, position 0,0
   * of the internal Grid object will be drawn on 0,0 of the Container;
   * and the distance between grid lines is 100.
   *
   * @param   gedit    A GraphEditor object encapsulating a GraphScrollPane object
   * which in turn encapsulates the VisualGraph object to be laid out.
   */
  public StraightLineLayout( GraphEditor gedit ){
    super( gedit.getVisualGraph() );
  }

  protected void routeEdge( VisualEdge vedge ) {
    Rectangle   frombounds = vedge.getVisualVertexA().getBounds();
    Rectangle   tobounds = vedge.getVisualVertexB().getBounds();
    Point2D.Float   fromcenter = new Point2D.Float(
        new Double( frombounds.getCenterX()).floatValue(),
        new Double( frombounds.getCenterY()).floatValue() );
    Point2D.Float   tocenter = new Point2D.Float(
        new Double( tobounds.getCenterX()).floatValue(),
        new Double( tobounds.getCenterY()).floatValue() );
    GeneralPath gPath = vedge.getGeneralPath();

    gPath.reset();
    gPath.moveTo( fromcenter.x , fromcenter.y  );
    gPath.lineTo( fromcenter.x, tocenter.y );
  }

}


