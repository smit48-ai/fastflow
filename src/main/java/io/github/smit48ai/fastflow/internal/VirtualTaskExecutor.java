package io.github.smit48ai.fastflow.internal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class VirtualTaskExecutor {

  private VirtualTaskExecutor() {
  }

  static ExecutorService create() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }
}