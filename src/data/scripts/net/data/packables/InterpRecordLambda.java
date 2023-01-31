package data.scripts.net.data.packables;

import data.scripts.net.data.records.BaseRecord;

import java.util.concurrent.TimeUnit;

public class InterpRecordLambda<T> extends RecordLambda<T> {

    private long timestamp;
    private float progressive; // 0.0 to 1.0
    private float gapInv;
    private T interpValue;
    private T v1;
    private T v2;

    public InterpRecordLambda(BaseRecord<T> record, SourceExecute<T> sourceExecute, DestExecute<T> destExecute) {
        super(record, sourceExecute, destExecute);

        v1 = record.getValue();
        v2 = record.getValue();
        interpValue = record.getValue();
        timestamp = System.nanoTime();
        gapInv = 0f;
        progressive = 0f;
    }

    @Override
    public void destExecute(BasePackable packable) {
        destExecute.execute(interpValue, packable);
    }

    @Override
    public void overwrite(BaseRecord<?> delta) {
        super.overwrite(delta);

        long n = System.nanoTime();
        long diff = n - timestamp;
        timestamp = n;
        long milli = TimeUnit.MILLISECONDS.convert(diff, TimeUnit.NANOSECONDS);
        gapInv = 1000f / milli;

        v1 = (T) delta.getValue();
        v2 = v1;

        progressive = 0f;
    }

    public void interp(float amount) {
        progressive += amount;

        float linterp = progressive * gapInv;
        interpValue = record.linterp(linterp, v1, v2);
    }
}
