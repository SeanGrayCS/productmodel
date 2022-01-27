package com.seangraycs;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class NotifyThread implements Runnable {
  private final Set<RunnableListener> listeners = new CopyOnWriteArraySet<RunnableListener>();

  public final void addListener(final RunnableListener listener) {
    listeners.add(listener);
  }

  public final void removeListener(final RunnableListener listener) {
    listeners.remove(listener);
  }

  private final void notifyListeners() {
    for (RunnableListener listener : listeners) {
      listener.notifyDone(this);
    }
  }

  @Override
  public final void run() {
    try {
      doRun();
    } finally {
      notifyListeners();
    }
  }

  public abstract void doRun();
}
