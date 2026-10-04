package io.github.smit48ai.fastflow;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void shouldCreateTask() throws Exception {
        Callable<String> callable = () -> "hello";

        Task<String> task = new Task<>("test-task", callable);

        assertEquals("test-task", task.getName());
        assertSame(callable, task.getCallable());
        assertEquals("hello", task.getCallable().call());
    }

    @Test
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () -> new Task<>(null, () -> "hello"));
    }

    @Test
    void shouldRejectNullCallable() {
        assertThrows(NullPointerException.class, () -> new Task<>("task", null));
    }
}
