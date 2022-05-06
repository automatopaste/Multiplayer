package data.scripts.net.io;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketContainerEncoder  extends MessageToByteEncoder<PacketContainer> {

    @Override
    protected void encode(ChannelHandlerContext ctx, PacketContainer msg, ByteBuf out) throws Exception {
        // encode length of buffer so completeness can be checked when reconstructed by client
        // account for <num entities, tick> integers
//        out.writeInt(msg.getLength() + (2 * (Integer.SIZE / Byte.SIZE)));
        out.writeInt(msg.getLength());

        // there it goes :))))
        out.writeBytes(msg.getData());
    }
}
