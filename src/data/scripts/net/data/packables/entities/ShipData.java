package data.scripts.net.data.packables.entities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.combat.entities.Ship;
import data.scripts.net.data.BasePackable;
import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.records.FloatRecord;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.net.data.records.Vector2fRecord;
import data.scripts.plugins.MPClientPlugin;
import data.scripts.plugins.MPPlugin;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;
import java.util.Map;

/**
 * Container for tracking network data about a ship
 */
public class ShipData extends BasePackable {
    public static int TYPE_ID;

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

    public ShipData(int instanceID, ShipAPI ship) {
        super(instanceID);

        id = (StringRecord) new StringRecord("DEFAULT_ID_STRING").setUndefined(true);
        loc = (Vector2fRecord) new Vector2fRecord(new Vector2f(0f, 0f)).setUseDecimalPrecision(false).setUndefined(true);
        vel = (Vector2fRecord) new Vector2fRecord(new Vector2f(0f, 0f)).setUseDecimalPrecision(false).setUndefined(true);
        ang = (FloatRecord) new FloatRecord(0f).setUseDecimalPrecision(false).setUndefined(true);
        angVel = (FloatRecord) new FloatRecord(0f).setUseDecimalPrecision(false).setUndefined(true);
        hull = (FloatRecord) new FloatRecord(0f).setUndefined(true);
        flux = (FloatRecord) new FloatRecord(0f).setUndefined(true);
        cursor = (Vector2fRecord) new Vector2fRecord(new Vector2f(0f, 0f)).setUndefined(true);
        owner = (IntRecord) new IntRecord(0).setUndefined(true);
        specId = (StringRecord) new StringRecord("DEFAULT_SPEC_ID").setUndefined(true);

        this.ship = ship;
    }

