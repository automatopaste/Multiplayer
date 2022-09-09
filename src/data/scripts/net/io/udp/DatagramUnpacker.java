package data.scripts.net.io.udp;

import data.scripts.net.io.UnpackAlgorithm;
import data.scripts.net.io.Unpacked;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.net.InetSocketAddress;
import java.util.List;

public class DatagramUnpacker extends MessageToMessageDecoder<DatagramUnpacker.DatagramWrapper> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, DatagramWrapper in, List<Object> out) throws Exception {
        Unpacked result = UnpackAlgorithm.unpack(
                in.getBuf(),
                (InetSocketAddress) channelHandlerContext.channel().remoteAddress(),
                (InetSocketAddress) channelHandlerContext.channel().localAddress()
        );

        out.add(result);
        in.getBuf().release();
    }

    public static class DatagramWrapper {
        private final ByteBuf buf;

        public DatagramWrapper(ByteBuf buf) {
            this.buf = buf;
        }

        public ByteBuf getBuf() {
            return buf;
        }
    }
}
