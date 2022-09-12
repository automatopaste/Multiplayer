package data.scripts.net.io.udp.client;

import cmu.CMUtils;
import cmu.plugins.debug.DebugGraphContainer;
import com.fs.starfarer.api.Global;
import data.scripts.net.io.*;
import data.scripts.net.io.udp.DatagramDecoder;
import data.scripts.net.io.udp.DatagramUnpacker;
import data.scripts.net.io.udp.server.DatagramServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
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

    private final DebugGraphContainer dataGraph;
    private final DebugGraphContainer dataGraphCompressed;

    public DatagramClient(String host, int port, ClientConnectionWrapper connection) {
        this.host = host;
        // use next port for UDP traffic
        this.port = port;
        this.connection = connection;

        clock = new Clock(TICK_RATE);

        dataGraph = new DebugGraphContainer("Bits Out", TICK_RATE * 2, 50f);
        dataGraphCompressed = new DebugGraphContainer("Compressed Bits Out", TICK_RATE * 2, 50f);
    }

    @Override
    public void run() {
        runClient();
    }

    public void runClient() {
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

                int bufSize = message.getBufSize();
                size += bufSize;

                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                byte[] compressed = CompressionUtils.deflate(bytes);
                sizeCompressed +=  compressed.length;

                ByteBuf out = PooledByteBufAllocator.DEFAULT.buffer();
                out.writeInt(bufSize);
                out.writeBytes(compressed);

                channel.writeAndFlush(new DatagramPacket(out, message.getDest())).sync();

                dataGraph.increment(size);
                CMUtils.getGuiDebug().putContainer(DatagramServer.class, "dataGraph", dataGraph);
                dataGraphCompressed.increment(sizeCompressed);
                CMUtils.getGuiDebug().putContainer(DatagramServer.class, "dataGraphCompressed", dataGraphCompressed);
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
    }
}
