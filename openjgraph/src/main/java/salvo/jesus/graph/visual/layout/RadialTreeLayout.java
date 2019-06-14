package salvo.jesus.graph.visual.layout;

import org.apache.log4j.Category;
import salvo.jesus.graph.Tree;
import salvo.jesus.graph.Vertex;
import salvo.jesus.graph.Visitor;
import salvo.jesus.graph.algorithm.BreadthFirstTraversal;
import salvo.jesus.graph.algorithm.GraphTraversal;
import salvo.jesus.graph.visual.VisualEdge;
import salvo.jesus.graph.visual.VisualGraph;
import salvo.jesus.graph.visual.VisualVertex;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * An implementation of a radial tree layout drawing, as described on
 * page 52 of the book "Graph Drawing".
 *
 * @author Jesus M. Salvo Jr.
 * @version $Id: RadialTreeLayout.java,v 1.2 2002/09/11 12:30:49 jmsalvo Exp $
 */

public class RadialTreeLayout implements GraphLayoutManager, Visitor {

  VisualGraph   vGraph;
  Tree          tree;
  HashMap       annulusWedgeMap;
  Point2D       center = null;

  double        radius = 100.0;
  int           previousDepth = 0;
  double        depthStartAtDegrees = 0;
  Vertex        previousParent = null;

  private boolean     initialized = false;

  /**
   * Log4J Category. The name of the category is the fully qualified name of the
   * enclosing class.
   */
  static        Category    logCategory;

  static {
      logCategory = Category.getInstance( LayeredTreeLayout.class.getName());
  }


  public RadialTreeLayout( VisualGraph vGraph ) {
    this.vGraph = vGraph;
    this.tree = ((Tree) vGraph.getGraph());
    this.annulusWedgeMap = new HashMap();
  }

  /**
  * Determines if the graph has been initially laid out.
  * This method should be called prior to any painting to be done by the
  * graph layout manager, as most internal variables are only
  * initialized during layout.
  *
  * @return  True if the graph has at least been laid out once.
  */
  public boolean isInitialized() {
      return initialized;
  }

  public void layout() {
    List    visited = new ArrayList( 10 );
    GraphTraversal traversal = new BreadthFirstTraversal( this.tree );

    // During traversal, the Visitor ( this ) performs
    // the necessary work to do the layout
    traversal.traverse( this.tree.getRoot(), visited, this );

    // Finally, draw
    this.drawLayout();
  }

