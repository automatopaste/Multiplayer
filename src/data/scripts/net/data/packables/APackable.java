package data.scripts.net.data.packables;

import data.scripts.net.data.records.ARecord;

import java.nio.ByteBuffer;
import java.util.Map;

public abstract class APackable {
    protected final ByteBuffer packer;

    private final int instanceID;

    // used to transfer complete snapshot instead of delta
    protected boolean flush = true;

    public APackable(int instanceID) {
        this.instanceID = instanceID;

        packer = ByteBuffer.allocate(1024);
    }

    public byte[] pack() {
        packer.clear();

        // so packer type can be identified
        packer.putInt(getTypeId());
        // so packer instance can be identified
        packer.putInt(getInstanceID());

        if (flush) {
            doFlushWrite();
        } else {
            if (!write()) return null;
        }

        packer.flip();
        byte[] out = new byte[packer.remaining()];
        packer.get(out);

        return out;
    }

    public int getInstanceID() {
        return instanceID;
    }

    public void flush() {
        flush = true;
    }

    private void doFlushWrite() {
        flushWrite();
        flush = false;
    }

    protected abstract void flushWrite();

    public abstract void updateFromDelta(APackable delta);

    protected abstract boolean write();

    public abstract int getTypeId();

    public abstract APackable unpack(int instanceID, Map<Integer, ARecord<?>> records);
}
