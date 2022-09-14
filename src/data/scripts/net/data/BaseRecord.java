package data.scripts.net.data;

import data.scripts.net.io.ByteArrayReader;
import io.netty.buffer.ByteBuf;

public abstract class BaseRecord<T> {
    protected DeltaFunc<T> func;
    protected T value;
    protected final int uniqueID;

    public BaseRecord(T value, int uniqueID) {
        this.value = value;
        this.uniqueID = uniqueID;
    }

    public BaseRecord(DeltaFunc<T> func, int uniqueID) {
        this.func = func;
        this.uniqueID = uniqueID;
        value = func.get();
    }

    public void updateFromDelta(BaseRecord<?> delta) {
        this.value = (T) delta.value;
    }

    public void write(boolean force, ByteBuf dest) {
        boolean isUpdate = check();
        if (value != null && (force || isUpdate)) {
            dest.writeInt(getTypeId());
            dest.writeInt(uniqueID);

            get(dest);
        }
    }

    /**
     * Get raw data without writing base IDs
     * @param dest buffer to write to
     */
    public abstract void get(ByteBuf dest);

    public abstract BaseRecord<T> read(ByteBuf in, int uniqueID);
    public abstract BaseRecord<T> read(ByteArrayReader in, int uniqueID);

    public abstract boolean check();

    public abstract int getTypeId();

    public T getValue() {
        return value;
    }

    public interface DeltaFunc<T> {
        T get();
    }
}
