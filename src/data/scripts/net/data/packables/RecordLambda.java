package data.scripts.net.data.packables;

import data.scripts.net.data.records.BaseRecord;

public class RecordLambda<T> {

    public final BaseRecord<T> record;
    public final SourceLambda<T> sourceLambda;
    public final DestExecute<T> destExecute;

    public RecordLambda(BaseRecord<T> record, SourceLambda<T> sourceLambda, DestExecute<T> destExecute) {
        this.record = record;
        this.sourceLambda = sourceLambda;
        this.destExecute = destExecute;
    }

    public void sourceUpdate() {
        record.sourceUpdate(sourceLambda);
    }

    public void overwrite(BaseRecord<?> delta) {
        record.overwrite(delta.getValue());
    }

    public void execute(BasePackable packable) {
        destExecute.execute(record, packable);
    }
}
