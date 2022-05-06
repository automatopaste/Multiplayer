package data.scripts.net.data.packables;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.IDTypes;
import data.scripts.net.data.RecordDelta;
import data.scripts.net.data.records.FloatRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.net.data.records.Vector2fRecord;

import java.util.Map;

/**
 * Container for tracking network data about a ship
 */
public class ShipData extends APackable {
    private final StringRecord id;
    private final Vector2fRecord loc;
    private final Vector2fRecord vel;
    private final FloatRecord ang;
    private final FloatRecord angVel;
    private final FloatRecord hull;
    private final FloatRecord flux;

    private ShipAPI ship;
    
    private static final int SHIP_LOC = 0;
    private static final int SHIP_VEL = 1;
    private static final int SHIP_ANG = 2;
    private static final int SHIP_ANGVEL = 3;
    private static final int SHIP_HULL = 4;
    private static final int SHIP_FLUX = 5;
    private static final int SHIP_ID = 6;

    public ShipData(ShipAPI ship) {
        this.ship = ship;

        id = new StringRecord(ship.getId());
        loc = new Vector2fRecord(ship.getLocation()).setUseDecimalPrecision(false);
        vel = new Vector2fRecord(ship.getVelocity()).setUseDecimalPrecision(false);
        ang = new FloatRecord(ship.getFacing()).setUseDecimalPrecision(false);
        angVel = new FloatRecord(ship.getAngularVelocity()).setUseDecimalPrecision(false);
        hull = new FloatRecord(ship.getHullLevel());
        flux = new FloatRecord(ship.getFluxLevel());
    }

    public ShipData(Map<Integer, RecordDelta> records) {
        id = (StringRecord) records.get(SHIP_ID);
        loc = (Vector2fRecord) records.get(SHIP_LOC);
        vel = (Vector2fRecord) records.get(SHIP_VEL);
        ang = (FloatRecord) records.get(SHIP_ANG);
        angVel = (FloatRecord) records.get(SHIP_ANGVEL);
        hull = (FloatRecord) records.get(SHIP_HULL);
        flux = (FloatRecord) records.get(SHIP_FLUX);

        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (ship.getId().equals(id.getRecord())) {
                this.ship = ship;
            }
        }
    }

    @Override
    void write() {
        if (id.checkUpdate(ship.getId())) id.write(packer, SHIP_ID);
        if (loc.checkUpdate(ship.getLocation())) loc.write(packer, SHIP_LOC);
        if (vel.checkUpdate(ship.getVelocity())) vel.write(packer, SHIP_VEL);
        if (ang.checkUpdate(ship.getFacing())) ang.write(packer, SHIP_VEL);
        if (angVel.checkUpdate(ship.getAngularVelocity())) angVel.write(packer, SHIP_ANGVEL);
        if (hull.checkUpdate(ship.getHullLevel())) hull.write(packer, SHIP_HULL);
        if (flux.checkUpdate(ship.getFluxLevel())) flux.write(packer, SHIP_FLUX);
    }

    @Override
    public int getTypeId() {
        return IDTypes.SHIP;
    }
}
