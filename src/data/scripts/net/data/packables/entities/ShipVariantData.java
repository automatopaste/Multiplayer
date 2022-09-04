package data.scripts.net.data.packables.entities;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import data.scripts.net.data.BasePackable;
import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.plugins.MPPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShipVariantData extends BasePackable {
    public static int TYPE_ID;

    private final IntRecord capacitors;
    private final IntRecord vents;
    private final StringRecord shipId;
    private final List<StringRecord> weaponIds;
    private final List<StringRecord> weaponSlots;

    private static final int CAPACITORS = 1;
    private static final int VENTS = 2;
    private static final int SHIP_ID = 3;
    private static final int WEAPONS = 69;

    public ShipVariantData(int instanceID, ShipVariantAPI variant, String id) {
        super(instanceID);

        int c = (variant == null) ? 0 : variant.getNumFluxCapacitors();
        int v = (variant == null) ? 0 : variant.getNumFluxVents();

        capacitors = new IntRecord(c);
        vents = new IntRecord(v);
        shipId = new StringRecord(id);

        weaponIds = new ArrayList<>();
        weaponSlots = new ArrayList<>();

        if (variant != null) {
            for (String slot : variant.getNonBuiltInWeaponSlots()) {
                String weaponId = variant.getWeaponId(slot);

                if (weaponId == null) continue;

                weaponSlots.add(new StringRecord(slot));
                weaponIds.add(new StringRecord(weaponId));
            }
        }
    }

    public ShipVariantData(int instanceID, Map<Integer, BaseRecord<?>> records) {
        super(instanceID);

        BaseRecord<?> temp;
        temp = records.get(CAPACITORS);
        capacitors = (temp == null) ? new IntRecord(0) : (IntRecord) temp;
        temp = records.get(VENTS);
        vents = (temp == null) ? new IntRecord(0) : (IntRecord) temp;
        temp = records.get(SHIP_ID);
        shipId = (temp == null) ? new StringRecord("DEFAULT_SHIP_ID_FOR_VARIANT") : (StringRecord) temp;

        List<StringRecord> tempWeapons = new ArrayList<>();

        int gaming = WEAPONS;
        temp = records.get(gaming);
        while (temp != null) {
            tempWeapons.add((StringRecord) temp);
            gaming++;

            temp = records.get(gaming);
        }

        int num = tempWeapons.size();
        int num2 = num / 2;

        weaponIds = new ArrayList<>();
        weaponSlots = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            StringRecord record = tempWeapons.get(i);

            if (i < num2) weaponIds.add(record);
            else weaponSlots.add(record);
        }
    }


    @Override
    public void destinationUpdate() {

    }

    @Override
    public void destinationInit(MPPlugin plugin) {

    }

    @Override
    public void destinationDelete() {

    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public void updateFromDelta(BasePackable delta) {
        ShipVariantData d = (ShipVariantData) delta;
        if (d.getCapacitors() != null) capacitors.forceUpdate(d.getCapacitors().getRecord());
        if (d.getVents() != null) vents.forceUpdate(d.getVents().getRecord());
        if (d.getShipId() != null) shipId.forceUpdate(d.getShipId().getRecord());
        if (d.getWeaponIds() != null && !d.getWeaponIds().isEmpty()) {
            weaponIds.clear();
            weaponIds.addAll(d.getWeaponIds());
        }
        if (d.getWeaponSlots() != null && !d.getWeaponSlots().isEmpty()) {
            weaponSlots.clear();
            weaponSlots.addAll(d.getWeaponSlots());
        }
    }

    @Override
    protected boolean write(boolean flush) {
        capacitors.write(packer, CAPACITORS);
        vents.write(packer, VENTS);
        shipId.write(packer, SHIP_ID);

        int gaming = WEAPONS;
        for (StringRecord weaponId : weaponIds) {
            weaponId.write(packer, gaming);
            gaming++;
        }
        for (StringRecord weaponSlot : weaponSlots) {
            weaponSlot.write(packer, gaming);
            gaming++;
        }

        return true;
    }

    @Override
    public int getTypeId() {
        return TYPE_ID;
    }

    @Override
    public BasePackable unpack(int instanceID, Map<Integer, BaseRecord<?>> records) {
        return new ShipVariantData(instanceID, records);
    }

    public IntRecord getCapacitors() {
        return capacitors;
    }

    public IntRecord getVents() {
        return vents;
    }

    public StringRecord getShipId() {
        return shipId;
    }

    public List<StringRecord> getWeaponIds() {
        return weaponIds;
    }

    public List<StringRecord> getWeaponSlots() {
        return weaponSlots;
    }

    public static void setTypeId(int typeId) {
        ShipVariantData.TYPE_ID = typeId;
    }
}
