package data.scripts.net.data.packables;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.DataManager;
import data.scripts.net.data.records.ARecord;
import data.scripts.net.data.records.IntRecord;
import org.lwjgl.input.Keyboard;

import java.util.Map;

public class InputAggregateData extends APackable {
    private static int typeID;

    private final IntRecord keysBitmask;

    private static final int BITMASK = 0;

    static {
        DataManager.registerEntityType(InputAggregateData.class, new InputAggregateData(-1));
    }

    public InputAggregateData(int instanceID) {
        super(instanceID);

        keysBitmask = new IntRecord(0);
    }

    public InputAggregateData(int instanceID, Map<Integer, ARecord<?>> records) {
        super(instanceID);

        keysBitmask = (IntRecord) records.get(BITMASK);
    }

    @Override
    protected boolean write() {
        boolean update = false;

        boolean[] controls = new boolean[4];
        controls[0] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_ACCELERATE")));
        controls[1] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_ACCELERATE_BACKWARDS")));
        controls[2] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_TURN_LEFT")));
        controls[3] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_TURN_RIGHT")));

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

    public static void setTypeID(int typeID) {
        InputAggregateData.typeID = typeID;
    }

    @Override
    public int getTypeId() {
        return typeID;
    }

    // https://stackoverflow.com/questions/32550451/packing-an-array-of-booleans-into-an-int-in-java
    public static boolean[] unmask(int bitmask) {
        boolean[] controls = new boolean[4];
        for (int i = 0; i < controls.length; i++) {
            if ((bitmask & 1 << i) != 0) controls[i] = true;
        }
        return controls;
    }

    @Override
    public InputAggregateData unpack(int instanceID, Map<Integer, ARecord<?>> records) {
        return new InputAggregateData(instanceID, records);
    }

    public IntRecord getKeysBitmask() {
        return keysBitmask;
    }
}