  /**
   * Implementation of the visit() method of the Visitor interface.
   */
  public boolean visit( Vertex vertexToVisit ) {
    VisualVertex visualVertex;
    Arc2D   arc;
    double  annulusWedgeDegree;
    double  positionDegree;
    double  startAtDegree;
    int     depth;
    VertexDegrees degrees = new VertexDegrees();
    VertexDegrees parentDegrees;

    this.logCategory.debug( "Visiting " + vertexToVisit );

    try {
      visualVertex = this.vGraph.getVisualVertex( vertexToVisit );
      depth = this.tree.getDepth( vertexToVisit );
      this.logCategory.debug( "Depth: " + depth );

      if( this.tree.getRoot() == vertexToVisit ) {
        // For the root of the tree, set the annulus wedge to 360 degrees
        annulusWedgeDegree = 360;
        center = new Point2D.Double( this.tree.getHeight() * this.radius / 2, this.tree.getHeight() * this.radius / 2 );
        this.logCategory.debug( "Setting center " + center );
        visualVertex.setLocation( center.getX(), center.getY() );
        degrees.annulusWedgeDegree = annulusWedgeDegree;
        degrees.positionDegree = 0;
        this.annulusWedgeMap.put( vertexToVisit, degrees );
        this.previousParent = vertexToVisit;
      }
      else {
        // For non-root, set the annulus wedge to:
        // ( leaves of current node / leaves of parent node ) * annulus wedge of parent
        Vertex  parent = this.tree.getParent( vertexToVisit );
        parentDegrees = (VertexDegrees) this.annulusWedgeMap.get( parent );
        double  parentAnnulusWedge = parentDegrees.annulusWedgeDegree;
        Tree    parentSubTree = this.tree.getSubTree( parent );
        Tree    subTree = this.tree.getSubTree( vertexToVisit );

        // Note that 120 degrees here is valid only if the concentric circles
        // are doubling in radius for every depth.
        // 120 degrees is taken from cos(A)=radius/(radius/2)=0.5
        annulusWedgeDegree =
          Math.min(
          ( (double) subTree.getLeaves().size() / (double) parentSubTree.getLeaves().size() ) * parentAnnulusWedge,
          Math.toDegrees( Math.acos( (this.radius * ( depth - 1 ) ) / (this.radius * depth) )  ) * 2 );

        this.logCategory.debug( "Annulus Wedge: " + annulusWedgeDegree  );


        if( this.previousDepth != this.tree.getDepth( vertexToVisit ))
          this.depthStartAtDegrees = parentDegrees.positionDegree - ( parentDegrees.annulusWedgeDegree / 2 );
        if( this.previousParent != parent )
          this.depthStartAtDegrees = parentDegrees.positionDegree - ( parentDegrees.annulusWedgeDegree / 2 );

        positionDegree = this.depthStartAtDegrees + annulusWedgeDegree / 2;

        this.logCategory.debug( "Start At:" + this.depthStartAtDegrees  );
        this.logCategory.debug( "Position:" + positionDegree );

        arc = new Arc2D.Double(
          center.getX() - ( this.radius / 2 ) * ( depth - 1 ),
          center.getY() - ( this.radius / 2 ) * ( depth - 1 ),
          (double) ( this.radius * ( depth - 1 )),
          (double) ( this.radius * ( depth - 1 )),
          positionDegree, 1,
          Arc2D.OPEN );

        visualVertex.setLocation( arc.getEndPoint().getX(), arc.getEndPoint().getY() );

        degrees.annulusWedgeDegree = annulusWedgeDegree;
        degrees.positionDegree = positionDegree;
        this.annulusWedgeMap.put( vertexToVisit, degrees );

        this.depthStartAtDegrees += annulusWedgeDegree;
        this.previousDepth = this.tree.getDepth( vertexToVisit );
        this.previousParent = parent;
      }
    }
    catch( Exception ex ) {
      ex.printStackTrace();
    }

    return true;
  }


  public void addVertex(VisualVertex vVertex) {
      // Do nothing here
  }

  public void removeEdge(VisualEdge vEdge) {
      // Do nothing here
  }

  public void removeVertex(VisualVertex vVertex) {
      // Do nothing here
  }

  public void addEdge(VisualEdge vEdge) {
      // Do nothing here
  }

  public void routeEdge( Graphics2D g2d, VisualEdge vEdge ) {

      Rectangle     fromvertexBounds;
      Rectangle     tovertexBounds;
      GeneralPath   drawPath;
      VisualVertex  visualVertexA = vEdge.getVisualVertexA();
      VisualVertex  visualVertexB = vEdge.getVisualVertexB();

      drawPath = vEdge.getGeneralPath();

      fromvertexBounds = visualVertexA.getBounds();
      tovertexBounds = visualVertexB.getBounds();

      // Make sure to clear the GeneralPath() first. Otherwise, the edge's previous
      // path will be redrawn as well.
      drawPath.reset();

      // Start the line from the center of the vEdgertex
      drawPath.moveTo( (float)fromvertexBounds.getCenterX(), (float)fromvertexBounds.getCenterY() );
      drawPath.lineTo( (float)tovertexBounds.getCenterX(), (float)tovertexBounds.getCenterY() );
  }

  public void drawLayout() {
    this.vGraph.repaint();
  }
}


class VertexDegrees {
  double  annulusWedgeDegree;
  double  positionDegree;
}