package data.scripts.net.data.packables.entities.ships;

import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.packables.*;
import data.scripts.net.data.records.ByteRecord;
import data.scripts.net.data.records.Float16Record;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.client.ClientShipTable;
import data.scripts.plugins.MPPlugin;
import org.lazywizard.lazylib.MathUtils;

public class ShieldData extends EntityData {

    public static byte TYPE_ID;

    private final short instanceID;

    private ShieldAPI shield;

    /**
     * Shield must be passed in constructor
     */
    public ShieldData(short instanceID, final ShieldAPI shield, final ShipAPI ship) {
        super(instanceID);
        this.instanceID = instanceID;

        if (shield == null) throw new NullPointerException("Null shield object");

        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("shield_active"),
                new SourceExecute<Byte>() {
                    @Override
                    public Byte get() {
                         return shield.isOn() ? (byte) 1 : (byte) 0;
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(Byte value, EntityData packable) {
                        ShieldData shieldData = (ShieldData) packable;
                        ShieldAPI shield = shieldData.getShield();
                        if (shield != null) {
                            boolean active = value == (byte) 1;

                            if (active) shield.toggleOn();
                            else shield.toggleOff();
                        }
                    }
                }
        ));
        addInterpRecord(new InterpRecordLambda<>(
                Float16Record.getDefault().setDebugText("shield facing"),
                new SourceExecute<Float>() {
                    @Override
                    public Float get() {
                        return shield.getFacing() - ship.getFacing();
                    }
                },
                new DestExecute<Float>() {
                    @Override
                    public void execute(Float value, EntityData packable) {
                        ShieldData shieldData = (ShieldData) packable;
                        ShieldAPI shield = shieldData.getShield();
                        if (shield != null) {
                            shield.forceFacing(value);
                        }
                    }
                }
        ).setInterpExecute(new InterpExecute<Float>() {
            @Override
            public Float interpExecute(float progressive, Float v1, Float v2) {
                return v1 + (progressive * MathUtils.getShortestRotation(v1, v2));
            }
        }));
        addInterpRecord(new InterpRecordLambda<>(
                Float16Record.getDefault().setDebugText("shield arc"),
                new SourceExecute<Float>() {
                    @Override
                    public Float get() {
                        return shield.getActiveArc();
                    }
                },
                new DestExecute<Float>() {
                    @Override
                    public void execute(Float value, EntityData packable) {
                        ShieldData shieldData = (ShieldData) packable;
                        ShieldAPI shield = shieldData.getShield();
                        if (shield != null) {
                            shield.setActiveArc(value);
                        }
                    }
                }
        ).setInterpExecute(new InterpExecute<Float>() {
            @Override
            public Float interpExecute(float progressive, Float v1, Float v2) {
                return v1 + (progressive * MathUtils.getShortestRotation(v1, v2));
            }
        }));
    }

    @Override
    public byte getTypeID() {
        return TYPE_ID;
    }

    @Override
    public void update(float amount, BaseEntityManager manager) {
        if (shield == null && manager instanceof ClientShipTable) {
            ClientShipTable clientShipTable = (ClientShipTable) manager;

            setShield(clientShipTable.getTable()[instanceID].getShip().getShield());
        }
    }

    @Override
    public void init(MPPlugin plugin, InboundEntityManager manager) {

    }

    @Override
    public void delete() {

    }

    public ShieldAPI getShield() {
        return shield;
    }

    public void setShield(ShieldAPI shield) {
        this.shield = shield;
    }
}
