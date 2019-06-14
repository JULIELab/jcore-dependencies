package salvo.jesus.graph.visual;

import salvo.jesus.graph.Edge;
import salvo.jesus.graph.javax.swing.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * A subclass of JTabbedPane so that an initial JTabPanel
 * is automatically added as a tab page. The intention is to have
 * groups of JTabPanels added to show properties of a VisualGraphComponent.
 *
 * The object created will have an initial JTabPanel, FontandColorChooser,
 * to allow the user to specify colors and font for the VisualGraphComponent.
 *
 * @author  Jesus M. Salvo Jr.
 */

public class GraphTabbedPane extends JTabbedPane {

  /**
   * Creates a GraphTabbedPane object where all JTabPanels show
   * certain properties of a VisualGraphComponent. The object created
   * will have an initial JTabPanel, FontandColorChooser, to allow
   * the user to specify colors and font for the VisualGraphComponent.
   *
   * @param vgcomponent   VisualGraphComponent where
   */
  public GraphTabbedPane( VisualGraphComponent vgcomponent ) {
    try {
      this.initGraphTabbedPane( vgcomponent );
    }
    catch( Exception e ) {
      e.printStackTrace();
    }
  }

  private void initGraphTabbedPane ( VisualGraphComponent vgcomponent )
  throws Exception {
    this.addTab( "Label", new FontChooserPanel( vgcomponent ));
    this.addTab( "Shape", new ShapePanel( vgcomponent ));

    // Get the factory
    VisualGraphComponentEditorFactory factory =
        vgcomponent.getVisualGraph().getVisualGraphComponentEditorFactory();
    if( factory == null )
        return;
    // Get the custom tabs
    JTabPanel[] customTabs = factory.getTabEditors( vgcomponent );
    // If there are custom tabs, add them
    if( customTabs != null && customTabs.length > 0 ) {
        for( int i = 0; i < customTabs.length; i++ ) {
            this.addTab( customTabs[i].toString(), customTabs[i] );
        }
    }
  }
}

/**
 * An implementation of JPanel that allows the user to specify the
 * font and and colors of a specified VisualGraphComponent.
 *
 * @author  Jesus M. Salvo Jr.
 */
class FontChooserPanel extends JTabPanel {
  VisualGraphComponent  vgcomponent;

  JFontChooser    fontchooser;

  JLabel          textLabel;
  JTextArea       textarea;

  JLabel          fontcolorlabel;
  JButton         fontcolorbutton;
  JButtonColorListener  fontcolorlistener;
  JColor          fontcolor;

  JCheckBox       followLabel;

  /**
   * Creates an FontandColorChooser object where the current font and colors
   * of the specified VisualGraphComponent are shown and allowed to be
   * modified.
   *
   * @param   vgcomponent   VisualGraphComponent whose font and colors
   *                        will be shown and modified.
   */
  FontChooserPanel( VisualGraphComponent vgcomponent ) {
    this.vgcomponent = vgcomponent;
    this.initFontandColorChooser( vgcomponent.getFont(), vgcomponent.getFontcolor() );

  }

  /**
   * Creates the components.
   *
   * @param   font            The current font of the VisualGraphComponent.
   * @param   fontcolor       The current font color of the VisualGraphComponent.
   */
  private void initFontandColorChooser( Font font, Color fontcolor ) {
    // Initialie font chooser
    this.fontchooser = new JFontChooser( font );

    // Initialise text area
    this.textarea = new JTextArea( vgcomponent.getLabel() );
    this.textarea.setEditable( true );
    this.textarea.setRows( 4 );

    JScrollPane scroller = new JScrollPane( this.textarea );
    scroller.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
    scroller.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );

    JPanel  textPanel = new JPanel();
    textPanel.setLayout( new BorderLayout() );
    textPanel.add( scroller, BorderLayout.CENTER );

    if( this.vgcomponent instanceof VisualEdge ) {
      VisualEdge vEdge = (VisualEdge) this.vgcomponent;
      this.followLabel = new JCheckBox( "Dynamically generate label from vertices" );
      this.followLabel.addItemListener(
        new FollowVertexLabelCheckBoxListener( vEdge.getEdge(), this.textarea ) );
      if( vEdge.getEdge().isFollowVertexLabel() ) {
        this.followLabel.setSelected( true );
        this.textarea.disable();
      }

      textPanel.add( this.followLabel, BorderLayout.NORTH );
    }

