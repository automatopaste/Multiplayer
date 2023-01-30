package data.scripts.net.io.udp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class DatagramDecoder extends MessageToMessageDecoder<DatagramPacket> {

    @Override
    protected void decode(ChannelHandlerContext context, DatagramPacket in, List<Object> out) throws Exception {
        byte[] decompressed = DatagramUtils.read(in);

        if (decompressed.length == 0) {
            context.flush();
            return;
        }

        out.add(decompressed);
    }
}
