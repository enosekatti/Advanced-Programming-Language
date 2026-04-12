package dps;

/**
 * Sentinel placed on the queue so workers can exit without polling an empty queue indefinitely.
 */
public final class ShutdownSignal {

    public static final ShutdownSignal INSTANCE = new ShutdownSignal();

    private ShutdownSignal() {
    }
}
