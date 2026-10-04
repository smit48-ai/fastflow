package io.github.smit48ai.fastflow.internal;

final class TaskExecutionResult {

  private final String taskName;
  private final Object result;
  private final Throwable failure;

  private TaskExecutionResult(String taskName, Object result, Throwable failure) {
    this.taskName = taskName;
    this.result = result;
    this.failure = failure;
  }

  static TaskExecutionResult success(String taskName, Object result) {
    return new TaskExecutionResult(taskName, result, null);
  }

  static TaskExecutionResult failure(String taskName, Throwable failure) {
    return new TaskExecutionResult(taskName, null, failure);
  }

  String getTaskName() {
    return taskName;
  }

  Object getResult() {
    return result;
  }

  Throwable getFailure() {
    return failure;
  }

  boolean isSuccess() {
    return failure == null;
  }
}