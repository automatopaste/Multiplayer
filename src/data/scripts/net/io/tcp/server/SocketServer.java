package data.scripts.net.io.tcp.server;

import data.scripts.net.io.Check;
import data.scripts.net.io.PacketContainer;
import data.scripts.net.io.ServerConnectionManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.lazywizard.console.Console;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SocketServer implements Runnable {
    private final int port;
    private final ServerConnectionManager connectionManager;
    private final Queue<PacketContainer> messageQueue;
    private final Check check;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private final DefaultChannelGroup channelGroup;

    public SocketServer(int port, ServerConnectionManager connectionManager) {
        this.port = port;
        this.connectionManager = connectionManager;

        messageQueue = new LinkedList<>();
        check = new Check(messageQueue);

        channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    @Override
    public void run() {
        Console.showMessage("Running TCP server on port " + port + " at " + ServerConnectionManager.TICK_RATE + "Hz");

        ChannelFuture channelFuture = start();

        ChannelFuture closeFuture = channelFuture.channel().closeFuture();

        try {
            while (connectionManager.isActive()) {
                synchronized (messageQueue) {
                    while (!messageQueue.isEmpty()) {
                        final PacketContainer message = messageQueue.poll();

                        channelGroup.writeAndFlush(message, new ChannelMatcher() {
                            @Override
                            public boolean matches(Channel channel) {
                                SocketAddress address = channel.remoteAddress();
                                InetSocketAddress messageAddress = message.getDest();

                                return address == messageAddress;
                            }
                        });
                    }
                }
            }

            closeFuture.sync();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            System.err.println("CLOSING THREAD");
        }
    }

    private ChannelFuture start() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        final ServerBootstrap server = new ServerBootstrap();
        server.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new SocketChannelInitializer(this, connectionManager))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        // Bind to TCP port and wait for channel from ready socket
        ChannelFuture future = server.bind(port).syncUninterruptibly();
        serverChannel = future.channel();

        return future;
    }

    public void queueMessages(List<PacketContainer> messages) {
        if (messages.isEmpty()) return;

        synchronized (messageQueue) {
            messageQueue.addAll(messages);
        }
    }

    public void stop() {
        if (serverChannel != null) serverChannel.close();
        if (workerGroup != null) workerGroup.shutdownGracefully();
        if (bossGroup != null) bossGroup.shutdownGracefully();
    }

    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }
}
