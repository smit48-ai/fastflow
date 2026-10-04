package io.github.smit48ai.fastflow.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskExecutionResultTest {

    @Test
    void shouldCreateSuccessfulResult() {
        TaskExecutionResult result = TaskExecutionResult.success("task-1", "hello");

        assertTrue(result.isSuccess());
        assertEquals("task-1", result.getTaskName());
        assertEquals("hello", result.getResult());
        assertNull(result.getFailure());
    }

    @Test
    void shouldCreateFailedResult() {
        RuntimeException failure = new RuntimeException("boom");

        TaskExecutionResult result = TaskExecutionResult.failure("task-1", failure);

        assertFalse(result.isSuccess());
        assertEquals("task-1", result.getTaskName());
        assertNull(result.getResult());
        assertSame(failure, result.getFailure());
    }
}
