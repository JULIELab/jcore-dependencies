package salvo.jesus.graph.visual;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import salvo.jesus.graph.*;
import salvo.jesus.graph.listener.*;
import salvo.jesus.graph.visual.layout.*;
import salvo.jesus.graph.visual.drawing.*;

/**
 * The VisualGraph encapsulates a Graph object with the necessary
 * attributes and methods for drawing the entire graph on one or more containers.
 *
 * @author		Jesus M. Salvo Jr.
 * @see         salvo.jesus.graph.Graph
 */

public class VisualGraph extends NullGraphListener
  implements Serializable {

  /**
   * The Graph object that the VisualGraph encapsulates.
   */
  private Graph		graph;

  /**
   * A List of containers where the graph will be drawn.
   */
  private java.util.List      containers;

  /**
   * A List of VisualVertex that corresponds to the vertices in the
   * graph that VisualGraph encapsulates.
   */
  private java.util.List      visualVertices;

  /**
   * A List of VisualEdge that corresponds to the edges in the
   * graph that VisualGraph encapsulates.
   */
  private java.util.List      visualEdges;

  /**
   * Factory for creating painters for VisualVertices
   */
  private VisualVertexPainterFactory    vertexPainterFactory;

  /**
   * Factory for creating painters for VisualEdges
   */
  private VisualEdgePainterFactory      edgePainterFactory;

  /**
   * Factory for creating editors for VisualVertex and VisualEdges
   */
  private VisualGraphComponentEditorFactory  editorFactory;

  /**
   * The layout manager to layout the vertices in the graph.
   */
  private GraphLayoutManager  layoutManager;

  int     linetype = salvo.jesus.graph.visual.VisualEdge.STRAIGHT_LINE;

  /**
   * Creates a new, empty VisualGraph object. You need to later on
   * call setGraph() to set the Graph that the VisualGraph object
   * will encapsulate.
   */
  public VisualGraph( ) {
    this.containers = new ArrayList( 10 );
    this.visualVertices = new ArrayList( 10 );
    this.visualEdges = new ArrayList( 10 );
    this.vertexPainterFactory = VisualVertexPainterFactoryImpl.getInstance();
    this.edgePainterFactory = VisualEdgePainterFactoryImpl.getInstance();
  }

  public VisualGraph(Graph g){
    this();
    this.setGraph(g);
  }

  /**
   * Returns the Containers that are registered with the VisualGraph object
   * where VisualGraph will be drawn.
   *
   * @return	List of Containers where VisualGraph will be drawn.
   * @see			#addContainer( Container )
   * @see			#removeContainer( Container )
   */
  public java.util.List getContainers( ) {
    return this.containers;
  }

  /**
    * Registers a Container for drawing the VisualGraph. The next repaint()
    * on the VisualGraph object will draw the VisualGraph on the added container.
    * For the moment, limit the types to be added to be instances of GraphScrollPane.
    *
    * @param		c		Additional Container where VisualGraph will be drawn
    * @see			#removeContainer( Container )
    */
  public void addContainer( Container c ) {
    containers.add( c );
  }

  /**
    * Removes a Container from drawing of the VisualGraph object. this will not
    * necessarily clear or remove the existing paint on the Container. You have
    * to clear the Container yourself.
    *
    * @param		c		Container where VisualGraph will no longer be drawn
    * @see			#addContainer( Container )
    */
  public void removeContainer( Container c ) {
    containers.remove( c );
  }

  /**
    * Determines the dimension that contains all the VisualGraph's VisualVertex.
    *
    * @return	Dimension object that represents the dimension that covers
    * all the VisualVertex in the VisualGraph.
    */
  public Dimension getMaxSize() {
    VisualVertex	vvertex;
    Dimension		  vvertexdimension;
    Rectangle		  rectangle;
    Iterator		  iterator;
    int					  maxwidth = 0, maxheight = 0;

    iterator = visualVertices.iterator();
    while( iterator.hasNext()) {
      vvertex = (VisualVertex) iterator.next();
      rectangle = vvertex.getBounds();
      vvertexdimension = rectangle.getSize();
      if( rectangle.getY() + vvertexdimension.getHeight() > maxheight )
        maxheight = (int) (rectangle.getY() + vvertexdimension.getHeight());
      if( rectangle.getX() + vvertexdimension.getWidth() > maxwidth )
        maxwidth = (int) (rectangle.getX() + vvertexdimension.getWidth());
    }

    return new Dimension( maxwidth, maxheight );
  }

  /**
    * Determines the starting point where a VisualVertex is drawn.
    *
    * @return	Point object of the VisualVertex which has the minimum x and y
    * coordinate.
    */
  public Point getStartDrawPoint() {
    VisualVertex	vvertex;
    Rectangle		  rectangle;
    Iterator		  iterator;
    int					  minx = -1, miny = -1;

    iterator = visualVertices.iterator();
    while( iterator.hasNext()) {
      vvertex = (VisualVertex) iterator.next();
      rectangle = vvertex.getBounds();
      minx = (minx == -1 ? rectangle.x :
             (minx < rectangle.x ? minx : rectangle.x ));
      miny = (miny == -1 ? rectangle.y :
             (miny < rectangle.y ? miny : rectangle.y ));
    }

    return new Point( minx, miny );
  }

  /**
    * Repaints the VisualGraph in all the registered Containers.
    */
  public void repaint( ) {
    Iterator	iterator;
    Container	container;

    iterator = containers.iterator();
    while( iterator.hasNext()) {
      container = (Container) iterator.next();
      if( container != null )
        container.repaint();
    }
  }

  /**
    * Returns the Graph object that the VisualGraph object encapsulates.
    *
    * @return	Graph object encapsulated by the VisualGraph object
    */
  public Graph getGraph() {
    return graph;
  }

  /**
    * Sets the Graph object that the VisualGraph object encapsulates. If an existing
    * Graph object is already encapsulated, the existing Graph object is replaced
    * by the new Graph object. This will not however trigger methods in the
    * registered listener objects.
    *
    * @param	graph		The new Graph object that will be encapsulated by the VisualGraph object
    */
  public void setGraph( Graph graph ) {
    Iterator	verticesiterator;
    Iterator	edgesiterator;
    Vertex		currentvertex;
    HashSet		uniqueedges = new HashSet();

    // Remove this visualgraph from the graph's listener
    if( this.graph != null ) {
      this.graph.removeListener( this );
    }

    // Remove the visual representation of the graph structure
    visualEdges.clear( );
    visualVertices.clear( );

    // Set the graph
    this.graph = graph;

    // Create visual representation of the existing graph structure
    // That is, add elements to visualedges and visualvertices
    verticesiterator = this.graph.getVerticesIterator();
    while( verticesiterator.hasNext() ){
      currentvertex = (Vertex) verticesiterator.next( );
      visualVertices.add( new VisualVertex( currentvertex, this ));

      // Get the adjacent edges for each vertex in the graph
      edgesiterator = this.graph.getEdges( currentvertex ).iterator();
      while( edgesiterator.hasNext() ){
        // Using a set guarantees that edges are unique
        uniqueedges.add( (Edge) edgesiterator.next( ));
      }
    }

    // Now create the visual representation of the edges
    edgesiterator = uniqueedges.iterator();
    while( edgesiterator.hasNext() ){
      visualEdges.add( new VisualEdge( (Edge) edgesiterator.next(), this ));
    }

    // Add this visualgraph as listener to the new graph.
    this.graph.addListener( this );
  }

  /**
   * Returns the layout manager used to layout the vertices of the graph.
   */
  public GraphLayoutManager getGraphLayoutManager( ) {
    return this.layoutManager;
  }

  /**
    * Sets the layout manager to use to layout the vertices of the graph.
    *
    * @param  layoutmanager   An object implementing the GraphLayoutManager interface.
    */
  public void setGraphLayoutManager( GraphLayoutManager layoutmanager ) {
    this.layoutManager = layoutmanager;
  }

    /**
     * Returns an instance of the factory used to create painters for VisualVertices.
     * Unless <tt>setVisualVertexPainterFactory()</tt> was called, the factory
     * used by default is <tt>VisualVertexPainterFactoryImpl</tt>.
     */
    public VisualVertexPainterFactory getVisualVertexPainterFactory() {
        return this.vertexPainterFactory;
    }

    /**
     * Sets the factory for creating painters for VisualVertices
     */
    public void setVisualVertexPainterFactory( VisualVertexPainterFactory newFactory ) {
        this.vertexPainterFactory = newFactory;
    }

    /**
     * Returns an instance of the factory used to create painters for VisualEdges.
     * Unless <tt>setVisualEdgePainterFactory()</tt> was called, the factory
     * used by default is <tt>VisualEdgePainterFactoryImpl</tt>.
     */
    public VisualEdgePainterFactory getVisualEdgePainterFactory() {
        return this.edgePainterFactory;
    }

    /**
     * Sets the factory for creating painters for VisualEdges.
     */
    public void setVisualEdgePainterFactory( VisualEdgePainterFactory newFactory ) {
        this.edgePainterFactory = newFactory;
    }

    /**
     * Returns an instance of the factory used to create custom editors
     * for VisualVertex and VisualEdge.
     */
    public VisualGraphComponentEditorFactory getVisualGraphComponentEditorFactory() {
        return this.editorFactory;
    }

    /**
     * Sets the factory for creating custom editors for VisualVertex and VisualEdges.
     */
    public void setVisualGraphComponentEditorFactory( VisualGraphComponentEditorFactory editorFactory ) {
        this.editorFactory = editorFactory;
    }

    /**
     * Returns the List of VisualVertices contained within this VisualGraph
     */
    public java.util.List getVisualVertices() {
        return this.visualVertices;
    }

    /**
     * Returns the List of VisualEdges contained within this VisualGraph
     */
    public java.util.List getVisualEdges() {
        return this.visualEdges;
    }



  private void setLinetype( int linetype ) {
    int i, size = visualEdges.size();

    this.linetype = linetype;
    for( i = 0; i < size; i++ ) {
      ((VisualEdge) this.visualEdges.get( i )).setLinetype( linetype );
    }
  }

  public void setOrthogonalLine(){
    this.setLinetype( salvo.jesus.graph.visual.VisualEdge.ORTHOGONAL_LINE );
  }

  public void setStraightLine(){
    this.setLinetype( salvo.jesus.graph.visual.VisualEdge.STRAIGHT_LINE );
  }

  /**
    * Ask the layout manager to layout the vertices of the graph according
    * to the rules of the layout manager.
    */
  public void layout() {
    if( this.layoutManager != null ) this.layoutManager.layout();
  }

  public VisualEdge getVisualEdge( Edge edge ) {
    Iterator			iterator;
    VisualEdge    visualedge;

    iterator = this.visualEdges.iterator();
    while( iterator.hasNext( )) {
      visualedge = (VisualEdge) iterator.next( );
      if( visualedge.getEdge( ) == edge )
        return visualedge;
    }

    return null;
  }

  public void setVisualEdge( Edge edge, VisualEdge newVisualEdge ) {
    VisualEdge  existingVisualEdge = this.getVisualEdge( edge );

    if( edge != null && newVisualEdge.getEdge() == edge ) {
        this.visualEdges.set(  this.visualEdges.indexOf( existingVisualEdge ), newVisualEdge );
    }
  }

  /**
   * Returns the VisualVertex object for the corresponding Vertex object in the
   * Graph object encapsulated by VisualGraph.
   *
   * @param	vertex		The Vertex object whose corresponding VisualVertex object we want
   * @return					The corresponding VisualVertx object of the vertex. Returns null if
   * vertex in not a Vertex in the Graph encapsulated by the VisualGraph object.
   */
  public VisualVertex getVisualVertex( Vertex vertex ) {
    Iterator			iterator;
    VisualVertex  visualvertex;

    iterator = visualVertices.iterator();
    while( iterator.hasNext( )) {
      visualvertex = (VisualVertex) iterator.next( );
      if( visualvertex.getVertex( ) == vertex )
        return visualvertex;
    }

    return null;
  }

  public void setVisualVertex( Vertex vertex, VisualVertex newVisualVertex ) {
    VisualVertex    existingVisualVertex = this.getVisualVertex( vertex );

    // Replace the VisualVertex of that vertex with a new one.
    if( existingVisualVertex != null && vertex == newVisualVertex.getVertex() ) {
        this.visualVertices.set(
            this.visualVertices.indexOf( existingVisualVertex ), newVisualVertex );
    }
  }

  /**
   * Returns a <tt>VisualEdge</tt> whose line is nearest the specified point.
   * Only <tt>VisualEdge</tt>s that have at least a distance of 5 are considered,
   * from which the <tt>VisualEdge</tt> that has the least distance among them
   * are returned.
   *
   * @param	x		x-coordinate
   * @param	y		y-coordinate
   * @return	    A <tt>VisualEdge</tt> object that is nearest the specified point
   */
  public VisualEdge getVisualEdge( int x, int y ){
    Iterator    iterator;
    VisualEdge  currentVEdge;
    VisualEdge  nearestVEdge = null;
    double      leastDistance = 5.0;
    double      currentDistance;

    iterator = visualEdges.iterator();
    while( iterator.hasNext() ){
        currentVEdge = (VisualEdge) iterator.next();
        currentDistance = currentVEdge.ptSegDist( x, y );
        if( currentDistance < leastDistance) {
            leastDistance = currentDistance;
            nearestVEdge = currentVEdge;
        }
    }

    return nearestVEdge;
  }

  /**
   * Adds a Vertex object to the Graph object encapsulated by the VisualGraph object.
   * This is simply a wrapper around graph.add( Vertex ).
   *
   * @param	vertex		The Vertex object to be added to the graph
   * @see							#add( Vertex, Font )
   */
  public void add( Vertex vertex ) throws Exception {
    graph.add( vertex );
    // No need to add to visualvertices ourselves, since the
    // graph will notify visualgraph anyway via vertexAdded()
  }

  /**
    * Adds a Vertex object to the Graph object encapsulated by the VisualGraph object
    * then set the Font used by corresponding VisualVertex object.
    *
    * @param	vertex		The Vertex object to be added to the graph
    * @param	f					Font object to be used for drawing the String inside the shape of VisualVertex.
    */
  public void add( Vertex vertex, Font f ) throws Exception {
    VisualVertex	newvisualvertex;

    graph.add( vertex );
    // At this point, the visual vertex has been added

    newvisualvertex = this.getVisualVertex( vertex );
    newvisualvertex.setFont( f );
    // No need to add to visualvertices ourselves, since the
    // graph will notify visualgraph anyway via vertexAdded()
  }

  /**
    * Adds an edge to the Graph object encapsulated by the VisualGraph object.
    * This is simply a wrapper around graph.addEdge( Vertex, Vertex ).
    *
    * @param	from		The source Vertex object of the edge
    * @param	to  		The destination Vertex object of the edge
    */
  public void addEdge( VisualVertex from, VisualVertex to ) throws Exception {
    graph.addEdge( from.getVertex(), to.getVertex() );
  }

  /**
   * Removes a Vertex object from the Graph object encapsulated by the VisualGraph object.
   * This is simply a wrapper around graph.remove( Vertex ). This will also eventually
   * remove the corresponding VisualVertex object from the VisualGraph object.
   *
   * @param	vertex	The Vertex object to be removed
   * @see						#remove( VisualVertex )
   */
  public void remove( Vertex vertex ) throws Exception {
    // All we need to do is remove the vertex from the graph,
    // and the graph will notify visualgraph (this) to remove it
    // from its visualvertices.
    graph.remove( vertex );
  }

  /**
   * Removes a Vertex object from the Graph object encapsulated by the VisualGraph object.
   * This is simply a wrapper around graph.remove( Vertex ), as this method
   * actually calls graph.remove( VisualVertex.getVertex()) so that the graph
   * removes the corresponding Vertex object first before the VisualVertex object
   * is removed from the VisualGraph object.
   *
   * @param	vertex	The VisualVertex object to be removed
   * @see						#remove( Vertex )
   */
  public void remove( VisualVertex vvertex ) throws Exception {
    // All we need to do is remove the vertex from the graph,
    // and the graph will notify visualgraph (this) to remove it
    // from its visualvertices.
    graph.remove( vvertex.getVertex());
  }

  /**
   * Removes an Edge object from the Graph object encapsulated by the VisualGraph object.
   * This is simply a wrapper around graph.removeEdge( Edge ). This will also eventually
   * remove the corresponding VisualEdge object from the VisualGraph object.
   *
   * @param	edge	The Edge object to be removed
   * @see					#removeEdge( VisualEdge )
   */
  public void removeEdge( Edge edge ) throws Exception {
    graph.removeEdge( edge );
  }

  /**
   * Removes an Edge object from the Graph object encapsulated by the VisualGraph object.
   * This is simply a wrapper around graph.removeEdge( Edge ), as this method
   * actually calls graph.removeEdge( VisualEdge.getEdge()) so that the graph
   * removes the corresponding Edge object first before the VisualEdge object
   * is removed from the VisualGraph object.
   *
   * @param	vedge	 The VisualEdge object to be removed
   * @see					 #removeEdge( Edge )
   */
  public void removeEdge( VisualEdge vedge ) throws Exception {
    // All we need to do is remove the edge from the graph,
    // and the graph will notify visualgraph (this) to remove it
    // from its visualedges.
    graph.removeEdge( vedge.getEdge() );
  }

  /**
    * Returns the VisualVertex object that has the given x and y coordinate
    * inside its shape. If there are more than one VisualVertex which contains
    * the specified coordinate, the first VisualVertex object in the
    * List visualvertices will be returned.
    *
    * @param	x		x-coordinate
    * @param	y		y-coordinate
    * @return		A VisualVertex object that contains the given coordinate
    */
  public VisualVertex getNode( int x, int y ){
    Iterator			  iterator;
    VisualVertex    vvertex;

    iterator = visualVertices.iterator();
    while( iterator.hasNext() ){
      vvertex = (VisualVertex) iterator.next();
      if( vvertex.getGeneralPath().contains( x, y ))
        return vvertex;
    }

    return null;
  }

  /**
   * Translates the vertices in the specified collection to their VisualVertex
   * counterpart. In reality, this method creates a new Collection object to
   * hold the visual vertices, thereby leaving the collection specified in the
   * argument untouched.
   *
   * @param   vertices  Collection of vertices to be translated to their
   *                    corresponding Visual Vertex. If the vertex does not have
   *                    a corresponding Visual Vertex or the vertex is not in the
   *                    graph, then null will be the translation.
   * @return  A List object that holds the corresponding Visual Vertex
   *          of each vertex in the vertices collection.
   *
   */
  public java.util.List translateToVisualVertices( Collection vertices ) {
    java.util.List    translated = new ArrayList( vertices );
    int     i = 0, size = translated.size();

    // ... translate the vertices into visualvertices
    for( i = 0;  i < size; i++ ) {
      translated.set( i, this.getVisualVertex( (Vertex) translated.get( i )) );
    }

    return translated;
  }

  /**
   * Translates each Visual Vertex object in the specified collection to their Vertex
   * counterpart. In reality, this method creates a new Collection object to
   * hold the vertices, thereby leaving the collection specified in the
   * argument untouched.
   *
   * @param   vertices  Collection of visual vertices to be translated to their
   *                    corresponding Vertex. If the vertex does not have
   *                    a corresponding Vertex or the visual vertex is not in the
   *                    graph, then null will be the translation.
   * @return  A List object that holds the corresponding Vertex
   *          of each Visual Vertex in the collection.
   *
   */
  public java.util.List translateToVertices( Collection visualvertices ) {
    java.util.List    translated = new ArrayList( visualvertices );
    int     i = 0, size = translated.size();

    // ... translate the visual vertices into vertices
    for( i = 0;  i < size; i++ ) {
      translated.set( i, ( (VisualVertex) translated.get( i )).getVertex() );
    }

    return translated;
  }

  /**
   * Translates each of the Edge object in the specified collection to their Edge
   * counterpart. In reality, this method creates a new Collection object to
   * hold the visual edges, thereby leaving the collection specified in the
   * argument untouched.
   *
   * @param   vertices  Collection of edges to be translated to their
   *                    corresponding Visual Edge. If the edge does not have
   *                    a corresponding Visual Edge or the edge is not in the
   *                    graph, then null will be the translation.
   * @return  A Collection object that holds the corresponding Visual Edge
   *          of each edge in the vertices collection.
   *
   */
  public java.util.List translateToVisualEdges( java.util.List edges ) {
    java.util.List    translated = new ArrayList( edges );
    int     i = 0, size = translated.size();

    // ... translate the edges into Visual Edges
    for( i = 0;  i < size; i++ ) {
      translated.set( i, this.getVisualEdge( (Edge) translated.get( i )) );
    }

    return translated;
  }

  /**
    * Draws the elements of VisualGraph (all of its VisualVertex and VisualEdges) using
    * the given Graphics2D graphics context object. This method is used when
    * dragging a VisualVertex so that the VisualVertex being dragged is always
    * on front of ther vertices and edges.
    *
    * @param	g2d		Graphics2D graphics context object to be used for painting
    * the elements of VisualGraph.
    * @param	onfront	If not null, the given VisualVertex will be redrawn at the last
    * stage so that it will always be on front of all other vertices and edges.
    */
  public void paint( Graphics2D g2d, VisualVertex onfront ){
    Iterator	    iterator;
    VisualEdge  	vedge;
    VisualVertex    vvertex;

    // Now paint the edges
    iterator = visualEdges.iterator();
    while( iterator.hasNext() ){
      vedge = (VisualEdge) iterator.next();
      vedge.paint( g2d, this.layoutManager );
    }

    // Now paint the vertices
    iterator = visualVertices.iterator();
    while( iterator.hasNext() ){
      vvertex = (VisualVertex) iterator.next();
      vvertex.paint( g2d );
    }

    // Draw the vertex onfront to make sure it is on top of all other vertices
    if( onfront != null )
      onfront.paint( g2d );

  }

  /**
    * Draws the elements of VisualGraph (all of its VisualVertex and VisualEdges) using
    * the given Graphics2D graphics context object.
    *
    * @param	g2d		Graphics2D graphics context object to be used for painting
    * the elements of VisualGraph.
    */
  public void paint( Graphics2D g2d ) {
    this.paint( g2d, (VisualVertex) null );
  }

  /**
    * Draws the elements of VisualGraph (all of its VisualVertex and VisualEdges) using
    * the given Graphics2D graphics context object. This method is used when
    * creating an edge by clicking on a given VisualVertex and dragging the
    * mouse so that a dashed line is drawn from the VisualVertex that was clicked
    * to the current coordinates of the mouse position.
    *
    * @param	g2d		Graphics2D graphics context object to be used for painting
    * the elements of VisualGraph.
    * @param	probableedgeline If not null, a dashed line is drawn.
    */
  public void paint( Graphics2D g2d, Line2D probableedgeline ) {
    final float dash1[] = {5.0f};

    final BasicStroke dashedstroke = new BasicStroke(
                                       1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                                       5.0f, dash1, 0.0f);
    Stroke	originalstroke;

    this.paint( g2d, (VisualVertex) null );
    if( probableedgeline != null ) {
      originalstroke = g2d.getStroke();
      g2d.setStroke( dashedstroke );
      g2d.draw( probableedgeline );
      g2d.setStroke( originalstroke );
    }

  }

  // ================== IMPLEMENTATION OF INTERFACES ==========================
  /**
    * This method is automatically called whenever a Vertex is added to the
    * Graph object encapsulated by the VisualGraph object.
    *
    * @param	e		GraphAddVertexEvent object that also specifies the Vertex that was added
    * to the graph
    */
  public void afterVertexAdded( GraphAddVertexEvent e ) {
    // Do not call this.add().
    // Otherwise, we would have a stack overflow. That is,
    // Graph.add() -> VisualGraph.vertexAdded() -> VisualGraph.add() ->
    // Graph.add() ad infinitum.
    VisualVertex    vvertex = new VisualVertex( e.getVertex(), this );

    visualVertices.add( vvertex );
    if( this.layoutManager != null )
      this.layoutManager.addVertex( vvertex );
    this.repaint();
  }

  /**
    * This method is automatically called whenever an Edge is added to the
    * Graph object encapsulated by the VisualGraph object.
    *
    * @param	e		GraphAddEdgeEvent object that also specifies the Edge that was added
    * to the graph
    */
  public void afterEdgeAdded( GraphAddEdgeEvent e ) {
    // Do not call this.addEdge().
    // Otherwise, we would have a stack overflow. That is,
    // Graph.addEdge() -> VisualGraph.edgeAdded() -> VisualGraph.addEdge() ->
    // Graph.addEdge() ad infinitum.
    VisualEdge  vedge = new VisualEdge( e.getEdge( ), this );
    vedge.setLinetype( this.linetype );

    visualEdges.add( vedge );
    if( this.layoutManager != null )
      this.layoutManager.addEdge( vedge );
    this.repaint();
  }

  /**
    * This method is automatically called whenever a Vertex is about to be
    * removed from the Graph object encapsulated by the VisualGraph object.
    *
    * @param	e		GraphRemoveVertexEvent object that also specifies the Vertex
    * that is about to be removed from the graph
    */
  public void afterVertexRemoved( GraphRemoveVertexEvent e ) {
    Vertex				vertextoremove;
    VisualVertex  visualvertex;
    Iterator			iterator;

    // Find the visual vertex representing the vertex to be removed
    // and remove it.
    vertextoremove = e.getVertex();
    iterator = visualVertices.iterator();
    while( iterator.hasNext()) {
      visualvertex = (VisualVertex) iterator.next();
      if( visualvertex.getVertex() == vertextoremove ){
        // Inform the layoutmanager first before removing the VisualVertex
        if( this.layoutManager != null )
          this.layoutManager.removeVertex( visualvertex );
        // Now safe to remove the VisualVertex
        visualVertices.remove( visualvertex );
        this.repaint();
        return;
      }
    }
  }

  /**
    * This method is automatically called whenever an Edge is 
    * removed from the Graph object encapsulated by the VisualGraph object.
    *
    * @param	e		GraphRemoveEdgeEvent object that also specifies the Edge
    * that is about to be removed from the graph
    */
  public void afterEdgeRemoved( GraphRemoveEdgeEvent e ) {
    Edge				 edgetoremove;
    VisualEdge  visualedge;
    Iterator		 iterator;

    // Find the visual edge representing the edge to be removed
    // and remove it.
    edgetoremove = e.getEdge();
    iterator = visualEdges.iterator();
    while( iterator.hasNext()) {
      visualedge = (VisualEdge) iterator.next();
      if( visualedge.getEdge() == edgetoremove ){
        // Inform the layoutmanager first before removing the VisualEdge
        if( this.layoutManager != null )
          this.layoutManager.removeEdge( visualedge );
        // Now safe to remove the VisualEdge object.
        visualEdges.remove( visualedge );
        this.repaint();
        return;
      }
    }
  }

  /**
   * Emphasizes a subgraph by changing the painters for the <tt>VisualGraphComponent</tt>s of
   * the subgraph.
   */
  public void emphasizeSubGraph( Graph subGraph, VisualVertexPainter vPainter, VisualEdgePainter ePainter ) {
    Iterator    vertexIterator;
    Iterator    edgeIterator;
    Vertex      ivertex;
    VisualVertex    vvertex;
    VisualEdge  vedge;

    vertexIterator = subGraph.getVerticesIterator();
    while( vertexIterator.hasNext() ) {
        ivertex = (Vertex) vertexIterator.next();
        vvertex = this.getVisualVertex( ivertex );
        if( vPainter != null ) {
            vvertex.setPainter( vPainter );
        }

        if( ePainter != null ) {
            edgeIterator = subGraph.getEdges( ivertex ).iterator();
            while( edgeIterator.hasNext() ) {
                vedge = this.getVisualEdge( (Edge) edgeIterator.next() );
                vedge.setPainter( ePainter );
            }
        }
    }

    this.repaint();
  }

  /**
   * Emphasizes a subgraph by changing the both the painters for the <tt>VisualGraphComponent</tt>s of
   * the subgraph and the painters for the main graph.
   */
  public void emphasizeSubGraph( Graph subGraph,
        VisualVertexPainter mainGraphVPainter, VisualEdgePainter mainGraphEPainter,
        VisualVertexPainter subGraphVPainter, VisualEdgePainter subGraphEPainter )
  {
    Iterator    vertexIterator;
    Iterator    edgeIterator;
    Vertex      ivertex;
    VisualVertex    vvertex;
    VisualEdge  vedge;

    vertexIterator = this.getGraph().getVerticesIterator();
    while( vertexIterator.hasNext() ) {
        ivertex = (Vertex) vertexIterator.next();
        vvertex = this.getVisualVertex( ivertex );
        if( mainGraphVPainter != null ) {
            vvertex.setPainter( mainGraphVPainter );
        }

        if( mainGraphEPainter != null ) {
            edgeIterator = this.getGraph().getEdges( ivertex ).iterator();
            while( edgeIterator.hasNext() ) {
                vedge = this.getVisualEdge( (Edge) edgeIterator.next() );
                vedge.setPainter( mainGraphEPainter );
            }
        }
    }

    this.emphasizeSubGraph( subGraph, subGraphVPainter, subGraphEPainter );

    this.repaint();
  }

}

