package salvo.jesus.graph.visual;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.Serializable;

public class Arrowhead implements Serializable{
  Line2D.Double		stem;
  Point             head;
  Point             base1, base2, arrowmedian;
  final double		arrowsize = 10.0;

  public Arrowhead( Line2D.Double line, Point intersection ){
    stem = line;
    head = intersection;
    this.init( );
  }

  private void init( ){
    Point2D.Double    edgeto;
    double            dy, dx, distance;

    dy = stem.getY2() - stem.getY1();
    dx = stem.getX2() - stem.getX1();

    edgeto = new Point2D.Double( stem.getX2(), stem.getY2() );
    distance = edgeto.distance( stem.getX1(), stem.getY1() );
    distance = distance == 0 ? 1 : distance;

    arrowmedian = new Point(
      (int) (head.getX() - dx * arrowsize / distance ),
      (int) (head.getY() - dy * arrowsize / distance ));

    base1 = new Point(
      (int) (arrowmedian.getX() - dy * ( arrowsize / 2 ) / distance ),
      (int) (arrowmedian.getY() + dx * ( arrowsize / 2 ) / distance ));

    base2 = new Point(
      (int) (arrowmedian.getX() + dy * ( arrowsize / 2 ) / distance ),
      (int) (arrowmedian.getY() - dx * ( arrowsize / 2 ) / distance ));
  }

  public Point getBase1( ){
    return base1;
  }

  public Point getBase2( ){
    return base2;
  }
}

