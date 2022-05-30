package data.scripts.net.connection;

import java.util.concurrent.TimeUnit;

public class Clock {
    private long initialTime;
    private final double timeU;
    private double deltaU;

    public Clock(int rate) {
        initialTime = System.nanoTime();
        timeU = 1000000000d / rate;
        deltaU = 1d;
    }

    public void runUntilTick() {
        long currentTime;

        while (deltaU < 1d) {
            currentTime = System.nanoTime();
            deltaU += (currentTime - initialTime) / timeU;
            initialTime = currentTime;
        }

        deltaU--;
    }

    public void sleepUntilTick() {
        try {
            Thread.sleep(TimeUnit.MILLISECONDS.convert((long) (timeU * 0.95d), TimeUnit.NANOSECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        runUntilTick();
    }
}
