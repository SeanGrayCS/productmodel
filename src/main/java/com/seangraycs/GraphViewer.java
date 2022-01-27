/*package com.seangraycs;

import java.awt.Color;

import javax.swing.JFrame;

public class GraphViewer extends JFrame {
  
  private Graph graph;

  public GraphViewer(Graph graph, String title) {
    super();
    this.graph = graph;

    this.graph.setBackground(Color.WHITE);

    setTitle(title.substring(6, title.length()-3));
    add(this.graph);
    setSize(800, 600);
    setVisible(true);
    repaint();
    Quality q = Quality.Advanced();
    q.setAnimated(false);
    q.setHiDPIEnabled(true);

    Chart chart = new EmulGLChartFactory().newChart(q);
    chart.add(scatter);
    chart.open();
    chart.addMouseCameraController();

    EmulGLSkin skin = EmulGLSkin.on(chart);
    skin.getCanvas().setProfileDisplayMethod(true);
  }

  

}
*/