package data.scripts.net.io.udp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class DatagramEncoder extends MessageToMessageEncoder<DatagramPacket> {

    @Override
    protected void encode(ChannelHandlerContext context, DatagramPacket in, List<Object> out) throws Exception {
        ByteBuf buf = in.content();
        int length = buf.readableBytes();

        ByteBuf content = PooledByteBufAllocator.DEFAULT.buffer();
        content.writeInt(length);
        content.writeBytes(buf.readBytes(length));

        out.add(new DatagramPacket(content, in.recipient()));
        buf.release();
    }
}
