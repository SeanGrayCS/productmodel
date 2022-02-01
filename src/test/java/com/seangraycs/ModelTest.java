package com.seangraycs;

import java.io.IOException;

public class ModelTest {
  public static void main(String[] args) throws IOException, ClassNotFoundException {

    ModelBuilder mb = new ModelBuilder(true);
    int numTests = 6;
    Model[] models = new Model[numTests];
    for (int i = 0; i < numTests; i++) {
      System.out.println("\nSet #" + (i + 1) + ":");
      String file = "./dat/products" + i + ".ppctd";
      System.out.println("\tGenerating model...");
      Model model = mb.createModel(file);
      System.out.println("\tModel generated:\n" + model);

      models[i] = model;
    }

    for (int i = 0; i < numTests/2; i++) {
      if (i == 2) {
        continue;
      }

      String title = "Multivariate PPF of " + i + " vs " + (i+(numTests/2));
      System.out.println("\n\tMaking graph from " + title + "...");
      mb.graphModels(new Model[] {models[i], models[i+(numTests/2)]}, title);
      System.out.println("\nGraph made from " + title + ".");
    }

  }
}
