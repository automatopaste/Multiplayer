package data.scripts.net.io.udp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;
import java.util.zip.Inflater;

public class DatagramDecoder extends MessageToMessageDecoder<DatagramPacket> {

    @Override
    protected void decode(ChannelHandlerContext context, DatagramPacket in, List<Object> out) throws Exception {
        ByteBuf content = in.content();
        int size = content.readInt();

        byte[] bytes = new byte[size];
        content.readBytes(bytes, 0, size);

        Inflater decompressor = new Inflater();
        decompressor.setInput(bytes);
        byte[] decompressed = new byte[size];
        int length = decompressor.inflate(decompressed);
        decompressor.end();

        out.add(decompressed);
    }
}
