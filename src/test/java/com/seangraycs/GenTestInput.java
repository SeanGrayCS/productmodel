package com.seangraycs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class GenTestInput {
  private static final String[] PRODUCTS = new String[] { "Calzones", "Pizzas", "Cheese", "Milk", "Bread" };

  private static int n;
  private static int m;
  private static int numAngles;

  private static String[] prodArr;
  private static ArrayList<double[]> dataList;

  public static void main(String[] args) throws IOException {

    int maxDims = 4;
    for (int i = 0; i < (maxDims - 1) * 2; i++) {
      n = (i % (maxDims - 1)) + 2;
      m = 100;
      numAngles = (i == (maxDims - 1)) ? 181 : 46;

      String file = "./dat/products" + i;// + ".ppctd";

      dataList = new ArrayList<>(Arrays.asList(new double[n]));
      if (i / (maxDims - 1) == 0) {
        genLinear();
      } else {
        genSphere();
      }

      prodArr = new String[n];
      System.arraycopy(PRODUCTS, 0, prodArr, 0, n);

      PPCTrainingData dataOut = new PPCTrainingData(prodArr, dataList);

      File f = new File(file + ".ppctd");
      if (f.exists()) {
        f.delete();
      }

      FileOutputStream fos = new FileOutputStream(file + ".ppctd");
      ObjectOutputStream oos = new ObjectOutputStream(fos);

      oos.writeObject(dataOut);
      oos.close();
      fos.close();

      // Also write test data to CSV for demo of CSV to PPCTD conversion
      java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(file + ".csv"));
      writer.write(dataList.size() + ", " + prodArr.length + "\n");

      for (int j = 0; j < prodArr.length; j++) {
        writer.write(prodArr[j]);
        if (j == prodArr.length - 1) {
          writer.write("\n");
          break;
        }
        writer.write(", ");
      }

      for (int j = 0; j < dataList.size(); j++) {
        double[] point = dataList.get(j);
        for (int k = 0; k < point.length; k++) {
          writer.write("" + point[k]);
          if (k != point.length - 1) {
            writer.write(", ");
          }
        }
        writer.write("\n");
      }
      writer.close();
    }

  }

  private static void genLinear() {
    for (int i = 0; i < n; i++) {
      for (int j = dataList.size() - 1; j >= 0; j--) {
        double[] arr = dataList.remove(j);

        double sum = m;
        for (int k = 0; k < i; k++) {
          sum -= arr[k];
        }

        if (i == n - 1) {
          double[] res = Arrays.copyOf(arr, n);
          res[i] = sum;
          dataList.add(res);
          continue;
        }

        for (int k = 0; k <= sum; k++) {
          double[] res = Arrays.copyOf(arr, n);
          res[i] = k;
          dataList.add(res);
        }
      }
    }
  }

  // Generate points on the positive section
  // (for all angles phi(0) to phi(n-1), 0 <= phi(i) <= pi/2)
  // of an n-dimensional sphere with radius m
  // (output points should be in cartesian coordinates)
  private static void genSphere() {
    double[] phi = new double[numAngles]; // In radians
    for (int i = 0; i < numAngles; i++) {
      phi[i] = i * (Math.PI / (2 * (numAngles - 1)));
    }

    ArrayList<Double> pastList = new ArrayList<>();
    pastList.add((double) m);

    for (int angle = 0; angle < n - 1; angle++) {
      ArrayList<Double> newList = new ArrayList<>();

      int numPasts = pastList.size();
      for (int idx = 0; idx < numPasts; idx++) {
        double past = pastList.get(idx);

        double[] coords = dataList.remove(0);
        for (int i = 0; i < numAngles; i++) {
          double curAngle = phi[i];

          double cartesian = 1.0, nextCart = 0.0;
          if (i == numAngles - 1) {
            cartesian = 0.0;
            nextCart = 1.0;
          } else if (i != 0) {
            cartesian = Math.cos(curAngle);
            nextCart = Math.sin(curAngle);
          }
          cartesian *= past;
          nextCart *= past;

          double[] newCoords = Arrays.copyOf(coords, n);

          if (angle < n - 2) {
            newList.add(nextCart);
            newCoords[n - 1 - angle] = cartesian;
          } else {
            newCoords[0] = cartesian;
            newCoords[1] = nextCart;
          }
          dataList.add(newCoords);
        }
      }
      pastList = newList;
    }
  }
}
