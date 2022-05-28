package data.scripts.net.connection.client;

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

/**
 * Main logic for handling network packet data
 */
public class ClientChannelHandler extends ChannelInboundHandlerAdapter {
    private final ClientConnectionWrapper connection;

    private final Logger logger;

    private int clientTick;

    public ClientChannelHandler(ClientConnectionWrapper connection) {
        this.connection = connection;

        logger = Global.getLogger(ClientChannelHandler.class);

        clientTick = 0;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("Channel active on client");

        Console.showMessage("Channel active on client");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Unpacked unpacked = (Unpacked) msg;

        int serverTick = unpacked.getTick();
        logger.info("Received unpacked with tick: " + serverTick);

        connection.getDuplex().setCurrTick(serverTick);

        Map<Integer, BasePackable> entities = unpacked.getUnpacked();

        // if getting -1 value tick from server, server is sending preload data
        connection.setLoading(serverTick == -1);

        connection.getDuplex().updateInbound(entities);
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws IOException {
        ChannelFuture future = sendQueuedData(ctx);

        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) {
                if (!channelFuture.isSuccess()) {
                    ctx.fireChannelReadComplete();
                }
            }
        });
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        logger.info("Client channel handler added");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        logger.info("Client channel handler removed");
    }

    private ChannelFuture sendQueuedData(ChannelHandlerContext ctx) throws IOException {
        int tick = connection.getDuplex().getCurrTick();

        PacketContainer container = connection.getDuplex().getPacket(tick);

        ChannelFuture future = ctx.newSucceededFuture();
        while (container.getSections().peek() != null) {
            ByteBuffer packet = container.getSections().poll();

            future = ctx.write(packet);
        }

        ctx.flush();
        return future;
    }
}
