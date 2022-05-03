package data.scripts.net.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.lazywizard.console.Console;

public class NettyServer implements Runnable {
    private final int port;

    private boolean stop = false;

    public NettyServer(int port) {
        this.port = port;
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
                                    new RequestDecoder(),
                                    new ResponseDataEncoder(),
                                    new ProcessingHandler()
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
            Console.showMessage("Server stopped");
        }
    }

    public void stop() {
        stop = true;
    }
}
