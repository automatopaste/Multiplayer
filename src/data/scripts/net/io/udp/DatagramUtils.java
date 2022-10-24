package data.scripts.net.io.udp;

import data.scripts.net.io.CompressionUtils;
import data.scripts.net.io.PacketContainer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

public class DatagramUtils {

    public static SizeData write(Channel channel, PacketContainer message) throws InterruptedException {
        ByteBuf buf = message.get();
        if (buf.readableBytes() <= 4) {
            channel.flush();
            return null;
        }

        SizeData sizeData = new SizeData();

        sizeData.size = message.getBufSize();

        byte[] bytes = new byte[buf.readableBytes()];
        int length = bytes.length;
        buf.readBytes(bytes);
        byte[] compressed = CompressionUtils.deflate(bytes);
        sizeData.sizeCompressed = compressed.length;

        ByteBuf out = PooledByteBufAllocator.DEFAULT.buffer();
        out.writeInt(length);

        out.writeBytes(bytes);
//        out.writeBytes(compressed);

        channel.writeAndFlush(new DatagramPacket(out, message.getDest())).sync();
        return sizeData;
    }

    public static class SizeData {
        public int size;
        public int sizeCompressed;
    }
}