    // Initialise color chooser
    this.fontcolor = new JColor( fontcolor );
    this.fontcolorlabel = new JLabel( "Font Color:" );
    this.fontcolorbutton = new JButton();

    // Create the listeners (the controller), specifying the
    // colors (model) and the buttons (view) representing the colors.
    this.fontcolorlistener = new JButtonColorListener( this, this.fontcolor, this.fontcolorbutton );

    // Register the listeners to listen for changes in the Color object
    // encapsulated by the JColor objects.
    this.fontcolor.addActionListener( this.fontcolorlistener );

    // Register the listeners to listen button clicks/press on the
    // JButtons.
    this.fontcolorbutton.addActionListener( this.fontcolorlistener );

    // Force update on the buttons
    this.fontcolor.setColor( this.fontcolor.getColor() );


    JPanel  colorspanel = new JPanel();
    colorspanel.setLayout( new GridLayout( 0, 2 ));
    colorspanel.add( fontcolorlabel );
    colorspanel.add( fontcolorbutton );
    colorspanel.add( Box.createRigidArea( new Dimension( 5, 5 )));
    colorspanel.add( Box.createRigidArea( new Dimension( 5, 5 )));

    this.setLayout( new BorderLayout() );
    this.add( fontchooser, BorderLayout.NORTH );
    this.add( textPanel, BorderLayout.CENTER );
    this.add( colorspanel, BorderLayout.SOUTH );
  }

  /**
   * Implementation of JTabPanel's apply() method. This implementation
   * sets the label, font, outline and fill color of the VisualGraphComponent specified
   * in the constructor.
   */
  public void apply() {
    this.vgcomponent.setFont( this.fontchooser.getSelectedFont() );
    this.vgcomponent.setFontcolor( this.fontcolor.getColor() );

    if( this.vgcomponent instanceof VisualEdge ) {
      VisualEdge vEdge = (VisualEdge) this.vgcomponent;
      vEdge.getEdge().setFollowVertexLabel( this.followLabel.isSelected() );
      if( !this.followLabel.isSelected() ) {
        vEdge.getEdge().setLabel( this.textarea.getText() );
      }
    }
    else {
      this.vgcomponent.setLabel( this.textarea.getText() );
    }
  }

  /**
   * Implementation of JTabPanel's ok() method. Simply calls FontandColorChooser.apply()
   */
  public void ok() {
    this.apply();
  }

}

/**
 * An implementation of JPanel that allows the user to specify the
 * font and colors of the GeneralPath of the VisualGraphComponent.
 *
 * @author  Jesus M. Salvo Jr.
 */
class ShapePanel extends JTabPanel {
    VisualGraphComponent  vgcomponent;

    JPanel      shapePanel;
    JComboBox   shapeList;

    GeneralPathPanelList    pathPanelList;

    JLabel      outlineColorButtonLabel;
    JLabel      fillColorLabel;

    JColor      outlineColorButton;
    JColor      fillColor;

    JButton     outlineColorButtonbutton;
    JButton     fillColorButton;

    JButtonColorListener  outlineColorButtonListener;
    JButtonColorListener  fillColorlistener;


    ShapePanel( VisualGraphComponent vgcomponent ) {
        this.vgcomponent = vgcomponent;
        this.initShapePanel( vgcomponent.getOutlinecolor(), vgcomponent.getFillcolor() );
    }

