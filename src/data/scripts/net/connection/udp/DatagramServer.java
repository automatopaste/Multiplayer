package data.scripts.net.connection.udp;

import com.fs.starfarer.api.Global;
import data.scripts.net.connection.server.ServerConnectionWrapper;
import data.scripts.net.io.PacketContainerDecoder;
import data.scripts.net.io.PacketContainerEncoder;
import data.scripts.net.io.PacketDecoder;
import data.scripts.plugins.mpServerPlugin;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.lazywizard.console.Console;

public class DatagramServer implements Runnable {
    public static final int TICK_RATE = Global.getSettings().getInt("mpServerTickRate");

    private final int port;
    private final mpServerPlugin serverPlugin;
    private final EventLoopGroup bossLoopGroup;
    private final ChannelGroup channelGroup;

    private final Clock clock;

    public DatagramServer(int port, mpServerPlugin serverPlugin) {
        this.port = port;
        this.serverPlugin = serverPlugin;

        bossLoopGroup = new NioEventLoopGroup();
        channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        clock = new Clock(TICK_RATE);
    }

    @Override
    public void run() {
        try {
            runServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runServer() {
        try {
            final Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(bossLoopGroup)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        private ServerConnectionWrapper connection;

                        @Override
                        protected void initChannel(DatagramChannel datagramChannel) throws InterruptedException {
                            connection = serverPlugin.getNewConnection();

                            if (connection == null) {
                                throw new InterruptedException("Channel connection refused: max connections exceeded");
                            }

                            datagramChannel.pipeline().addLast(
                                    new PacketContainerEncoder(),
                                    new PacketContainerDecoder(),
                                    new PacketDecoder(),
                                    new ServerOutboundChannelHandler(connection)
                            );
                        }

                        @Override
                        public void channelUnregistered(ChannelHandlerContext ctx) {
                            serverPlugin.removeConnection(connection);
                        }
                    })
                    .option(ChannelOption.AUTO_CLOSE, true)
                    .option(ChannelOption.SO_BROADCAST, true);

            Channel channel = bootstrap.bind(port).sync().channel();
            channelGroup.add(channel);

            Console.showMessage("UDP Server active on port " + port + " at " + TICK_RATE + "Hz");
            while (serverPlugin.isActive()) {
                // engages thread until time passed
                clock.runUntilUpdate();

                channelGroup.write(serverPlugin.getTick()).sync();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            channelGroup.close();
            bossLoopGroup.shutdownGracefully();
        }
    }
}
