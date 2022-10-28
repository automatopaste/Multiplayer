package data.scripts.net.data.packables.metadata.player;

import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.packables.DestPackable;
import data.scripts.net.data.records.Float32Record;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.Vector2f32Record;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class PlayerDest extends DestPackable {

    public PlayerDest(int instanceID, Map<Integer, BaseRecord<?>> records) {
        super(instanceID, records);
    }

    @Override
    protected void initDefaultRecords() {
        putRecord(Vector2f32Record.getDefault(PlayerIDs.CAMERA_CENTER));
        putRecord(Float32Record.getDefault(PlayerIDs.ZOOM));
        putRecord(IntRecord.getDefault(PlayerIDs.IS_HOST));
    }

    @Override
    public void update(float amount) {

    }

    @Override
    public void init(MPPlugin plugin) {
    }



    @Override
    public void delete() {

    }

    @Override
    public int getTypeID() {
        return PlayerIDs.TYPE_ID;
    }

    public static PlayerDest getDefault() {
        return new PlayerDest(-1, new HashMap<Integer, BaseRecord<?>>());
    }
}
