package data.scripts.net.data.packables.entities.ship;

import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.records.*;
import org.lwjgl.util.vector.Vector2f;

public class ShipData extends BasePackable {

    public ShipData(short instanceID, final ShipAPI ship) {
        super(instanceID);

        putRecord(new StringRecord(new BaseRecord.DeltaFunc<String>() {
            @Override
            public String get() {
                return ship.getFleetMemberId();
            }
        }, ShipIDs.SHIP_ID));
        putRecord(new Vector2f32Record(new BaseRecord.DeltaFunc<Vector2f>() {
            @Override
            public Vector2f get() {
                return new Vector2f(ship.getLocation());
            }
        }, ShipIDs.SHIP_LOC).setUseDecimalPrecision(false));
        putRecord(new Vector2f32Record(new BaseRecord.DeltaFunc<Vector2f>() {
            @Override
            public Vector2f get() {
                return new Vector2f(ship.getVelocity());
            }
        }, ShipIDs.SHIP_VEL).setUseDecimalPrecision(false));
        putRecord(new ByteRecord(new BaseRecord.DeltaFunc<Byte>() {
            @Override
            public Byte get() {
                return ConversionUtils.floatToByte(ship.getFacing(), 360f);
            }
        }, ShipIDs.SHIP_ANG));
        putRecord(new Float16Record(new BaseRecord.DeltaFunc<Float>() {
            @Override
            public Float get() {
                return ship.getAngularVelocity();
            }
        }, ShipIDs.SHIP_ANGVEL).setUseDecimalPrecision(false));
        putRecord(new ByteRecord(new BaseRecord.DeltaFunc<Byte>() {
            @Override
            public Byte get() {
                return ConversionUtils.floatToByte(ship.getHullLevel(), 1f);
            }
        }, ShipIDs.SHIP_HULL));
        putRecord(new ByteRecord(new BaseRecord.DeltaFunc<Byte>() {
            @Override
            public Byte get() {
                return ConversionUtils.floatToByte(ship.getFluxLevel(), 1f);
            }
        }, ShipIDs.SHIP_FLUX));
        putRecord(new Vector2f32Record(new BaseRecord.DeltaFunc<Vector2f>() {
            @Override
            public Vector2f get() {
                return new Vector2f(ship.getMouseTarget());
            }
        }, ShipIDs.CURSOR).setUseDecimalPrecision(false));
        putRecord(new ByteRecord(new BaseRecord.DeltaFunc<Byte>() {
            @Override
            public Byte get() {
                return (byte) ship.getOwner();
            }
        }, ShipIDs.OWNER));
        putRecord(new StringRecord(new BaseRecord.DeltaFunc<String>() {
            @Override
            public String get() {
                return ship.getHullSpec().getHullId();
            }
        }, ShipIDs.SPEC_ID));
        putRecord(new ByteRecord(new BaseRecord.DeltaFunc<Byte>() {
            @Override
            public Byte get() {
                return ConversionUtils.floatToByte(ship.getCurrentCR(), 1f);
            }
        }, ShipIDs.COMBAT_READINESS));
    }

    @Override
    public int getTypeID() {
        return ShipIDs.TYPE_ID;
    }
}
