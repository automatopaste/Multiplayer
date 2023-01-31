package data.scripts.net.data.packables;

import data.scripts.net.data.records.BaseRecord;

import java.util.concurrent.TimeUnit;

public class InterpRecordLambda<T> extends RecordLambda<T> {

    private long timestamp;
    private float progressive; // 0.0 to 1.0
    private float gap;
    private T interpValue;
    private T v1;
    private T v2;
    private int tick;

    public InterpRecordLambda(BaseRecord<T> record, SourceExecute<T> sourceExecute, DestExecute<T> destExecute) {
        super(record, sourceExecute, destExecute);

        v1 = record.getValue();
        v2 = record.getValue();
        interpValue = record.getValue();
        timestamp = System.nanoTime();
        gap = 0f;
        progressive = 0f;
        tick = -1;
    }

    @Override
    public void overwrite(int tick, BaseRecord<?> delta) {
        super.overwrite(tick, delta);

        if (tick > this.tick) {
            long n = System.nanoTime();
            long diff = n - timestamp;
            timestamp = n;
            long milli = TimeUnit.MILLISECONDS.convert(diff, TimeUnit.NANOSECONDS);
            gap = milli * 0.001f;

            v2 = v1;
            v1 = (T) delta.getValue();

            progressive = 0f;
            this.tick = tick;
        }
    }

    public void interp(float amount, BasePackable packable) {
        progressive += amount;

        float linterp = progressive * gap;
        interpValue = record.linterp(linterp, v2, v1);

        destExecute.execute(interpValue, packable);
    }
}
