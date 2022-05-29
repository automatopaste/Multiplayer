package data.scripts.net.connection.udp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.StandardCharsets;

public class ClientInboundChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {
//    private final ClientConnectionWrapper connection;


    public ClientInboundChannelHandler() {
//        this.connection = connection;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        String out = in.readCharSequence(4, StandardCharsets.UTF_8).toString();

        int i = 0;

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
}
