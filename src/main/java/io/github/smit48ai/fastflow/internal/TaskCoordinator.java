package io.github.smit48ai.fastflow.internal;

import io.github.smit48ai.fastflow.FastFlowResult;
import io.github.smit48ai.fastflow.Task;
import io.github.smit48ai.fastflow.exception.FastFlowExecutionException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class TaskCoordinator {

  private final List<Task<?>> tasks;

  public TaskCoordinator(List<Task<?>> tasks) {
    this.tasks = tasks;
  }

  public FastFlowResult execute() {

    ExecutorService executor = VirtualTaskExecutor.create();

    CompletionService<TaskExecutionResult> completionService = new ExecutorCompletionService<>(
        executor);

    List<Future<TaskExecutionResult>> futures = new ArrayList<>();

    try {

      submitTasks(completionService, futures);

      return collectResults(completionService, futures);

    } catch (InterruptedException e) {

      cancelRemaining(futures);

      Thread.currentThread().interrupt();

      throw new FastFlowExecutionException("FastFlow execution was interrupted", null, e);

    } finally {

      executor.shutdownNow();
    }
  }

  private void submitTasks(CompletionService<TaskExecutionResult> completionService,
      List<Future<TaskExecutionResult>> futures) {

    for (Task<?> task : tasks) {

      CompletionServiceTask executionTask = new CompletionServiceTask(task);

      Future<TaskExecutionResult> future = completionService.submit(executionTask);

      futures.add(future);
    }
  }

  private FastFlowResult collectResults(CompletionService<TaskExecutionResult> completionService,
      List<Future<TaskExecutionResult>> futures) throws InterruptedException {

    Map<String, Object> results = new LinkedHashMap<>();

    for (int i = 0; i < tasks.size(); i++) {

      Future<TaskExecutionResult> completedFuture = completionService.take();

      TaskExecutionResult executionResult;

      try {

        executionResult = completedFuture.get();

      } catch (Exception e) {

        cancelRemaining(futures);

        throw new FastFlowExecutionException("Unexpected FastFlow execution failure", null, e);
      }

      if (!executionResult.isSuccess()) {

        cancelRemaining(futures);

        throw new FastFlowExecutionException(executionResult.getTaskName(),
            executionResult.getFailure());
      }

      results.put(executionResult.getTaskName(), executionResult.getResult());
    }

    return new FastFlowResult(results);
  }

  private void cancelRemaining(List<? extends Future<?>> futures) {

    for (Future<?> future : futures) {

      if (!future.isDone()) {
        future.cancel(true);
      }
    }
  }
}