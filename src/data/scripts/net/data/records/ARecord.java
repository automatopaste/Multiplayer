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

        doWrite(output);
    }

    public abstract ARecord<T> read(ByteBuf in);

    public abstract void doWrite(ByteBuffer output);

    public abstract boolean checkUpdate(T curr);

    public void forceUpdate(T curr) {
        record = curr;
    }

    public abstract int getTypeId();

    public T getRecord() {
        return record;
    }
}
