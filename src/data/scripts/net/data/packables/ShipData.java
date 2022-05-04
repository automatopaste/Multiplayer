package data.scripts.net.data.packables;

import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.IDTypes;
import data.scripts.net.data.Packable;
import data.scripts.net.data.records.ARecord;
import data.scripts.net.data.records.FloatRecord;
import data.scripts.net.data.records.Vector2fRecord;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for tracking network data about a ship
 */
public class ShipData implements Packable {
    private static final int TYPE_ID = 1;

    private final Vector2fRecord loc;
    private final Vector2fRecord vel;
//    private final Vector2fRecord acc;
    private final FloatRecord ang;
    private final FloatRecord angVel;
//    private final FloatRecord angAcc;
    private final FloatRecord hull;
    private final FloatRecord flux;

    private final ShipAPI ship;
    private final ByteBuffer packer;

    public ShipData(ShipAPI ship) {
        this.ship = ship;

        loc = new Vector2fRecord(ship.getLocation(), 1);
        vel = new Vector2fRecord(ship.getVelocity(), 2);
//        acc = new Vector2fRecord(new Vector2f(), 3);
        ang = new FloatRecord(ship.getFacing(), 4);
        angVel = new FloatRecord(ship.getAngularVelocity(), 5);
//        angAcc = new FloatRecord(0f, 6);
        hull = new FloatRecord(ship.getHullLevel(), 7);
        flux = new FloatRecord(ship.getFluxLevel(), 8);

        packer = ByteBuffer.allocate(100);
    }

    public byte[] pack() throws IOException {
        packer.clear();

        // so packer type can be identified
        packer.putInt(TYPE_ID);

        if (loc.update(ship.getLocation())) loc.write(packer);
        if (vel.update(ship.getVelocity())) vel.write(packer);
//        if (acc.update(new Vector2f())) acc.write(output);
        if (ang.update(ship.getFacing())) ang.write(packer);
        if (angVel.update(ship.getAngularVelocity())) angVel.write(packer);
//        if (angAcc.update(0f)) angAcc.update(0f);
        if (hull.update(ship.getHullLevel())) hull.write(packer);
        if (flux.update(ship.getFluxLevel())) flux.write(packer);

        packer.flip();
        byte[] out = new byte[packer.remaining()];
        packer.get(out);

        return out;
    }

    public static List<ARecord> unpack(ByteBuf in) {
        List<ARecord> out = new ArrayList<>();

        //iterate until new entity encountered
        outer:
        while (true) {
            // mark index so it can be reset if new entity is encountered
            in.markReaderIndex();

            if (in.readerIndex() + 4 >= in.writerIndex()) {
                return out;
            }

            int type = in.readInt();

            switch(type) {
                case IDTypes.FLOAT_RECORD:
                    out.add(FloatRecord.read(in));
                    break;
                case IDTypes.V2F_RECORD:
                    out.add(Vector2fRecord.read(in));
                    break;
                case IDTypes.SHIP:
                    //reset index if encountering a new entity
                    in.resetReaderIndex();
                    break outer;
            }
        }

        return out;
    }

    @Override
    public int getTypeId() {
        return TYPE_ID;
    }
}
