package salvo.jesus.graph.visual;

import salvo.jesus.graph.Vertex;
import salvo.jesus.graph.visual.drawing.VisualVertexPainter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * The VisualVertex class encapsulates a Vertex with attributes
 * used for visual rendering of the vertex.
 *
 * @author		Jesus M. Salvo Jr.
 */
public class VisualVertex extends AbstractVisualGraphComponent {

  /**
    * Random object used to give an initial random coordinates for newly
    * created instances of VisualVertex if no Shape has been provided.
    */
  static Random		rand = new Random( );

  /**
    * Creates a new VisualVertex object that encapsulates the given Vertex object
    * This defaults the font to Lucida Sans, the outline color to black and the
    * fill or background colot to Color( 0, 255, 255 ).
    *
    * @param	vertex	The Vertex object that the VisualVertex will encapsulate.
    */
  public VisualVertex( Vertex vertex, VisualGraph vGraph ){
    this.component = vertex;
    this.painter = vGraph.getVisualVertexPainterFactory().getPainter( this );
    this.visualGraph = vGraph;
    this.setFont( new Font( "Lucida Sans", Font.PLAIN, 10 ));

    this.initLocation();
    // Force adjustment of width and height.
    this.rescale();

    this.setOutlinecolor( Color.black );
    this.setFillcolor( new Color( 0, 225, 255 ));
  }

  /**
    * Creates a new VisualVertex object that encapsulates the given Vertex object
    * using the Font specified to draw the string inside the VisualVertex.
    *
    * @param	vertex	The Vertex object that the VisualVertex will encapsulate.
    * @param f				The font that will be used to draw the String representation of the vertex
    */
  public VisualVertex( Vertex vertex, Font f, VisualGraph vGraph ){
    this.component = vertex;
    this.painter = vGraph.getVisualVertexPainterFactory().getPainter( this );
    this.visualGraph = vGraph;
    this.setFont( f );

    this.initLocation();
    // Force adjustment of width and height.
    this.rescale();

    outlinecolor = Color.black;
    this.setFillcolor( new Color( 0, 225, 255 ));
  }

  /**
    * Creates a new VisualVertex object that encapsulates the given Vertex object
    * with the given visual attributes.
    *
    * @param	vertex	The Vertex object that the VisualVertex will encapsulate.
    * @param	shape		The shape used to render the VisualVertex.
    * @param c				The color used to draw the outline of the VisualVertex's shape.
    * @param bgcolor	The color used to fill the VisualVertex's shape.
    * @param f				The font that will be used to draw the String representation of the vertex
    */
  public VisualVertex( Vertex vertex, Shape shape,
        Color c, Color bgcolor, Font f, VisualGraph vGraph )
  {
    this.component = vertex;
    this.painter = vGraph.getVisualVertexPainterFactory().getPainter( this );
    this.visualGraph = vGraph;
    this.setFont( f );

    drawpath = new GeneralPath( shape );
    this.setOutlinecolor( c );
    this.setFillcolor( bgcolor );
  }

  /**
   * Initialize the location of the VisualVertex by generating a random
   * number for its x and y coordinates.
   */
  private void initLocation(){
    StringTokenizer   sttokenizer;
    int               height = 0, width, maxwidth = 0;
    int               lineheight;

    drawpath = new GeneralPath( (Shape) new Rectangle.Double( 5, 5, 10, 10 ));
    this.setLocation( rand.nextInt( 500 ), rand.nextInt( 400 ) );
  }

  /**
   * Returns the Vertex that VisualVertex encapsulates.
   *
   * @return	The Vertex object that the VisualVertex encapsulates.
   */
  public Vertex getVertex(){
    return (Vertex) this.component;
  }

  /**
   * Sets geometry used to draw the VisualVertex.
   *
   * @param   path   A GeneralPath object used to draw the VisualVertex. If
   * the GeneralPath is not a closed polygon, this method will close the path.
   */
  public void setGeneralPath( GeneralPath path ){
    // Make sure the path is closed
    path.closePath();
    super.setGeneralPath( path );
    this.rescale();
  }

  /**
    * Sets the vertex that the VisualVertex encapsulates. The next call to
    * paint() will redraw the string inside the VisualVertex' shape.
    *
    * @param	vertex		The new Vertex object that VisualVertex encapsulates.
    */
  public void setVertex( Vertex vertex ){
    this.component = vertex;
  }

  /**
   * Tests if the coordinate is inside the VisualVertex's shape. Do not call
   * this method if paint() has not been called at least once, because
   * the first call to paint() will initialize the shape used to draw
   * the VisualVertex.
   *
   * This method is simply a wrapper on GeneralPath.contains( x, y ).
   *
   * @param	x		x-coordinate of the point you want to test
   * @param y		y-coordinate of the point you want to test
   * @return		True if the x and y coordinate is inside the shape of the VisualVertex.
   */
  public boolean contains( double x, double y ){
    return drawpath.contains( x, y );
  }

  /**
   * Moves the location of the VisualVertex to the new coordinate.
   * Do not call this method if paint() has not been called at least once, because
   * the first call to paint() will initialize the shape used to draw
   * the VisualVertex.
   *
   * @param	x		The new x-coordinate for the VisualVertex
   * @param y		The new y-coordinate for the VisualVertex
   */
  public void setLocation( int x, int y ){
    this.setLocationDelta(
      (int)(x - drawpath.getBounds2D().getCenterX()),
      (int)(y - drawpath.getBounds2D().getCenterY()));

  }

  public void setLocation( double x, double y ){
    this.setLocationDelta(
      (x - drawpath.getBounds2D().getCenterX()),
      (y - drawpath.getBounds2D().getCenterY()));

  }

  /**
   * Moves the location of the VisualVertex' shape by the specified delta values.
   * Do not call this method if paint() has not been called at least once, because
   * the first call to paint() will initialize the shape used to draw
   * the VisualVertex.
   *
   * This method is simply a wrapper on Rectangle.translate( x, y ).
   *
   * @param	x		The delta x-coordinate for the VisualVertex
   * @param y		The delta y-coordinate for the VisualVertex
   */
  public void setLocationDelta( int dx, int dy ){
    AffineTransform     transform = new AffineTransform();

    transform.translate( dx, dy );
    drawpath.transform( transform );
  }

  public void setLocationDelta( double dx, double dy ){
    AffineTransform     transform = new AffineTransform();

    transform.translate( dx, dy );
    drawpath.transform( transform );
  }


  /**
    * Wrapper method that simply returns this.displaytext.
    *
    * @return   Display string of the VisualVertex object
    */
  public String toString() {
    return this.getLabel();
  }

  /**
   * Draw the VisualVertex with the specified 2D graphics context. Each call
   * to this method will draw the fill color, the outline color, and the string
   * inside the shape, in that order.
   *
   * @param	g2d		The Graphics2D graphics context object used to draw
   * the VisualVertex.
   */
  public void paint( Graphics2D g2d ){
    this.painter.paint(this, g2d );
  }

  /**
   * Forces a rescale of the internal shape used to represent VisualVertex.
   */
  public void rescale( ) {
    (( VisualVertexPainter ) this.painter ).rescale( this );
  }
}
