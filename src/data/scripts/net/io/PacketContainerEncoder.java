package data.scripts.net.io;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketContainerEncoder extends MessageToByteEncoder<PacketContainer> {

    @Override
    protected void encode(ChannelHandlerContext ctx, PacketContainer msg, ByteBuf out) throws Exception {
        // encode length of buffer so completeness can be checked when reconstructed by client.
        ByteBuf data = msg.get();
        int length = data.readableBytes();

        int tick = msg.getTick();
        length += Integer.SIZE / Byte.SIZE;

        out.writeInt(length);
        out.writeInt(tick);
        out.writeBytes(data);
    }
}
