package io.github.smit48ai.fastflow.internal;

import io.github.smit48ai.fastflow.FastFlowResult;
import io.github.smit48ai.fastflow.Task;
import io.github.smit48ai.fastflow.exception.FastFlowExecutionException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class TaskCoordinatorTest {

    @Test
    void shouldExecuteAllTasksAndCollectResults() {
        Task<String> task1 = new Task<>("task-1", () -> "hello");

        Task<Integer> task2 = new Task<>("task-2", () -> 100);

        TaskCoordinator coordinator = new TaskCoordinator(List.of(task1, task2));

        FastFlowResult result = coordinator.execute();

        assertEquals("hello", result.get("task-1"));
        assertEquals(100, result.<Integer>get("task-2"));
        assertEquals(2, result.size());
    }

    @Test
    void shouldExecuteTasksConcurrently() {
        CountDownLatch bothStarted = new CountDownLatch(2);

        CountDownLatch release = new CountDownLatch(1);

        Task<String> task1 = new Task<>("task-1", () -> {
            bothStarted.countDown();
            release.await();
            return "first";
        });

        Task<String> task2 = new Task<>("task-2", () -> {
            bothStarted.countDown();
            release.await();
            return "second";
        });

        TaskCoordinator coordinator = new TaskCoordinator(List.of(task1, task2));

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {

            Future<FastFlowResult> execution = executor.submit(coordinator::execute);

            try {
                assertTrue(bothStarted.await(2, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                fail(e);
            } finally {
                release.countDown();
            }

            assertDoesNotThrow(() -> execution.get(2, TimeUnit.SECONDS));
        }
    }

    @Test
    void shouldDetectFailureInCompletionOrder() throws Exception {
        CountDownLatch slowTaskStarted = new CountDownLatch(1);

        CountDownLatch keepSlowTaskBlocked = new CountDownLatch(1);

        Task<String> slowTask = new Task<>("slow-task", () -> {
            slowTaskStarted.countDown();

            keepSlowTaskBlocked.await();

            return "slow";
        });

        Task<String> failingTask = new Task<>("failing-task", () -> {
            slowTaskStarted.await();

            throw new IllegalStateException("boom");
        });

        TaskCoordinator coordinator = new TaskCoordinator(List.of(slowTask, failingTask));

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {

            Future<FastFlowResult> execution = executor.submit(coordinator::execute);

            ExecutionException executionException =
                    assertThrows(ExecutionException.class, () -> execution.get(2, TimeUnit.SECONDS));

            FastFlowExecutionException exception =
                    assertInstanceOf(FastFlowExecutionException.class, executionException.getCause());

            assertEquals("failing-task", exception.getFailedTask());

            assertInstanceOf(IllegalStateException.class, exception.getCause());

        } finally {
            keepSlowTaskBlocked.countDown();
        }
    }

    @Test
    void shouldCancelRemainingTasksAfterFailure() throws Exception {

        CountDownLatch longTaskStarted = new CountDownLatch(1);

        CountDownLatch taskInterrupted = new CountDownLatch(1);

        AtomicBoolean interrupted = new AtomicBoolean(false);

        Task<String> longRunningTask = new Task<>("long-task", () -> {
            longTaskStarted.countDown();

            try {
                Thread.sleep(30_000);
            } catch (InterruptedException e) {
                interrupted.set(true);
                taskInterrupted.countDown();
                throw e;
            }

            return "finished";
        });

        Task<String> failingTask = new Task<>("failing-task", () -> {
            longTaskStarted.await();

            throw new RuntimeException("failure");
        });

        TaskCoordinator coordinator = new TaskCoordinator(List.of(longRunningTask, failingTask));

        assertThrows(FastFlowExecutionException.class, coordinator::execute);

        assertTrue(taskInterrupted.await(2, TimeUnit.SECONDS));

        assertTrue(interrupted.get());
    }
}
