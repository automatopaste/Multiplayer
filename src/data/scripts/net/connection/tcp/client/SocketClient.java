package data.scripts.net.connection.tcp.client;

import com.fs.starfarer.api.Global;
import data.scripts.net.connection.BaseConnectionWrapper;
import data.scripts.net.connection.ClientConnectionWrapper;
import data.scripts.net.connection.Clock;
import data.scripts.net.io.BufferUnpacker;
import data.scripts.net.io.PacketContainer;
import data.scripts.net.io.PacketContainerDecoder;
import data.scripts.net.io.PacketContainerEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.lazywizard.console.Console;

import java.io.IOException;
import java.net.InetSocketAddress;

public class SocketClient implements Runnable {
    public static final int TICK_RATE = Global.getSettings().getInt("mpClientTickrate");

    private final String host;
    private final int port;

    private EventLoopGroup workerGroup;
    private final ClientConnectionWrapper connection;
    private SocketChannel channel;

    private final Clock clock;

    public SocketClient(String host, int port, ClientConnectionWrapper connection) {
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

        try {
            ChannelFuture channelFuture = start();
            ChannelFuture closeFuture = channelFuture.channel().closeFuture();

            Console.showMessage("TCP socket active on port " + port);

            // LOOP WRITE OPERATIONS ONLY
            // Incoming messages handled by inbound channel adapter
            while (connection.getConnectionState() != ClientConnectionWrapper.ConnectionState.CLOSED) {
                clock.sleepUntilTick();

                PacketContainer message = connection.getSocketMessage();
                if (message == null) continue;

                write(new DatagramPacket(message.get(), remoteAddress));
            }

            // Wait for channel to close
            closeFuture.sync();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            stop();
            connection.setConnectionState(BaseConnectionWrapper.ConnectionState.CLOSED);
        }
    }

    private ChannelFuture write(Object msg) throws InterruptedException {
        return channel.writeAndFlush(msg).sync();
    }

    private ChannelFuture start() throws InterruptedException {
        workerGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(
                        new PacketContainerEncoder(),
                        new PacketContainerDecoder(),
                        new BufferUnpacker(),
                        new ClientChannelHandler(connection)
                );
            }
        });

        // Get channel after connected socket
        ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
        channel = (SocketChannel) channelFuture.channel();

        return channelFuture;
    }

    public void stop() {
        if (channel != null) channel.close();
        if (workerGroup != null) workerGroup.shutdownGracefully();
    }
}
