package io.github.smit48ai.fastflow.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class FastFlowExecutionExceptionTest {

    @Test
    void shouldStoreFailedTaskAndCause() {
        RuntimeException cause = new RuntimeException("database failed");

        FastFlowExecutionException exception = new FastFlowExecutionException("database-task", cause);

        assertEquals("database-task", exception.getFailedTask());

        assertSame(cause, exception.getCause());
    }

    @Test
    void shouldSupportCustomMessage() {
        RuntimeException cause = new RuntimeException("boom");

        FastFlowExecutionException exception =
                new FastFlowExecutionException("FastFlow execution failed", "task-1", cause);

        assertEquals("FastFlow execution failed", exception.getMessage());

        assertEquals("task-1", exception.getFailedTask());

        assertSame(cause, exception.getCause());
    }
}
