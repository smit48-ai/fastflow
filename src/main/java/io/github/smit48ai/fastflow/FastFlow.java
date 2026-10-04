package io.github.smit48ai.fastflow;

import io.github.smit48ai.fastflow.internal.TaskCoordinator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

public final class FastFlow {

  private final List<Task<?>> tasks;

  private FastFlow() {
    this.tasks = new ArrayList<>();
  }

  public static FastFlow create() {
    return new FastFlow();
  }

  public <T> FastFlow addTask(String name, Callable<T> callable) {

    validateTask(name, callable);
    tasks.add(new Task<>(name, callable));

    return this;
  }

  public FastFlowResult execute() {

    if (tasks.isEmpty()) {
      throw new IllegalStateException("FastFlow must contain at least one task");
    }

    TaskCoordinator coordinator = new TaskCoordinator(List.copyOf(tasks));

    return coordinator.execute();
  }

  private void validateTask(String name, Callable<?> callable) {

    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Task name cannot be null or blank");
    }

    Objects.requireNonNull(callable, "Task callable cannot be null");

    boolean duplicateTask = tasks.stream().anyMatch(task -> task.getName().equals(name));

    if (duplicateTask) {
      throw new IllegalArgumentException("Task with name '" + name + "' already exists");
    }
  }
}