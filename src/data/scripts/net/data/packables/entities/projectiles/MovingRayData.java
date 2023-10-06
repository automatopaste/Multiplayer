package data.scripts.net.data.packables.entities.projectiles;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.combat.entities.MovingRay;
import data.scripts.net.data.packables.DestExecute;
import data.scripts.net.data.packables.EntityData;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.packables.entities.ships.ShipData;
import data.scripts.net.data.datagen.ProjectileSpecDatastore;
import data.scripts.net.data.records.*;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.client.combat.entities.ClientShipTable;
import data.scripts.net.data.tables.server.combat.entities.ShipTable;
import data.scripts.plugins.MPPlugin;
import org.lwjgl.util.vector.Vector2f;

public class MovingRayData extends EntityData {

    public static byte TYPE_ID;

    private DamagingProjectileAPI projectile;
    private short weaponSpecID;
    private short shipID;
    private byte weaponID;
    private ShipAPI ship;
    private WeaponAPI weapon;
    private Vector2f location = new Vector2f(0f, 0f);
    private float facing = 0f;

    public MovingRayData(short instanceID, final MovingRay projectile, final short weaponSpecID, final ShipTable shipTable) {
        super(instanceID);

        addRecord(new RecordLambda<>(
                ShortRecord.getDefault().setDebugText("weapon spec id"),
                new SourceExecute<Short>() {
                    @Override
                    public Short get() {
                        return weaponSpecID;
                    }
                },
                new DestExecute<Short>() {
                    @Override
                    public void execute(Short value, EntityData packable) {
                        setWeaponSpecID(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ShortRecord.getDefault().setDebugText("ship id"),
                new SourceExecute<Short>() {
                    @Override
                    public Short get() {
                        try {
                            Short s = shipTable.getRegistered().get(projectile.getWeapon().getShip());
                            if (s == null) return -1;
                            else return s;
                        } catch (NullPointerException ignored) {
                            return -1;
                        }
                    }
                },
                new DestExecute<Short>() {
                    @Override
                    public void execute(Short value, EntityData packable) {
                        setShipID(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("weapon id"),
                new SourceExecute<Byte>() {
                    @Override
                    public Byte get() {
                        try {
                            short id = shipTable.getRegistered().get(projectile.getWeapon().getShip());
                            ShipData data = shipTable.getTable()[id];
                            Byte b = data.getWeaponSlots().getB(projectile.getWeapon());
                            if (b == null) return -1;
                            else return b;
                        } catch (NullPointerException ignored) {
                            return -1;
                        }
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(Byte value, EntityData packable) {
                        setWeaponID(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                Vector2f32Record.getDefault().setDebugText("location"),
                new SourceExecute<Vector2f>() {
                    @Override
                    public Vector2f get() {
                        return new Vector2f(projectile.getLocation());
                    }
                },
                new DestExecute<Vector2f>() {
                    @Override
                    public void execute(Vector2f value, EntityData packable) {
                        setLocation(new Vector2f(value));

                        DamagingProjectileAPI projectile = getProjectile();
                        if (projectile != null) {
                            projectile.getLocation().set(value);
                        }
                    }
                }
        ).setRate(9999f));
        addRecord(new RecordLambda<>(
                Vector2f16Record.getDefault().setDebugText("velocity"),
                new SourceExecute<Vector2f>() {
                    @Override
                    public Vector2f get() {
                        return new Vector2f(projectile.getVelocity());
                    }
                },
                new DestExecute<Vector2f>() {
                    @Override
                    public void execute(Vector2f value, EntityData packable) {
                        DamagingProjectileAPI projectile = getProjectile();
                        if (projectile != null) projectile.getVelocity().set(value);
                    }
                }
        ).setRate(9999f));
        addRecord(new RecordLambda<>(
                Float16Record.getDefault().setDebugText("facing"),
                new SourceExecute<Float>() {
                    @Override
                    public Float get() {
                        return projectile.getFacing();
                    }
                },
                new DestExecute<Float>() {
                    @Override
                    public void execute(Float value, EntityData packable) {
                        setFacing(value);

                        DamagingProjectileAPI projectile = getProjectile();
                        if (projectile != null) {
                            projectile.setFacing(value);
                        }
                    }
                }
        ).setRate(9999f));
        addRecord(new RecordLambda<>(
                Float16Record.getDefault().setDebugText("angular vel"),
                new SourceExecute<Float>() {
                    @Override
                    public Float get() {
                        return projectile.getAngularVelocity();
                    }
                },
                new DestExecute<Float>() {
                    @Override
                    public void execute(Float value, EntityData packable) {
                        DamagingProjectileAPI projectile = getProjectile();
                        if (projectile != null) projectile.setAngularVelocity(value);
                    }
                }
        ).setRate(9999f));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("hitpoints"),
                new SourceExecute<Byte>() {
                    @Override
                    public Byte get() {
                        return ConversionUtils.floatToByte(projectile.getHitpoints(), 1f);
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(Byte value, EntityData packable) {
                        DamagingProjectileAPI projectile = getProjectile();
                        if (projectile != null) projectile.setHitpoints(projectile.getMaxHitpoints() * ConversionUtils.byteToFloat(value, 1f));
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("owner"),
                new SourceExecute<Byte>() {
                    @Override
                    public Byte get() {
                        return (byte) projectile.getOwner();
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(Byte value, EntityData packable) {
                        DamagingProjectileAPI projectile = getProjectile();
                        if (projectile != null) projectile.setOwner(value);
                    }
                }
        ));
    }

    @Override
    public byte getTypeID() {
        return TYPE_ID;
    }

    @Override
    public void update(float amount, BaseEntityManager manager, MPPlugin plugin) {

    }

    @Override
    public void init(MPPlugin plugin, InboundEntityManager manager) {
        if (shipID != -1) {
            ClientShipTable clientShipTable = (ClientShipTable) plugin.getEntityManagers().get(ClientShipTable.class);
            ShipData shipData = clientShipTable.getShips().get(shipID);

            if (shipData != null && shipData.getShip() != null) {
                setShip(shipData.getShip());

                if (weaponID != -1) {
                    setWeapon(shipData.getWeaponSlots().getA(weaponID));
                }
            }
        }

        Vector2f vel = ship == null ? new Vector2f(0f, 0f) : new Vector2f(ship.getVelocity());

        String weaponID;
        ProjectileSpecDatastore datastore;
        try {
            datastore = (ProjectileSpecDatastore) plugin.getDatastore(ProjectileSpecDatastore.class);
            weaponID = datastore.getWeaponIDKeys().get(weaponSpecID);
        } catch (Exception e) {
            Global.getLogger(BallisticProjectileData.class).error("Unable to recover projectile ID from datastore");
            return;
        }

        try {
            projectile = (DamagingProjectileAPI) Global.getCombatEngine().spawnProjectile(
                    ship, weapon, weaponID, new Vector2f(location), facing, vel
            );
        } catch (Throwable n) {
            Global.getLogger(BallisticProjectileData.class).error(String.format("Error spawning projectile with weapon ID [%s]", weaponID));
        }
    }

    @Override
    public void delete() {

    }

    public void setWeaponSpecID(short weaponSpecID) {
        this.weaponSpecID = weaponSpecID;
    }

    public short getWeaponSpecID() {
        return weaponSpecID;
    }

    public ShipAPI getShip() {
        return ship;
    }

    public void setShip(ShipAPI ship) {
        this.ship = ship;
    }

    public void setWeapon(WeaponAPI weapon) {
        this.weapon = weapon;
    }

    public void setShipID(short shipID) {
        this.shipID = shipID;
    }

    public void setWeaponID(byte weaponID) {
        this.weaponID = weaponID;
    }

    public void setLocation(Vector2f location) {
        this.location = location;
    }

    public void setFacing(float facing) {
        this.facing = facing;
    }

    public DamagingProjectileAPI getProjectile() {
        return projectile;
    }
}
