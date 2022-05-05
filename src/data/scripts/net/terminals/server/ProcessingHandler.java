package data.scripts.net.terminals.server;

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

public class ProcessingHandler extends ChannelInboundHandlerAdapter {
    public static final float TICK_RATE = 2f;

    private final Logger logger;

    private long initialTime;
    private final double timeU;
    private double deltaU;
    private long updateTime;

    private final ServerPacketManager serverPacketManager;

    public ProcessingHandler(ServerPacketManager serverPacketManager) {
        this.serverPacketManager = serverPacketManager;

        logger = Global.getLogger(ProcessingHandler.class);

        initialTime = System.nanoTime();
        timeU = 1000000000d / TICK_RATE;
        deltaU = 1d;

        updateTime = initialTime;
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
        for (List<ARecord> unpackedEntity : unpacked.getUnpacked()) {
            for (ARecord record : unpackedEntity) {
                logger.info(record.toString());
            }
        }
    }

    /**
     * Called once when TCP connection is active
     * @param ctx context
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws IOException {
        logger.info("Channel active on server");

        PacketContainer packet = serverPacketManager.getPacket();

        final ChannelFuture future = ctx.writeAndFlush(packet);    }

    /**
     * Called once read is complete. Is used to wait and send next packet.
     * @param ctx context
     */
    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws IOException {
        // keep looping until timer lets it send another packet

        long currentTime;
        while (deltaU < 1d) {
            currentTime = System.nanoTime();
            deltaU += (currentTime - initialTime) / timeU;
            initialTime = currentTime;
        }

        // time delta
        //long diffTimeNanos = currentTime - updateTime;

        PacketContainer packet = serverPacketManager.getPacket();

        final ChannelFuture future = ctx.writeAndFlush(packet);

        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) {
                if (!future.isSuccess()) {
                    deltaU = 1d;
                }
            }
        });

        //updateTime = currentTime;
        deltaU--;
    }
}