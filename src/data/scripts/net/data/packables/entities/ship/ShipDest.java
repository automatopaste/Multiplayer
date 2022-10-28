package data.scripts.net.data.packables.entities.ship;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.combat.entities.Ship;
import data.scripts.net.data.records.*;
import data.scripts.net.data.packables.DestPackable;
import data.scripts.net.data.packables.entities.variant.VariantDest;
import data.scripts.net.data.packables.entities.variant.VariantIDs;
import data.scripts.plugins.MPClientPlugin;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.ai.MPDefaultShipAIPlugin;
import org.lwjgl.util.vector.Vector2f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShipDest extends DestPackable {

    private ShipAPI ship;
    private MPPlugin plugin;

    public ShipDest(short instanceID, Map<Byte, BaseRecord<?>> records) {
        super(instanceID, records);
    }

    @Override
    protected void initDefaultRecords() {
        putRecord(StringRecord.getDefault(ShipIDs.SHIP_ID));
        putRecord(Vector2f32Record.getDefault(ShipIDs.SHIP_LOC));
        putRecord(Vector2f32Record.getDefault(ShipIDs.SHIP_VEL));
        putRecord(ByteRecord.getDefault(ShipIDs.SHIP_ANG)); // 0..255 represent 0..360
        putRecord(Float16Record.getDefault(ShipIDs.SHIP_ANGVEL));
        putRecord(ByteRecord.getDefault(ShipIDs.SHIP_HULL)); // 0..255 represent 0%..100%
        putRecord(ByteRecord.getDefault(ShipIDs.SHIP_FLUX)); // 0..255 represent 0%..100%
        putRecord(Vector2f32Record.getDefault(ShipIDs.CURSOR));
        putRecord(ByteRecord.getDefault(ShipIDs.OWNER)); // values 0..1
        putRecord(ByteRecord.getDefault(ShipIDs.COMBAT_READINESS)); // 0..255 represent 0%..100%
    }

    @Override
    public void init(MPPlugin plugin) {
        this.plugin = plugin;
        initShip(plugin);
    }

    @Override
    public void update(float amount) {
        if (ship == null) {
            initShip(plugin);
        } else {
            ship.getLocation().set(getLoc());
            ship.getVelocity().set(getVel());
            ship.setFacing(getAng());
            ship.setAngularVelocity(getAngVel());
            ship.setHitpoints(getHull());
            ship.getFluxTracker().setCurrFlux(getFlux());
            ship.getMouseTarget().set(getCursor());
            ship.setOwner(getOwner());
            ship.setCurrentCR(getCR());
        }
    }

    private void initShip(MPPlugin plugin) {
        if (plugin.getType() != MPPlugin.PluginType.CLIENT) return;
        MPClientPlugin clientPlugin = (MPClientPlugin) plugin;

        CombatEngineAPI engine = Global.getCombatEngine();

        String id = getShipID();

        VariantDest variantDest = clientPlugin.getVariantDataMap().getVariantData().get(id);
        if (variantDest == null) return;

        // update variant
        String hullSpecId = getSpecID();
        ShipHullSpecAPI hullSpec = Global.getSettings().getHullSpec(hullSpecId);

        CombatFleetManagerAPI fleetManager = engine.getFleetManager(getOwner());

        if (hullSpec.getHullSize() != ShipAPI.HullSize.FIGHTER) {
            String hullVariantId = hullSpecId + "_Hull";
            ShipVariantAPI variant = Global.getSettings().createEmptyVariant(
                    hullVariantId,
                    hullSpec
            );

            int numCapacitors = (int) variantDest.getRecord(VariantIDs.CAPACITORS).getValue();
            variant.setNumFluxCapacitors(numCapacitors);
            int numVents = (int) variantDest.getRecord(VariantIDs.VENTS).getValue();
            variant.setNumFluxVents(numVents);

            List<StringRecord> weaponSlots = (List<StringRecord>) variantDest.getRecord(VariantIDs.WEAPON_SLOTS).getValue();
            List<StringRecord> weaponIds = (List<StringRecord>) variantDest.getRecord(VariantIDs.WEAPON_IDS).getValue();
            for (int i = 0; i < weaponSlots.size(); i++) {
                String slot = weaponSlots.get(i).getValue();
                variant.addWeapon(slot, weaponIds.get(i).getValue());
            }

            variant.autoGenerateWeaponGroups();

            FleetMemberType fleetMemberType = FleetMemberType.SHIP;
            FleetMemberAPI fleetMember = Global.getFactory().createFleetMember(fleetMemberType, variant);

            fleetManager.addToReserves(fleetMember);
            Vector2f loc = getLoc();
            float ang = getAng();

            fleetMember.getCrewComposition().setCrew(fleetMember.getHullSpec().getMaxCrew());

            ship = fleetManager.spawnFleetMember(fleetMember, loc, ang, 0f);
            ship.setCRAtDeployment(0.7f);
            ship.setControlsLocked(false);

            // set fleetmember id to sync with server
            Ship s = (Ship) ship;
            String fleetmemberID = getShipID();
            s.setFleetMemberId(fleetmemberID);
        } else {
            throw new NullPointerException("Attempted fighter init in ship data");
        }

        ship.setShipAI(new MPDefaultShipAIPlugin());
    }

    @Override
    public void delete() {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (ship != null) engine.removeEntity(ship);
    }

    @Override
    public int getTypeID() {
        return ShipIDs.TYPE_ID;
    }

    public static ShipDest getDefault() {
        return new ShipDest((short) -1, new HashMap<Byte, BaseRecord<?>>());
    }

    public String getSpecID() {
        return (String) getRecord(ShipIDs.SPEC_ID).getValue();
    }

    public String getShipID() {
        return (String) getRecord(ShipIDs.SHIP_ID).getValue();
    }

    public Vector2f getLoc() {
        return (Vector2f) getRecord(ShipIDs.SHIP_LOC).getValue();
    }

    public Vector2f getVel() {
        return (Vector2f) getRecord(ShipIDs.SHIP_VEL).getValue();
    }

    public float getAng() {
        byte ang = (byte) getRecord(ShipIDs.SHIP_ANG).getValue();
        return ConversionUtils.byteToFloat(ang, 360f);
    }

    public float getAngVel() {
        return (float) getRecord(ShipIDs.SHIP_ANGVEL).getValue();
    }

    public float getHull() {
        byte hull = (byte) getRecord(ShipIDs.SHIP_HULL).getValue();
        return ship.getMaxHitpoints() * ConversionUtils.byteToFloat(hull, 1f);
    }

    public float getFlux() {
        byte flux = (byte) getRecord(ShipIDs.SHIP_FLUX).getValue();
        return ship.getMaxFlux() * ConversionUtils.byteToFloat(flux, 1f);
    }

    public Vector2f getCursor() {
        return (Vector2f) getRecord(ShipIDs.CURSOR).getValue();
    }

    public int getOwner() {
        return (byte) getRecord(ShipIDs.OWNER).getValue();
    }

    public float getCR() {
        byte cr = (byte) getRecord(ShipIDs.COMBAT_READINESS).getValue();
        return ConversionUtils.byteToFloat(cr, 1f);
    }
}
