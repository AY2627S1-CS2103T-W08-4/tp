package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

/**
 * Runs GUI tests on the JavaFX application thread. Linux test runs require a display such as Xvfb.
 */
public class FxTestUtil {

    private FxTestUtil() {}

    /**
     * Starts JavaFX and keeps it running between tests that close all their windows.
     */
    public static void initializeToolkit() throws InterruptedException {
        CountDownLatch ready = new CountDownLatch(1);
        Runnable initialize = () -> {
            Platform.setImplicitExit(false);
            ready.countDown();
        };
        try {
            Platform.startup(initialize);
        } catch (IllegalStateException e) {
            Platform.runLater(initialize);
        }
        assertTrue(ready.await(10, TimeUnit.SECONDS), "JavaFX did not start within ten seconds");
    }

    /**
     * Runs an action on JavaFX and propagates failures to the test thread with a bounded wait.
     */
    public static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
