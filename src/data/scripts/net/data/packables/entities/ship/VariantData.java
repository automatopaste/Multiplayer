package data.scripts.net.data.packables.entities.ship;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.DestExecute;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.collections.ListRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.util.ArrayList;
import java.util.List;

public class VariantData extends BasePackable {

    public static byte TYPE_ID;

    private int numFluxCapacitors;
    private int numFluxVents;
    private String fleetMemberID;
    private List<String> weaponIDs;
    private List<String> weaponSlots;

    public VariantData(short instanceID, final ShipVariantAPI variant, final String id) {
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
                new SourceExecute<String>() {
                    @Override
                    public String get() {
                        return id;
                    }
                },
                new DestExecute<String>() {
                    @Override
                    public void execute(BaseRecord<String> record, BasePackable packable) {
                        VariantData variantData = (VariantData) packable;
                        variantData.setFleetMemberID(record.getValue());
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                new ListRecord<>(new ArrayList<String>(), StringRecord.TYPE_ID),
                new SourceExecute<List<String>>() {
                    @Override
                    public List<String> get() {
                        List<String> weaponIDs = new ArrayList<>();
                        for (String slot : variant.getNonBuiltInWeaponSlots()) {
                            String weaponID = variant.getWeaponId(slot);

                            if (weaponID == null) continue;

                            weaponIDs.add(weaponID);
                        }
                        return weaponIDs;
                    }
                },
                new DestExecute<List<String>>() {
                    @Override
                    public void execute(BaseRecord<List<String>> record, BasePackable packable) {
                        VariantData variantData = (VariantData) packable;
                        variantData.setWeaponIDs(record.getValue());
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                new ListRecord<>(new ArrayList<String>(), StringRecord.TYPE_ID),
                new SourceExecute<List<String>>() {
                    @Override
                    public List<String> get() {
                        List<String> weaponSlots = new ArrayList<>();
                        for (String slot : variant.getNonBuiltInWeaponSlots()) {
                            String weaponID = variant.getWeaponId(slot);

                            if (weaponID == null) continue;

                            weaponSlots.add(slot);
                        }
                        return weaponSlots;
                    }
                },
                new DestExecute<List<String>>() {
                    @Override
                    public void execute(BaseRecord<List<String>> record, BasePackable packable) {
                        VariantData variantData = (VariantData) packable;
                        variantData.setWeaponSlots(record.getValue());
                    }
                }
        ));
    }

    @Override
    public void init(MPPlugin plugin, InboundEntityManager manager) {

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

    public String getFleetMemberID() {
        return fleetMemberID;
    }

    public void setFleetMemberID(String fleetMemberID) {
        this.fleetMemberID = fleetMemberID;
    }

    public List<String> getWeaponIDs() {
        return weaponIDs;
    }

    public void setWeaponIDs(List<String> weaponIDs) {
        this.weaponIDs = weaponIDs;
    }

    public List<String> getWeaponSlots() {
        return weaponSlots;
    }

    public void setWeaponSlots(List<String> weaponSlots) {
        this.weaponSlots = weaponSlots;
    }
}
