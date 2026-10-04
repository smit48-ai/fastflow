package io.github.smit48ai.fastflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FastFlowTest {

    @Test
    void shouldCreateFastFlow() {
        FastFlow fastFlow = FastFlow.create();

        assertNotNull(fastFlow);
    }

    @Test
    void shouldExecuteTasksAndReturnResults() {
        FastFlowResult result = FastFlow.create()
                .addTask("task-1", () -> "hello")
                .addTask("task-2", () -> 100)
                .execute();

        assertEquals("hello", result.get("task-1"));
        assertEquals(100, result.<Integer>get("task-2"));
        assertEquals(2, result.size());
    }

    @Test
    void shouldRejectDuplicateTaskNames() {
        FastFlow fastFlow = FastFlow.create().addTask("task", () -> "first");

        assertThrows(IllegalArgumentException.class, () -> fastFlow.addTask("task", () -> "second"));
    }

    @Test
    void shouldRejectNullTaskName() {
        assertThrows(IllegalArgumentException.class, () -> FastFlow.create().addTask(null, () -> "result"));
    }

    @Test
    void shouldRejectBlankTaskName() {
        assertThrows(IllegalArgumentException.class, () -> FastFlow.create().addTask("   ", () -> "result"));
    }

    @Test
    void shouldRejectNullCallable() {
        assertThrows(IllegalArgumentException.class, () -> FastFlow.create().addTask("task", null));
    }

    @Test
    void shouldRejectExecutionWithoutTasks() {
        FastFlow fastFlow = FastFlow.create();

        assertThrows(IllegalStateException.class, fastFlow::execute);
    }
}
