package data.scripts.net.data.packables;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import data.scripts.net.data.DataManager;
import data.scripts.net.data.records.ARecord;
import data.scripts.net.data.records.IntRecord;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.List;
import java.util.Map;

public class InputAggregateData extends APackable {
    private static int typeID;

    // must be below 32
    private static final int NUM_CONTROLS = 21;

    private final IntRecord keysBitmask;

    private static final int BITMASK = 0;

    private ShipAPI shipUnderControl;

    static {
        DataManager.registerEntityType(InputAggregateData.class, new InputAggregateData(-1));
    }

    public InputAggregateData(int instanceID) {
        super(instanceID);

        keysBitmask = new IntRecord(0);
    }

    public InputAggregateData(int instanceID, Map<Integer, ARecord<?>> records) {
        super(instanceID);

        IntRecord keysBitmask1 = (IntRecord) records.get(BITMASK);
        if (keysBitmask1 == null) keysBitmask1 = new IntRecord(0);
        keysBitmask = keysBitmask1;
    }

    @Override
    protected boolean write(boolean flush) {
        if (flush) {
            flushWrite();
            return true;
        }

        boolean update = false;

        boolean[] controls = poll();

        // max length 32
        int bits = 0;
        for (int i = 0; i < controls.length; i++) {
            if (controls[i]) bits |= 1 << i;
        }

        if (keysBitmask.checkUpdate(bits)) {
            keysBitmask.write(packer, BITMASK);
            update = true;
        }

        return update;
    }

    private void flushWrite() {
        boolean[] controls = poll();

        // max length 32
        int bits = 0;
        for (int i = 0; i < controls.length; i++) {
            if (controls[i]) bits |= 1 << i;
        }

        keysBitmask.forceUpdate(bits);
        keysBitmask.write(packer, BITMASK);
    }

    @Override
    public void destinationInit() {

    }

    @Override
    public void destinationDelete() {

    }

    @Override
    public boolean shouldDeleteOnDestination() {
        return false;
    }

    @Override
    public void destinationUpdate() {
        if (shipUnderControl == null || !shipUnderControl.isAlive() || !Global.getCombatEngine().isEntityInPlay(shipUnderControl)) return;

        unmask(keysBitmask.getRecord(), shipUnderControl);

    }

    public static void setTypeID(int typeID) {
        InputAggregateData.typeID = typeID;
    }

    @Override
    public int getTypeId() {
        return typeID;
    }

    // https://stackoverflow.com/questions/32550451/packing-an-array-of-booleans-into-an-int-in-java
    private void unmask(int bitmask, ShipAPI ship) {
        boolean[] controls = new boolean[NUM_CONTROLS];
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

    private boolean[] poll() {
        boolean[] controls = new boolean[NUM_CONTROLS];
        controls[0] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_ACCELERATE")));
        controls[1] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_ACCELERATE_BACKWARDS")));
        controls[2] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_TURN_LEFT")));
        controls[3] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_TURN_RIGHT")));
        controls[4] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_DECELERATE")));

        //not sure
        //controls[5] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_STRAFE_KEY")));

        controls[6] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_STRAFE_LEFT_NOTURN")));
        controls[7] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_STRAFE_RIGHT_NOTURN")));
        controls[8] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_USE_SYSTEM")));

//        controls[9] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SHIELDS")));
        controls[9] = Mouse.isButtonDown(1);

//        controls[10] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_FIRE")));
        controls[10] = Mouse.isButtonDown(0);

        controls[11] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_VENT_FLUX")));
        controls[12] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_HOLD_FIRE")));
        controls[13] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_PULL_BACK_FIGHTERS")));
        controls[14] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_1")));
        controls[15] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_2")));
        controls[16] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_3")));
        controls[17] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_4")));
        controls[18] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_5")));
        controls[19] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_6")));
        controls[20] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_7")));

        return controls;
    }

    @Override
    public InputAggregateData unpack(int instanceID, Map<Integer, ARecord<?>> records) {
        return new InputAggregateData(instanceID, records);
    }

    @Override
    public void updateFromDelta(APackable delta) {
        InputAggregateData d = (InputAggregateData) delta;
        if (d.getKeysBitmask() != null) keysBitmask.forceUpdate(d.getKeysBitmask().getRecord());
    }

    public IntRecord getKeysBitmask() {
        return keysBitmask;
    }
}
