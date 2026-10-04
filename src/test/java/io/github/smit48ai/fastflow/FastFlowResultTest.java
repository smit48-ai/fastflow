package io.github.smit48ai.fastflow;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FastFlowResultTest {

    @Test
    void shouldReturnResultByTaskName() {
        FastFlowResult result = new FastFlowResult(Map.of("task-1", "hello", "task-2", 100));

        assertEquals("hello", result.get("task-1"));
        assertEquals(100, result.<Integer>get("task-2"));
    }

    @Test
    void shouldCheckIfTaskExists() {
        FastFlowResult result = new FastFlowResult(Map.of("task-1", "hello"));

        assertTrue(result.contains("task-1"));
        assertFalse(result.contains("unknown"));
    }

    @Test
    void shouldReturnTaskNames() {
        FastFlowResult result = new FastFlowResult(Map.of("task-1", "hello", "task-2", 100));

        assertEquals(Set.of("task-1", "task-2"), result.taskNames());
    }

    @Test
    void shouldReturnSize() {
        FastFlowResult result = new FastFlowResult(Map.of("task-1", "hello", "task-2", 100));

        assertEquals(2, result.size());
    }

    @Test
    void shouldCreateImmutableCopyOfResults() {
        Map<String, Object> original = new HashMap<>();
        original.put("task-1", "hello");

        FastFlowResult result = new FastFlowResult(original);

        original.put("task-2", "new-value");

        assertEquals(1, result.size());
        assertFalse(result.contains("task-2"));
    }
}
