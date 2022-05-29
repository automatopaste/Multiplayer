package data.scripts.net.connection.udp;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.nio.charset.StandardCharsets;

public class OutboundHandler extends ChannelOutboundHandlerAdapter {
    //private final ServerConnectionWrapper connection;

    public OutboundHandler() {
        //this.connection = connection;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//        DataDuplex duplex = connection.getDuplex();
//        int tick = (connection.isRequestLoad()) ? -1 : (Integer) msg;
//        PacketContainer container = duplex.getPacket(tick);
//
//        ByteBuf data = container.get();
//
//        // wait for async io to be finished before releasing buffer
//        try {
//            ctx.writeAndFlush(data).sync();
//        } finally {
//            data.release();
//        }

        ctx.writeAndFlush(Unpooled.wrappedBuffer("yeah".getBytes(StandardCharsets.UTF_8)));
    }
}
