package io.github.smit48ai.fastflow.internal;

import io.github.smit48ai.fastflow.Task;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class CompletionServiceTaskTest {

    @Test
    void shouldReturnSuccessfulTaskExecutionResult() throws Exception {
        Task<String> task = new Task<>("task-1", () -> "hello");

        CompletionServiceTask completionTask = new CompletionServiceTask(task);

        TaskExecutionResult result = completionTask.call();

        assertTrue(result.isSuccess());
        assertEquals("task-1", result.getTaskName());
        assertEquals("hello", result.getResult());
        assertNull(result.getFailure());
    }

    @Test
    void shouldCaptureTaskFailure() throws Exception {
        RuntimeException expected = new RuntimeException("task failed");

        Task<String> task = new Task<>("failing-task", () -> {
            throw expected;
        });

        CompletionServiceTask completionTask = new CompletionServiceTask(task);

        TaskExecutionResult result = completionTask.call();

        assertFalse(result.isSuccess());
        assertEquals("failing-task", result.getTaskName());

        assertSame(expected, result.getFailure());
    }

    @Test
    void shouldExecuteCallableOnlyOnce() throws Exception {
        AtomicInteger executionCount = new AtomicInteger();

        Task<Integer> task = new Task<>("task", executionCount::incrementAndGet);

        CompletionServiceTask completionTask = new CompletionServiceTask(task);

        TaskExecutionResult result = completionTask.call();

        assertEquals(1, result.getResult());
        assertEquals(1, executionCount.get());
    }
}
