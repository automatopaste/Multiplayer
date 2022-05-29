package data.scripts.net.connection.udp;

import com.fs.starfarer.api.Global;
import data.scripts.net.connection.server.ServerConnectionWrapper;
import data.scripts.plugins.mpServerPlugin;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class DatagramServer implements Runnable {
    public static final int TICK_RATE = Global.getSettings().getInt("mpServerTickRate");

    private final int port;
    private final mpServerPlugin serverPlugin;
    private EventLoopGroup workerLoopGroup;
    private Channel channel;
//    private final ChannelGroup channelGroup;

    private final Clock clock;

    public DatagramServer(int port, mpServerPlugin serverPlugin) {
        this.port = port;
        this.serverPlugin = serverPlugin;

//        channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        clock = new Clock(TICK_RATE);
    }

    @Override
    public void run() {
        try {
            runServer();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (workerLoopGroup != null) workerLoopGroup.shutdownGracefully();
        }
    }

    public void runServer() throws InterruptedException {
        ChannelFuture channelFuture = start();

        channelFuture.channel().closeFuture().sync();

//        Console.showMessage("UDP Server active on port " + port + " at " + TICK_RATE + "Hz");
//        while (serverPlugin.isActive()) {
//            // engages thread until time passed
//            clock.runUntilUpdate();
//        }
    }

    private ChannelFuture start() throws InterruptedException {
        workerLoopGroup = new NioEventLoopGroup();

        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerLoopGroup)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    private ServerConnectionWrapper connection;

                    @Override
                    protected void initChannel(DatagramChannel datagramChannel) throws InterruptedException {
                        //connection = serverPlugin.getNewConnection();

                        //if (connection == null) {
                        //    throw new InterruptedException("Channel connection refused: max connections exceeded");
                        //}

                        datagramChannel.pipeline().addLast(
//                                new PacketContainerEncoder(),
//                                new PacketContainerDecoder(),
//                                new PacketDecoder(),
                                new InboundHandler(),
                                new OutboundHandler()
                        );
                    }

                    @Override
                    public void channelUnregistered(ChannelHandlerContext ctx) {
                        //Console.showMessage("Channel unregisted " + connection.getId());
                        //serverPlugin.removeConnection(connection);
                    }
                });

        ChannelFuture future = bootstrap.bind(port).syncUninterruptibly();
        channel = future.channel();

        return future;
    }

    public void stop() {
        if (channel != null) channel.close();
        workerLoopGroup.shutdownGracefully();
    }
}
