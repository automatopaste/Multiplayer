package data.scripts.net.data.records;

import data.scripts.net.data.packables.SourceExecute;
import io.netty.buffer.ByteBuf;

public abstract class DataRecord<T> {
    protected T value;
    private String debug = "default record description";

    public DataRecord(T value) {
        this.value = value;
    }

    /**
     * Executes the source update lambda
     * @param sourceExecute the lambda script
     * @return true if the data has updated
     */
    public boolean sourceExecute(SourceExecute<T> sourceExecute) {
        T t = sourceExecute.get();
        boolean isUpdated;
        try {
             isUpdated = checkUpdate(t);
        } catch (NullPointerException n) {
            throw new NullPointerException("Null value in record delta check: " + debug);
        }

        value = t;
        return isUpdated;
    }

    /**
     * Get raw data without writing IDs
     * @param dest buffer to write to
     */
    public abstract void write(ByteBuf dest);

    public abstract T read(ByteBuf in);

    public T linterp(float p, T v1, T v2) {
        return value;
    }

    public abstract byte getTypeId();

    /**
     * Return true if updated
     * @param delta incoming delta
     * @return result
     */
    protected abstract boolean checkUpdate(T delta);

    public T getValue() {
        return value;
    }

    /**
     * Overwrite the stored record with new data
     * @param delta incoming delta
     */
    public void overwrite(Object delta) {
        value = (T) delta;
    }

    public DataRecord<T> setDebugText(String debug) {
        this.debug = debug;
        return this;
    }

    public String getDebugText() {
        return debug;
    }

    @Override
    public String toString() {
        return  "value=" + value +
                ", debug='" + debug + '\'' +
                '}';
    }
}
