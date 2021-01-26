package salvo.jesus.graph.visual;

import salvo.jesus.graph.Graph;
import salvo.jesus.graph.visual.layout.GraphLayoutManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * GraphPanel encapsulates the visual representation of a graph
 * (VisualGraph) into a panel. The panel is automatically resized depending
 * on the location of the vertices. Therefore, if the user drags the mouse
 * outside of the existing size of the panel, the panel is automaticall resized.
 * However, the panel will not be smaller than the viewport of the
 * GraphScrollPane.
 *
 * @author		Jesus M. Salvo Jr.
 * @see         salvo.jesus.graph.visual.GraphScrollPane
 */
public class GraphPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
  /**
    * The GraphScrollPane that encapsulates this class.
    */
  GraphScrollPane		gpcontainer;

  /**
    * The VisualGraph that this pane encapsulates
    */
  VisualGraph		vgraph;

  /**
   * State object representing the current mode of the GraphPanel.
   */
  GraphPanelState   state;

  GraphPanel(){
    this(new GraphPanelNormalState(), new VisualGraph());
  }

  GraphPanel(VisualGraph vgraph){
    this(new GraphPanelNormalState(),vgraph);
  }

  GraphPanel(GraphPanelState gps){
    this(gps, new VisualGraph());
  }

  GraphPanel(GraphPanelState gps, VisualGraph vgraph){
    this.state = gps;
    gps.setGraphPanel(this);
    this.vgraph = vgraph;
    vgraph.addContainer( this );
    try  {
      jbInit();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    // this.setBorder( new EtchedBorder());
    // By default, mode of operation is normal
    this.setNormalMode();
    this.setBackground(Color.white);
    this.addMouseListener( this );
    this.addMouseMotionListener(  this );
  }

  /**
    * Sets the VisualGraph that this class encapsulates. This will
    * automatically redraw the contents of the pane, thereby drawing
    * the vertices and edges of VisualGraph.
    *
    * @param vg	The visual graph to be encapsulated and drawn.
    * @param	gp	The container that will contain this class.
    *						This is later on used to determine if we should
    *						resize GraphPanelSizeable
   */
  public void setVisualGraph( VisualGraph vg, GraphScrollPane gp ){
    if( vgraph != null )
      vgraph.removeContainer( this );

    this.vgraph = vg;
    this.vgraph.addContainer( this );
    this.gpcontainer = gp;
    this.repaint();
  }

  /**
    * Sets the Graph that this class encapsulates. This will
    * automatically redraw the contents of the pane, thereby drawing
    * the vertices and edges of Graph.
    *
    * @param g		The graph to be encapsulated and drawn.
    * @param	gp	The container that will contain this class.
    *						This is later on used to determine if we should
    *						resize GraphPanelSizeable
   */
  public void setGraph( Graph g, GraphScrollPane gp ){
    VisualGraph	vg = new VisualGraph();

    vg.setGraph( g );
    this.setVisualGraph( vg, gp );
  }

  /**
    * Returns the VisualGraph that this class encapsulates.
    *
    * @return	The VisualGraph that is encapsulated within this class.
   */
  public VisualGraph getVisualGraph() {
    return this.vgraph;
  }

  /**
    * Sets the mode of operation to NORMAL_MODE
    *
   */
  public void setNormalMode() {
    this.state = state.recommendState(
        new ChangeStateEvent( this, new GraphPanelNormalState( this )));
  }

  /**
    * Sets the mode of operation to VERTEX_MODE
    *
   */
  public void setVertexMode() {
    this.state = state.recommendState(
        new ChangeStateEvent( this, new GraphPanelVertexState( this )));
  }

  /**
    * Sets the mode of operation to EDGE_MODE
    *
   */
  public void setEdgeMode() {
    this.state = state.recommendState(
        new ChangeStateEvent( this, new GraphPanelEdgeState( this )));
  }

  /**
    * Sets the layout manager to use to layout the vertices of the graph.
    *
    * @param  layoutmanager   An object implementing the GraphLayoutManager interface.
    */
  public void setGraphLayoutManager( GraphLayoutManager layoutmanager ) {
    this.vgraph.setGraphLayoutManager( layoutmanager );
  }

  /**
    * Override of JPanel's paint() method. Before the superclass'
    * paint method is called, it compares the size (the maximum coordinates
    * of the visual vertices) of vgraph with that of gpcontainer.
    * If any coordinates (x or y) of vgraph is greater than the coordinates (size)
    * of gpcontainer, GraphPanelSizeable's size is adjusted to that of vgraph.
    * Doing this will have the effect of automatically adjusting the scrollbars
    * of gpcontainer.
    *
    * @param g		Graphics context that will be used to draw.
   */
  public void paintComponent( Graphics g ){
    Dimension		vgraphsize;
    Graphics2D		g2d;
    int			width, height;

    // Set the size of the graphanelsizeable depending on the
    // coordinates of the vertices on visualgraph.
    // Settings the size will adjust the scrollbars on
    // GraphScrollPane.

    // Note that setting the width/height of graphpanelsizeable
    // to the width/height of graphpanel has the effect of
    // moving the viewport area when a vertex that is at the edge
    // of the width/height is deleted (thereby at least one of the
    // if conditions below is false).
    // If this is the desired, do not change the width/height.
    // That is, on the else statement:
    // 	width = (int) this.getSize().getWidth();
    // Do the same for the height.
    vgraphsize = this.vgraph.getMaxSize();
    if( vgraphsize.getWidth() > this.gpcontainer.getSize().getWidth())
      width = (int)vgraphsize.getWidth();
    else
      width = (int)this.gpcontainer.getSize().getWidth();

    if( vgraphsize.getHeight() > this.gpcontainer.getSize().getHeight())
      height = (int)vgraphsize.getHeight();
    else
      height = (int)this.gpcontainer.getSize().getHeight();

    this.setSize( width, height );
    this.setPreferredSize( new Dimension( width, height ));
    this.revalidate();

    // Call the superclass paint() method, then draw the graph
    // using vgraph.paint(). If we call vgraph's paint() method first,
    // it will be overwritten by the superclass' paint() method.
    super.paintComponent( g );
    g2d = (Graphics2D) g;

    // Do additional paint operation based on the current state or mode.
    this.state.paint( g2d );
  }

  /**
   * Automatically called when the mouse is dragged. The result of this
   * method call depends on the current mode or state of the GraphPanel,
   * as determined by the internal GraphPanelState variable by passing
   * the method call to the internal GraphPanelState object.
   */
  public void mouseDragged( MouseEvent e ) {
   this.state = this.state.mouseDragged( e );
  }

  /**
   * Automatically called when the mouse is pressed. The result of this
   * method call depends on the current mode or state of the GraphPanel,
   * as determined by the internal GraphPanelState variable by passing
   * the method call to the internal GraphPanelState object.
   */
  public void mousePressed( MouseEvent e ) {
    this.state = this.state.mousePressed( e );
  }

  /**
   * Automatically called when the mouse is released. The result of this
   * method call depends on the current mode or state of the GraphPanel,
   * as determined by the internal GraphPanelState variable by passing
   * the method call to the internal GraphPanelState object.
   */
  public void mouseReleased( MouseEvent e ) {
    this.state = this.state.mouseReleased( e );
  }

  /**
   * Automatically called when the mouse enters the GraphPanel. The result of this
   * method call depends on the current mode or state of the GraphPanel,
   * as determined by the internal GraphPanelState variable by passing
   * the method call to the internal GraphPanelState object.
   */
  public void mouseEntered( MouseEvent e ) {
    this.state = this.state.mouseEntered( e );
  }

  /**
   * Automatically called when the mouse leaves the GraphPanel. The result of this
   * method call depends on the current mode or state of the GraphPanel,
   * as determined by the internal GraphPanelState variable by passing
   * the method call to the internal GraphPanelState object.
   */
  public void mouseExited( MouseEvent e ) {
    this.state = this.state.mouseExited( e );
  }

  /**
   * Automatically called when the mouse is clicked on the GraphPanel. The result of this
   * method call depends on the current mode or state of the GraphPanel,
   * as determined by the internal GraphPanelState variable by passing
   * the method call to the internal GraphPanelState object.
   */
  public void mouseClicked( MouseEvent e ) {
    this.state = this.state.mouseClicked( e );
  }

  /**
   * Automatically called when the mouse is moved over the GraphPanel. The result of this
   * method call depends on the current mode or state of the GraphPanel,
   * as determined by the internal GraphPanelState variable by passing
   * the method call to the internal GraphPanelState object.
   */
  public void mouseMoved( MouseEvent e ) {
    this.state = this.state.mouseMoved( e );
  }

  public void keyPressed( KeyEvent e ) {
    this.state = this.state.keyPressed( e );
  }

  public void keyReleased( KeyEvent e ) {
    this.state = this.state.keyReleased( e );
  }

  public void keyTyped( KeyEvent e ) {
    this.state = this.state.keyTyped( e );
  }

  /**
   * Just make sure that we remove GraphPanelSizeable from vgraph's
   * container list.
  */
  protected void finalize() {
    // Make sure we remove this container from the virtual graph's container list
    this.vgraph.removeContainer( this );
  }

  /**
   * Processes the <tt>ChangeStateEvent</tt> by delegating the event to the
   * <tt>recommendState()</tt> method of the current state, possibly
   * returning a new state.
   *
   * @return the resulting state. Can be the previous state or a new state,
   * depending on the current state's decision.
   */
  public GraphPanelState processChangeStateEvent(ChangeStateEvent cse){
    this.state = this.state.recommendState(cse);
    return this.state;
  }


}

