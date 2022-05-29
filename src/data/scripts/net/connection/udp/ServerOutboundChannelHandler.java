package data.scripts.net.connection.udp;

import data.scripts.net.connection.DataDuplex;
import data.scripts.net.connection.server.ServerConnectionWrapper;
import data.scripts.net.io.PacketContainer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class ServerOutboundChannelHandler extends ChannelOutboundHandlerAdapter {
    private final ServerConnectionWrapper connection;

    public ServerOutboundChannelHandler(ServerConnectionWrapper connection) {
        this.connection = connection;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        DataDuplex duplex = connection.getDuplex();
        int tick = (connection.isRequestLoad()) ? -1 : (Integer) msg;
        PacketContainer container = duplex.getPacket(tick);

        ByteBuf data = container.get();

        // wait for async io to be finished before releasing buffer
        try {
            ctx.writeAndFlush(data).sync();
        } finally {
            data.release();
        }
    }
}
