package data.scripts.net.io.udp.client;

import cmu.CMUtils;
import cmu.plugins.debug.DebugGraphContainer;
import com.fs.starfarer.api.Global;
import data.scripts.net.io.BaseConnectionWrapper;
import data.scripts.net.io.ClientConnectionWrapper;
import data.scripts.net.io.Clock;
import data.scripts.net.io.PacketContainer;
import data.scripts.net.io.udp.DatagramDecoder;
import data.scripts.net.io.udp.DatagramUnpacker;
import data.scripts.net.io.udp.DatagramUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.lazywizard.console.Console;

import java.io.IOException;
import java.net.InetSocketAddress;

public class DatagramClient implements Runnable {
    public static final int TICK_RATE = Global.getSettings().getInt("mpClientTickrate");

    private final int port;
    private final String host;
    private final ClientConnectionWrapper connection;

    private EventLoopGroup workerGroup;

    private NioDatagramChannel channel;

    private final Clock clock;
    private boolean running;

    private final DebugGraphContainer dataGraph;
    private final DebugGraphContainer dataGraphCompressed;

    public DatagramClient(String host, int port, ClientConnectionWrapper connection) {
        this.host = host;
        this.port = port;
        this.connection = connection;

        clock = new Clock(TICK_RATE);

        dataGraph = new DebugGraphContainer("Bits Out", TICK_RATE * 2, 50f);
        dataGraphCompressed = new DebugGraphContainer("Compressed Bits Out", TICK_RATE * 2, 50f);

        running = false;
    }

    @Override
    public void run() {
        runClient();
    }

    public void runClient() {
        running = true;

        InetSocketAddress remoteAddress = new InetSocketAddress(host, port);

        ChannelFuture future = start();
        ChannelFuture closeFuture = future.channel().closeFuture();
        Console.showMessage("UDP channel active on port " + port + " at " + TICK_RATE + "Hz");

        try {
            // LOOP WRITE OPERATIONS ONLY
            // Incoming messages handled by inbound channel adapter
            while (connection.getConnectionState() != ClientConnectionWrapper.ConnectionState.CLOSED) {
                int size = 0;
                int sizeCompressed = 0;

                clock.sleepUntilTick();

                PacketContainer message = connection.getDatagram();
                if (message == null || message.isEmpty()) continue;

                ByteBuf buf = message.get();
                if (buf.readableBytes() <= 4) {
                    continue;
                }

                DatagramUtils.SizeData sizeData = DatagramUtils.write(channel, message);
                if (sizeData == null) {
                    return;
                } else {
                    size += sizeData.size;
                    sizeCompressed += sizeData.sizeCompressed;
                }

                dataGraph.increment(size);
                CMUtils.getGuiDebug().putContainer(DatagramClient.class, "dataGraph", dataGraph);
                dataGraphCompressed.increment(sizeCompressed);
                CMUtils.getGuiDebug().putContainer(DatagramClient.class, "dataGraphCompressed", dataGraphCompressed);
            }

            closeFuture.sync();
        } catch (InterruptedException | IOException e) {
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

        // use same port as server to avoid sending datagrams to ephemeral ports
        ChannelFuture channelFuture = bootstrap.bind(port).syncUninterruptibly();
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
