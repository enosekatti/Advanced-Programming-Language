package dps;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe aggregation of processed results and optional file persistence.
 */
public class ResultsCollector {

    private static final Logger LOG = Logger.getLogger(ResultsCollector.class.getName());

    private final List<String> results = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Path outputPath;

    public ResultsCollector(Path outputPath) {
        this.outputPath = outputPath;
    }

    public void addResult(String line) {
        lock.lock();
        try {
            results.add(line);
        } finally {
            lock.unlock();
        }
    }

    public List<String> snapshot() {
        lock.lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(results));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Append one line to the output file with proper resource handling.
     */
    public void appendLineToFile(String line) {
        try (BufferedWriter writer = Files.newBufferedWriter(
                outputPath,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to append result to file: " + outputPath, e);
            throw new RuntimeException("I/O error writing results", e);
        }
    }

    public void flushAllToFile() {
        lock.lock();
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(
                    outputPath,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                for (String line : results) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to flush results to file: " + outputPath, e);
        } finally {
            lock.unlock();
        }
    }
}
