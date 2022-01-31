package com.seangraycs;

import java.io.Serializable;
import java.util.Arrays;

import org.ejml.simple.SimpleMatrix;

public class Model implements Serializable {
  private static final long serialVersionUID = 0L;

  private int n, m;
  private String[] products;
  private double[][] points;
  private double[] maxs;

  private int numFolds;
  private boolean trained;

  private SimpleMatrix[] thetas;
  private double fitness;

  public Model(String[] products, double[][] points, int numFolds) {
    this.products = products;
    this.points = points;
    n = points.length;
    m = points[0].length;

    maxs = new double[m];
    updateMaxs(points);

    this.numFolds = numFolds;
    thetas = new SimpleMatrix[m];
    fitness = -1;
  }

  private void updateMaxs(double[][] data) {
    for (int i = 0; i < data.length; i++) {
      for (int j = 0; j < m; j++) {
        double val = data[i][j];
        if (val > maxs[j]) {
          maxs[j] = val;
        }
      }
    }
  }

  public double[][] getPoints() {
    return points;
  }

  public SimpleMatrix[] getThetas() {
    return thetas;
  }

  public double getFitness() {
    return fitness;
  }

  public void addPoints(double[][] data) {
    if (data[0].length != m) {
      return;
    }

    updateMaxs(data);
    int len = data.length;
    n += len;
    double[][] result = Arrays.copyOf(points, n);
    System.arraycopy(data, 0, result, n - len, len);

    points = result;
    trained = false;
  }

  public void setFitness(double fitness) {
    this.fitness = fitness;
  }

  public double test(double[][] testData) {
    return test(testData, -1);
  }

  public double test(double[][] testData, int idx) {
    if (!trained) {
      train();
    }
    if (idx == -1) {
      return test(null, testData, idx);
    }
    return test(thetas[idx], testData, idx);
  }

  public double test(SimpleMatrix Theta, double[][] testData, int idx) {
    double fitness = 0.0;
    for (int i = 0; i < testData.length; i++) {
      double score = test(Theta, testData[i], idx);
      fitness += (score / testData.length);
    }

    fitness = Math.sqrt(fitness);
    // System.out.println("Fitness: " + fitness);
    return fitness;
  }

  public double test(double[] testData) {
    double score = 0.0;
    for (int i = 0; i < m; i++) {
      score += (test(thetas[i], testData, i) / m);
    }
    return score;
  }

  public double test(SimpleMatrix Theta, double[] testData, int idx) {
    if (idx == -1) {
      return test(testData);
    }

    double pred = predict(Theta, getX(testData, idx), idx);
    return Math.pow(pred - testData[idx], 2);
  }

  public double[] predictMany(double[][] input, int idx) {
    if (!trained) {
      train();
    }
    return predictMany(thetas[idx], input, idx);
  }

  public double[] predictMany(SimpleMatrix Theta, double[][] input, int idx) {
    int len = input.length;
    double[] ret = new double[len];

    for (int i = 0; i < len; i++) {
      ret[i] = predict(Theta, getX(input[i], idx), idx);
    }
    return ret;
  }

  private double predict(SimpleMatrix Theta, SimpleMatrix input, int idx) {
    return Theta.dot(input);
  }

  public void train() {
    for (int i = 0; i < m; i++) {
      // System.out.println("\nTraining to predict " + products[i] + "...");
      trainValue(i);
    }
    trained = true;
  }

  private void trainValue(int idx) {
    SimpleMatrix bestTheta = null;
    double bestScore = Double.MAX_VALUE;
    for (int i = 0; i < numFolds; i++) {
      int l = i * (n / numFolds);
      int u = (i + 1) * (n / numFolds);
      double[][] xvalFold = Arrays.copyOfRange(points, l, u);

      double[][] trainFold = null;
      if (l == 0) {
        trainFold = Arrays.copyOfRange(points, u, n);
      } else if (u == n) {
        trainFold = Arrays.copyOf(points, l);
      } else {
        trainFold = Arrays.copyOf(points, (numFolds - 1) * (n / numFolds));
        System.arraycopy(points, u, trainFold, l, n - u - 1);
      }

      SimpleMatrix Theta = trainFold(idx, trainFold);
      double score = test(Theta, xvalFold, idx) + test(Theta, trainFold, idx);

      if (score < bestScore) {
        bestScore = score;
        bestTheta = Theta;
      }
    }
    thetas[idx] = bestTheta;
  }

  private SimpleMatrix trainFold(int idx, double[][] points) {
    SimpleMatrix X = getX(points, idx, points.length);
    SimpleMatrix Y = getY(points, idx);

    SimpleMatrix mpinv = X.pseudoInverse();
    SimpleMatrix Theta = mpinv.mult(Y);
    return Theta;
  }

  private SimpleMatrix getX(double[][] points, int idx, int n) {
    double[][] X = new double[n][];
    for (int i = 0; i < n; i++) {
      double[] row = new double[((m * m) + (5 * m) - 4) / 2];
      row[0] = 1.0;

      int rowIdx = 0; // Constant feature (intercept)
      for (int j = 0; j < m; j++) {
        if (j == idx) {
          continue;
        }
        double val = points[i][j];

        row[++rowIdx] = val; // Linear feature
        row[++rowIdx] = val * val; // Square feature
        row[++rowIdx] = Math.sqrt(val); // Square-root feature
        row[++rowIdx] = Math.log1p(maxs[j] - val); // Logarithmic feature

        for (int k = j + 1; k < m; k++) { // Multivariable features
          if (k == idx) {
            continue;
          }
          row[++rowIdx] = val * points[i][k];
        }
      }

      X[i] = row;
    }
    return new SimpleMatrix(X);
  }

  // private SimpleMatrix getX(int idx) {
  // return getX(this.points, idx, n);
  // }

  private SimpleMatrix getX(double[] point, int idx) {
    double[][] newPoints = new double[1][];
    newPoints[0] = point;
    return getX(newPoints, idx, 1);
  }

  // private SimpleMatrix getX(double[][] points, int idx) {
  //   return getX(points, idx, points.length);
  // }

  private SimpleMatrix getY(double[][] points, int idx) {
    double[][] Y = new double[points.length][1];
    for (int i = 0; i < points.length; i++) {
      Y[i][0] = points[i][idx];
    }

    return new SimpleMatrix(Y);
  }

  // private SimpleMatrix getY(int idx) {
  // return getY(idx, points);
  // }

  @Override
  public String toString() {
    String s = String.format("Fitness: %12.5e", fitness);
    String format = "||  %-12s".repeat(products.length);
    String prods = String.format(format, (Object[]) products);
    s += "\n" + prods + "\n";
    return s;
  }

}
