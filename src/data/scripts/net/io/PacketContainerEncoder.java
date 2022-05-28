package data.scripts.net.io;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.ByteBuffer;

public class PacketContainerEncoder extends MessageToByteEncoder<ByteBuffer> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuffer msg, ByteBuf out) throws Exception {
        // encode length of buffer so completeness can be checked when reconstructed by client
        out.writeInt(msg.limit());

        // there it goes :))))
        out.writeBytes(msg);
    }
}
