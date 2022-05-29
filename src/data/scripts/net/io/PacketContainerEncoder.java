package data.scripts.net.io;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.ArrayList;

public class PacketContainerEncoder extends MessageToByteEncoder<PacketContainer> {

    @Override
    protected void encode(ChannelHandlerContext ctx, PacketContainer msg, ByteBuf out) throws Exception {
        // encode length of buffer so completeness can be checked when reconstructed by client
        ByteBuf data = msg.get();

        out.writeInt(data.readableBytes());

        int tick = msg.getTick();
        out.writeInt(tick);

        // there it goes :))))
        out.writeBytes(data);

        // for debug
        new PacketDecoder().decode(ctx, out, new ArrayList<>());
    }
}
