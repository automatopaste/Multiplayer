package data.scripts.net.io.tcp.server;

import data.scripts.net.io.*;
import data.scripts.net.io.tcp.BufferUnpacker;
import data.scripts.net.io.tcp.PacketContainerDecoder;
import data.scripts.net.io.tcp.PacketContainerEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class SocketChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final SocketServer socketServer;
    private final ServerConnectionManager serverConnectionManager;

    public SocketChannelInitializer(SocketServer socketServer, ServerConnectionManager serverConnectionManager) {
        this.socketServer = socketServer;
        this.serverConnectionManager = serverConnectionManager;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws InterruptedException {
        ServerConnectionWrapper connection = serverConnectionManager.getNewConnection(socketChannel.remoteAddress());

        if (connection == null) {
            throw new InterruptedException("Channel connection refused: max connections exceeded");
        }

        socketChannel.remoteAddress();

        socketChannel.pipeline().addLast(
                new PacketContainerEncoder(),
                new PacketContainerDecoder(),
                new BufferUnpacker(),
                new ServerChannelHandler(connection, socketServer)
        );
    }
}
