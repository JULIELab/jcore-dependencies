/* Copyright (C) 2002 Dept. of Computer Science, Univ. of Massachusetts, Amherst

   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet

   This program toolkit free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License as
   published by the Free Software Foundation; either version 2 of the
   License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  For more
   details see the GNU General Public License and the file README-LEGAL.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
   02111-1307, USA. */


/**
	 @author Ben Wellner
 */
package edu.umass.cs.mallet.projects.seg_plus_coref.graphs;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import salvo.jesus.graph.*;
import salvo.jesus.graph.visual.*;
import salvo.jesus.graph.visual.layout.*;

public class Test1 extends JFrame
{
	Vertex v1, v2, v3;
	Edge   e1, e2, e3;

	Graph graph;
	GraphEditor gedit1, gedit2;

	public Test1() throws Exception
	{
		graph = new WeightedGraphImpl();
		gedit1 = new GraphEditor();
		gedit2 = new GraphEditor();
		
		v1 = new VertexImpl ("1");
		v2 = new VertexImpl ("2");
		v3 = new VertexImpl ("3");

		graph.add (v1);
		graph.add (v2);
		graph.add (v3);

		e1 = new WeightedEdgeImpl (v1, v2, 1.0);
		e2 = new WeightedEdgeImpl (v3, v2, 1.0);
		e3 = new WeightedEdgeImpl (v1, v2, -1.0);
		graph.addEdge (e1);
		graph.addEdge (e2);
		graph.addEdge (e3);

		gedit1.setGraph (graph);
		gedit2.setGraph (graph);

		this.getContentPane().setLayout (new GridLayout (1,2));
		    this.getContentPane().add( gedit1 );
    this.getContentPane().add( gedit2 );

    gedit1.setGraphLayoutManager( new StraightLineLayout( gedit1.getVisualGraph() ) );
    gedit2.setGraphLayoutManager( new OrthogonalLineLayout( gedit2.getVisualGraph() ) );

    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) { System.exit(0); }
      });

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = new Dimension( screenSize.width - 80, screenSize.height - 80 );

    this.setSize( frameSize );
    this.setLocation((int)(screenSize.getWidth() - frameSize.getWidth()) / 2, (int)(screenSize.getHeight() - frameSize.getHeight()) / 2);
		
	}

	public static void main(String[] args) throws Exception {
    Test1 frame = new Test1();
    frame.setTitle("Test1");
    frame.setVisible(true);
  }
		

}

	
