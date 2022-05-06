package data.scripts.net.data.packables;

import data.scripts.net.data.IDTypes;
import data.scripts.net.data.records.StringRecord;
import org.lwjgl.input.Keyboard;

public class InputAggregateData extends APackable {
    private final StringRecord w;
    private final StringRecord a;
    private final StringRecord s;
    private final StringRecord d;

    public InputAggregateData() {
        w = new StringRecord("LOL GOTEM", 1);
        a = new StringRecord("LOL GOTEM", 1);
        s = new StringRecord("LOL GOTEM", 1);
        d = new StringRecord("LOL GOTEM", 1);
    }

    @Override
    public int getTypeId() {
        return IDTypes.INPUT_AGGREGATE;
    }

    @Override
    void update() {
        if (w.update("KEY W IS DOWN: " + Keyboard.isKeyDown(Keyboard.KEY_W))) w.write(packer);
        if (a.update("KEY A IS DOWN: " + Keyboard.isKeyDown(Keyboard.KEY_S))) a.write(packer);
        if (s.update("KEY S IS DOWN: " + Keyboard.isKeyDown(Keyboard.KEY_A))) s.write(packer);
        if (d.update("KEY D IS DOWN: " + Keyboard.isKeyDown(Keyboard.KEY_D))) d.write(packer);
    }
}
