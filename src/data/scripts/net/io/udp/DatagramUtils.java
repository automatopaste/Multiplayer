package data.scripts.net.io.udp;

import data.scripts.net.io.MessageContainer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.zip.DataFormatException;

public class DatagramUtils {

    public static SizeData write(Channel channel, MessageContainer message, InetSocketAddress dest, byte connectionID) throws InterruptedException {
        ByteBuf buf = message.getData();
        try {
            if (buf.readableBytes() <= 4) {
                channel.flush();
                return null;
            }

            SizeData sizeData = new SizeData();

            byte[] bytes = new byte[buf.readableBytes()];
            sizeData.size = bytes.length;
            buf.readBytes(bytes);

            ByteBuf out = PooledByteBufAllocator.DEFAULT.buffer(bytes.length + 5);

            Date now = new Date();
            out.writeLong(now.getTime());

            out.writeInt(sizeData.size);

            out.writeBytes(bytes);

            channel.writeAndFlush(new DatagramPacket(out, dest)).sync();

            return sizeData;
        } finally {
            buf.release();
        }
    }

    public static Decompressed read(DatagramPacket in) throws DataFormatException {
        try {
            ByteBuf content = in.content();

            long timestamp = content.readLong();
            int size = content.readInt();

            if (size == 0) return new Decompressed();

            byte[] bytes = new byte[size];
            content.readBytes(bytes);

            Decompressed out = new Decompressed();
            out.data = bytes;
            out.timestamp = timestamp;

            return out;
        } finally {
            in.release();
        }

    }

    public static class SizeData {
        public int size;
        public int sizeCompressed;
    }

    public static class Decompressed {
        public byte[] data;
        public long timestamp;
    }
}
