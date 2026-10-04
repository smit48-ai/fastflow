package io.github.smit48ai.fastflow;

import java.util.concurrent.Callable;

public final class Task<T> {

  private final String name;
  private final Callable<T> callable;

  public Task(String name, Callable<T> callable) {
    this.name = name;
    this.callable = callable;
  }

  public String getName() {
    return name;
  }

  public Callable<T> getCallable() {
    return callable;
  }
}