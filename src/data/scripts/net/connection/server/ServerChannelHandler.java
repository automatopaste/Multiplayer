package data.scripts.net.connection.server;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.BasePackable;
import data.scripts.net.io.PacketContainer;
import data.scripts.net.io.Unpacked;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;
import org.lazywizard.console.Console;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

public class ServerChannelHandler extends ChannelInboundHandlerAdapter {
    public static final float TICK_RATE = Global.getSettings().getFloat("mpServerTickRate");

    private final Logger logger;

    private long initialTime;
    private final double timeU;
    private double deltaU;
    private boolean doFlush = true;

    private final ServerConnectionWrapper connection;

    public ServerChannelHandler(ServerConnectionWrapper connection) {
        this.connection = connection;

        logger = Global.getLogger(ServerChannelHandler.class);

        initialTime = System.nanoTime();
        timeU = 1000000000d / TICK_RATE;
        deltaU = 1d;

        // -1 indicates in process of loading, not sending remote simulation data yet
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
    public void channelActive(final ChannelHandlerContext ctx) throws IOException {
        logger.info("Channel active on server");
        logger.info("Server running at " + TICK_RATE + "Hz");

        Console.showMessage("Channel active on server");
        Console.showMessage("Server running at " + TICK_RATE + "Hz");

        sendQueuedData(ctx);
    }

    /**
     * Called once read is complete. Is used to wait and send next packet.
     * @param ctx context
     */
    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws IOException {
        // keep looping until timer lets it send another packet
        // probably should replace with a thread sleep

        long currentTime;
        while (deltaU < 1d) {
            currentTime = System.nanoTime();
            deltaU += (currentTime - initialTime) / timeU;
            initialTime = currentTime;
        }

        // time delta
        //long diffTimeNanos = currentTime - updateTime;

        final ChannelFuture future = sendQueuedData(ctx);

        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) {
                if (!future.isSuccess()) {
                    deltaU = 1d;
                    doFlush = true;
                }
            }
        });

        //updateTime = currentTime;
        deltaU--;
    }

    private ChannelFuture sendQueuedData(ChannelHandlerContext ctx) throws IOException {
        if (doFlush) connection.getDuplex().flush();

        int tick = connection.getDuplex().getCurrTick();
        // send -1 value tick to indicate server is sending preload data
        if (connection.isRequestLoad()) tick = -1;

        PacketContainer container = connection.getDuplex().getPacket(tick);

        ChannelFuture future = ctx.newSucceededFuture();
        while (container.getSections().peek() != null) {
            ByteBuffer packet = container.getSections().poll();

            future = ctx.writeAndFlush(packet);
        }

        if (connection.isRequestLoad()) connection.setRequestLoad(false);

        return future;
    }
}
