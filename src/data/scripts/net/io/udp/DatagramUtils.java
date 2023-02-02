package data.scripts.net.io.udp;

import data.scripts.net.io.MessageContainer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.util.zip.DataFormatException;

public class DatagramUtils {

    public static SizeData write(Channel channel, MessageContainer message, InetSocketAddress dest, int connectionID) throws InterruptedException {
        ByteBuf buf = message.getData();
        if (buf.readableBytes() <= 4) {
            channel.flush();
            return null;
        }

        SizeData sizeData = new SizeData();

        byte[] bytes = new byte[buf.readableBytes()];
        //if (bytes.length > Short.MAX_VALUE) throw new IndexOutOfBoundsException();
        sizeData.size = bytes.length;
        buf.readBytes(bytes);
//        byte[] compressed = CompressionUtils.deflate(bytes);
//        sizeData.sizeCompressed = (short) compressed.length;

        ByteBuf out = PooledByteBufAllocator.DEFAULT.buffer(bytes.length + 8);

        out.writeInt(sizeData.size);
//        out.writeInt(sizeData.sizeCompressed);
//        out.writeBytes(compressed);

        out.writeBytes(bytes);

        channel.writeAndFlush(new DatagramPacket(out, dest)).sync();
        return sizeData;
    }

    public static byte[] read(DatagramPacket in) throws DataFormatException {
        ByteBuf content = in.content();
        int size = content.readInt();
//        int sizeCompressed = content.readInt();

        if (size == 0) return new byte[0];

//        byte[] bytes = new byte[sizeCompressed];
//        content.readBytes(bytes);

        byte[] bytes = new byte[size];
        content.readBytes(bytes);

//        return CompressionUtils.inflate(bytes, size, sizeCompressed);
        return bytes;
    }

    public static class SizeData {
        public int size;
        public int sizeCompressed;
    }
}
