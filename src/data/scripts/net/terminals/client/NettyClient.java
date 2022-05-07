package data.scripts.net.terminals.client;

import data.scripts.net.io.PacketContainerDecoder;
import data.scripts.net.io.PacketContainerEncoder;
import data.scripts.net.io.PacketDecoder;
import data.scripts.plugins.state.ClientDataDuplex;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient implements Runnable {
    private final String host;
    private final int port;

    private final ClientDataDuplex clientDataDuplex;

    private boolean stop;

    public NettyClient(String host, int port, ClientDataDuplex clientDataDuplex) {
        this.host = host;
        this.port = port;
        this.clientDataDuplex = clientDataDuplex;
    }

    @Override
    public void run() {
        try {
            runClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runClient() throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        while (!stop) {
            try {
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
                                new PacketDecoder(),
                                new ClientHandler(clientDataDuplex)
                        );
                    }
                });

                // Get channel after connected socket
                Channel channel = bootstrap.connect(host, port).sync().channel();

                // Wait for channel to close
                channel.closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                workerGroup.shutdownGracefully();
            }
        }
    }

    public ClientDataDuplex getClientDataDuplex() {
        return clientDataDuplex;
    }

    public void stop() {
        stop = true;
    }
}
