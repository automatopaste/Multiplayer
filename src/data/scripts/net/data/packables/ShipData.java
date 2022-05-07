package data.scripts.net.data.packables;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
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
    private static int typeID;

    private final StringRecord id;
    private final Vector2fRecord loc;
    private final Vector2fRecord vel;
    private final FloatRecord ang;
    private final FloatRecord angVel;
    private final FloatRecord hull;
    private final FloatRecord flux;
    private final Vector2fRecord cursor;

    private ShipAPI ship;
    
    private static final int SHIP_LOC = 10;
    private static final int SHIP_VEL = 20;
    private static final int SHIP_ANG = 30;
    private static final int SHIP_ANGVEL = 40;
    private static final int SHIP_HULL = 50;
    private static final int SHIP_FLUX = 60;
    private static final int SHIP_ID = 70;
    private static final int CURSOR = 80;

    public ShipData(int instanceID) {
        super(instanceID);

        id = new StringRecord("DEFAULT_ID_STRING");
        loc = new Vector2fRecord(new Vector2f(0f, 0f)).setUseDecimalPrecision(false);
        vel = new Vector2fRecord(new Vector2f(0f, 0f)).setUseDecimalPrecision(false);
        ang = new FloatRecord(0f).setUseDecimalPrecision(false);
        angVel = new FloatRecord(0f).setUseDecimalPrecision(false);
        hull = new FloatRecord(0f);
        flux = new FloatRecord(0f);
        cursor = new Vector2fRecord(new Vector2f(0f, 0f));
    }

    @Override
    public void updateFromDelta(APackable delta) {
        ShipData d = (ShipData) delta;
        if (d.getId() != null) id.doUpdate(d.getId().getRecord());
        if (d.getLoc() != null) loc.doUpdate(d.getLoc().getRecord());
        if (d.getVel() != null) vel.doUpdate(d.getVel().getRecord());
        if (d.getAng() != null) ang.doUpdate(d.getAng().getRecord());
        if (d.getAngVel() != null) angVel.doUpdate(d.getAngVel().getRecord());
        if (d.getFlux() != null) flux.doUpdate(d.getFlux().getRecord());
        if (d.getHull() != null) hull.doUpdate(d.getHull().getRecord());
        if (d.getCursor() != null) cursor.doUpdate(d.getCursor().getRecord());
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
        cursor = (Vector2fRecord) records.get(CURSOR);

        if (id != null) {
            for (ShipAPI ship : Global.getCombatEngine().getShips()) {
                if (ship.getId().equals(id.getRecord())) {
                    this.ship = ship;
                }
            }
        }
    }

    @Override
    public ShipData unpack(int instanceID, Map<Integer, ARecord<?>> records) {
        return new ShipData(instanceID, records);
    }

    @Override
    protected boolean write() {
        if (ship == null) return false;

        boolean update = false;
        if (id.checkUpdate(ship.getId())) {
            id.write(packer, SHIP_ID);
            update = true;
        }
        if (loc.checkUpdate(ship.getLocation())) {
            loc.write(packer, SHIP_LOC);
            update = true;
        }
        if (vel.checkUpdate(ship.getVelocity())) {
            vel.write(packer, SHIP_VEL);
            update = true;
        }
        if (ang.checkUpdate(ship.getFacing())) {
            ang.write(packer, SHIP_VEL);
            update = true;
        }
        if (angVel.checkUpdate(ship.getAngularVelocity())) {
            angVel.write(packer, SHIP_ANGVEL);
            update = true;
        }
        if (hull.checkUpdate(ship.getHullLevel())) {
            hull.write(packer, SHIP_HULL);
            update = true;
        }
        if (flux.checkUpdate(ship.getFluxLevel())) {
            flux.write(packer, SHIP_FLUX);
            update = true;
        }
        if (cursor.checkUpdate(ship.getMouseTarget())) {
            cursor.write(packer, CURSOR);
            update = true;
        }

        return update;
    }

    public static void setTypeID(int typeID) {
        ShipData.typeID = typeID;
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

    public Vector2fRecord getLoc() {
        return loc;
    }

    public Vector2fRecord getVel() {
        return vel;
    }

    public FloatRecord getAng() {
        return ang;
    }

    public FloatRecord getAngVel() {
        return angVel;
    }

    public FloatRecord getFlux() {
        return flux;
    }

    public FloatRecord getHull() {
        return hull;
    }

    public StringRecord getId() {
        return id;
    }

    public Vector2fRecord getCursor() {
        return cursor;
    }
}
