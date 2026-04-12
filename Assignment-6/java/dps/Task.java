package dps;

/**
 * A unit of work for the data processing pipeline.
 */
public final class Task {

    private final int id;
    private final String payload;

    public Task(int id, String payload) {
        this.id = id;
        this.payload = payload;
    }

    public int getId() {
        return id;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "Task{id=" + id + ", payload='" + payload + "'}";
    }
}
