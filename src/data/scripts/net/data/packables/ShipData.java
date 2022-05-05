package data.scripts.net.data.packables;

import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.IDTypes;
import data.scripts.net.data.records.FloatRecord;
import data.scripts.net.data.records.Vector2fRecord;

/**
 * Container for tracking network data about a ship
 */
public class ShipData extends APackable {
    private final Vector2fRecord loc;
    private final Vector2fRecord vel;
    private final FloatRecord ang;
    private final FloatRecord angVel;
    private final FloatRecord hull;
    private final FloatRecord flux;

    private final ShipAPI ship;

    public ShipData(ShipAPI ship) {
        this.ship = ship;

        loc = new Vector2fRecord(ship.getLocation(), 1).setUseDecimalPrecision(false);
        vel = new Vector2fRecord(ship.getVelocity(), 2).setUseDecimalPrecision(false);
        ang = new FloatRecord(ship.getFacing(), 4).setUseDecimalPrecision(false);
        angVel = new FloatRecord(ship.getAngularVelocity(), 5).setUseDecimalPrecision(false);
        hull = new FloatRecord(ship.getHullLevel(), 7);
        flux = new FloatRecord(ship.getFluxLevel(), 8);
    }

    @Override
    void update() {
        if (loc.update(ship.getLocation())) loc.write(packer);
        if (vel.update(ship.getVelocity())) vel.write(packer);
        if (ang.update(ship.getFacing())) ang.write(packer);
        if (angVel.update(ship.getAngularVelocity())) angVel.write(packer);
        if (hull.update(ship.getHullLevel())) hull.write(packer);
        if (flux.update(ship.getFluxLevel())) flux.write(packer);
    }

    @Override
    public int getTypeId() {
        return IDTypes.SHIP;
    }
}
