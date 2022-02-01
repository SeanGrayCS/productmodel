package com.seangraycs;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.EmulGLSkin;
import org.jzy3d.chart.factories.EmulGLChartFactory;
import org.jzy3d.colors.colormaps.ColorMapHotCold;
import org.jzy3d.colors.colormaps.IColorMap;
import org.jzy3d.maths.Rectangle;
import org.jzy3d.plot3d.rendering.canvas.Quality;

public class Graph3D {

  private String title;
  private int numModels;
  private ModelGraph[] modelGraphs;

  public Graph3D(Model[] models, String title) {
    this(models, title, defaultColorMaps(models.length));
  }

  public Graph3D(Model[] models, String title, IColorMap[] baseColorMaps) {
    this.title = title;
    numModels = models.length;
    modelGraphs = new ModelGraph[numModels];
    for (int i = 0; i < numModels; i++) {
      modelGraphs[i] = new ModelGraph(models[i], baseColorMaps[i]);
    }
  }

  private static ColorMapHotCold[] defaultColorMaps(int len) {
    ColorMapHotCold[] colorMaps = new ColorMapHotCold[len];
    for (int i = 0; i < len; i++) {
      colorMaps[i] = new ColorMapHotCold();
    }
    return colorMaps;
  }

  public String getTitle() {
    return title;
  }

  public void makeChart() {
    Quality q = Quality.Advanced();
    q.setAnimated(false);
    q.setHiDPIEnabled(true);

    Chart chart = new EmulGLChartFactory().newChart(q);

    for (ModelGraph modelGraph : modelGraphs) {
      chart.add(modelGraph.getScatter());
      chart.add(modelGraph.getRegs());
    }

    chart.open(title, new Rectangle(0, 0, 1920, 1080));
    chart.addMouseCameraController();

    EmulGLSkin skin = EmulGLSkin.on(chart);
    skin.getCanvas().setProfileDisplayMethod(true);
  }

}