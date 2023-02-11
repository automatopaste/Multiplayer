package data.scripts.net.io.udp.server;

import cmu.CMUtils;
import cmu.plugins.debug.DebugGraphContainer;
import com.fs.starfarer.api.Global;
import data.scripts.net.io.Clock;
import data.scripts.net.io.MessageContainer;
import data.scripts.net.io.ServerConnectionManager;
import data.scripts.net.io.udp.DatagramUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.lazywizard.console.Console;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DatagramServer implements Runnable {
    public static final int MAX_QUEUE_SIZE = Global.getSettings().getInt("mpDatagramQueueLimit");

    private final int port;
    private final ServerConnectionManager connectionManager;
    private final Queue<MessageContainer> messageQueue;

    private EventLoopGroup workerLoopGroup;
    private Channel channel;

    private final Clock clock;
    private boolean running;

    private final DebugGraphContainer dataGraph;
    private final DebugGraphContainer dataGraph2;
//    private final DebugGraphContainer dataGraphCompressed;
//    private final DebugGraphContainer dataGraphRatio;

    public DatagramServer(int port, ServerConnectionManager connectionManager) {
        this.port = port;
        this.connectionManager = connectionManager;

        messageQueue = new ConcurrentLinkedQueue<>();

        clock = new Clock(ServerConnectionManager.TICK_RATE);

        dataGraph = new DebugGraphContainer("Packet Size", ServerConnectionManager.TICK_RATE, 40f);
        dataGraph2 = new DebugGraphContainer("Packet Count", ServerConnectionManager.TICK_RATE, 40f);
//        dataGraphCompressed = new DebugGraphContainer("Compressed Bytes Out", ServerConnectionManager.TICK_RATE, 50f);
//        dataGraphRatio = new DebugGraphContainer("Compression Ratio", ServerConnectionManager.TICK_RATE, 50f);

        running = false;
    }

    @Override
    public void run() {
        running = true;

        Console.showMessage("Running UDP server on port " + port + " at " + ServerConnectionManager.TICK_RATE + "Hz");

        ChannelFuture channelFuture = start();

        ChannelFuture closeFuture = channelFuture.channel().closeFuture();

        try {
            int size = 0;
            int sizeCompressed = 0;
            int num = 0;

            while (connectionManager.isActive()) {
                while (!messageQueue.isEmpty()) {
                    MessageContainer message = messageQueue.poll();

                    if (message == null || message.isEmpty()) continue;

                    DatagramUtils.SizeData sizeData = DatagramUtils.write(channel, message, message.getDest(), message.getConnectionID());
                    num++;

                    if (sizeData != null) {
                        size += sizeData.size;
                        sizeCompressed += sizeData.sizeCompressed;
                    }
                }

                if (clock.mark()) {
                    dataGraph2.increment(num);
                    dataGraph.increment(size);
                    CMUtils.getGuiDebug().putContainer(DatagramServer.class, "dataGraph", dataGraph);
                    CMUtils.getGuiDebug().putContainer(DatagramServer.class, "dataGraph2", dataGraph2);
//                    dataGraphCompressed.increment(sizeCompressed);
//                    CMUtils.getGuiDebug().putContainer(DatagramServer.class, "dataGraphCompressed", dataGraphCompressed);
//                    dataGraphRatio.increment(100f * ((float) sizeCompressed / size));
//                    CMUtils.getGuiDebug().putContainer(DatagramServer.class, "dataGraphRatio", dataGraphRatio);

                    size = 0;
                    sizeCompressed = 0;
                    num = 0;
                }
            }

            closeFuture.sync();
            stop();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            System.err.println("CLOSING DATAGRAM THREAD");
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

        return future;
    }

    public void addMessages(List<MessageContainer> messages) {
        messageQueue.addAll(messages);

        while (messageQueue.size() > MAX_QUEUE_SIZE) {
            messageQueue.remove();
        }
    }

    public void stop() {
        if (channel != null) channel.close();
        if (workerLoopGroup != null) workerLoopGroup.shutdownGracefully();
        running = false;

        dataGraph.expire();
        dataGraph2.expire();
//        dataGraphCompressed.expire();
//        dataGraphRatio.expire();

        System.err.println("CLOSED DATAGRAM THREAD SYNC GRACEFULLY");
    }

    public boolean isRunning() {
        return running;
    }
}
