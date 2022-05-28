package data.scripts.net.io;

import data.scripts.net.data.BasePackable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class PacketContainer {
    private static final int PACKET_SIZE = 1536;

    private final int tick;

    private final Queue<ByteBuffer> sections;

    public PacketContainer(List<BasePackable> packables, int tick, boolean flush) throws IOException {
        this.tick = tick;

        sections = new LinkedList<>();

        Queue<byte[]> entities = new LinkedList<>();
        for (BasePackable packable : packables) {
            byte[] written = packable.pack(flush);
            if (written != null && written.length > 0) entities.add(written);
        }

        int size = Integer.SIZE / Byte.SIZE;
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        data.write(tick);
        while (!entities.isEmpty()) {
            byte[] entity = entities.poll();

            int newSize = size + entity.length;

            if (newSize > PACKET_SIZE) {
                sections.add(ByteBuffer.wrap(data.toByteArray()));

                data.reset();
                data.write(tick);
                size = Integer.SIZE / Byte.SIZE;
            }

            data.write(entity);
            size += entity.length;

            if (entities.isEmpty()) {
                ByteBuffer buffer = ByteBuffer.wrap(data.toByteArray());
                buffer.flip();

                sections.add(buffer);
            }
        }
    }

    public Queue<ByteBuffer> getSections() {
        return sections;
    }

    public int getTick() {
        return tick;
    }
}
