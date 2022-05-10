package data.scripts.net.terminals.server;

import data.scripts.net.io.PacketContainerDecoder;
import data.scripts.net.io.PacketContainerEncoder;
import data.scripts.net.io.PacketDecoder;
import data.scripts.plugins.state.DataDuplex;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer implements Runnable {
    private final int port;
    private final DataDuplex serverDataDuplex;

    public NettyServer(int port, DataDuplex serverDataDuplex) {
        this.port = port;
        this.serverDataDuplex = serverDataDuplex;
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
                                    new PacketContainerEncoder(),
                                    new PacketContainerDecoder(),
                                    new PacketDecoder(),
                                    new ProcessingHandler(serverDataDuplex)
                            );
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

    public DataDuplex getServerDataDuplex() {
        return serverDataDuplex;
    }
}
