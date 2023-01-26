package data.scripts.net.io.udp;

import data.scripts.net.io.CompressionUtils;
import data.scripts.net.io.MessageContainer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

public class DatagramUtils {

    public static SizeData write(Channel channel, MessageContainer message, InetSocketAddress dest, int connectionID) throws InterruptedException {
        ByteBuf buf = message.getData();
        if (buf.readableBytes() <= 4) {
            channel.flush();
            return null;
        }

        SizeData sizeData = new SizeData();

        byte[] bytes = new byte[buf.readableBytes()];
        if (bytes.length > Short.MAX_VALUE) throw new IndexOutOfBoundsException();
        sizeData.size = (short) bytes.length;
        buf.readBytes(bytes);
        byte[] compressed = CompressionUtils.deflate(bytes);
        sizeData.sizeCompressed = (short) compressed.length;

        ByteBuf out = PooledByteBufAllocator.DEFAULT.buffer();

        out.writeShort(sizeData.size);
        out.writeShort(sizeData.sizeCompressed);
        out.writeBytes(compressed);

        channel.writeAndFlush(new DatagramPacket(out, dest)).sync();
        return sizeData;
    }

    public static class SizeData {
        public short size;
        public short sizeCompressed;
    }
}
