package data.scripts.net.data.packables;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
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

    public ShipData(int instanceID, Map<Integer, ARecord<?>> records) {
        super(instanceID);

        StringRecord id1 = (StringRecord) records.get(SHIP_ID);
        if (id1 == null) id1 = new StringRecord("DEFAULT_ID_STRING");
        id = id1;

        Vector2fRecord loc1 = (Vector2fRecord) records.get(SHIP_LOC);
        if (loc1 == null) loc1 = new Vector2fRecord(new Vector2f(0f, 0f)).setUseDecimalPrecision(false);
        loc = loc1;

        Vector2fRecord vel1 = (Vector2fRecord) records.get(SHIP_VEL);
        if (vel1 == null) vel1 = new Vector2fRecord(new Vector2f(0f, 0f)).setUseDecimalPrecision(false);
        vel = vel1;

        FloatRecord ang1 = (FloatRecord) records.get(SHIP_ANG);
        if (ang1 == null) ang1 = new FloatRecord(0f).setUseDecimalPrecision(false);
        ang = ang1;

        FloatRecord angVel1 = (FloatRecord) records.get(SHIP_ANGVEL);
        if (angVel1 == null) angVel1 = new FloatRecord(0f).setUseDecimalPrecision(false);
        angVel = angVel1;

        FloatRecord hull1 = (FloatRecord) records.get(SHIP_HULL);
        if (hull1 == null) hull1 = new FloatRecord(0f);
        hull = hull1;

        FloatRecord flux1 = (FloatRecord) records.get(SHIP_FLUX);
        if (flux1 == null) flux1 = new FloatRecord(0f);
        flux = flux1;

        Vector2fRecord cursor1 = (Vector2fRecord) records.get(CURSOR);
        if (cursor1 == null) cursor1 = new Vector2fRecord(new Vector2f(0f, 0f));
        cursor = cursor1;

        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (ship.getId().equals(id.getRecord())) {
                this.ship = ship;
            }
        }
    }

    @Override
    public void updateFromDelta(APackable delta) {
        ShipData d = (ShipData) delta;
        if (d.getId() != null) id.forceUpdate(d.getId().getRecord());
        if (d.getLoc() != null) loc.forceUpdate(d.getLoc().getRecord());
        if (d.getVel() != null) vel.forceUpdate(d.getVel().getRecord());
        if (d.getAng() != null) ang.forceUpdate(d.getAng().getRecord());
        if (d.getAngVel() != null) angVel.forceUpdate(d.getAngVel().getRecord());
        if (d.getFlux() != null) flux.forceUpdate(d.getFlux().getRecord());
        if (d.getHull() != null) hull.forceUpdate(d.getHull().getRecord());
        if (d.getCursor() != null) cursor.forceUpdate(d.getCursor().getRecord());
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
            ang.write(packer, SHIP_ANG);
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

    @Override
    protected void flushWrite() {
        id.forceUpdate(ship.getId());
        id.write(packer, SHIP_ID);

        loc.forceUpdate(ship.getLocation());
        loc.write(packer, SHIP_LOC);

        vel.forceUpdate(ship.getVelocity());
        vel.write(packer, SHIP_VEL);

        ang.forceUpdate(ship.getFacing());
        ang.write(packer, SHIP_ANG);

        angVel.forceUpdate(ship.getAngularVelocity());
        angVel.write(packer, SHIP_ANGVEL);

        hull.forceUpdate(ship.getHullLevel());
        hull.write(packer, SHIP_HULL);

        flux.forceUpdate(ship.getFluxLevel());
        flux.write(packer, SHIP_FLUX);

        cursor.forceUpdate(ship.getMouseTarget());
        cursor.write(packer, CURSOR);
    }

    @Override
    public void destinationInit() {

    }

    @Override
    public void destinationDelete() {

    }

    @Override
    public boolean destinationUpdate() {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (ship == null || !engine.isEntityInPlay(ship)) return true;

        ship.getLocation().set(loc.getRecord());
        ship.getVelocity().set(loc.getRecord());
        ship.setFacing(ang.getRecord());
        ship.setAngularVelocity(angVel.getRecord());
        ship.setHitpoints(ship.getMaxHitpoints() * hull.getRecord());
        ship.getFluxTracker().setCurrFlux(ship.getMaxFlux() * flux.getRecord());
        ship.getMouseTarget().set(cursor.getRecord());

        return false;
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
