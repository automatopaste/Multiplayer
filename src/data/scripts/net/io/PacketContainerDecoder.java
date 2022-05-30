package data.scripts.net.io;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class PacketContainerDecoder extends ReplayingDecoder<Object> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int length = in.readInt();
        if (in.readableBytes() < length) return;
        //else if (in.readableBytes() > length) throw new IndexOutOfBoundsException("Readable bytes exceeds header value");

        ByteBuf frame = in.readBytes(length);
        out.add(frame);
    }
}
