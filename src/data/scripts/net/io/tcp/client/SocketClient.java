package data.scripts.net.io.tcp.client;

import com.fs.starfarer.api.Global;
import data.scripts.net.io.ClientConnectionWrapper;
import data.scripts.net.io.Clock;
import data.scripts.net.io.MessageContainer;
import data.scripts.net.io.tcp.BufferUnpacker;
import data.scripts.net.io.tcp.MessageContainerDecoder;
import data.scripts.net.io.tcp.MessageContainerEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.lazywizard.console.Console;

import java.net.InetSocketAddress;
import java.util.List;

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

                List<MessageContainer> containers = connection.getSocketMessages();
                if (containers == null || containers.isEmpty()) continue;

                for (MessageContainer message : containers) {
                    write(message);
                }
            }

            // Wait for channel to close
            closeFuture.sync();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            System.err.println("CLOSING SOCKET THREAD");
            connection.stop();
        }
    }

    private void write(Object msg) throws InterruptedException {
        channel.writeAndFlush(msg).sync();
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
                        new MessageContainerEncoder(),
                        new MessageContainerDecoder(),
                        new BufferUnpacker(),
                        new ClientChannelHandler(connection)
                );
            }
        });

        // Get channel after connected socket

        String h = host.equals("localhost") ? "127.0.0.1" : host;
        int p = host.equals("localhost") ? 8080 : port;
        ChannelFuture channelFuture = bootstrap.connect(h, p).sync();
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

    public int getLocalPort() {
        return ((InetSocketAddress) channel.localAddress()).getPort();
    }
}
