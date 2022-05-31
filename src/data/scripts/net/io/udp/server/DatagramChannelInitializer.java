package data.scripts.net.io.udp.server;

import data.scripts.net.io.ServerConnectionManager;
import data.scripts.net.io.udp.DatagramUnpacker;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class DatagramChannelInitializer extends ChannelInitializer<NioDatagramChannel> {
    private final DatagramServer datagramServer;
    private final ServerConnectionManager serverConnectionManager;

    public DatagramChannelInitializer(DatagramServer datagramServer, ServerConnectionManager serverConnectionManager) {
        this.datagramServer = datagramServer;
        this.serverConnectionManager = serverConnectionManager;
    }

    @Override
    protected void initChannel(NioDatagramChannel datagramChannel) {
        datagramChannel.pipeline().addLast(
                new DatagramUnpacker(),
                new ServerInboundHandler(serverConnectionManager)
        );
    }
}
