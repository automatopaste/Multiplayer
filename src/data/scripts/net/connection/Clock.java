package data.scripts.net.connection;

public class Clock {
    private long initialTime;
    private final double timeU;
    private double deltaU;

    public Clock(int rate) {
        initialTime = System.nanoTime();
        timeU = 1000000000d / rate;
        deltaU = 1d;
    }

    public void sleepUntilTick() throws InterruptedException {
        long currentTime;

        while (deltaU < 1d) {
            Thread.sleep(0);

            currentTime = System.nanoTime();
            deltaU += (currentTime - initialTime) / timeU;
            initialTime = currentTime;
        }

        deltaU--;
    }
}
