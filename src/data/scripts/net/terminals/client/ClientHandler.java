package data.scripts.net.terminals.client;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.records.ARecord;
import data.scripts.net.io.PacketContainer;
import data.scripts.net.io.Unpacked;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private final ClientPacketManager clientPacketManager;

    private final Logger logger;

    public ClientHandler(ClientPacketManager clientPacketManager) {
        this.clientPacketManager = clientPacketManager;

        logger = Global.getLogger(ClientHandler.class);
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
        logger.info("Client received packet from server");

        Unpacked unpacked = (Unpacked) msg;
        for (List<ARecord> unpackedEntity : unpacked.getUnpacked()) {
            for (ARecord record : unpackedEntity) {
                logger.info(record.toString());
            }
        }
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws IOException {
        PacketContainer packet = clientPacketManager.getPacket();

        ChannelFuture future = ctx.writeAndFlush(packet);

        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
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
}
