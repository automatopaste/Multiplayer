package data.scripts.net.terminals.client;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.records.ARecord;
import data.scripts.net.io.PacketContainer;
import data.scripts.net.io.Unpacked;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;

import java.util.List;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private final ClientPacketManager clientPacketManager;

    private final Logger logger;

    public ClientHandler(ClientPacketManager clientPacketManager) {
        this.clientPacketManager = clientPacketManager;

        logger = Global.getLogger(ClientHandler.class);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info("Client received packet from server");

        Unpacked unpacked = (Unpacked) msg;
        for (List<ARecord> unpackedEntity : unpacked.getUnpacked()) {
            for (ARecord record : unpackedEntity) {
                logger.info(record.toString());
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        PacketContainer packet = clientPacketManager.getPacket();
        ChannelFuture future = ctx.writeAndFlush(packet);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("Client handler added");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        logger.info("Client handler removed");
    }
}
