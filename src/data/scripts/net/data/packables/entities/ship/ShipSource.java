package data.scripts.net.data.packables.entities.ship;

import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.SourcePackable;
import data.scripts.net.data.records.FloatRecord;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.net.data.records.Vector2fRecord;
import org.lwjgl.util.vector.Vector2f;

public class ShipSource extends SourcePackable {

    public ShipSource(int instanceID, final ShipAPI ship) {
        super(instanceID);

        putRecord(new StringRecord(new BaseRecord.DeltaFunc<String>() {
            @Override
            public String get() {
                return ship.getFleetMemberId();
            }
        }, ShipIDs.SHIP_ID));
        putRecord(new Vector2fRecord(new BaseRecord.DeltaFunc<Vector2f>() {
            @Override
            public Vector2f get() {
                return ship.getLocation();
            }
        }, ShipIDs.SHIP_LOC).setUseDecimalPrecision(false));
        putRecord(new Vector2fRecord(new BaseRecord.DeltaFunc<Vector2f>() {
            @Override
            public Vector2f get() {
                return ship.getVelocity();
            }
        }, ShipIDs.SHIP_VEL).setUseDecimalPrecision(false));
        putRecord(new FloatRecord(new BaseRecord.DeltaFunc<Float>() {
            @Override
            public Float get() {
                return ship.getFacing();
            }
        }, ShipIDs.SHIP_ANG).setUseDecimalPrecision(false));
        putRecord(new FloatRecord(new BaseRecord.DeltaFunc<Float>() {
            @Override
            public Float get() {
                return ship.getAngularVelocity();
            }
        }, ShipIDs.SHIP_ANGVEL).setUseDecimalPrecision(false));
        putRecord(new FloatRecord(new BaseRecord.DeltaFunc<Float>() {
            @Override
            public Float get() {
                return ship.getHullLevel();
            }
        }, ShipIDs.SHIP_HULL));
        putRecord(new FloatRecord(new BaseRecord.DeltaFunc<Float>() {
            @Override
            public Float get() {
                return ship.getFluxLevel();
            }
        }, ShipIDs.SHIP_FLUX));
        putRecord(new Vector2fRecord(new BaseRecord.DeltaFunc<Vector2f>() {
            @Override
            public Vector2f get() {
                return ship.getMouseTarget();
            }
        }, ShipIDs.CURSOR).setUseDecimalPrecision(false));
        putRecord(new IntRecord(new BaseRecord.DeltaFunc<Integer>() {
            @Override
            public Integer get() {
                return ship.getOwner();
            }
        }, ShipIDs.OWNER));
        putRecord(new StringRecord(new BaseRecord.DeltaFunc<String>() {
            @Override
            public String get() {
                return ship.getHullSpec().getHullId();
            }
        }, ShipIDs.SPEC_ID));
        putRecord(new FloatRecord(new BaseRecord.DeltaFunc<Float>() {
            @Override
            public Float get() {
                return ship.getCurrentCR();
            }
        }, ShipIDs.COMBAT_READINESS));
    }

    @Override
    public int getTypeId() {
        return ShipIDs.TYPE_ID;
    }
}
