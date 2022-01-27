package com.seangraycs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class CSVToPPCTD {
  public static void main(String[] args) throws IOException, ClassNotFoundException {
    for (int i = 0; i < 6; i++) {
      String file = "./dat/products" + i;
      convertFile(file + ".csv", file + ".ppctd", false);
    }
  }

  public static void convertFile(String input, String output, boolean deleteInput) throws IOException, ClassNotFoundException {
    BufferedReader csv = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
    
    int m = Integer.parseInt(csv.readLine());

    String[] products = new String[m];
    StringTokenizer st = new StringTokenizer(csv.readLine(), ", ");
    int idx = 0;
    while (st.hasMoreTokens()) {
      products[idx++] = st.nextToken();
    }

    int n = Integer.parseInt(csv.readLine());

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

    FileOutputStream fos = new FileOutputStream(output);
    ObjectOutputStream oos = new ObjectOutputStream(fos);

    oos.writeObject(data);
    oos.close();
    fos.close();

    if (deleteInput) {
      (new File(input)).delete();
    }
  }
}
