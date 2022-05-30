package data.scripts.net.io.udp.client;

import com.fs.starfarer.api.Global;
import data.scripts.net.io.BaseConnectionWrapper;
import data.scripts.net.io.ClientConnectionWrapper;
import data.scripts.net.io.Clock;
import data.scripts.net.io.udp.DatagramUnpacker;
import data.scripts.net.io.PacketContainer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
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

    public DatagramClient(String host, int port, ClientConnectionWrapper connection) {
        this.host = host;
        this.port = port;
        this.connection = connection;

        clock = new Clock(TICK_RATE);
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
                clock.sleepUntilTick();

                PacketContainer container = connection.getDatagram();
                if (container == null || container.isEmpty()) continue;;

                ByteBuf message = container.get();
                if (message.readableBytes() == 0) continue;

                write(new DatagramPacket(message, remoteAddress));
                //message.release(); released by packet??? throws IllegalReferenceCountException
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
                        new DatagramUnpacker(),
                        new ClientInboundHandler(connection)
                );
            }
        });

        ChannelFuture channelFuture = bootstrap.bind(new InetSocketAddress(0));
        channelFuture.syncUninterruptibly();

        channel = (NioDatagramChannel) channelFuture.channel();

        return channelFuture;
    }

    private ChannelFuture write(Object msg) throws InterruptedException {
        return channel.writeAndFlush(msg).sync();
    }

    public void stop() {
        if (channel != null) channel.close();
        if (workerGroup != null) workerGroup.shutdownGracefully();
    }
}
