package dps;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Entry point: enqueues tasks, runs a fixed pool of workers, and shuts down cleanly.
 */
public final class DataProcessingApp {

    private static final Logger LOG = Logger.getLogger(DataProcessingApp.class.getName());

    public static void main(String[] args) {
        configureLogging();

        int workerCount = args.length > 0 ? parsePositive(args[0], 4) : 4;
        int taskCount = args.length > 1 ? parsePositive(args[1], 20) : 20;
        Path out = Paths.get(args.length > 2 ? args[2] : "output/java_results.txt");

        try {
            Path parent = out.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (java.io.IOException e) {
            LOG.log(Level.SEVERE, "Could not create output directory for " + out, e);
            return;
        }

        LOG.info(() -> "Starting Data Processing System (Java): workers=" + workerCount + ", tasks=" + taskCount);

        SharedTaskQueue queue = new SharedTaskQueue();
        ResultsCollector results = new ResultsCollector(out);

        ExecutorService executor = Executors.newFixedThreadPool(workerCount);
        List<Future<Void>> futures = new ArrayList<>();

        for (int w = 0; w < workerCount; w++) {
            String name = "Worker-" + w;
            futures.add(executor.submit(new WorkerTask(name, queue, results)));
        }

        try {
            for (int i = 0; i < taskCount; i++) {
                String payload = (i % 7 == 0) ? "chunk-FAIL-" + i : "chunk-" + i;
                queue.addTask(new Task(i, payload));
            }
            for (int w = 0; w < workerCount; w++) {
                queue.addTask(ShutdownSignal.INSTANCE);
            }
            queue.markProducerFinished();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Producer failed while enqueueing tasks", e);
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(120, TimeUnit.SECONDS)) {
                LOG.warning("Executor did not terminate in time; forcing shutdown");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.log(Level.WARNING, "Main interrupted while awaiting worker termination", e);
            executor.shutdownNow();
        }

        for (int i = 0; i < futures.size(); i++) {
            try {
                futures.get(i).get();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Worker future completed with exception", e);
            }
        }

        results.flushAllToFile();
        LOG.info(() -> "All workers finished. Results file: " + out.toAbsolutePath());
        LOG.info(() -> "In-memory result count: " + results.snapshot().size());
    }

    private static int parsePositive(String s, int defaultVal) {
        try {
            int v = Integer.parseInt(s.trim());
            return v > 0 ? v : defaultVal;
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private static void configureLogging() {
        Logger root = Logger.getLogger("");
        root.setLevel(Level.INFO);
        for (var h : root.getHandlers()) {
            root.removeHandler(h);
        }
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.INFO);
        ch.setFormatter(new SimpleFormatter());
        root.addHandler(ch);
    }
}
