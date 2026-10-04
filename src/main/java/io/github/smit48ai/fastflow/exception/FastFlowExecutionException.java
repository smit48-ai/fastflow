package io.github.smit48ai.fastflow.exception;

public class FastFlowExecutionException extends RuntimeException {

  private final String failedTask;

  public FastFlowExecutionException(String failedTask, Throwable cause) {
    super("FastFlow task failed: " + failedTask, cause);

    this.failedTask = failedTask;
  }

  public FastFlowExecutionException(String message, String failedTask, Throwable cause) {
    super(message, cause);
    this.failedTask = failedTask;
  }

  public String getFailedTask() {
    return failedTask;
  }
}