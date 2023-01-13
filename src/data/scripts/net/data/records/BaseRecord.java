package data.scripts.net.data.records;

import data.scripts.net.data.packables.SourceExecute;
import io.netty.buffer.ByteBuf;

public abstract class BaseRecord<T> {
    protected T value;

    public BaseRecord(T value) {
        this.value = value;
    }

    /**
     * Executes the source update lambda
     * @param sourceExecute the lambda script
     * @return true if the data has updated
     */
    public boolean sourceExecute(SourceExecute<T> sourceExecute) {
        T t = sourceExecute.get();
        boolean isUpdated = checkNotEqual(t);
        value = t;
        return isUpdated;
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

    /**
     * Overwrite the stored record with new data
     * @param delta incoming delta
     */
    public void overwrite(Object delta) {
        value = (T) delta;}
}
