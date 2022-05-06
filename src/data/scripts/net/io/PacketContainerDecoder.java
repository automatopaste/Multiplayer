package data.scripts.net.io;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class PacketContainerDecoder extends ReplayingDecoder<DecodeState> {
    public PacketContainerDecoder() {
        super(DecodeState.READ_LENGTH);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int length = in.readInt();
        out.add(in.readBytes(length));
    }
}
