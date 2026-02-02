import java.util.*;

public class sec3 {

    static class BigObject {
        byte[] payload = new byte[1024 * 1024]; // 1 MB
    }

    public static void main(String[] args) {
        List<BigObject> list = new ArrayList<>();

        // Allocate objects on heap
        for (int i = 0; i < 200; i++) {
            list.add(new BigObject());
        }

        // Remove references so objects become eligible for GC
        list.clear();

        System.out.println("Objects eligible for GC (collection timing is non-deterministic).");
    }
}
