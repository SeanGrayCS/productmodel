package com.seangraycs;

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

      String file = "./dat/products" + i + ".ppctd";

      dataList = new ArrayList<>(Arrays.asList(new double[n]));
      if (i / (maxDims - 1) == 0) {
        genLinear();
      } else {
        genSphere();
      }

      prodArr = new String[n];
      System.arraycopy(PRODUCTS, 0, prodArr, 0, n);

      PPCTrainingData dataOut = new PPCTrainingData(prodArr, dataList);

      FileOutputStream fos = new FileOutputStream(file);
      ObjectOutputStream oos = new ObjectOutputStream(fos);

      oos.writeObject(dataOut);
      oos.close();
      fos.close();
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

  // CHANGE NEEDED: SHOULD GENERATE POINTS ON THE POSITIVE SECTION
  // (for all angles phi(0) to phi(n-1), 0 <= phi(i) <= pi/2)
  // OF AN N-DIMENSIONAL SPHERE WITH r = m
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

    // ArrayList<Integer> val0s = new ArrayList<>();
    // for (int i = 0; i < n; i++) {
    // for (int j = dataList.size() - 1; j >= 0; j--) {
    // int[] arr = dataList.remove(j);

    // int sum = m;
    // for (int k = 0; k < i; k++) {
    // sum -= arr[k];
    // }

    // if (i == n - 1) {
    // int[] res = Arrays.copyOf(arr, i + 1);
    // int idx = val0s.size() - (j % val0s.size()) - 1;
    // res[i] = val0s.get(idx);
    // dataList.add(res);
    // continue;
    // }

    // int inc = 0;
    // while (inc * (inc + 1) <= m) {
    // inc++;
    // }
    // inc--;
    // int inc2 = 0;
    // for (int k = 0; k <= sum; k += inc) {
    // int[] res = Arrays.copyOf(arr, i + 1);
    // res[i] = k;
    // dataList.add(res);
    // if (i == 0) {
    // val0s.add(res[0]);
    // }

    // if (k == 0 || inc == 1) {
    // continue;
    // }

    // inc -= inc2;
    // inc2 ^= 1;
    // }
    // }
    // }
  }
}
