package data.scripts.net.io;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class PacketContainerDecoder extends ReplayingDecoder<DecodeState> {
    private int length;

    public PacketContainerDecoder() {
        super(DecodeState.READ_LENGTH);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        switch (state()) {
            case READ_LENGTH:
                length = in.readInt();
                checkpoint(DecodeState.READ_CONTENT);
                break;
            case READ_CONTENT:
                ByteBuf frame = in.readBytes(length);
                checkpoint(DecodeState.READ_LENGTH);
                out.add(frame);
                break;
            default:
                throw new Error("bugger");
        }
    }
}
