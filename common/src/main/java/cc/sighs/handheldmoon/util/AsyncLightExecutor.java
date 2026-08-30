package cc.sighs.handheldmoon.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/** Small daemon pool for pure dynamic-light calculations. */
public final class AsyncLightExecutor {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(
            2,
            new DaemonThreadFactory()
    );

    private AsyncLightExecutor() {
    }

    public static ExecutorService executor() {
        return EXECUTOR;
    }

    private static final class DaemonThreadFactory implements ThreadFactory {
        private final AtomicInteger nextId = new AtomicInteger();

        @Override
        public Thread newThread(Runnable task) {
            Thread thread = new Thread(task, "handheldmoon-light-" + nextId.incrementAndGet());
            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            return thread;
        }
    }
}
