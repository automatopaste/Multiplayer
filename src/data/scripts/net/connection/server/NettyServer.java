package data.scripts.net.connection.server;

import data.scripts.net.io.PacketContainerDecoder;
import data.scripts.net.io.PacketContainerEncoder;
import data.scripts.net.io.PacketDecoder;
import data.scripts.plugins.mpServerPlugin;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer implements Runnable {
    private final int port;
    private final mpServerPlugin serverPlugin;

    public NettyServer(int port, mpServerPlugin serverPlugin) {
        this.port = port;
        this.serverPlugin = serverPlugin;
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
            final ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        private ServerConnectionWrapper connection;

                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            connection = serverPlugin.getNewConnection();

                            socketChannel.pipeline().addLast(
                                    new PacketContainerEncoder(),
                                    new PacketContainerDecoder(),
                                    new PacketDecoder(),
                                    new ServerChannelHandler(connection)
                            );
                        }

                        @Override
                        public void channelUnregistered(ChannelHandlerContext ctx) {
                            serverPlugin.removeConnection(connection);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind to TCP port and wait for channel from ready socket
            Channel channel = server.bind(port).sync().channel();

            // Wait for channel to close
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
        }
    }
}
