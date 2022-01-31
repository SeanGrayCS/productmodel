package com.seangraycs;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;

/**
 * Graph is deprecated. Use Graph3D instead.
 */
public class Graph extends Canvas {

  private double[][] points;
  private double[] mins;
  private double[] maxs;

  private Model model;

  private ArrayList<Line2D.Float> linesX;
  private ArrayList<Line2D.Float> linesY;

  public Graph(Model model, int width, int height) {
    super();
    setSize(width, height);
    this.model = model;
    this.points = model.getPoints();

    mins = new double[points[0].length];
    maxs = new double[points[0].length];
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

    linesX = new ArrayList<>();
    linesY = new ArrayList<>();
    initLines();
  }

  public void paint(Graphics g) {
    int h = getHeight(), w = getWidth();
    g.setColor(Color.BLACK);
    g.setFont(new Font(Font.SERIF, Font.PLAIN, 10));

    Graphics2D g2d = (Graphics2D) g;
    g2d.setStroke(new BasicStroke(3));
    g2d.draw(new Line2D.Float(30, h - 10, 30, 10));
    g2d.draw(new Line2D.Float(10, h - 30, w - 10, h - 30));

    double xscale = (w - 150) / (maxs[0] - mins[0]);
    double yscale = (h - 150) / (maxs[1] - mins[1]);
    // System.out.println(w + " " + h);

    double min = Math.min(mins[0], mins[1]);
    double max = Math.max(maxs[0], maxs[1]);

    int interval = 10 * (int) (Math.floor((max - min) / 100));
    for (int i = 2; i < 10; i++) {
      double pow = Math.pow(10, i);
      if (interval % ((int) pow) == interval) {
        break;
      }

      double itvl = interval;
      itvl /= pow;
      itvl = Math.floor(itvl);
      interval = (int) pow * (int) itvl;
    }

    for (int i = interval * (int) (1 + Math.ceil(min) / interval); i <= interval
        * (int) (1 + Math.floor(max) / interval); i += interval) {

      int x = (int) (xscale * (i - mins[0]));
      int y = (int) (yscale * (i - mins[1]));

      g.drawLine(x, h - 25, x, h - 35);
      g.drawString("" + i, x, h - 15);
      g.drawLine(25, h - y, 35, h - y);
      g.drawString("" + i, 15, h - y);
    }

    for (double[] point : points) {
      int x = (int) (xscale * (point[0] - mins[0]));
      int y = (int) (yscale * (point[1] - mins[1]));
      g.drawOval(x + 47, h - 53 - y, 5, 5);
    }

    g2d.setColor(Color.BLUE);
    for (Line2D.Float line : linesX) {
      g2d.draw(line);
    }

    g2d.setColor(Color.GREEN);
    for (Line2D line : linesY) {
      g2d.draw(line);
    }
  }

  private void initLines() {
    int h = getHeight(), w = getWidth();

    double xscale = (w - 150) / (maxs[0] - mins[0]);
    double yscale = (h - 150) / (maxs[1] - mins[1]);
    // System.out.println(w + " " + h);

    double[][] xVals = new double[(int) (1 + (maxs[0] - mins[0]) * 2)][model.getThetas().length];
    double[][] yVals = new double[(int) (1 + (maxs[1] - mins[1]) * 2)][model.getThetas().length];

    int count = 0;
    for (double x = mins[0]; x <= maxs[0]; x += 0.5, count++) {
      xVals[count][0] = x;

      int len = xVals[count].length;
      for (int i = 2; i < len; i++) {
        xVals[count][i] = (50 - x) / (len - 1);
      }
    }

    count = 0;
    for (double y = mins[1]; y <= maxs[1]; y += 0.5, count++) {
      yVals[count][1] = y;

      int len = yVals[count].length;
      for (int i = 2; i < len; i++) {
        yVals[count][i] = (50 - y) / (len - 1);
      }
    }

    double[] xPreds = model.predictMany(yVals, 0);
    double[] yPreds = model.predictMany(xVals, 1);

    int x1 = 50 + (int) (xscale * (xVals[0][0] - mins[0]));
    int y1 = h - 50 - (int) (yscale * (yPreds[0] - mins[1]));
    for (int i = 1; i < xVals.length; i++) {
      int x2 = 50 + (int) (xscale * (xVals[i][0] - mins[0]));
      int y2 = h - 50 - (int) (yscale * (yPreds[i] - mins[1]));

      // System.out.println("X, Y: " + xVals[i][1] + ", " + yPreds[i]);
      // System.out.println("Scaled: " + (xscale * (xVals[i][1] - mins[0])) + ", " +
      // (yscale * (yPreds[i] - mins[1])));
      // System.out.println("Pixels: " + x1 + ", " + y1);

      linesX.add(new Line2D.Float(x1, y1, x2, y2));
      x1 = x2;
      y1 = y2;
    }

    x1 = 50 + (int) (xscale * (xPreds[0] - mins[0]));
    y1 = h - 50 - (int) (yscale * (yVals[0][1] - mins[1]));
    for (int i = 1; i < yVals.length; i++) {
      int x2 = 50 + (int) (xscale * (xPreds[i] - mins[0]));
      int y2 = h - 50 - (int) (yscale * (yVals[i][1] - mins[1]));

      // System.out.println("X, Y: " + xPreds[i] + ", " + yVals[i][1]);
      // System.out.println("Scaled: " + (xscale * (xPreds[i] - mins[0])) + ", " +
      // (yscale * (yVals[i][1] - mins[1])));
      // System.out.println("Pixels: " + x1 + ", " + y1);

      linesY.add(new Line2D.Float(x1, y1, x2, y2));
      x1 = x2;
      y1 = y2;
    }
  }

}