package data.scripts.net.io.udp.client;

import cmu.CMUtils;
import cmu.plugins.debug.DebugGraphContainer;
import com.fs.starfarer.api.Global;
import data.scripts.net.io.*;
import data.scripts.net.io.udp.DatagramDecoder;
import data.scripts.net.io.udp.DatagramUnpacker;
import data.scripts.net.io.udp.DatagramUtils;
import data.scripts.net.io.udp.server.DatagramServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.lazywizard.console.Console;

import java.net.InetSocketAddress;

public class DatagramClient implements Runnable {
    public static final int TICK_RATE = Global.getSettings().getInt("mpClientTickrate");

    private final int remotePort;
    private final String host;
    private final int localPort;
    private final ClientConnectionWrapper connection;

    private EventLoopGroup workerGroup;

    private NioDatagramChannel channel;

    private final Clock clock;
    private boolean running;

    private final DebugGraphContainer dataGraph;
    private final DebugGraphContainer dataGraphCompressed;
    private final DebugGraphContainer dataGraphRatio;


    public DatagramClient(String host, int remotePort, int localPort, ClientConnectionWrapper connection) {
        this.host = host;
        this.remotePort = remotePort;
        this.localPort = localPort;
        this.connection = connection;

        clock = new Clock(TICK_RATE);

        dataGraph = new DebugGraphContainer("Bits Out", ServerConnectionManager.TICK_RATE, 50f);
        dataGraphCompressed = new DebugGraphContainer("Compressed Bits Out", ServerConnectionManager.TICK_RATE, 50f);
        dataGraphRatio = new DebugGraphContainer("Compression Ratio", ServerConnectionManager.TICK_RATE, 50f);


        running = false;
    }

    @Override
    public void run() {
        runClient();
    }

    public void runClient() {
        running = true;

        InetSocketAddress remoteAddress = new InetSocketAddress(host, remotePort);

        ChannelFuture future = start();
        ChannelFuture closeFuture = future.channel().closeFuture();
        Console.showMessage("UDP channel active on port " + remotePort + " at " + TICK_RATE + "Hz");

        try {
            // LOOP WRITE OPERATIONS ONLY
            // Incoming messages handled by inbound channel adapter
            while (connection.getConnectionState() != ClientConnectionWrapper.ConnectionState.CLOSED) {
                int size = 0;
                int sizeCompressed = 0;

                clock.sleepUntilTick();

                MessageContainer message = connection.getDatagram();

                if (message == null || message.isEmpty()) continue;

                DatagramUtils.SizeData sizeData = DatagramUtils.write(channel, message, remoteAddress, message.getConnectionID());
                if (sizeData != null) {
                    size += sizeData.size;
                    sizeCompressed += sizeData.sizeCompressed;
                }

                dataGraph.increment(size);
                CMUtils.getGuiDebug().putContainer(DatagramServer.class, "dataGraph", dataGraph);
                dataGraphCompressed.increment(sizeCompressed);
                CMUtils.getGuiDebug().putContainer(DatagramServer.class, "dataGraphCompressed", dataGraphCompressed);
                dataGraphRatio.increment(100f * ((float) sizeCompressed / size));
                CMUtils.getGuiDebug().putContainer(DatagramServer.class, "dataGraphRatio", dataGraphRatio);
            }

            closeFuture.sync();
            stop();
        } catch (Throwable e) {
            e.printStackTrace();
            stop();
            connection.setConnectionState(BaseConnectionWrapper.ConnectionState.CLOSED);
        }
    }

    private ChannelFuture start() {
        workerGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioDatagramChannel.class);
        bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {
            @Override
            protected void initChannel(NioDatagramChannel datagramChannel) {
                datagramChannel.pipeline().addLast(
                        new DatagramDecoder(),
                        new DatagramUnpacker(),
                        new ClientInboundHandler(connection)
                        //new DatagramEncoder()
                );
            }
        });

        ChannelFuture channelFuture = bootstrap.bind(localPort).syncUninterruptibly();
        channelFuture.syncUninterruptibly();

        channel = (NioDatagramChannel) channelFuture.channel();

        return channelFuture;
    }

    public void stop() {
        if (channel != null) channel.close();
        if (workerGroup != null) workerGroup.shutdownGracefully();
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}
