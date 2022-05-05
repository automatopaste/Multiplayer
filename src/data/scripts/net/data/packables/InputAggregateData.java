package data.scripts.net.data.packables;

import data.scripts.net.data.IDTypes;
import data.scripts.net.data.records.StringRecord;
import org.lwjgl.input.Keyboard;

public class InputAggregateData extends APackable {
    private final StringRecord info;

    public InputAggregateData() {
        info = new StringRecord("LOL GOTEM", 1);
    }

    @Override
    public int getTypeId() {
        return IDTypes.INPUT_AGGREGATE;
    }

    @Override
    void update() {
        if (info.update("KEY J IS DOWN: " + Keyboard.isKeyDown(Keyboard.KEY_J))) info.write(packer);
    }
}
