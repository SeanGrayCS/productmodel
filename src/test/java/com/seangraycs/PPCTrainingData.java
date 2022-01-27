package com.seangraycs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PPCTrainingData implements Serializable {
  private int numPoints;
  private int numFeatures;

  private String[] products;
  private double[][] data;

  private double[] mins;
  private double[] maxs;
  private int[] minIdxs;
  private int[] maxIdxs;
  private Set<Integer> minMaxSet;

  public PPCTrainingData(String[] products, ArrayList<double[]> data) {
    this(products, data.toArray(new double[data.size()][]));
  }

  public PPCTrainingData(String[] products, double[][] data) {
    numPoints = data.length;
    numFeatures = products.length;

    this.products = products;
    this.data = data;

    initMinMax();
  }

  private void initMinMax() {
    mins = new double[numFeatures];
    maxs = new double[numFeatures];
    for (int i = 0; i < numFeatures; i++) {
      mins[i] = Double.MAX_VALUE;
      maxs[i] = Double.MIN_VALUE;
    }
    minIdxs = new int[numFeatures];
    maxIdxs = new int[numFeatures];

    for (int i = 0; i < numPoints; i++) {
      double[] point = data[i];

      for (int j = 0; j < numFeatures; j++) {
        if (point[j] < mins[j]) {
          mins[j] = point[j];
          minIdxs[j] = i;
        }
        if (point[j] > maxs[j]) {
          maxs[j] = point[j];
          maxIdxs[j] = i;
        }
      }
    }

    minMaxSet = new HashSet<>();
    for (int i = 0; i < numFeatures; i++) {
      minMaxSet.add(minIdxs[i]);
      minMaxSet.add(maxIdxs[i]);
    }
  }

  public int getNumPoints() {
    return numPoints;
  }

  public int getNumFeatures() {
    return numFeatures;
  }

  public String[] getProducts() {
    return products;
  }

  public double[][] getData() {
    return data;
  }

  public double[] getDataLine(int idx) {
    return data[idx];
  }

  public double getDataPoint(int pointIdx, int featureIdx) {
    return data[pointIdx][featureIdx];
  }

  public double[] getMins() {
    return mins;
  }

  public double[] getMaxs() {
    return maxs;
  }

  public int[] getMinIdxs() {
    return minIdxs;
  }

  public int[] getMaxIdxs() {
    return maxIdxs;
  }

  public double getMin(int idx) {
    return mins[idx];
  }

  public double getMax(int idx) {
    return maxs[idx];
  }

  public int getMinIdx(int idx) {
    return minIdxs[idx];
  }

  public int getMaxIdx(int idx) {
    return maxIdxs[idx];
  }

  public boolean minMaxContains(int idx) {
    return minMaxSet.contains(idx);
  }

  public int minMaxSize() {
    return minMaxSet.size();
  }
}
