package data.scripts.net.io.udp;

import data.scripts.net.io.UnpackAlgorithm;
import data.scripts.net.io.Unpacked;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.net.InetSocketAddress;
import java.util.List;

public class DatagramUnpacker extends MessageToMessageDecoder<DatagramUnpacker.DatagramBytes> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, DatagramBytes in, List<Object> out) throws Exception {
        Unpacked result = UnpackAlgorithm.unpack(
                in.getBytes(),
                (InetSocketAddress) channelHandlerContext.channel().remoteAddress(),
                (InetSocketAddress) channelHandlerContext.channel().localAddress(),
                in.getConnectionID()
        );

        out.add(result);
    }

    public static class DatagramBytes {
        private final byte[] bytes;
        private final int connectionID;

        public DatagramBytes(byte[] bytes, int connectionID) {
            this.bytes = bytes;
            this.connectionID = connectionID;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public int getConnectionID() {
            return connectionID;
        }
    }
}
