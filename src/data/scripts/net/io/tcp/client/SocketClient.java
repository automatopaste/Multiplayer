package data.scripts.net.io.tcp.client;

import com.fs.starfarer.api.Global;
import data.scripts.net.io.BaseConnectionWrapper;
import data.scripts.net.io.ClientConnectionWrapper;
import data.scripts.net.io.Clock;
import data.scripts.net.io.PacketContainer;
import data.scripts.net.io.tcp.BufferUnpacker;
import data.scripts.net.io.tcp.PacketContainerDecoder;
import data.scripts.net.io.tcp.PacketContainerEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.lazywizard.console.Console;

import java.io.IOException;
import java.net.InetSocketAddress;

public class SocketClient implements Runnable {
    public static final int TICK_RATE = Global.getSettings().getInt("mpClientTickrate");

    private final String host;
    private final int port;
    private InetSocketAddress local;

    private EventLoopGroup workerGroup;
    private final ClientConnectionWrapper connection;
    private Channel channel;

    private final Clock clock;

    public SocketClient(String host, int port, ClientConnectionWrapper connection) {
        this.host = host;
        this.port = port;
        this.connection = connection;

        clock = new Clock(TICK_RATE);
    }

    public InetSocketAddress getLocal() {
        return local;
    }

    @Override
    public void run() {
        runClient();
    }

    public void runClient() {
        try {
            ChannelFuture channelFuture = start();
            ChannelFuture closeFuture = channelFuture.channel().closeFuture();

            Console.showMessage("TCP socket active on port " + port);

            // LOOP WRITE OPERATIONS ONLY
            // Incoming messages handled by inbound channel adapter
            while (connection.getConnectionState() != ClientConnectionWrapper.ConnectionState.CLOSED) {
                clock.sleepUntilTick();

                PacketContainer container = connection.getSocketMessage();
                if (container == null || container.isEmpty()) continue;;

                write(container);
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
        channel = channelFuture.channel();
        local = (InetSocketAddress) channel.localAddress();

        return channelFuture;
    }

    public void stop() {
        if (channel != null) channel.close();
        if (workerGroup != null) workerGroup.shutdownGracefully();
    }

    public Channel getChannel() {
        return channel;
    }
}
