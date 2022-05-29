package data.scripts.net.connection.tcp.server;

import com.fs.starfarer.api.Global;
import data.scripts.net.connection.Clock;
import data.scripts.net.connection.ServerConnectionManager;
import data.scripts.net.connection.ServerConnectionWrapper;
import data.scripts.net.io.BufferUnpacker;
import data.scripts.net.io.PacketContainer;
import data.scripts.net.io.PacketContainerDecoder;
import data.scripts.net.io.PacketContainerEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.lazywizard.console.Console;

import java.io.IOException;
import java.util.List;

public class SocketServer implements Runnable {
    public static final int TICK_RATE = Global.getSettings().getInt("mpServerTickRate");

    private final int port;
    private final ServerConnectionManager connectionManager;
    private final Clock clock;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private SocketChannel channel;

    public SocketServer(int port, ServerConnectionManager connectionManager) {
        this.port = port;
        this.connectionManager = connectionManager;
        clock = new Clock(TICK_RATE);
    }

    @Override
    public void run() {
        ChannelFuture channelFuture = start();

        ChannelFuture closeFuture = channelFuture.channel().closeFuture();

        Console.showMessage("TCP server socket active on port " + port + " at " + TICK_RATE + "Hz");

        // LOOP WRITE OPERATIONS ONLY
        // Incoming messages handled by inbound channel adapter
        try {
            while (connectionManager.isActive()) {
                clock.sleepUntilTick();

                List<PacketContainer> messages = connectionManager.getSocketMessages();
                for (PacketContainer message : messages) {
                    write(message);
                }
            }

            closeFuture.sync();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            stop();
        }
    }

    private ChannelFuture start() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        final ServerBootstrap server = new ServerBootstrap();
        server.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    private ServerConnectionWrapper connection;

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws InterruptedException {
                        connection = connectionManager.getNewConnection();

                        if (connection == null) {
                            throw new InterruptedException("Channel connection refused: max connections exceeded");
                        }

                        socketChannel.pipeline().addLast(
                                new PacketContainerEncoder(),
                                new PacketContainerDecoder(),
                                new BufferUnpacker(),
                                new ServerChannelHandler(connection)
                        );
                    }

                    @Override
                    public void channelUnregistered(ChannelHandlerContext ctx) {
                        connectionManager.removeConnection(connection);
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        // Bind to TCP port and wait for channel from ready socket
        ChannelFuture future = server.bind(port).syncUninterruptibly();
        channel = (SocketChannel) future.channel();

        return future;
    }

    private ChannelFuture write(Object msg) throws InterruptedException {
        return channel.writeAndFlush(msg).sync();
    }

    public void stop() {
        if (channel != null) channel.close();
        if (workerGroup != null) workerGroup.shutdownGracefully();
        if (bossGroup != null) bossGroup.shutdownGracefully();
    }
}
