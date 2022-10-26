package data.scripts.net.io.tcp;

import data.scripts.net.io.MessageContainer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageContainerEncoder extends MessageToByteEncoder<MessageContainer> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageContainer msg, ByteBuf out) throws Exception {
        // encode length of buffer so completeness can be checked when reconstructed
        ByteBuf data = msg.get();
        int length = data.readableBytes();

        out.writeInt(length);
        out.writeBytes(data);
    }
}
