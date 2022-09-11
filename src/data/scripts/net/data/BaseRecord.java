package data.scripts.net.data;

import data.scripts.net.io.ByteArrayReader;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public abstract class BaseRecord<T> {
    protected T record;
    protected boolean undefined = false;

    public BaseRecord(T record) {
        this.record = record;
    }

    public void write(ByteBuffer output, int uniqueId) {
        output.putInt(getTypeId());
        output.putInt(uniqueId);

        doWrite(output);
    }

    public abstract BaseRecord<T> read(ByteBuf in);

    public abstract BaseRecord<T> readArray(ByteArrayReader reader);

    public abstract void doWrite(ByteBuffer output);

    public boolean checkUpdate(T curr) {
        undefined = false;
        return check(curr);
    }

    protected abstract boolean check(T curr);

    public void forceUpdate(T curr) {
        record = getCopy(curr);
        undefined = false;
    }

    protected abstract T getCopy(T curr);

    public abstract int getTypeId();

    public T getRecord() {
        return record;
    }

    public boolean isDefined() {
        return !undefined;
    }

    public BaseRecord<T> setUndefined(boolean undefined) {
        this.undefined = undefined;
        return this;
    }
}
