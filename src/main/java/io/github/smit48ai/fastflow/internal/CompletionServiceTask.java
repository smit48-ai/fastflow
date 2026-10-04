package io.github.smit48ai.fastflow.internal;

import io.github.smit48ai.fastflow.Task;

import java.util.concurrent.Callable;

final class CompletionServiceTask implements Callable<TaskExecutionResult> {

    private final Task<?> task;

    CompletionServiceTask(Task<?> task) {
        this.task = task;
    }

    @Override
    public TaskExecutionResult call() {

        try {

            Object result = task.getCallable().call();

            return TaskExecutionResult.success(task.getName(), result);

        } catch (Exception e) {

            return TaskExecutionResult.failure(task.getName(), e);
        }
    }
}