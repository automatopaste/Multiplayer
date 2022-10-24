package data.scripts.net.io.udp.server;

import cmu.plugins.debug.DebugGraphContainer;
import data.scripts.net.io.PacketContainer;
import data.scripts.net.io.ServerConnectionManager;
import data.scripts.net.io.udp.DatagramUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.lazywizard.console.Console;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class DatagramServer implements Runnable {
    public static final int MAX_QUEUE_SIZE = 128;

    private final int port;
    private final ServerConnectionManager connectionManager;
    private final Queue<PacketContainer> messageQueue;
    private final Queue<PacketContainer> externalQueue;

    private EventLoopGroup workerLoopGroup;
    private Channel channel;

    private boolean running;

    private final DebugGraphContainer dataGraph;
    private final DebugGraphContainer dataGraphCompressed;
    private final DebugGraphContainer dataGraphRatio;

    public DatagramServer(int port, ServerConnectionManager connectionManager) {
        this.port = port;
        this.connectionManager = connectionManager;

        messageQueue = new LinkedList<>();
        externalQueue = new LinkedList<>();

        dataGraph = new DebugGraphContainer("Bits Out", ServerConnectionManager.TICK_RATE, 50f);
        dataGraphCompressed = new DebugGraphContainer("Compressed Bits Out", ServerConnectionManager.TICK_RATE, 50f);
        dataGraphRatio = new DebugGraphContainer("Compression Ratio", ServerConnectionManager.TICK_RATE, 50f);

        running = false;
    }

    @Override
    public void run() {
        running = true;

        Console.showMessage("Running UDP server on port " + port + " at " + ServerConnectionManager.TICK_RATE + "Hz");

        ChannelFuture channelFuture = start();

        ChannelFuture closeFuture = channelFuture.channel().closeFuture();

        try {
            float counter = 0f;
            long nano = System.nanoTime();

            while (connectionManager.isActive()) {
                int size = 0;
                int sizeCompressed = 0;

                synchronized (externalQueue) {
                    messageQueue.addAll(externalQueue);
                }

                while (!messageQueue.isEmpty()) {
                    PacketContainer message = messageQueue.poll();

                    if (message == null || message.isEmpty()) continue;

                    DatagramUtils.SizeData sizeData = DatagramUtils.write(channel, message);
                    if (sizeData == null) {
                        return;
                    } else {
                        size += sizeData.size;
                        sizeCompressed += sizeData.sizeCompressed;
                    }
                }

                long diff = System.nanoTime() - nano;
                nano = System.nanoTime();
                counter += TimeUnit.SECONDS.convert(diff, TimeUnit.NANOSECONDS);

//                if (counter > 1f / ServerConnectionManager.TICK_RATE) {
//                    dataGraph.increment(size);
//                    CMUtils.getGuiDebug().putContainer(DatagramServer.class, "dataGraph", dataGraph);
//                    dataGraphCompressed.increment(sizeCompressed);
//                    CMUtils.getGuiDebug().putContainer(DatagramServer.class, "dataGraphCompressed", dataGraphCompressed);
//                    dataGraphRatio.increment(100f * ((float) sizeCompressed / size));
//                    CMUtils.getGuiDebug().putContainer(DatagramServer.class, "dataGraphRatio", dataGraphRatio);
//
//                    counter -= 1f / ServerConnectionManager.TICK_RATE;
//                }

                try {
                    synchronized (externalQueue) {
                        externalQueue.wait();
                    }
                } catch (InterruptedException i) {
                    System.err.println("datagram server wait interrupted");
                }
            }

            System.err.println("CLOSING THREAD SYNC");
            closeFuture.sync();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            System.err.println("CLOSING THREAD");
        }
    }

    private ChannelFuture start() {
        workerLoopGroup = new NioEventLoopGroup();

        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerLoopGroup)
                .channel(NioDatagramChannel.class)
                .handler(new DatagramChannelInitializer(this, connectionManager)
                );

        ChannelFuture future = bootstrap.bind(port).syncUninterruptibly();
        channel = future.channel();
//        channelGroup.add(channel);

        return future;
    }

    public void addMessages(List<PacketContainer> messages) {
        synchronized (externalQueue) {
            externalQueue.addAll(messages);

            while (externalQueue.size() > MAX_QUEUE_SIZE) {
                externalQueue.remove();
            }

            externalQueue.notifyAll();
        }
    }

    public void stop() {
        if (channel != null) channel.close();
        if (workerLoopGroup != null) workerLoopGroup.shutdownGracefully();
        running = false;

        dataGraph.expire();
        dataGraphCompressed.expire();
        dataGraphRatio.expire();
    }

    public boolean isRunning() {
        return running;
    }
}
