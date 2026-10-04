package io.github.smit48ai.fastflow.internal;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VirtualTaskExecutorTest {

    @Test
    void shouldExecuteTaskOnVirtualThread() throws Exception {
        try (ExecutorService executor = VirtualTaskExecutor.create()) {

            Future<Boolean> result =
                    executor.submit(() -> Thread.currentThread().isVirtual());

            assertTrue(result.get());
        }
    }

    @Test
    void shouldExecuteMultipleTasks() throws Exception {
        try (ExecutorService executor = VirtualTaskExecutor.create()) {

            Future<String> first = executor.submit(() -> "first");

            Future<String> second = executor.submit(() -> "second");

            assertEquals("first", first.get());
            assertEquals("second", second.get());
        }
    }
}
