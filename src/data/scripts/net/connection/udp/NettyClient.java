package data.scripts.net.connection.udp;

import data.scripts.net.connection.client.ClientConnectionWrapper;
import data.scripts.net.io.PacketContainerDecoder;
import data.scripts.net.io.PacketContainerEncoder;
import data.scripts.net.io.PacketDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class NettyClient implements Runnable{
    private final int port;
    private final String host;
    private final EventLoopGroup workGroup;
    private final ClientConnectionWrapper connection;

    private Channel channel;

    public NettyClient(String host, int port, ClientConnectionWrapper connection) {
        this.host = host;
        this.port = port;
        this.connection = connection;
        workGroup = new NioEventLoopGroup();
    }

    @Override
    public void run() {
        try {
            runClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runClient() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup);
            bootstrap.channel(NioDatagramChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(
                            new PacketContainerEncoder(),
                            new PacketContainerDecoder(),
                            new PacketDecoder(),
                            new ClientChannelHandler(connection)
                    );
                }
            });

            // Get channel after connected socket
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();

            // Wait for channel to close
            this.channel = channelFuture.channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
