package data.scripts.net.data.packables.entities.ship;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.combat.entities.Ship;
import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.DestPackable;
import data.scripts.net.data.packables.entities.variant.VariantDest;
import data.scripts.net.data.packables.entities.variant.VariantIDs;
import data.scripts.net.data.records.FloatRecord;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.net.data.records.Vector2fRecord;
import data.scripts.plugins.MPClientPlugin;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.ai.DefaultShipAIPlugin;
import org.lwjgl.util.vector.Vector2f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShipDest extends DestPackable {

    private ShipAPI ship;
    private MPPlugin plugin;

    public ShipDest(int instanceID, Map<Integer, BaseRecord<?>> records) {
        super(instanceID, records);
    }

    @Override
    protected void initDefaultRecords() {
        putRecord(StringRecord.getDefault(ShipIDs.SHIP_ID));
        putRecord(Vector2fRecord.getDefault(ShipIDs.SHIP_LOC));
        putRecord(Vector2fRecord.getDefault(ShipIDs.SHIP_VEL));
        putRecord(FloatRecord.getDefault(ShipIDs.SHIP_ANG));
        putRecord(FloatRecord.getDefault(ShipIDs.SHIP_ANGVEL));
        putRecord(FloatRecord.getDefault(ShipIDs.SHIP_FLUX));
        putRecord(FloatRecord.getDefault(ShipIDs.SHIP_HULL));
        putRecord(Vector2fRecord.getDefault(ShipIDs.CURSOR));
        putRecord(IntRecord.getDefault(ShipIDs.OWNER));
        putRecord(FloatRecord.getDefault(ShipIDs.COMBAT_READINESS));
    }

    @Override
    public void init(MPPlugin plugin) {
        this.plugin = plugin;
        initShip(plugin);
    }

    @Override
    public void update(float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();

        String id = (String) getRecord(ShipIDs.SHIP_ID).getValue();

        if (ship == null) {
            initShip(plugin);
        } else {
            Vector2f loc = (Vector2f) getRecord(ShipIDs.SHIP_LOC).getValue();
            ship.getLocation().set(loc);
            Vector2f vel = (Vector2f) getRecord(ShipIDs.SHIP_VEL).getValue();
            ship.getVelocity().set(vel);
            float ang = (float) getRecord(ShipIDs.SHIP_ANG).getValue();
            ship.setFacing(ang);
            float angVel = (float) getRecord(ShipIDs.SHIP_ANGVEL).getValue();
            ship.setAngularVelocity(angVel);
            float hull = (float) getRecord(ShipIDs.SHIP_HULL).getValue();
            ship.setHitpoints(ship.getMaxHitpoints() * hull);
            float flux = (float) getRecord(ShipIDs.SHIP_FLUX).getValue();
            ship.getFluxTracker().setCurrFlux(ship.getMaxFlux() * flux);
            Vector2f cursor = (Vector2f) getRecord(ShipIDs.CURSOR).getValue();
            ship.getMouseTarget().set(cursor);
            int owner = (int) getRecord(ShipIDs.OWNER).getValue();
            ship.setOwner(owner);
            float cr = (float) getRecord(ShipIDs.COMBAT_READINESS).getValue();
            ship.setCurrentCR(cr);
        }
    }

    private void initShip(MPPlugin plugin) {
        if (plugin.getType() != MPPlugin.PluginType.CLIENT) return;
        MPClientPlugin clientPlugin = (MPClientPlugin) plugin;

        CombatEngineAPI engine = Global.getCombatEngine();

        String id = (String) getRecord(ShipIDs.SHIP_ID).getValue();

        VariantDest variantDest = clientPlugin.getVariantDataMap().getVariantData().get(id);
        if (variantDest == null) return;

        // update variant
        String hullSpecId = (String) getRecord(ShipIDs.SPEC_ID).getValue();
        ShipHullSpecAPI hullSpec = Global.getSettings().getHullSpec(hullSpecId);

        CombatFleetManagerAPI fleetManager = engine.getFleetManager((int) getRecord(ShipIDs.OWNER).getValue());

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
            Vector2f loc = (Vector2f) getRecord(ShipIDs.SHIP_LOC).getValue();
            float ang = (float) getRecord(ShipIDs.SHIP_ANG).getValue();
            ship = fleetManager.spawnFleetMember(fleetMember, loc, ang, 0f);
            float cr = (float) getRecord(ShipIDs.COMBAT_READINESS).getValue();
            ship.setCurrentCR(cr);

            // set fleetmember id to sync with server
            Ship s = (Ship) ship;
            String fleetmemberID = (String) getRecord(ShipIDs.SHIP_ID).getValue();
            s.setFleetMemberId(fleetmemberID);
        } else {
            throw new NullPointerException("Attempted fighter init in ship data");
        }

        ship.setShipAI(new DefaultShipAIPlugin());
    }

    @Override
    public void delete() {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (ship != null) engine.removeEntity(ship);
    }

    @Override
    public int getTypeId() {
        return ShipIDs.TYPE_ID;
    }

    public static ShipDest getDefault() {
        return new ShipDest(-1, new HashMap<Integer, BaseRecord<?>>());
    }
}
