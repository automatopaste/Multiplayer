package data.scripts.net.connection.udp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class ClientDecoder extends ReplayingDecoder<Object> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        int length = in.readInt();
        ByteBuf frame = in.readBytes(length);
        out.add(frame);
    }
}
