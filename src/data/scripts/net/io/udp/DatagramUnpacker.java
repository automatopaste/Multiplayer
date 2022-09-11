package data.scripts.net.io.udp;

import data.scripts.net.io.UnpackAlgorithm;
import data.scripts.net.io.Unpacked;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.net.InetSocketAddress;
import java.util.List;

public class DatagramUnpacker extends MessageToMessageDecoder<byte[]> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, byte[] in, List<Object> out) throws Exception {
        Unpacked result = UnpackAlgorithm.unpack(
                in,
                (InetSocketAddress) channelHandlerContext.channel().remoteAddress(),
                (InetSocketAddress) channelHandlerContext.channel().localAddress()
        );

        out.add(result);
    }
}
