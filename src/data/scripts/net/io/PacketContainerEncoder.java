package data.scripts.net.io;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.ArrayList;

public class PacketContainerEncoder extends MessageToByteEncoder<PacketContainer> {

    @Override
    protected void encode(ChannelHandlerContext ctx, PacketContainer msg, ByteBuf out) throws Exception {
        // encode length of buffer so completeness can be checked when reconstructed by client
        out.writeInt(msg.get().size());
        out.writeInt(msg.getTick());

        // there it goes :))))
        out.writeBytes(msg.get().toByteArray());

        new PacketDecoder().decode(ctx, out, new ArrayList<>());
    }
}
