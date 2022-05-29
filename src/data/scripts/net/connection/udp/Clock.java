package data.scripts.net.connection.udp;

public class Clock {
    private long initialTime;
    private final double timeU;
    private double deltaU;

    public Clock(int rate) {
        initialTime = System.nanoTime();
        timeU = 1000000000d / rate;
        deltaU = 1d;
    }
    public void runUntilUpdate() {
        long currentTime;

        while (deltaU < 1d) {
            currentTime = System.nanoTime();
            deltaU += (currentTime - initialTime) / timeU;
            initialTime = currentTime;
        }

        deltaU--;
    }
}
