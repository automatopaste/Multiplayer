package data.scripts.net.io.udp;

import data.scripts.net.io.CompressionUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class DatagramDecoder extends MessageToMessageDecoder<DatagramPacket> {

    @Override
    protected void decode(ChannelHandlerContext context, DatagramPacket in, List<Object> out) throws Exception {
        ByteBuf content = in.content();
        int size = content.readInt();

        byte[] bytes = new byte[size];
        content.readBytes(bytes, 0, size);

        byte[] compressed = CompressionUtils.inflate(bytes, size);

        out.add(bytes);
    }
}
