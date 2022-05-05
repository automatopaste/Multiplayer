package data.scripts.net.terminals.server;

import data.scripts.net.io.PacketContainerDecoder;
import data.scripts.net.io.PacketContainerEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer implements Runnable {
    private final int port;
    private final ServerPacketManager serverPacketManager;

    public NettyServer(int port, ServerPacketManager serverPacketManager) {
        this.port = port;
        this.serverPacketManager = serverPacketManager;
    }

    @Override
    public void run() {
        try {
            runServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runServer() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(
                                    new PacketContainerDecoder(),
                                    new PacketContainerEncoder(),
                                    new ProcessingHandler(serverPacketManager)
                            );
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind to TCP port and wait for channel from ready socket
            Channel channel = server.bind(port).sync().channel();

            // Wait for channel to close
            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
        }
    }
}
