package data.scripts.net.data.packables.entities.ship;

import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.DestExecute;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.records.ByteRecord;
import data.scripts.net.data.records.ConversionUtils;
import data.scripts.plugins.MPPlugin;

public class ShieldData extends BasePackable {

    public static byte TYPE_ID;

    public ShieldData(short instanceID, final ShieldAPI shield) {
        super(instanceID);

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
                    public void execute(BaseRecord<Byte> record, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        boolean active = record.getValue() == (byte) 1;
                        if (ship != null && ship.getShield() != null) {
                            if (active) ship.getShield().toggleOn();
                            else ship.getShield().toggleOff();
                        }
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("shield facing"),
                new SourceExecute<Byte>() {
                    @Override
                    public Byte get() {
                        return ConversionUtils.floatToByte(shield.getFacing(), 360f);
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(BaseRecord<Byte> record, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null && ship.getShield() != null) ship.getShield().forceFacing(ConversionUtils.byteToFloat(record.getValue(), 360f));
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("shield arc"),
                new SourceExecute<Byte>() {
                    @Override
                    public Byte get() {
                        return ConversionUtils.floatToByte(shield.getActiveArc(), 360f);
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(BaseRecord<Byte> record, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null && ship.getShield() != null) ship.getShield().setActiveArc(ConversionUtils.byteToFloat(record.getValue(), 360f));
                    }
                }
        ));
    }

    @Override
    public byte getTypeID() {
        return TYPE_ID;
    }

    @Override
    public void update(float amount) {

    }

    @Override
    public void init(MPPlugin plugin) {

    }

    @Override
    public void delete() {

    }
}
