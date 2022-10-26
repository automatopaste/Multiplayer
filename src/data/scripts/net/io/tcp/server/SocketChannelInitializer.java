package data.scripts.net.io.tcp.server;

import data.scripts.net.io.*;
import data.scripts.net.io.tcp.BufferUnpacker;
import data.scripts.net.io.tcp.MessageContainerDecoder;
import data.scripts.net.io.tcp.MessageContainerEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import java.net.InetSocketAddress;

public class SocketChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final SocketServer socketServer;
    private final ServerConnectionManager serverConnectionManager;

    public SocketChannelInitializer(SocketServer socketServer, ServerConnectionManager serverConnectionManager) {
        this.socketServer = socketServer;
        this.serverConnectionManager = serverConnectionManager;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws InterruptedException {
        InetSocketAddress a = socketChannel.remoteAddress();
        ServerConnectionWrapper connection = serverConnectionManager.getNewConnection(socketChannel.remoteAddress());

        if (connection == null) {
            throw new InterruptedException("Channel connection refused: max connections exceeded");
        }

        socketChannel.remoteAddress();

        socketChannel.pipeline().addLast(
                new MessageContainerEncoder(),
                new MessageContainerDecoder(),
                new BufferUnpacker(),
                new ServerChannelHandler(connection, socketServer)
        );
    }
}
