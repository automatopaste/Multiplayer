package data.scripts.net.io;

public class Clock {
    private long prev;
    private final double timeU;
    private double deltaU;

    public Clock(int rate) {
        prev = System.nanoTime();
        timeU = 1000000000d / rate;
        deltaU = 1d;
    }

    public void sleepUntilTick() throws InterruptedException {
        long curr;

        while (deltaU < 1d) {
            Thread.sleep(0);

            curr = System.nanoTime();
            deltaU += (curr - prev) / timeU;
            prev = curr;
        }

        deltaU--;
    }

    public boolean mark() {
        long curr = System.nanoTime();
        deltaU += (curr - prev) / timeU;
        prev = curr;

        if (deltaU > 1d) {
            deltaU -= 1d;
            return true;
        }
        return false;
    }
}
