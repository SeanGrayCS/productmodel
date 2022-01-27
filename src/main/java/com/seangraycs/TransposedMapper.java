package com.seangraycs;

import java.util.HashMap;

import org.jzy3d.plot3d.builder.Mapper;

public class TransposedMapper extends Mapper {
  public enum MappedValue {
    X(0), Y(1), Z(2);

    private int idx;

    private MappedValue(final int idx) {
      this.idx = idx;
    }

    public int getIdx() {
      return idx;
    }

  };

  private Model model;
  private MappedValue mappedValue;
  private boolean zExists;

  private double[] predictions;
  private boolean hasPredicted;
  private HashMap<Integer, Integer> predictionMap;

  public TransposedMapper(Model model, MappedValue mappedValue, boolean zExists) {
    this.model = model;
    this.mappedValue = mappedValue;
    this.zExists = zExists;
    this.predictions = null;
    this.hasPredicted = false;
    this.predictionMap = null;
  }

  public TransposedMapper(Model model, MappedValue mappedValue, boolean zExists, double[] aVals, double[] bVals) {
    this(model, mappedValue, zExists);
    makePredictions(aVals, bVals);
  }

  /**
   * Returns predictions.
   */
  public double[] getPredictions() {
    if (!hasPredicted) {
      return null;
    }
    return predictions;
  }

  public double[] makePredictions(double[] vals) {
    return makePredictions(vals, new double[vals.length]);
  }

  /**
   * Stores and returns predictions based on parameter data.
   */
  public double[] makePredictions(double[] aVals, double[] bVals) {
    int numPredictions = aVals.length;
    if (bVals.length != numPredictions) {
      return null;
    }

    predictionMap = new HashMap<>(numPredictions + 1, 1.0f);

    for (int i = 0; i < numPredictions; i++) {
      double aVal = aVals[i];
      double bVal = bVals[i];
      predictionMap.put(genHash(aVal, bVal), i);
    }

    hasPredicted = true;
    predictions = getPredictions(aVals, bVals);
    return predictions;
  }

  public double[] getPredictions(double[] vals) {
    return getPredictions(vals, new double[vals.length]);
  }

  /**
   * Returns predictions based on parameter data.
   */
  public double[] getPredictions(double[] aVals, double[] bVals) {
    int numPredictions = aVals.length;
    if (bVals.length != numPredictions) {
      return null;
    }

    double[][] input = new double[numPredictions][zExists ? 3 : 2];
    int idx = mappedValue.getIdx();

    int aIdx = idx ^ 1, bIdx = 2;
    if (zExists) {
      aIdx = (idx >> (idx - 1)) ^ 1;
      bIdx = ((idx >> 1) + 1) ^ 3;
    }

    for (int i = 0; i < numPredictions; i++) {
      double aVal = aVals[i];
      double bVal = bVals[i];

      input[i][idx] = 0;
      input[i][aIdx] = aVal;
      if (zExists) {
        input[i][bIdx] = bVal;
      }
    }

    return model.predictMany(input, idx);
  }

  private int genHash(double a, double b) {
    int hA = Double.hashCode(a);
    if (zExists) {
      return hA;
    }
    int hB = Double.hashCode(b);

    int h = 17;
    h *= 31;
    h += hA;
    h *= 31;
    h += hB;
    return h;
  }

  @Override
  public double f(double a, double b) {
    return predictions[predictionMap.get(genHash(a, b))];
  }
}
