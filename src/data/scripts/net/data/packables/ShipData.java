package data.scripts.net.data.packables;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.records.*;
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
    private final IntRecord owner;
    private final StringRecord specId;

    private ShipAPI ship;
    
    private static final int SHIP_LOC = 1;
    private static final int SHIP_VEL = 2;
    private static final int SHIP_ANG = 3;
    private static final int SHIP_ANGVEL = 4;
    private static final int SHIP_HULL = 5;
    private static final int SHIP_FLUX = 6;
    private static final int SHIP_ID = 7;
    private static final int CURSOR = 8;
    private static final int OWNER = 9;
    private static final int SPEC_ID = 10;

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
        owner = new IntRecord(0);
        specId = new StringRecord("DEFAULT_SPEC_ID");
    }

    public ShipData(int instanceID, Map<Integer, ARecord<?>> records) {
        super(instanceID);

        ARecord<?> temp;

        temp = records.get(SHIP_ID);
        id = (temp == null) ? new StringRecord("DEFAULT_ID_STRING") : (StringRecord) temp;
        temp = records.get(SHIP_LOC);
        loc = (temp == null) ? new Vector2fRecord(new Vector2f(0f, 0f)).setUseDecimalPrecision(false) : (Vector2fRecord) temp;
        temp = records.get(SHIP_VEL);
        vel = (temp == null) ? new Vector2fRecord(new Vector2f(0f, 0f)).setUseDecimalPrecision(false) : (Vector2fRecord) temp;
        temp = records.get(SHIP_ANG);
        ang = (temp == null) ? new FloatRecord(0f).setUseDecimalPrecision(false) : (FloatRecord) temp;
        temp = records.get(SHIP_ANG);
        angVel = (temp == null) ? new FloatRecord(0f).setUseDecimalPrecision(false) : (FloatRecord) temp;
        temp = records.get(SHIP_HULL);
        hull = (temp == null) ? new FloatRecord(0f) : (FloatRecord) temp;
        temp = records.get(SHIP_FLUX);
        flux = (temp == null) ? new FloatRecord(0f) : (FloatRecord) temp;
        temp = records.get(CURSOR);
        cursor = (temp == null) ? new Vector2fRecord(new Vector2f(0f, 0f)) : (Vector2fRecord) temp;
        temp = records.get(OWNER);
        owner = (temp == null) ? new IntRecord(0) : (IntRecord) temp;
        temp = records.get(SPEC_ID);
        specId = (temp == null) ? new StringRecord("DEFAULT_SPEC_ID") : (StringRecord) temp;

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
        if (d.getOwner() != null) cursor.forceUpdate(d.getCursor().getRecord());
    }

    @Override
    public ShipData unpack(int instanceID, Map<Integer, ARecord<?>> records) {
        return new ShipData(instanceID, records);
    }

    @Override
    protected boolean write(boolean flush) {
        if (flush) {
            flushWrite();
            return false;
        }

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
        if (owner.checkUpdate(ship.getOwner())) {
            owner.write(packer, OWNER);
            update = true;
        }
        if (specId.checkUpdate(ship.getHullSpec().getHullId())) {
            specId.write(packer, SPEC_ID);
            update = true;
        }

        return update;
    }

    private void flushWrite() {
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

        owner.forceUpdate(ship.getOwner());
        owner.write(packer, OWNER);

        specId.forceUpdate(ship.getHullSpec().getHullId());
        specId.write(packer, SPEC_ID);
    }

    @Override
    public void destinationInit() {
        CombatEngineAPI engine = Global.getCombatEngine();
        engine.getFleetManager(owner.getRecord()).spawnShipOrWing(specId.getRecord() + "_Hull", loc.getRecord(), ang.getRecord());
    }

    @Override
    public void destinationDelete() {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (ship != null) engine.removeEntity(ship);
    }

    @Override
    public void destinationUpdate() {
        CombatEngineAPI engine = Global.getCombatEngine();

        if (ship == null || !engine.isEntityInPlay(ship)) {
            for (ShipAPI ship : engine.getShips()) {
                if (ship.getId().equals(id.getRecord())) {
                    this.ship = ship;
                }
            }
        }

        if (ship == null) return;
        ship.getLocation().set(loc.getRecord());
        ship.getVelocity().set(loc.getRecord());
        ship.setFacing(ang.getRecord());
        ship.setAngularVelocity(angVel.getRecord());
        ship.setHitpoints(ship.getMaxHitpoints() * hull.getRecord());
        ship.getFluxTracker().setCurrFlux(ship.getMaxFlux() * flux.getRecord());
        ship.getMouseTarget().set(cursor.getRecord());
        ship.setOwner(owner.getRecord());

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

    public IntRecord getOwner() {
        return owner;
    }

    public StringRecord getSpecId() {
        return specId;
    }
}
