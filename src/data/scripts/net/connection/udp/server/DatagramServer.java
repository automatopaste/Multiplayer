package data.scripts.net.connection.udp.server;

import data.scripts.net.connection.ServerConnectionManager;
import data.scripts.net.connection.udp.DatagramUnpacker;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.lazywizard.console.Console;

public class DatagramServer implements Runnable {
//    public static final int TICK_RATE = Global.getSettings().getInt("mpServerTickRate");

    private final int port;
    private final ServerConnectionManager serverConnectionManager;
    private EventLoopGroup workerLoopGroup;
    private NioDatagramChannel channel;
//    private final ChannelGroup channelGroup;

//    private final Clock clock;

    public DatagramServer(int port, ServerConnectionManager serverConnectionManager) {
        this.port = port;
        this.serverConnectionManager = serverConnectionManager;

//        channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

//        clock = new Clock(TICK_RATE);
    }

    @Override
    public void run() {
        Console.showMessage("Running UDP server on port " + port + " at " + ServerConnectionManager.TICK_RATE + "Hz");

        ChannelFuture channelFuture = start();

        ChannelFuture closeFuture = channelFuture.channel().closeFuture();

        try {
            closeFuture.sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        // LOOP WRITE OPERATIONS ONLY
//        // Incoming messages handled by inbound channel adapter
//        try {
//            while (serverConnectionManager.isActive()) {
//                clock.sleepUntilTick();
//
//                List<PacketContainer> messages = serverConnectionManager.getDatagrams();
//                for (PacketContainer message : messages) {
//                    if (message == null || message.isEmpty()) continue;
//                    ByteBuf buf = message.get();
//                    write(new DatagramPacket(buf, message.getDest()));
//                    //buf.release(); released by packet??? throws IllegalReferenceCountException
//                }
//            }
//
//            closeFuture.sync();
//        } catch (InterruptedException | IOException e) {
//            e.printStackTrace();
//            stop();
//        }
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
                                new ServerInboundHandler(serverConnectionManager.getConnection(datagramChannel.remoteAddress()))
                        );
                    }
                });

        ChannelFuture future = bootstrap.bind(port).syncUninterruptibly();
        channel = (NioDatagramChannel) future.channel();

        return future;
    }

    public ChannelFuture write(Object msg) throws InterruptedException {
        return channel.write(msg).sync();
    }

    public void flush() {
        channel.flush();
    }

    public void stop() {
        if (channel != null) channel.close();
        if (workerLoopGroup != null) workerLoopGroup.shutdownGracefully();
    }
}
