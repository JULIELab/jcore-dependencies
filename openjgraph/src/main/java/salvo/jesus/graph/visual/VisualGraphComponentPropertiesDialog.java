package salvo.jesus.graph.visual;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A modal dialog that whose only component is a VisualGraphComponentPropertiesPanel.
 * This is usually called from a popup menu on a VisualGraphComponent.
 *
 * @author   Jesus M. Salvo Jr.
 */
class VisualGraphComponentPropertiesDialog extends JDialog implements ActionListener {
  GraphPanel  gpanel;
  VisualGraphComponentPropertiesPanel panel;

  /**
   * Creates a VisualGraphComponentPropertiesDialog.
   *
   * @param   gpanel    The GraphPanel object on which the VisualGraphComponent's
   *                    VisualGraph is contained.
   * @param   vgcomponent The VisualGraphComponent object whose properties are to be shown.
   */
  public VisualGraphComponentPropertiesDialog( GraphPanel gpanel, VisualGraphComponent vgcomponent ) {
    try {
      this.gpanel = gpanel;
      this.initVisualGraphComponentPropertiesDialog( vgcomponent );
    }
    catch( Exception e ) {
      e.printStackTrace();
    }
  }

  /**
   * Creates the internal VisualGraphComponentPropertiesPanel object and registers itself
   * as a listener for the VisualGraphComponentPropertiesPanel's apply, ok, and cancel JButtons.
   */
  private void initVisualGraphComponentPropertiesDialog( VisualGraphComponent vgcomponent ) throws Exception {
    Dimension   dialogSize;
    Dimension   screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    this.panel = new VisualGraphComponentPropertiesPanel( vgcomponent );

    this.getContentPane().setLayout( new BorderLayout() );
    this.getContentPane().add( panel, BorderLayout.CENTER );

    this.panel.addApplyActionListener( this );
    this.panel.addOKActionListener( this );
    this.panel.addCancelActionListener( this );
    this.setTitle( "Properties" );

    this.pack();

    dialogSize = this.getSize();
    this.setLocation(
      (screenSize.width - dialogSize.width )/2 + 50,
      (screenSize.height - dialogSize.height)/2 );

    this.setModal( true );
    this.setResizable( false );
    this.setVisible( true );
  }

  /**
   * Implementation of the actionPerformed method of the ActionListener interface.
   * This method is called when the apply, ok JButtons of the internal
   * VisualGraphComponentPropertiesPanel are pressed.
   */
  public void actionPerformed( ActionEvent e ) {
    String  action = e.getActionCommand();

    if( action.equals( VisualGraphComponentPropertiesPanel.APPLY )) {
      this.gpanel.repaint();
    }

    if( action.equals( VisualGraphComponentPropertiesPanel.OK ) ||
        action.equals( VisualGraphComponentPropertiesPanel.CANCEL )) {
      this.setVisible( false );
      this.dispatchEvent( new WindowEvent( this, WindowEvent.WINDOW_CLOSING ));
      this.dispose();
    }
  }
}