    private void initShapePanel( Color outlineColorButton, Color fillColor ) {

        if( this.vgcomponent instanceof VisualVertex ) {
            String options[] = { "Rectangle", "RoundedRectangle", "Ellipse" };
            this.shapeList = new JComboBox( options );
            this.shapePanel = new JPanel();
            this.shapePanel.setLayout( new GridLayout(2,1));
            this.shapePanel.add( new JLabel( "Change Shape To" ));
            this.shapePanel.add( this.shapeList );
        }

        this.pathPanelList = new GeneralPathPanelList( vgcomponent.getGeneralPath(),
            vgcomponent.getOutlinecolor() );

        this.outlineColorButton = new JColor( outlineColorButton );
        this.fillColor = new JColor( fillColor );

        this.outlineColorButtonLabel = new JLabel( "Outline Color:" );
        this.fillColorLabel = new JLabel( "Fill Color:" );

        this.outlineColorButtonbutton = new JButton();
        this.fillColorButton = new JButton();


        // Register the listeners to listen for changes in the Color object
        // encapsulated by the JColor objects.
        this.outlineColorButtonListener = new JButtonColorListener( this, this.outlineColorButton, this.outlineColorButtonbutton );
        this.fillColorlistener = new JButtonColorListener( this, this.fillColor, this.fillColorButton );

        // Register the listeners to listen button clicks/press on the
        // JButtons.
        this.outlineColorButton.addActionListener( this.outlineColorButtonListener );
        this.fillColor.addActionListener( this.fillColorlistener );

        // Register the listeners to listen button clicks/press on the
        // JButtons.
        this.outlineColorButtonbutton.addActionListener( this.outlineColorButtonListener );
        this.fillColorButton.addActionListener( this.fillColorlistener );

        // Force update on the buttons
        this.outlineColorButton.setColor( this.outlineColorButton.getColor() );
        this.fillColor.setColor( this.fillColor.getColor() );

        JPanel  colorspanel = new JPanel();
        colorspanel.setLayout( new GridLayout( 0, 2 ));
        colorspanel.add( this.outlineColorButtonLabel );
        colorspanel.add( this.outlineColorButtonbutton );
        colorspanel.add( this.fillColorLabel );
        colorspanel.add( this.fillColorButton );

        this.setLayout( new BorderLayout() );
        this.add( this.pathPanelList, BorderLayout.CENTER );
        if( this.vgcomponent instanceof VisualVertex ) {
            this.add( this.shapePanel, BorderLayout.NORTH );
            this.add( colorspanel, BorderLayout.SOUTH );
        }
        else {
            this.add( colorspanel, BorderLayout.NORTH );
        }
    }

    /**
    * Implementation of JTabPanel's apply() method. This implementation
    * sets the shape, outline and fill color of the VisualGraphComponent specified
    * in the constructor.
    */
    public void apply() {

        if( this.vgcomponent instanceof VisualVertex ) {
            String  selectedShape = (String) this.shapeList.getSelectedItem();
            Rectangle2D bounds = this.vgcomponent.getGeneralPath().getBounds2D();

            if( selectedShape.equals( "Rectangle")) {
                Rectangle2D rect = new Rectangle2D.Double(
                    bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
                this.vgcomponent.setGeneralPath( rect.getPathIterator( null ) );
            }
            else if( selectedShape.equals( "RoundedRectangle")) {
                RoundRectangle2D rect = new RoundRectangle2D.Double(
                    bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight(),
                    10, 10 );
                this.vgcomponent.setGeneralPath( rect.getPathIterator( null ) );
            }
            else if( selectedShape.equals( "Ellipse")) {
                Ellipse2D ellipse = new Ellipse2D.Double(
                    bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
                this.vgcomponent.setGeneralPath( ellipse.getPathIterator( null ) );
            }
        }

        this.vgcomponent.setFillcolor( this.fillColor.getColor() );
        this.vgcomponent.setOutlinecolor( this.outlineColorButton.getColor() );

        this.pathPanelList.setGeneralPath( this.vgcomponent.getGeneralPath() );
        this.pathPanelList.setOutlineColor( this.vgcomponent.getOutlinecolor() );
    }

    /**
    * Implementation of JTabPanel's ok() method. Simply calls FontandColorChooser.apply()
    */
    public void ok() {
        this.apply();
    }

}


/**
 * Listener for check box that will enable or disable the editing of the label
 */
class FollowVertexLabelCheckBoxListener implements ItemListener {

  Edge        edge;
  JTextArea   text;

  public FollowVertexLabelCheckBoxListener( Edge p_edge, JTextArea p_area ) {
    this.edge = p_edge;
    this.text = p_area;
  }

  public void itemStateChanged( ItemEvent e ) {
    if( e.getStateChange() == ItemEvent.SELECTED ) {
      edge.setFollowVertexLabel( true );
      this.text.setText( edge.getLabel() );
      this.text.disable();
      this.text.repaint();
    }
    else if( e.getStateChange() == ItemEvent.DESELECTED ) {
      edge.setFollowVertexLabel( false );
      this.text.enable();
      this.text.repaint();
    }
  }

}