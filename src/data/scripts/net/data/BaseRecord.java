package data.scripts.net.data;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public abstract class BaseRecord<T> {
    protected T record;

    public BaseRecord(T record) {
        this.record = record;
    }

    public void write(ByteBuffer output, int uniqueId) {
        output.putInt(getTypeId());
        output.putInt(uniqueId);

        if (record == null) {
            throw new NullPointerException(this.getClass().getSimpleName() + " had null value");
        }

        doWrite(output);
    }

    public abstract BaseRecord<T> read(ByteBuf in);

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
