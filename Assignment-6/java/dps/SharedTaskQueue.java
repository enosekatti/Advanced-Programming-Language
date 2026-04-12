package dps;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
/**
 * Thread-safe task queue with {@link #addTask} and {@link #getTask} using a {@link ReentrantLock}
 * and conditions to avoid busy-waiting and race conditions.
 */
public class SharedTaskQueue {

    private final Deque<Object> queue = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private volatile boolean producerFinished;

    /**
     * Enqueue a normal task or a {@link ShutdownSignal} sentinel.
     */
    public void addTask(Object item) {
        if (item == null) {
            throw new IllegalArgumentException("task cannot be null");
        }
        lock.lock();
        try {
            queue.addLast(item);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Blocks until a task or shutdown signal is available, or until the producer is done and the
     * queue is empty (returns null).
     */
    public Object getTask() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                if (producerFinished) {
                    return null;
                }
                notEmpty.await();
            }
            return queue.removeFirst();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Called when no more tasks will be added (except shutdown signals already queued).
     */
    public void markProducerFinished() {
        lock.lock();
        try {
            producerFinished = true;
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Peek-style check under lock (for diagnostics / tests).
     */
    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }
}
