package data.scripts.net.io;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ByteArrayOutputStream;

public class PacketContainerEncoder extends MessageToByteEncoder<ByteArrayOutputStream> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteArrayOutputStream msg, ByteBuf out) throws Exception {
        // encode length of buffer so completeness can be checked when reconstructed by client
        out.writeInt(msg.size());

        // there it goes :))))
        out.writeBytes(msg.toByteArray());
    }
}
