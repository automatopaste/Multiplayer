package data.scripts.net.data.packables;

import data.scripts.net.data.Packable;

import java.nio.ByteBuffer;

public abstract class APackable implements Packable {
    protected final ByteBuffer packer;

    public APackable() {
        packer = ByteBuffer.allocate(1024);
    }

    public byte[] pack() {
        packer.clear();

        // so packer type can be identified
        packer.putInt(getTypeId());

        update();

        packer.flip();
        byte[] out = new byte[packer.remaining()];
        packer.get(out);

        return out;
    }

    abstract void update();
}
