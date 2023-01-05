package data.scripts.net.data.records;

import data.scripts.net.data.packables.SourceExecute;
import io.netty.buffer.ByteBuf;

public abstract class BaseRecord<T> {
    protected T value;
    private boolean isUpdated = true;

    public BaseRecord(T value) {
        this.value = value;
    }

    public void sourceExecute(SourceExecute<T> sourceExecute) {
        T t = sourceExecute.get();
        isUpdated = checkNotEqual(t);
        value = t;
    }

    /**
     * Get raw data without writing IDs
     * @param dest buffer to write to
     */
    public abstract void write(ByteBuf dest);

    public abstract BaseRecord<T> read(ByteBuf in);

    public abstract byte getTypeId();

    /**
     * Return true if updated
     * @param delta incoming delta
     * @return result
     */
    protected abstract boolean checkNotEqual(T delta);

    public T getValue() {
        return value;
    }

    public void overwrite(Object delta) {
        T t = (T) delta;
        isUpdated = checkNotEqual(t);
        value = t;
    }

    public boolean isUpdated() {
        return isUpdated;
    }
}
