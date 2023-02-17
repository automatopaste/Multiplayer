package data.scripts.net.io.udp;

import data.scripts.net.io.Unpacked;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.net.InetSocketAddress;
import java.util.List;

public class DatagramUnpacker extends MessageToMessageDecoder<DatagramUtils.Decompressed> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, DatagramUtils.Decompressed in, List<Object> out) throws Exception {
        ByteBuf data = PooledByteBufAllocator.DEFAULT.buffer(in.data.length);
        data.writeBytes(in.data);

        try {
            Unpacked result = new Unpacked(
                    data,
                    (InetSocketAddress) channelHandlerContext.channel().remoteAddress(),
                    (InetSocketAddress) channelHandlerContext.channel().localAddress()
            );

            out.add(result);
        } catch (Throwable t) {
            t.printStackTrace();
            channelHandlerContext.flush();
        } finally {
            data.release();
        }
    }
}
