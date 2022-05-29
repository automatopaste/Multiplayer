package data.scripts.net.connection.udp;

import com.fs.starfarer.api.Global;
import data.scripts.net.connection.client.ClientConnectionWrapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

public class DatagramClient implements Runnable {
    public static final int TICK_RATE = Global.getSettings().getInt("mpClientTickrate");

    private final int port;
    private final String host;
    private final EventLoopGroup workGroup;
    private final ClientConnectionWrapper connection;

    private EventLoopGroup workerGroup;

    private Channel channel;

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
            if (workerGroup != null) workerGroup.shutdownGracefully();
        }
    }

    public void runClient() {
        InetSocketAddress remoteAddress = new InetSocketAddress(host, port);

        ChannelFuture future = start();

        String text = "test";
        System.out.println("Sending test to server");

        ByteBuf buf = Unpooled.copiedBuffer(text, CharsetUtil.UTF_8);

        try {
            write(new DatagramPacket(buf, remoteAddress));
            future.channel().closeFuture().sync();
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
        workerGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioDatagramChannel.class);
        bootstrap.handler(new ChannelInitializer<DatagramChannel>() {
            @Override
            protected void initChannel(DatagramChannel datagramChannel) throws Exception {
                datagramChannel.pipeline().addLast(
//                            new PacketContainerDecoder(),
//                            new PacketDecoder(),
                        new InboundHandler()
                );
            }
        });

        ChannelFuture channelFuture = bootstrap.bind(new InetSocketAddress(0));
        channelFuture.syncUninterruptibly();

        channel = channelFuture.channel();

        return channelFuture;
    }

    private ChannelFuture write(Object msg) throws InterruptedException {
        return channel.writeAndFlush(msg).sync();
    }

    public void stop() {
        if (channel != null) channel.close();
        workerGroup.shutdownGracefully();
    }
}
