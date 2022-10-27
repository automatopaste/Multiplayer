package data.scripts.net.data.packables.entities.variant;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.ListRecord;
import data.scripts.net.data.records.StringRecord;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class VariantData extends BasePackable {

    public VariantData(int instanceID, final ShipVariantAPI variant, final String id) {
        super(instanceID);

        putRecord(new IntRecord(new BaseRecord.DeltaFunc<Integer>() {
            @Override
            public Integer get() {
                return variant.getNumFluxCapacitors();
            }
        }, VariantIDs.CAPACITORS));
        putRecord(new IntRecord(new BaseRecord.DeltaFunc<Integer>() {
            @Override
            public Integer get() {
                return variant.getNumFluxVents();
            }
        }, VariantIDs.VENTS));
        putRecord(new StringRecord(new BaseRecord.DeltaFunc<String>() {
            @Override
            public String get() {
                return id;
            }
        }, VariantIDs.SHIP_ID));
        putRecord(new ListRecord<>(new BaseRecord.DeltaFunc<List<StringRecord>>() {
            @Override
            public List<StringRecord> get() {
                List<StringRecord> weaponIDs = new ArrayList<>();
                for (String slot : variant.getNonBuiltInWeaponSlots()) {
                    String weaponID = variant.getWeaponId(slot);

                    if (weaponID == null) continue;

                    weaponIDs.add(new StringRecord(weaponID, -1));
                }
                return weaponIDs;
            }
        }, VariantIDs.WEAPON_IDS, StringRecord.TYPE_ID));
        putRecord(new ListRecord<>(new BaseRecord.DeltaFunc<List<StringRecord>>() {
            @Override
            public List<StringRecord> get() {
                List<StringRecord> weaponSlots = new ArrayList<>();
                for (String slot : variant.getNonBuiltInWeaponSlots()) {
                    String weaponID = variant.getWeaponId(slot);

                    if (weaponID == null) continue;

                    weaponSlots.add(new StringRecord(slot, -1));
                }
                return weaponSlots;
            }
        }, VariantIDs.WEAPON_SLOTS, StringRecord.TYPE_ID));
    }

    @Override
    public int getTypeID() {
        return VariantIDs.TYPE_ID;
    }

    @Override
    public void write(boolean force, ByteBuf dest) {
        initialForce = true;

        super.write(force, dest);
    }
}
