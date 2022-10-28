package data.scripts.net.data.packables.metadata.player;

import com.fs.starfarer.api.combat.ViewportAPI;
import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.records.Float32Record;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.Vector2f32Record;
import data.scripts.plugins.MPPlugin;
import org.lwjgl.util.vector.Vector2f;

/**
 * Sends player camera data to the server
 */
public class PlayerData extends BasePackable {

    public PlayerData(int instanceID, final ViewportAPI viewport, final MPPlugin plugin) {
        super(instanceID);

        putRecord(new Vector2f32Record(new BaseRecord.DeltaFunc<Vector2f>() {
            @Override
            public Vector2f get() {
                return viewport.getCenter();
            }
        }, PlayerIDs.CAMERA_CENTER));
        putRecord(new Float32Record(new BaseRecord.DeltaFunc<Float>() {
            @Override
            public Float get() {
                return viewport.getViewMult();
            }
        }, PlayerIDs.ZOOM));
        putRecord(new IntRecord(new BaseRecord.DeltaFunc<Integer>() {
            @Override
            public Integer get() {
                switch (plugin.getType()) {
                    case SERVER:
                        return 1;
                    case CLIENT:
                    default:
                        return 0;
                }
            }
        }, PlayerIDs.IS_HOST));
    }

    @Override
    public int getTypeID() {
        return PlayerIDs.TYPE_ID;
    }


}
