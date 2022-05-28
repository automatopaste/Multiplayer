package data.scripts.net.io;

import data.scripts.net.data.BasePackable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PacketContainer {
    private static final int PACKET_SIZE = 1024;

    private final int tick;
    private final ByteArrayOutputStream data;

//    private final Queue<ByteBuffer> sections;

    public PacketContainer(List<BasePackable> packables, int tick, boolean flush) throws IOException {
        this.tick = tick;

        List<byte[]> entities = new ArrayList<>();
        for (BasePackable packable : packables) {
            byte[] written = packable.pack(flush);
            if (written != null && written.length > 0) entities.add(written);
        }

//        ByteBuffer buffer = ByteBuffer.allocateDirect(PACKET_SIZE);
        data = new ByteArrayOutputStream(PACKET_SIZE);

        for (byte[] entity : entities) {
            data.write(entity);
        }

//        while (!entities.isEmpty()) {
//            byte[] entity = entities.poll();
//
//            int newSize = data.size() + entity.length;
//
//            // flip and dump buffer into queue before writing new entity data
//            if (newSize > PACKET_SIZE) {
//                outputToQueue(data, buffer);
//
//                data.write(tick);
//            }
//
//            data.write(entity);
//
//            if (entities.isEmpty()) {
//                outputToQueue(data,buffer);
//            }
//        }
    }

//    private void outputToQueue(ByteArrayOutputStream stream, ByteBuffer buffer) {
//        buffer.clear();
//        buffer.put(stream.toByteArray());
//        buffer.flip();
//
//        sections.add(buffer);
//
//        stream.reset();
//    }

    public ByteArrayOutputStream get() {
        return data;
    }

//    public Queue<ByteBuffer> getSections() {
//        return sections;
//    }

    public int getTick() {
        return tick;
    }
}
