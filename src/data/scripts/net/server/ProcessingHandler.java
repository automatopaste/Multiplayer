package data.scripts.net.server;

import data.scripts.net.RequestData;
import data.scripts.net.data.PacketManager;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.lazywizard.console.Console;

public class ProcessingHandler extends ChannelInboundHandlerAdapter {
    public static final float TICK_RATE = 2f;
    private long initialTime;
    private final double timeU;
    private double deltaU;
    private long updateTime;

    private final PacketManager packetManager;

    public ProcessingHandler(PacketManager packetManager) {
        this.packetManager = packetManager;

        initialTime = System.nanoTime();
        timeU = 1000000000d / TICK_RATE;
        deltaU = 0;

        updateTime = initialTime;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Console.showMessage("Server channel handler added");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Console.showMessage("Server channel handler removed");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        RequestData requestData = (RequestData) msg;
        Console.showMessage("Server received request data: " + requestData.toString());
    }

    /**
     * Called once when TCP connection is active
     * @param ctx context
     * @throws Exception something
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Console.showMessage("Channel active on server");

        Console.showMessage("Sending packet");
        ChannelFuture future = ctx.writeAndFlush(packetManager.getPacket());
    }

    /**
     * Called once read is complete. Is used to wait and send next packet.
     * @param ctx context
     * @throws Exception something
     */
    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
        // keep looping until timer lets it send another packet

        long currentTime = System.nanoTime();
        while (deltaU < 1f) {
            currentTime = System.nanoTime();
            deltaU += (currentTime - initialTime) / timeU;
            initialTime = currentTime;
        }

        // time delta
        long diffTimeNanos = currentTime - updateTime;

        Console.showMessage("Sending packets at " + TICK_RATE + "Hz");

        final ChannelFuture future = ctx.writeAndFlush(packetManager.getPacket());
        ctx.fireChannelActive();

        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (future.isDone()) {
                    ctx.fireChannelReadComplete();
                }
            }
        });

        updateTime = currentTime;
        deltaU--;
    }
}
