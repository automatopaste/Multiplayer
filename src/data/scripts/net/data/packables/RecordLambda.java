package data.scripts.net.data.packables;

import data.scripts.net.data.records.BaseRecord;

public class RecordLambda<T> {

    public final BaseRecord<T> record;
    public final SourceExecute<T> sourceExecute;
    public final DestExecute<T> destExecute;

    public RecordLambda(BaseRecord<T> record, SourceExecute<T> sourceExecute, DestExecute<T> destExecute) {
        this.record = record;
        this.sourceExecute = sourceExecute;
        this.destExecute = destExecute;
    }

    public void sourceExecute() {
        record.sourceExecute(sourceExecute);
    }

    public void overwrite(BaseRecord<?> delta) {
        record.overwrite(delta.getValue());
    }

    public void destExecute(BasePackable packable) {
        destExecute.execute(record, packable);
    }
}
