package com.seangraycs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

public class ModelBuilder implements RunnableListener {
  private boolean exportModels;

  private boolean asyncGraphing;
  private boolean ready;
  private Queue<Thread> threads;

  public ModelBuilder() {
    this(false);
  }

  public ModelBuilder(boolean exportModels) {
    this(exportModels, false);
  }

  public ModelBuilder(boolean exportModels, boolean asyncGraphing) {
    this.exportModels = exportModels;

    this.asyncGraphing = asyncGraphing;
    ready = true;
    threads = new LinkedList<>();
  }

  public Model createModel(String fileName) throws IOException, ClassNotFoundException {
    int extIdx = FilenameUtils.indexOfExtension(fileName);
    if (extIdx != -1) {
      return createModel(fileName, fileName.substring(0, extIdx) + ".ppcm");
    }
    return createModel(fileName, fileName + ".ppcm");
  }

  public Model createModel(String input, String output) throws IOException, ClassNotFoundException {
    FileInputStream fis = new FileInputStream(input);
    ObjectInputStream ois = new ObjectInputStream(fis);

    String ext = FilenameUtils.getExtension(input);

    if (ext.equals("ppcm")) {
      Model model = (Model) ois.readObject();
      ois.close();
      fis.close();
      return model;
    }
    if (!ext.equals("ppctd")) {
      // Incorrect file extionsion
      ois.close();
      fis.close();
      throw new IOException("Incorrect file extension:\n\tRequires PPC Training Data (.ppctd) or PPC Model (.ppcm), got " + ext);
    }

    PPCTrainingData dataIn = (PPCTrainingData) ois.readObject();
    ois.close();
    fis.close();

    // Given m possible products
    int m = dataIn.getNumFeatures();

    // Given n efficient points
    int n = dataIn.getNumPoints();

    // Given n x m matrix of production possibilities
    // Split matrix for training/cross-validating and testing
    int numFolds = 4;
    int trainLen = (n / 5) * numFolds;
    double[][] trainSet = new double[trainLen][m];
    double[][] testSet = new double[n - trainLen][m];

    Set<Integer> idxSet = new HashSet<>();
    int minMaxSize = dataIn.minMaxSize();
    while (idxSet.size() < (n - trainLen)) {
      int idx = (int) (Math.random() * n);
      if ((idxSet.size() >= (n - minMaxSize) || !dataIn.minMaxContains(idx)) && idxSet.add(idx)) {
        testSet[idxSet.size() - 1] = dataIn.getDataLine(idx);
      }
    }

    int idx = 0;
    for (int i = 0; i < n; i++) {
      if (!idxSet.contains(i)) {
        trainSet[idx++] = dataIn.getDataLine(i);
      }
    }

    Model model = new Model(dataIn.getProducts(), trainSet, numFolds);
    model.train();
    // System.out.println("\nTesting Set:");
    model.setFitness(model.test(testSet));

    if (exportModels) {
      File file = new File(output);
      if (file.exists()) {
        file.delete();
      }

      FileOutputStream fos = new FileOutputStream(output);
      ObjectOutputStream oos = new ObjectOutputStream(fos);

      oos.writeObject(model);
      oos.close();
      fos.close();
    }

    return model;
  }

  public void graphModel(Model model, String title) {
    Graph3D graph = new Graph3D(model, title);

    if (asyncGraphing) {
      graphAsync(graph, title);
    } else {
      graph.makeChart();
    }
  }

  private void graphAsync(Graph3D graph, String title) {
    Thread thread = new Thread(new NotifyThread() {
      public void doRun() {
        // System.out.println("Making chart from " + title + "...");
        graph.makeChart();
      }
    }, title);

    if (!runThread(thread)) {
      threads.offer(thread);
    }
  }

  @Override
  public void notifyDone(Runnable runnable) {
    ready = true;
    if (!threads.isEmpty()) {
      runThread(threads.poll());
    }
  }

  private boolean runThread(Thread thread) {
    if (ready) {
      thread.start();
      ready = false;
      return true;
    }
    return false;
  }
}