    public ShipData(int instanceID, Map<Integer, BaseRecord<?>> records) {
        super(instanceID);

        BaseRecord<?> temp;

        temp = records.get(SHIP_ID);
        id = (temp == null) ? (StringRecord) new StringRecord("DEFAULT_ID_STRING").setUndefined(true) : (StringRecord) temp;
        temp = records.get(SHIP_LOC);
        loc = (temp == null) ? (Vector2fRecord) new Vector2fRecord(new Vector2f(0f, 0f)).setUseDecimalPrecision(false).setUndefined(true) : (Vector2fRecord) temp;
        temp = records.get(SHIP_VEL);
        vel = (temp == null) ? (Vector2fRecord) new Vector2fRecord(new Vector2f(0f, 0f)).setUseDecimalPrecision(false).setUndefined(true) : (Vector2fRecord) temp;
        temp = records.get(SHIP_ANG);
        ang = (temp == null) ? (FloatRecord) new FloatRecord(0f).setUseDecimalPrecision(false).setUndefined(true) : (FloatRecord) temp;
        temp = records.get(SHIP_ANGVEL);
        angVel = (temp == null) ? (FloatRecord) new FloatRecord(0f).setUseDecimalPrecision(false).setUndefined(true) : (FloatRecord) temp;
        temp = records.get(SHIP_HULL);
        hull = (temp == null) ? (FloatRecord) new FloatRecord(0f).setUndefined(true) : (FloatRecord) temp;
        temp = records.get(SHIP_FLUX);
        flux = (temp == null) ? (FloatRecord) new FloatRecord(0f).setUndefined(true) : (FloatRecord) temp;
        temp = records.get(CURSOR);
        cursor = (temp == null) ? (Vector2fRecord) new Vector2fRecord(new Vector2f(0f, 0f)).setUndefined(true) : (Vector2fRecord) temp;
        temp = records.get(OWNER);
        owner = (temp == null) ? (IntRecord) new IntRecord(0).setUndefined(true) : (IntRecord) temp;
        temp = records.get(SPEC_ID);
        specId = (temp == null) ? (StringRecord) new StringRecord("DEFAULT_SPEC_ID").setUndefined(true) : (StringRecord) temp;

        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (ship.getFleetMemberId().equals(id.getRecord())) {
                this.ship = ship;
            }
        }
    }

    @Override
    public void updateFromDelta(BasePackable delta) {
        ShipData d = (ShipData) delta;
        if (d.getId().isDefined()) id.forceUpdate(d.getId().getRecord());
        if (d.getLoc().isDefined()) loc.forceUpdate(d.getLoc().getRecord());
        if (d.getVel().isDefined()) vel.forceUpdate(d.getVel().getRecord());
        if (d.getAng().isDefined()) ang.forceUpdate(d.getAng().getRecord());
        if (d.getAngVel().isDefined()) angVel.forceUpdate(d.getAngVel().getRecord());
        if (d.getFlux().isDefined()) flux.forceUpdate(d.getFlux().getRecord());
        if (d.getHull().isDefined()) hull.forceUpdate(d.getHull().getRecord());
        if (d.getCursor().isDefined()) cursor.forceUpdate(d.getCursor().getRecord());
        if (d.getOwner().isDefined()) cursor.forceUpdate(d.getCursor().getRecord());
        if (d.getSpecId().isDefined()) specId.forceUpdate(d.getSpecId().getRecord());
    }

    @Override
    public ShipData unpack(int instanceID, Map<Integer, BaseRecord<?>> records) {
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
        if (id.checkUpdate(ship.getFleetMemberId())) {
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
        if (ship == null) throw new NullPointerException("Attempted flush write with null entity!");

        id.forceUpdate(ship.getFleetMemberId());
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
    public void destinationInit(MPPlugin plugin) {
        if (plugin.getType() != MPPlugin.PluginType.CLIENT) return;
        MPClientPlugin clientPlugin = (MPClientPlugin) plugin;

        CombatEngineAPI engine = Global.getCombatEngine();

        // update variant
        String hullSpecId = specId.getRecord();
        ShipHullSpecAPI hullSpec = Global.getSettings().getHullSpec(hullSpecId);

        String hullVariantId;
        ShipVariantAPI variant;
        if (hullSpec.getHullSize() != ShipAPI.HullSize.FIGHTER) {
            hullVariantId = hullSpecId + "_Hull";
            variant = Global.getSettings().createEmptyVariant(
                    hullVariantId,
                    hullSpec
            );

            VariantData variantData = clientPlugin.getVariantDataMap().getVariantData().get(id.getRecord());

            variant.setNumFluxCapacitors(variantData.getCapacitors().getRecord());
            variant.setNumFluxVents(variantData.getVents().getRecord());

            List<StringRecord> weaponSlots = variantData.getWeaponSlots();
            List<StringRecord> weaponIds = variantData.getWeaponIds();
            for (int i = 0; i < weaponSlots.size(); i++) {
                String slot = weaponSlots.get(i).getRecord();

                variant.addWeapon(slot, weaponIds.get(i).getRecord());
            }

            variant.autoGenerateWeaponGroups();
        } else {
            hullVariantId = hullSpecId + "_wing";
            variant = Global.getSettings().getVariant(hullVariantId);
        }

        FleetMemberType fleetMemberType = hullSpec.getHullSize() == ShipAPI.HullSize.FIGHTER ? FleetMemberType.FIGHTER_WING : FleetMemberType.SHIP;
        FleetMemberAPI fleetMember = Global.getFactory().createFleetMember(fleetMemberType, variant);

        CombatFleetManagerAPI fleetManager = engine.getFleetManager(owner.getRecord());
        fleetManager.addToReserves(fleetMember);
        ship = fleetManager.spawnFleetMember(fleetMember, loc.getRecord(), ang.getRecord(), 0f);

        // set fleetmember id to sync with server
        Ship s = (Ship) ship;
        s.setFleetMemberId(id.getRecord());

        ship.setShipAI(new ShipAIPlugin() {
            @Override
            public void setDoNotFireDelay(float amount) {

            }

            @Override
            public void forceCircumstanceEvaluation() {

            }

            @Override
            public void advance(float amount) {

            }

            @Override
            public boolean needsRefit() {
                return false;
            }

            @Override
            public ShipwideAIFlags getAIFlags() {
                return new ShipwideAIFlags();
            }

            @Override
            public void cancelCurrentManeuver() {

            }

            @Override
            public ShipAIConfig getConfig() {
                return new ShipAIConfig();
            }
        });
    }

    @Override
    public void destinationDelete() {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (ship != null) engine.removeEntity(ship);
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public void destinationUpdate() {
        CombatEngineAPI engine = Global.getCombatEngine();

        if (ship == null || !engine.isEntityInPlay(ship)) {
            for (ShipAPI ship : engine.getShips()) {
                if (ship.getFleetMemberId().equals(id.getRecord())) {
                    this.ship = ship;
                }
            }
        }

        if (ship == null) return;
        ship.getLocation().set(loc.getRecord());
        ship.getVelocity().set(vel.getRecord());
        ship.setFacing(ang.getRecord());
        ship.setAngularVelocity(angVel.getRecord());
        ship.setHitpoints(ship.getMaxHitpoints() * hull.getRecord());
        ship.getFluxTracker().setCurrFlux(ship.getMaxFlux() * flux.getRecord());
        ship.getMouseTarget().set(cursor.getRecord());
        ship.setOwner(owner.getRecord());
    }

    public static void setTypeId(int typeId) {
        ShipData.TYPE_ID = typeId;
    }

    @Override
    public int getTypeId() {
        return TYPE_ID;
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
