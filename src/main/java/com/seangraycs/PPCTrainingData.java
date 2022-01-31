package com.seangraycs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.io.FilenameUtils;

public class PPCTrainingData implements Serializable {
  private static final long serialVersionUID = 0L;
  
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

  public static PPCTrainingData fromCSV(String input) throws IOException, ClassNotFoundException {
    return fromCSV(false, input);
  }

  public static PPCTrainingData fromCSV(boolean exportData, String input) throws IOException, ClassNotFoundException {
    return fromCSV(exportData, input, false);
  }

  public static PPCTrainingData fromCSV(String input, boolean deleteInput) throws IOException, ClassNotFoundException {
    return fromCSV(false, input, deleteInput);
  }

  public static PPCTrainingData fromCSV(boolean exportData, String input, boolean deleteInput) throws IOException, ClassNotFoundException {
    return fromCSV(exportData, input, deleteInput, input);
  }

  public static PPCTrainingData fromCSV(String input, String output) throws IOException, ClassNotFoundException {
    return fromCSV(false, input, output);
  }

  public static PPCTrainingData fromCSV(boolean exportData, String input, String output) throws IOException, ClassNotFoundException {
    return fromCSV(exportData, input, false, output);
  }

  public static PPCTrainingData fromCSV(String input, boolean deleteInput, String output) throws IOException, ClassNotFoundException {
    return fromCSV(input, deleteInput, output, false);
  }

  public static PPCTrainingData fromCSV(String input, String output, boolean forceExtension) throws IOException, ClassNotFoundException {
    return fromCSV(input, false, output, forceExtension);
  }

  public static PPCTrainingData fromCSV(boolean exportData, String input, boolean deleteInput, String output) throws IOException, ClassNotFoundException {
    return fromCSV(exportData, input, deleteInput, output, false);
  }

  public static PPCTrainingData fromCSV(boolean exportData, String input, String output, boolean forceExtension) throws IOException, ClassNotFoundException {
    return fromCSV(exportData, input, false, output, forceExtension);
  }

  public static PPCTrainingData fromCSV(String input, boolean deleteInput, String output, boolean forceExtension) throws IOException, ClassNotFoundException {
    return fromCSV(false, input, deleteInput, output, forceExtension);
  }

  /**
   * 
   * CSV file must conform to the following format where n and m are integers,
   * each productnamej is a String, and each pointicoordj is a double:
   * 
   * n, m
   * productname1, productname2, ... productnamem
   * point1coord1, point1coord2, ... point1coordm
   * .
   * .
   * .
   * pointncoord1, pointncoord2, ... pointncoordm
   * 
   */
  public static PPCTrainingData fromCSV(boolean exportData, String input, boolean deleteInput, String output, boolean forceExtension) throws IOException, ClassNotFoundException {
    if (!forceExtension) {
      int extIdx = FilenameUtils.indexOfExtension(output);
      if (extIdx != -1) {
        output = output.substring(0, extIdx);
      }
      output += ".ppctd";
    }
    
    BufferedReader csv = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
    
    StringTokenizer st = new StringTokenizer(csv.readLine(), ", ");
    int n = Integer.parseInt(st.nextToken());
    int m = Integer.parseInt(st.nextToken());

    String[] products = new String[m];
    st = new StringTokenizer(csv.readLine(), ", ");
    int idx = 0;
    while (st.hasMoreTokens()) {
      products[idx++] = st.nextToken();
    }

    ArrayList<double[]> dataList = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      double[] point = new double[m];
      st = new StringTokenizer(csv.readLine(), ", ");
      idx = 0;
      while (st.hasMoreTokens()) {
        point[idx++] = Double.parseDouble(st.nextToken());
      }
      dataList.add(point);
    }

    PPCTrainingData data = new PPCTrainingData(products, dataList);
    csv.close();

    if (deleteInput) {
      (new File(input)).delete();
    }

    if (exportData) {
      FileOutputStream fos = new FileOutputStream(output);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      
      oos.writeObject(data);
      oos.close();
      fos.close();
    }

    return data;
  }
}
