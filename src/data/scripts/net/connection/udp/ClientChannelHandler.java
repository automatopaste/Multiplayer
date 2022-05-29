package data.scripts.net.connection.udp;

import com.fs.starfarer.api.Global;
import data.scripts.net.connection.client.ClientConnectionWrapper;
import data.scripts.net.data.BasePackable;
import data.scripts.net.io.PacketContainer;
import data.scripts.net.io.Unpacked;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;
import org.lazywizard.console.Console;

import java.io.IOException;
import java.util.Map;

/**
 * Main logic for handling network packet data
 */
public class ClientChannelHandler extends ChannelInboundHandlerAdapter {
    private final ClientConnectionWrapper connection;

    private final Logger logger;

    public ClientChannelHandler(ClientConnectionWrapper connection) {
        this.connection = connection;

        logger = Global.getLogger(ClientChannelHandler.class);

        int clientTick = 0;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
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
        int tick = connection.getDuplex().getCurrTick();
        PacketContainer container = connection.getDuplex().getPacket(tick);
        ctx.writeAndFlush(container);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        logger.info("Client channel handler added");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        logger.info("Client channel handler removed");
    }

//    private ChannelFuture sendQueuedData(ChannelHandlerContext ctx) throws IOException {
//        int tick = connection.getDuplex().getCurrTick();
//
//        PacketContainer container = connection.getDuplex().getPacket(tick);
//
//        //        ChannelFuture future = null;
////        while (container.getSections().peek() != null) {
////            ByteBuffer packet = container.getSections().poll();
////
////            future = ctx.writeAndFlush(packet);
////        }
////        if (future == null) {
////            ByteBuffer empty = ByteBuffer.allocateDirect(4).putInt(tick);
////            empty.flip();
////
////            return ctx.writeAndFlush(empty);
////        }
//
//        return ctx.writeAndFlush(container.get());
//    }
}
