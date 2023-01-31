package data.scripts.net.data.packables;

import data.scripts.net.data.records.BaseRecord;

public class RecordLambda<T> {

    public final BaseRecord<T> record;
    public final SourceExecute<T> sourceExecute;
    public final DestExecute<T> destExecute;
    protected int tick;

    public RecordLambda(BaseRecord<T> record, SourceExecute<T> sourceExecute, DestExecute<T> destExecute) {
        this.record = record;
        this.sourceExecute = sourceExecute;
        this.destExecute = destExecute;
    }

    public boolean sourceExecute() {
        return record.sourceExecute(sourceExecute);
    }

    public void overwrite(int tick, BaseRecord<?> delta) {
        if (tick > this.tick) {
            record.overwrite(delta.getValue());
            this.tick = tick;
        }
    }

    public void destExecute(BasePackable packable) {
        destExecute.execute(record.getValue(), packable);
    }
}
