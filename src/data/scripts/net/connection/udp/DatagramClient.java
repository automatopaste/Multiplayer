package data.scripts.net.connection.udp;

import com.fs.starfarer.api.Global;
import data.scripts.net.connection.client.ClientConnectionWrapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class DatagramClient implements Runnable {
    public static final int TICK_RATE = Global.getSettings().getInt("mpClientTickrate");

    private final int port;
    private final String host;
    private final EventLoopGroup workGroup;
    private final ClientConnectionWrapper connection;

    private EventLoopGroup workerGroup;

    private Channel channel;
    private boolean active;

    private Clock clock;

    public DatagramClient(String host, int port, ClientConnectionWrapper connection) {
        this.host = host;
        this.port = port;
        this.connection = connection;
        workGroup = new NioEventLoopGroup();

        clock = new Clock(TICK_RATE);
    }

    @Override
    public void run() {
        try {
            runClient();
        } catch (Exception e) {
            e.printStackTrace();
            active = false;
            if (workerGroup != null) workerGroup.shutdownGracefully();
        }
    }

    public void runClient() {
        ChannelFuture future = start();
        if (future == null) {
            throw new NullPointerException("Client failed to start: no channel future");
        }

        try {
            future.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        Console.showMessage("UDP Server active on port " + port + " at " + TICK_RATE + "Hz");
//        while (active) {
//            clock.runUntilUpdate();
//
//            try {
//                channel.writeAndFlush(new PacketContainer(new ArrayList<BasePackable>(), 69, false));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    private ChannelFuture start() {
        active = true;
        workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup);
            bootstrap.channel(NioDatagramChannel.class);
            bootstrap.handler(new ChannelInitializer<DatagramChannel>() {
                @Override
                protected void initChannel(DatagramChannel datagramChannel) throws Exception {
                    datagramChannel.pipeline().addLast(
//                            new PacketContainerDecoder(),
//                            new PacketDecoder(),
                            new ClientDecoder(),
                            new InboundHandler()
                    );
                }
            });

            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();

            this.channel = channelFuture.channel();

            return channelFuture;
        } catch (InterruptedException e) {
            e.printStackTrace();
            workerGroup.shutdownGracefully();
        }

        return null;
    }

    public void stop() {
        active = false;
        workerGroup.shutdownGracefully();
    }

    public boolean isActive() {
        return active;
    }
}
