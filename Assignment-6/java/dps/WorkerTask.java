package dps;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Retrieves work from the shared queue, simulates CPU work, and records results.
 */
public class WorkerTask implements Callable<Void> {

    private static final Logger LOG = Logger.getLogger(WorkerTask.class.getName());

    private final String workerName;
    private final SharedTaskQueue queue;
    private final ResultsCollector results;

    public WorkerTask(String workerName, SharedTaskQueue queue, ResultsCollector results) {
        this.workerName = workerName;
        this.queue = queue;
        this.results = results;
    }

    @Override
    public Void call() {
        LOG.info(() -> "Thread started: " + workerName);
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Object item;
                try {
                    item = queue.getTask();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOG.log(Level.WARNING, "Worker interrupted while waiting on queue: " + workerName, e);
                    break;
                }

                if (item == null) {
                    LOG.info(() -> "Worker " + workerName + " exiting: producer finished and queue empty");
                    break;
                }

                if (item instanceof ShutdownSignal) {
                    LOG.info(() -> "Worker " + workerName + " received shutdown signal");
                    break;
                }

                if (!(item instanceof Task)) {
                    LOG.warning(() -> "Worker " + workerName + " skipping unknown item: " + item);
                    continue;
                }

                Task task = (Task) item;
                try {
                    processTask(task);
                    String line = workerName + " processed " + task + " -> OK";
                    results.addResult(line);
                    try {
                        results.appendLineToFile(line);
                    } catch (RuntimeException ioWrap) {
                        LOG.log(Level.SEVERE, "Worker " + workerName + " could not write file for task " + task.getId(),
                                ioWrap.getCause() != null ? ioWrap.getCause() : ioWrap);
                    }
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, "Worker " + workerName + " error processing " + task, ex);
                    String errLine = workerName + " processed Task{id=" + task.getId() + "} -> ERROR: " + ex.getMessage();
                    results.addResult(errLine);
                }
            }
        } catch (Exception unexpected) {
            LOG.log(Level.SEVERE, "Worker " + workerName + " encountered unexpected error", unexpected);
        } finally {
            LOG.info(() -> "Thread completed: " + workerName);
        }
        return null;
    }

    /**
     * Simulates computational work; may throw if interrupted during sleep.
     */
    void processTask(Task task) throws InterruptedException {
        int ms = 50 + new Random().nextInt(120);
        Thread.sleep(ms);
        if (task.getPayload() != null && task.getPayload().contains("FAIL")) {
            throw new IllegalStateException("simulated processing failure for payload");
        }
    }
}
