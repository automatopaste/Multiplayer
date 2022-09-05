package data.scripts.net.io.udp;

import data.scripts.net.io.UnpackAlgorithm;
import data.scripts.net.io.Unpacked;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.net.InetSocketAddress;
import java.util.List;

public class DatagramUnpacker extends MessageToMessageDecoder<DatagramPacket> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, DatagramPacket datagram, List<Object> out) throws Exception {
        ByteBuf in = datagram.content();

        Unpacked result = UnpackAlgorithm.unpack(
                in,
                (InetSocketAddress) channelHandlerContext.channel().remoteAddress(),
                (InetSocketAddress) channelHandlerContext.channel().localAddress()
        );

        //in.release();
        out.add(result);
    }
}
