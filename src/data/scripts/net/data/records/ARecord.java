package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public abstract class ARecord<T> {
    protected T record;

    public ARecord(T record) {
        this.record = record;
    }

    public void write(ByteBuffer output, int uniqueId) {
        output.putInt(getTypeId());
        output.putInt(uniqueId);
    }

    public abstract ARecord<T> read(ByteBuf in);

    public abstract boolean checkUpdate(T curr);

    protected abstract void doUpdate(T delta);

    public abstract int getTypeId();

    public T getRecord() {
        return record;
    }
}
