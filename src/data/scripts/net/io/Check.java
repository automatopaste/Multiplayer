package data.scripts.net.io;

import java.util.Queue;

public class Check {

    private final Queue<?> queue;

    public Check(Queue<?> queue) {

        this.queue = queue;
    }

    public boolean check() {
        synchronized (queue) {
            return queue.isEmpty();
        }
    }
}
