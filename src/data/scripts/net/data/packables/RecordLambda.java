package data.scripts.net.data.packables;

import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.net.data.records.DataRecord;

public class RecordLambda<T> {

    public final DataRecord<T> record;
    public final SourceExecute<T> sourceExecute;
    public final DestExecute<T> destExecute;
    private IntervalUtil interval = null;

    public RecordLambda(DataRecord<T> record, SourceExecute<T> sourceExecute, DestExecute<T> destExecute) {
        this.record = record;
        this.sourceExecute = sourceExecute;
        this.destExecute = destExecute;
    }

    public boolean sourceExecute(float amount) {
        boolean update = record.sourceExecute(sourceExecute);

        if (interval == null) {
            return update;
        } else {
            interval.advance(amount);

            if (interval.intervalElapsed()) {
                return update;
            }
        }

        return false;
    }

    public void overwrite(int tick, Object delta) {
        record.overwrite(delta);
    }

    public void destExecute(EntityData packable) {
        destExecute.execute(record.getValue(), packable);
    }

    public RecordLambda<T> setRate(float interval) {
        this.interval = new IntervalUtil(interval, interval);
        return this;
    }
}
