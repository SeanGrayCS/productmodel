package com.seangraycs;

import java.util.ArrayList;
import java.util.List;

import com.seangraycs.TransposedMapper.MappedValue;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.EmulGLSkin;
import org.jzy3d.chart.factories.EmulGLChartFactory;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.IMultiColorable;
import org.jzy3d.colors.colormaps.ColorMapHotCold;
import org.jzy3d.colors.colormaps.IColorMap;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;
import org.jzy3d.maths.Rectangle;
import org.jzy3d.maths.algorithms.interpolation.IInterpolator;
import org.jzy3d.maths.algorithms.interpolation.algorithms.BernsteinPolynomial;
import org.jzy3d.maths.algorithms.interpolation.algorithms.Spline3D;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.SurfaceBuilder;
import org.jzy3d.plot3d.primitives.Drawable;
import org.jzy3d.plot3d.primitives.LineStripInterpolated;
import org.jzy3d.plot3d.primitives.ScatterMultiColor;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;

public class Graph3D {

  private String title;

  private double[][] points;
  private double[] mins;
  private double[] maxs;
  private boolean zExists;

  private Model model;

  private ScatterMultiColor scatter;
  private ArrayList<Drawable> regs;

  private final ColorMapAlpha colorMap;
  private final ColorMapper colorMapper;

  public Graph3D(Model model, String title) {
    this(model, title, new ColorMapHotCold());
  }

  public Graph3D(Model model, String title, IColorMap baseColorMap) {
    colorMap = new ColorMapAlpha(baseColorMap, 0.75f);
    
    this.model = model;
    this.title = title;
    this.points = model.getPoints();

    mins = new double[points[0].length];
    maxs = new double[points[0].length];
    zExists = mins.length > 2;

    for (int j = 0; j < points[0].length; j++) {
      mins[j] = Double.MAX_VALUE;
      maxs[j] = Double.MIN_VALUE;

      for (int i = 0; i < points.length; i++) {
        if (points[i][j] < mins[j]) {
          mins[j] = points[i][j];
        }
        if (points[i][j] > maxs[j]) {
          maxs[j] = points[i][j];
        }
      }
    }

    initRegs();
    colorMapper = new ColorMapper(colorMap, getZRange());
    regs.forEach((reg) -> {
      if (reg instanceof IMultiColorable) {
        ((IMultiColorable) reg).setColorMapper(colorMapper);
      }
    });
    colorMap.setAlpha(1.0f);
    initScatter();
  }

  public String getTitle() {
    return title;
  }

  public void makeChart() {
    Quality q = Quality.Advanced();
    q.setAnimated(false);
    q.setHiDPIEnabled(true);

    Chart chart = new EmulGLChartFactory().newChart(q);

    chart.add(scatter);
    chart.add(regs);

    chart.open(title, new Rectangle(0, 0, 1920, 1080));
    chart.addMouseCameraController();

    EmulGLSkin skin = EmulGLSkin.on(chart);
    skin.getCanvas().setProfileDisplayMethod(true);
  }

  private void initScatter() {
    int numPoints = points.length;

    Coord3d[] points3d = new Coord3d[numPoints];
    for (int i = 0; i < numPoints; i++) {
      double[] point = points[i];
      float x = (float) point[0];
      float y = (float) point[1];
      float z = (float) (zExists ? point[2] : 0.0);
      points3d[i] = new Coord3d(x, y, z);
    }

    scatter = new ScatterMultiColor(points3d, colorMapper);
    scatter.setWidth(5);
  }

  private Range getZRange() {
    double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
    for (Drawable reg : regs) {
      BoundingBox3d bounds = reg.getBounds();

      double minZ = bounds.getZmin();
      if (minZ < min) {
        min = minZ;
      }

      double maxZ = bounds.getZmax();
      if (maxZ > max) {
        max = maxZ;
      }
    }
    return new Range(min, max);
  }

  private void initRegs() {
    regs = new ArrayList<>();

    Range xrange = new Range(mins[0], maxs[0]);
    Range yrange = new Range(mins[1], maxs[1]);

    int xsteps = (int) (xrange.getRange() / 6);
    int ysteps = (int) (yrange.getRange() / 6);

    // Major optimization needed - calculate predictions based on entire data arrays in mapper constructor,
    // then use mapper to pull from internal predictions
    MappedValue X = MappedValue.X, Y = MappedValue.Y;
    TransposedMapper xmapper = new TransposedMapper(model, X, zExists);
    TransposedMapper ymapper = new TransposedMapper(model, Y, zExists);

    if (!zExists) {
      BernsteinPolynomial polynomial = new BernsteinPolynomial(3);
      IInterpolator interpolator = new IInterpolator() {
        @Override
        public List<Coord3d> interpolate(List<Coord3d> controlPoints, int resolution) {
          Spline3D spline = new Spline3D(controlPoints, polynomial, 0.25f);
          return spline.computeVertices(resolution);
        }
      };

      List<Coord3d> xcoords = getLineCoords(xmapper, yrange, ysteps, X);
      List<Coord3d> ycoords = getLineCoords(ymapper, xrange, xsteps, Y);

      LineStripInterpolated xline = new LineStripInterpolated(interpolator, xcoords, 2);
      LineStripInterpolated yline = new LineStripInterpolated(interpolator, ycoords, 2);

      regs.add(xline);
      regs.add(yline);
    } else {
      Range zrange = new Range(mins[2], maxs[2]);
      int zsteps = (int) (1 + (maxs[2] - mins[2]) * 2);
      MappedValue Z = MappedValue.Z;
      Mapper zmapper = new TransposedMapper(model, Z, zExists);

      TransposedOrthonormalGrid xgrid = new TransposedOrthonormalGrid(yrange, ysteps, zrange, zsteps, X);
      TransposedOrthonormalGrid ygrid = new TransposedOrthonormalGrid(xrange, xsteps, zrange, zsteps, Y);
      TransposedOrthonormalGrid zgrid = new TransposedOrthonormalGrid(xrange, xsteps, yrange, ysteps, Z);

      SurfaceBuilder sb = new SurfaceBuilder();
      Shape xsurface = sb.orthonormal(xgrid, xmapper);
      Shape ysurface = sb.orthonormal(ygrid, ymapper);
      Shape zsurface = sb.orthonormal(zgrid, zmapper);

      regs.add(xsurface);
      regs.add(ysurface);
      regs.add(zsurface);
    }
  }

  private static List<Coord3d> getLineCoords(TransposedMapper mapper, Range range, int steps, MappedValue mappedValue) {
    List<Coord3d> coords = new ArrayList<>(steps);
    double step = range.getRange() / (double) (steps - 1);

    double[] givens = new double[steps];
    for (int i = 0; i < steps; i++) {
      givens[i] = range.getMin() + i * step;
    }
    double[] predictions = mapper.getPredictions(givens);

    for (int i = 0; i < steps; i++) {
      double given = givens[i];
      double pred = predictions[i];

      double x = 0, y = 0;
      switch (mappedValue) {
        case X -> {
          x = pred;
          y = given;
        }
        case Y -> {
          x = given;
          y = pred;
        }
        case Z -> {
          new Exception("Exception: Illegal Parameter:\n\tgetLineCoords passed Z. Only X and Y permitted.\n").printStackTrace();
        }
      }

      coords.add(new Coord3d(x, y, 0));
    }

    return coords;
  }
}