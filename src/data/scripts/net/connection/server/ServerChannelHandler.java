package data.scripts.net.connection.server;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.BasePackable;
import data.scripts.net.io.PacketContainer;
import data.scripts.net.io.Unpacked;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;
import org.lazywizard.console.Console;

import java.io.IOException;
import java.util.Map;

public class ServerChannelHandler extends ChannelInboundHandlerAdapter {
    public static final float TICK_RATE = Global.getSettings().getFloat("mpServerTickRate");

    private final Logger logger;

    private long initialTime;
    private final double timeU;
    private double deltaU;

    private final ServerConnectionWrapper connection;

    public ServerChannelHandler(ServerConnectionWrapper connection) {
        this.connection = connection;

        logger = Global.getLogger(ServerChannelHandler.class);

        initialTime = System.nanoTime();
        timeU = 1000000000d / TICK_RATE;
        deltaU = 1d;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        logger.info("Server channel handler added");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        logger.info("Server channel handler removed");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Unpacked unpacked = (Unpacked) msg;

        int clientTick = unpacked.getTick();
        logger.info("Received client tick notice: " + clientTick);

        Map<Integer, BasePackable> entities = unpacked.getUnpacked();

        connection.getDuplex().updateInbound(entities);
    }

    /**
     * Called once when TCP connection is active
     * @param ctx context
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws IOException, InterruptedException {
        logger.info("Channel active on server");
        logger.info("Server running at " + TICK_RATE + "Hz");

        Console.showMessage("Channel active on server");
        Console.showMessage("Server running at " + TICK_RATE + "Hz");

        int tick = connection.getDuplex().getCurrTick();
        // send -1 value tick to indicate server is sending preload data
        if (connection.isRequestLoad()) tick = -1;

        PacketContainer container = connection.getDuplex().getPacket(tick);

        ChannelFuture future = ctx.writeAndFlush(container);

        if (connection.isRequestLoad()) connection.setRequestLoad(false);
    }

    /**
     * Called once read is complete. Is used to wait and send next packet.
     * @param ctx context
     */
    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws IOException, InterruptedException {
        // keep looping until timer lets it send another packet

        long currentTime;
        while (deltaU < 1d) {
            currentTime = System.nanoTime();
            deltaU += (currentTime - initialTime) / timeU;
            initialTime = currentTime;
        }

        int tick = connection.getDuplex().getCurrTick();
        // send -1 value tick to indicate server is sending preload data
        if (connection.isRequestLoad()) tick = -1;

        PacketContainer container = connection.getDuplex().getPacket(tick);

        ChannelFuture future = ctx.writeAndFlush(container);

        if (connection.isRequestLoad()) connection.setRequestLoad(false);

        deltaU--;
    }
}
