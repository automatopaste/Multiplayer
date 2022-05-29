package data.scripts.net.connection.udp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

public class InboundHandler extends SimpleChannelInboundHandler<DatagramPacket> {
//    private final ClientConnectionWrapper connection;

    public InboundHandler() {
//        this.connection = connection;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket in) throws Exception {
        ByteBuf buf = in.content();
        String out = buf.toString(CharsetUtil.UTF_8);
        System.out.println(out);

        ctx.write(new DatagramPacket(Unpooled.copiedBuffer(out, CharsetUtil.UTF_8), in.sender()));

//        int serverTick = unpacked.getTick();
//        Console.showMessage("Received unpacked with tick: " + serverTick);
//
//        connection.getDuplex().setCurrTick(serverTick);
//        Map<Integer, BasePackable> entities = unpacked.getUnpacked();
//
//        // if getting -1 value tick from server, server is sending preload data
//        connection.setLoading(serverTick == -1);
//        connection.getDuplex().updateInbound(entities);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println(cause.getMessage());
        ctx.close();
    }
}
