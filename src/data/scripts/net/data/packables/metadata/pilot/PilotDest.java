package data.scripts.net.data.packables.metadata.pilot;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.DestPackable;
import data.scripts.net.data.records.IntRecord;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.MPServerPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PilotDest extends DestPackable {

    private ShipAPI ship;
    private MPPlugin plugin;

    /**
     * Destination constructor
     *
     * @param instanceID unique
     * @param records    incoming deltas
     */
    public PilotDest(int instanceID, Map<Integer, BaseRecord<?>> records) {
        super(instanceID, records);
    }

    @Override
    protected void initDefaultRecords() {
        putRecord(IntRecord.getDefault(PilotIDs.BITMASK));
    }

    @Override
    public void update(float amount) {
        if (ship == null) {
            initShip(plugin);
        } else {
            int bitmask = (int) getRecord(PilotIDs.BITMASK).getValue();
            unmask(ship, bitmask);
        }
    }

    @Override
    public void init(MPPlugin plugin) {
        this.plugin = plugin;
        initShip(plugin);
    }

    private void initShip(MPPlugin plugin) {
        MPServerPlugin serverPlugin = plugin.getType() == MPPlugin.PluginType.SERVER ? (MPServerPlugin) plugin : null;
        if (serverPlugin == null ) return;


    }

    @Override
    public void delete() {

    }

    @Override
    public int getTypeId() {
        return PilotIDs.TYPE_ID;
    }

    public static PilotDest getDefault() {
        return new PilotDest(-1, new HashMap<Integer, BaseRecord<?>>());
    }

    // https://stackoverflow.com/questions/32550451/packing-an-array-of-booleans-into-an-int-in-java
    public static void unmask(ShipAPI ship, int bitmask) {

        boolean[] controls = new boolean[PilotIDs.NUM_CONTROLS];
        for (int i = 0; i < controls.length; i++) {
            if ((bitmask & 1 << i) != 0) controls[i] = true;
        }

        if (controls[0]) ship.giveCommand(ShipCommand.ACCELERATE, null, 0);
        if (controls[1]) ship.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, 0);
        if (controls[2]) ship.giveCommand(ShipCommand.TURN_LEFT, null, 0);
        if (controls[3]) ship.giveCommand(ShipCommand.TURN_RIGHT, null, 0);
        if (controls[4]) ship.giveCommand(ShipCommand.DECELERATE, null, 0);
        //if (controls[5]) ship.giveCommand(ShipCommand., null, 0); STRAFE_KEY
        if (controls[6]) ship.giveCommand(ShipCommand.STRAFE_LEFT, null, 0);
        if (controls[7]) ship.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0);
        if (controls[8]) ship.giveCommand(ShipCommand.USE_SYSTEM, null, 0);
        if (controls[9]) ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);

        int selected = 0;
        List<WeaponGroupAPI> groups = ship.getWeaponGroupsCopy();
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).equals(ship.getSelectedGroupAPI())) {
                selected = i;
            }
        }

        if (controls[10]) ship.giveCommand(ShipCommand.FIRE, ship.getMouseTarget(), selected);
        if (controls[11]) ship.giveCommand(ShipCommand.VENT_FLUX, null, 0);
        if (controls[12]) ship.giveCommand(ShipCommand.HOLD_FIRE, null, 0);
        if (controls[13]) ship.giveCommand(ShipCommand.PULL_BACK_FIGHTERS, null, 0);
        if (controls[14]) ship.giveCommand(ShipCommand.SELECT_GROUP, null, 0);
        if (controls[15]) ship.giveCommand(ShipCommand.SELECT_GROUP, null, 1);
        if (controls[16]) ship.giveCommand(ShipCommand.SELECT_GROUP, null, 2);
        if (controls[17]) ship.giveCommand(ShipCommand.SELECT_GROUP, null, 3);
        if (controls[18]) ship.giveCommand(ShipCommand.SELECT_GROUP, null, 4);
        if (controls[19]) ship.giveCommand(ShipCommand.SELECT_GROUP, null, 5);
        if (controls[20]) ship.giveCommand(ShipCommand.SELECT_GROUP, null, 6);
    }
}
