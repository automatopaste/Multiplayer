package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;

public abstract class BaseRecord<T> {
    protected DeltaFunc<T> func;
    protected T value;
    public final byte uniqueID;

    public BaseRecord(T value, byte uniqueID) {
        this.value = value;
        this.uniqueID = uniqueID;
    }

    public BaseRecord(DeltaFunc<T> func, byte uniqueID) {
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
            dest.writeByte(getTypeId());
            dest.writeByte(uniqueID);

            write(dest);
        }
    }

    /**
     * Get raw data without writing base IDs
     * @param dest buffer to write to
     */
    public abstract void write(ByteBuf dest);

    public abstract BaseRecord<T> read(ByteBuf in, byte uniqueID);
    public abstract boolean check();

    public abstract byte getTypeId();

    public T getValue() {
        return value;
    }

    public interface DeltaFunc<T> {
        T get();
    }
}
