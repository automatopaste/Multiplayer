package data.scripts.net.data.packables;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.IDTypes;
import data.scripts.net.data.records.IntRecord;
import org.lwjgl.input.Keyboard;

public class InputAggregateData extends APackable {
    private final IntRecord keysBitmask;

    private static final int BITMASK = 0;

    public InputAggregateData() {
        keysBitmask = new IntRecord(0);
    }

    @Override
    public int getTypeId() {
        return IDTypes.INPUT_AGGREGATE;
    }

    @Override
    void write() {
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

        if (keysBitmask.checkUpdate(bits)) keysBitmask.write(packer, BITMASK);
    }

    public static boolean[] unmask(int bitmask) {
        boolean[] controls = new boolean[4];
        for (int i = 0; i < controls.length; i++) {
            if ((bitmask & 1 << i) != 0) controls[i] = true;
        }
        return controls;
    }

    // https://stackoverflow.com/questions/32550451/packing-an-array-of-booleans-into-an-int-in-java
}
