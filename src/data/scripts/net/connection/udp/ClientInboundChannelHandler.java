package data.scripts.net.connection.udp;

import data.scripts.net.io.Unpacked;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientInboundChannelHandler extends SimpleChannelInboundHandler<Unpacked> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Unpacked unpacked) throws Exception {

    }
}
