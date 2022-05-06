package data.scripts.net.terminals.client;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.RecordDelta;
import data.scripts.net.io.PacketContainer;
import data.scripts.net.io.Unpacked;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private final ClientDataDuplex clientDataDuplex;

    private final Logger logger;

    private int clientTick;

    public ClientHandler(ClientDataDuplex clientDataDuplex) {
        this.clientDataDuplex = clientDataDuplex;

        logger = Global.getLogger(ClientHandler.class);

        clientTick = 0;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("Channel active on client");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Unpacked unpacked = (Unpacked) msg;

        int serverTick = unpacked.getTick();
        logger.info("Received server tick notice: " + serverTick);

        List<Map<Integer, RecordDelta>> entities = unpacked.getUnpacked();

        for (Map<Integer, RecordDelta> unpackedEntity : entities) {
            for (RecordDelta record : unpackedEntity.values()) {
                logger.info(record.toString());
            }
        }

        clientDataDuplex.threadUpdate(entities);
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws IOException {
        ChannelFuture future = writeAndFlushPacket(ctx);

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

    private ChannelFuture writeAndFlushPacket(ChannelHandlerContext ctx) throws IOException {
        PacketContainer packet = clientDataDuplex.getPacket(clientTick);
        clientTick++;
        return ctx.writeAndFlush(packet);
    }
}
