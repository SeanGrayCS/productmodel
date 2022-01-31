package com.seangraycs;

import java.io.IOException;

public class ModelTest {
  public static void main(String[] args) throws IOException, ClassNotFoundException {

    ModelBuilder mg = new ModelBuilder(true);
    int numTests = 6;
    for (int i = 0; i < numTests; i++) {
      System.out.println("\nSet #" + (i + 1) + ":");
      String file = "./dat/products" + i + ".ppctd";
      System.out.println("\tGenerating model...");
      Model model = mg.createModel(file);
      System.out.println("\tModel generated:\n" + model);

      if ((i % 3) == 2) {
        continue;
      }

      String title = "Multivariate PPF " + i;
      System.out.println("\n\tMaking graph from " + title + "...");
      mg.graphModel(model, title);
      System.out.println("\nGraph made from " + title + ".");
    }

  }
}
