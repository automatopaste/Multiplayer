package data.scripts.net.data.packables;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.DataManager;
import data.scripts.net.data.records.ARecord;
import data.scripts.net.data.records.FloatRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.net.data.records.Vector2fRecord;
import org.lwjgl.util.vector.Vector2f;

import java.util.Map;

/**
 * Container for tracking network data about a ship
 */
public class ShipData extends APackable {
    private static final int typeID;
    static {
        typeID = DataManager.registerEntityType(ShipData.class, new ShipData(-1));
    }

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

    public ShipData(int instanceID) {
        super(instanceID);

        id = new StringRecord("DEFAULT_ID_STRING");
        loc = new Vector2fRecord(new Vector2f(0f, 0f)).setUseDecimalPrecision(false);
        vel = new Vector2fRecord(new Vector2f(0f, 0f)).setUseDecimalPrecision(false);
        ang = new FloatRecord(0f).setUseDecimalPrecision(false);
        angVel = new FloatRecord(0f).setUseDecimalPrecision(false);
        hull = new FloatRecord(0f);
        flux = new FloatRecord(0f);
    }

    public ShipData(int instanceID, Map<Integer, ARecord<?>> records) {
        super(instanceID);

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
    public ShipData unpack(int instanceID, Map<Integer, ARecord<?>> records) {
        return new ShipData(instanceID, records);
    }

    @Override
    protected boolean write() {
        boolean update = true;
        if (id.checkUpdate(ship.getId())) id.write(packer, SHIP_ID); else update = false;
        if (loc.checkUpdate(ship.getLocation())) loc.write(packer, SHIP_LOC); else update = false;
        if (vel.checkUpdate(ship.getVelocity())) vel.write(packer, SHIP_VEL); else update = false;
        if (ang.checkUpdate(ship.getFacing())) ang.write(packer, SHIP_VEL); else update = false;
        if (angVel.checkUpdate(ship.getAngularVelocity())) angVel.write(packer, SHIP_ANGVEL); else update = false;
        if (hull.checkUpdate(ship.getHullLevel())) hull.write(packer, SHIP_HULL); else update = false;
        if (flux.checkUpdate(ship.getFluxLevel())) flux.write(packer, SHIP_FLUX); else update = false;

        return update;
    }

    @Override
    public int getTypeId() {
        return typeID;
    }

    public void setShip(ShipAPI ship) {
        this.ship = ship;
    }

    public ShipAPI getShip() {
        return ship;
    }
}
