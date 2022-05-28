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

        Queue<byte[]> bytes = new LinkedList<>();
        for (BasePackable packable : packables) {
            byte[] written = packable.pack(flush);
            if (written != null && written.length > 0) bytes.add(written);
        }

        int size = 8;
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        data.write(tick);
        while (!bytes.isEmpty()) {
            byte[] byteArr = bytes.poll();

            int newSize = size + byteArr.length;

            if (newSize > PACKET_SIZE) {
                sections.add(ByteBuffer.wrap(data.toByteArray()));

                data.reset();
                data.write(tick);
                size = 8;
            }

            data.write(byteArr);
            size += byteArr.length;

            if (bytes.isEmpty()) {
                ByteBuffer buffer = ByteBuffer.wrap(data.toByteArray());
                buffer.flip();

                sections.add(buffer);
            }
        }
    }

    public Queue<ByteBuffer> getSections() {
        return sections;
    }

    public int getCurrentSectionLength() {
        try {
            return getSections().peek().limit();
        } catch (NullPointerException e) {
            return 0;
        }
    }

    public int getTick() {
        return tick;
    }
}
