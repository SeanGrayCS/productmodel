package com.seangraycs;

import java.io.IOException;

public class CSVToPPCTD {
  public static void main(String[] args) throws IOException, ClassNotFoundException {
    for (int i = 0; i < 6; i++) {
      String file = "./dat/products" + i + ".csv";
      PPCTrainingData.fromCSV(true, file, false);
    }
  }
}
