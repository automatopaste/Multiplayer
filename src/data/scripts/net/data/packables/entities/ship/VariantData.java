package data.scripts.net.data.packables.entities.ship;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.DestExecute;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.ListRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.plugins.MPPlugin;

import java.util.ArrayList;

public class VariantData extends BasePackable {

    public static byte TYPE_ID;

    private int numFluxCapacitors;
    private int numFluxVents;
    private java.lang.String fleetMemberID;
    private java.util.List<java.lang.String> weaponIDs;
    private java.util.List<java.lang.String> weaponSlots;

    public VariantData(short instanceID, final ShipVariantAPI variant, final java.lang.String id) {
        super(instanceID);

        addRecord(new RecordLambda<>(
                IntRecord.getDefault(),
                new SourceExecute<Integer>() {
                    @Override
                    public Integer get() {
                        return variant.getNumFluxCapacitors();
                    }
                },
                new DestExecute<Integer>() {
                    @Override
                    public void execute(BaseRecord<Integer> record, BasePackable packable) {
                        VariantData variantData = (VariantData) packable;
                        variantData.setNumFluxCapacitors(record.getValue());
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                IntRecord.getDefault(),
                new SourceExecute<Integer>() {
                    @Override
                    public Integer get() {
                        return variant.getNumFluxVents();
                    }
                },
                new DestExecute<Integer>() {
                    @Override
                    public void execute(BaseRecord<Integer> record, BasePackable packable) {
                        VariantData variantData = (VariantData) packable;
                        variantData.setNumFluxVents(record.getValue());
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                StringRecord.getDefault(),
                new SourceExecute<java.lang.String>() {
                    @Override
                    public java.lang.String get() {
                        return id;
                    }
                },
                new DestExecute<java.lang.String>() {
                    @Override
                    public void execute(BaseRecord<java.lang.String> record, BasePackable packable) {
                        VariantData variantData = (VariantData) packable;
                        variantData.setFleetMemberID(id);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                new ListRecord<>(new ArrayList<java.lang.String>(), StringRecord.TYPE_ID),
                new SourceExecute<java.util.List<java.lang.String>>() {
                    @Override
                    public java.util.List<java.lang.String> get() {
                        java.util.List<java.lang.String> weaponIDs = new ArrayList<>();
                        for (java.lang.String slot : variant.getNonBuiltInWeaponSlots()) {
                            java.lang.String weaponID = variant.getWeaponId(slot);

                            if (weaponID == null) continue;

                            weaponIDs.add(weaponID);
                        }
                        return weaponIDs;
                    }
                },
                new DestExecute<java.util.List<java.lang.String>>() {
                    @Override
                    public void execute(BaseRecord<java.util.List<java.lang.String>> record, BasePackable packable) {
                        VariantData variantData = (VariantData) packable;
                        variantData.setWeaponIDs(record.getValue());
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                new ListRecord<>(new ArrayList<java.lang.String>(), StringRecord.TYPE_ID),
                new SourceExecute<java.util.List<java.lang.String>>() {
                    @Override
                    public java.util.List<java.lang.String> get() {
                        java.util.List<java.lang.String> weaponSlots = new ArrayList<>();
                        for (java.lang.String slot : variant.getNonBuiltInWeaponSlots()) {
                            java.lang.String weaponID = variant.getWeaponId(slot);

                            if (weaponID == null) continue;

                            weaponSlots.add(slot);
                        }
                        return weaponSlots;
                    }
                },
                new DestExecute<java.util.List<java.lang.String>>() {
                    @Override
                    public void execute(BaseRecord<java.util.List<java.lang.String>> record, BasePackable packable) {
                        VariantData variantData = (VariantData) packable;
                        variantData.setWeaponSlots(record.getValue());
                    }
                }
        ));
    }

    @Override
    public void init(MPPlugin plugin) {

    }

    @Override
    public void update(float amount) {

    }

    @Override
    public void delete() {

    }

    @Override
    public byte getTypeID() {
        return TYPE_ID;
    }

//    @Override
//    public void write(boolean force, ByteBuf dest) {
//        initialForce = true;
//
//        super.write(force, dest);
//    }

    public int getNumFluxCapacitors() {
        return numFluxCapacitors;
    }

    public void setNumFluxCapacitors(int numFluxCapacitors) {
        this.numFluxCapacitors = numFluxCapacitors;
    }

    public int getNumFluxVents() {
        return numFluxVents;
    }

    public void setNumFluxVents(int numFluxVents) {
        this.numFluxVents = numFluxVents;
    }

    public java.lang.String getFleetMemberID() {
        return fleetMemberID;
    }

    public void setFleetMemberID(java.lang.String fleetMemberID) {
        this.fleetMemberID = fleetMemberID;
    }

    public java.util.List<java.lang.String> getWeaponIDs() {
        return weaponIDs;
    }

    public void setWeaponIDs(java.util.List<java.lang.String> weaponIDs) {
        this.weaponIDs = weaponIDs;
    }

    public java.util.List<java.lang.String> getWeaponSlots() {
        return weaponSlots;
    }

    public void setWeaponSlots(java.util.List<java.lang.String> weaponSlots) {
        this.weaponSlots = weaponSlots;
    }
}
