package data.scripts.net.connection.udp.server;

import com.fs.starfarer.api.Global;
import data.scripts.net.connection.Clock;
import data.scripts.net.connection.ServerConnectionManager;
import data.scripts.net.connection.udp.DatagramUnpacker;
import data.scripts.net.io.PacketContainer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.lazywizard.console.Console;

import java.io.IOException;
import java.util.List;

public class DatagramServer implements Runnable {
    public static final int TICK_RATE = Global.getSettings().getInt("mpServerTickRate");

    private final int port;
    private final ServerConnectionManager serverConnectionManager;
    private EventLoopGroup workerLoopGroup;
    private NioDatagramChannel channel;
//    private final ChannelGroup channelGroup;

    private final Clock clock;

    public DatagramServer(int port, ServerConnectionManager serverConnectionManager) {
        this.port = port;
        this.serverConnectionManager = serverConnectionManager;

//        channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        clock = new Clock(TICK_RATE);
    }

    @Override
    public void run() {
        ChannelFuture channelFuture = start();

        ChannelFuture closeFuture = channelFuture.channel().closeFuture();

        Console.showMessage("UDP Server active on port " + port + " at " + TICK_RATE + "Hz");

        // LOOP WRITE OPERATIONS ONLY
        // Incoming messages handled by inbound channel adapter
        try {
            while (serverConnectionManager.isActive()) {
                clock.sleepUntilTick();

                List<PacketContainer> messages = serverConnectionManager.getDatagrams();
                for (PacketContainer message : messages) {
                    write(new DatagramPacket(message.get(), message.getDest()));
                }
            }

            closeFuture.sync();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            stop();
        }
    }


    private ChannelFuture start() {
        workerLoopGroup = new NioEventLoopGroup();

        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerLoopGroup)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel datagramChannel) {
                        datagramChannel.pipeline().addLast(
                                new DatagramUnpacker(),
                                new ServerInboundHandler(serverConnectionManager.getNewConnection())
                        );
                    }
                });

        ChannelFuture future = bootstrap.bind(port).syncUninterruptibly();
        channel = (NioDatagramChannel) future.channel();

        return future;
    }

    private ChannelFuture write(Object msg) throws InterruptedException {
        return channel.writeAndFlush(msg).sync();
    }

    public void stop() {
        if (channel != null) channel.close();
        if (workerLoopGroup != null) workerLoopGroup.shutdownGracefully();
    }
}
