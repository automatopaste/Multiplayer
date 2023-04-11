package data.scripts.net.data.packables.metadata;

import data.scripts.net.data.packables.DestExecute;
import data.scripts.net.data.packables.EntityData;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.records.ShortRecord;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.plugins.MPPlugin;

public class ServerPlayerData extends EntityData {

    public static byte TYPE_ID;

    private short activeID;

    public ServerPlayerData(short instanceID) {
        super(instanceID);

        addRecord(new RecordLambda<>(
                ShortRecord.getDefault().setDebugText("active ship id"),
                new SourceExecute<Short>() {
                    @Override
                    public Short get() {
                        return activeID;
                    }
                },
                new DestExecute<Short>() {
                    @Override
                    public void execute(Short value, EntityData packable) {
                        setActiveID(value);
                    }
                }
        ));
    }

    @Override
    public byte getTypeID() {
        return TYPE_ID;
    }

    @Override
    public void update(float amount, BaseEntityManager manager, MPPlugin plugin) {

    }

    @Override
    public void init(MPPlugin plugin, InboundEntityManager manager) {

    }

    @Override
    public void delete() {

    }

    public short getActiveID() {
        return activeID;
    }

    public void setActiveID(short activeID) {
        this.activeID = activeID;
    }
}
