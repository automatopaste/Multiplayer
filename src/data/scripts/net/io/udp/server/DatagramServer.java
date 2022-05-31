package data.scripts.net.io.udp.server;

import data.scripts.net.io.PacketContainer;
import data.scripts.net.io.ServerConnectionManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.lazywizard.console.Console;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DatagramServer implements Runnable {
    private final int port;
    private final ServerConnectionManager connectionManager;
    private final Queue<PacketContainer> messageQueue;

    private final Object sync;
    private EventLoopGroup workerLoopGroup;
    private Channel channel;

    public DatagramServer(int port, ServerConnectionManager connectionManager) {
        this.port = port;
        this.connectionManager = connectionManager;

        sync = new Object();

        messageQueue = new LinkedList<>();
    }

    @Override
    public void run() {
        Console.showMessage("Running UDP server on port " + port + " at " + ServerConnectionManager.TICK_RATE + "Hz");

        ChannelFuture channelFuture = start();

        ChannelFuture closeFuture = channelFuture.channel().closeFuture();

        try {
            while (connectionManager.isActive()) {
                while (!messageQueue.isEmpty()) {
                    final PacketContainer message = messageQueue.poll();
                    if (message == null || message.isEmpty()) continue;

                    ByteBuf buf = message.get();
                    if (buf.readableBytes() <= 4) {
                        buf.release();
                        continue;
                    }

                    channel.writeAndFlush(new DatagramPacket(buf, message.getDest()));
                }

                while (messageQueue.isEmpty()) {
                    synchronized (sync) {
                        try {
                            sync.wait();
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }

            closeFuture.sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private ChannelFuture start() {
        workerLoopGroup = new NioEventLoopGroup();

        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerLoopGroup)
                .channel(NioDatagramChannel.class)
                .handler(new DatagramChannelInitializer(this, connectionManager)
                );

        ChannelFuture future = bootstrap.bind(port).syncUninterruptibly();
        channel = future.channel();
//        channelGroup.add(channel);

        return future;
    }

    public void queueMessages(List<PacketContainer> messages) {
        if (messages.isEmpty()) return;

        messageQueue.addAll(messages);

        synchronized (sync) {
            sync.notify();
        }
    }

    public void stop() {
        if (channel != null) channel.close();
        if (workerLoopGroup != null) workerLoopGroup.shutdownGracefully();
    }
}
