package data.scripts.net.data.packables.entities.ships;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponGroupType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import data.scripts.net.data.packables.DestExecute;
import data.scripts.net.data.packables.EntityData;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.records.ByteRecord;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.net.data.records.collections.ListenArrayRecord;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class VariantData extends EntityData {

    public static byte TYPE_ID;

    private int numFluxCapacitors;
    private int numFluxVents;
    private String fleetMemberID;
    private List<String> hullmods;
    private Map<String, Byte> slotIDs;
    private Map<Byte, String> slotIntIDs;
    private Map<Byte, String> weaponSlots;
    private List<WeaponGroupSpec> weaponGroups;

    public VariantData(short instanceID, final ShipVariantAPI variant, final String id) {
        super(instanceID);

        final Map<String, Integer> slotIDs = new HashMap<>();
        final Map<Integer, String> weaponSlots = new HashMap<>();
        try {
            slotIDs.putAll(VariantData.getSlotIDs(variant));

            List<String> fitted = new ArrayList<>(variant.getFittedWeaponSlots());
            for (String s : fitted) {
                String weaponID = variant.getWeaponId(s);
                int slotID = slotIDs.get(s);
                weaponSlots.put(slotID, weaponID);
            }
        } catch (NullPointerException ignored) {}

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
                    public void execute(Integer value, EntityData packable) {
                        VariantData variantData = (VariantData) packable;
                        variantData.setNumFluxCapacitors(value);
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
                    public void execute(Integer value, EntityData packable) {
                        VariantData variantData = (VariantData) packable;
                        variantData.setNumFluxVents(value);
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
                    public void execute(String value, EntityData packable) {
                        VariantData variantData = (VariantData) packable;
                        variantData.setFleetMemberID(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                new ListenArrayRecord<>(new ArrayList<String>(), StringRecord.TYPE_ID).setDebugText("hullmods"),
                new SourceExecute<List<String>>() {
                    @Override
                    public List<String> get() {
                        return new ArrayList<>(variant.getHullMods());
                    }
                },
                new DestExecute<List<String>>() {
                    @Override
                    public void execute(List<String> value, EntityData packable) {
                        VariantData variantData = (VariantData) packable;
                        variantData.setHullmods(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                new ListenArrayRecord<>(new ArrayList<Byte>(), ByteRecord.TYPE_ID).setDebugText("slot ids"),
                new SourceExecute<List<Byte>>() {
                    @Override
                    public List<Byte> get() {
                        List<Byte> out = new ArrayList<>();

                        for (String s : slotIDs.keySet()) {
                            int i = slotIDs.get(s);

                            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);

                            out.add((byte) bytes.length);
                            for (byte aByte : bytes) {
                                out.add(aByte);
                            }

                            out.add((byte) i);
                        }

                        return out;
                    }
                },
                new DestExecute<List<Byte>>() {
                    @Override
                    public void execute(List<Byte> value, EntityData packable) {
                        Map<String, Byte> slots = new HashMap<>();
                        Map<Byte, String> intSlots = new HashMap<>();

                        for (Iterator<Byte> iterator = value.iterator(); iterator.hasNext();) {
                            int size = iterator.next() & 0xFF;
                            byte[] bytes = new byte[size];
                            for (int i = 0; i < size; i++) bytes[i] = iterator.next();

                            String idString = new String(bytes, StandardCharsets.UTF_8);

                            int id = iterator.next();

                            slots.put(idString, (byte) id);
                            intSlots.put((byte) id, idString);
                        }

                        setSlotIDs(slots);
                        setSlotIntIDs(intSlots);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                new ListenArrayRecord<>(new ArrayList<Byte>(), ByteRecord.TYPE_ID).setDebugText("weapon slots"),
                new SourceExecute<List<Byte>>() {
                    @Override
                    public List<Byte> get() {
                        List<Byte> out = new ArrayList<>();

                        for (int w : weaponSlots.keySet()) {
                            String i = weaponSlots.get(w);

                            byte[] bytes = i.getBytes(StandardCharsets.UTF_8);

                            out.add((byte) bytes.length);
                            for (byte aByte : bytes) {
                                out.add(aByte);
                            }

                            out.add((byte) w);
                        }

                        return out;
                    }
                },
                new DestExecute<List<Byte>>() {
                    @Override
                    public void execute(List<Byte> value, EntityData packable) {
                        Map<Byte, String> slots = new HashMap<>();

                        for (Iterator<Byte> iterator = value.iterator(); iterator.hasNext();) {
                            int size = iterator.next() & 0xFF;
                            byte[] bytes = new byte[size];
                            for (int i = 0; i < size; i++) bytes[i] = iterator.next();

                            String idString = new String(bytes, StandardCharsets.UTF_8);

                            byte slot = iterator.next();

                            slots.put(slot, idString);
                        }

                        setWeaponSlots(slots);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                new ListenArrayRecord<>(new ArrayList<Byte>(), ByteRecord.TYPE_ID).setDebugText("weapon groups"),
                new SourceExecute<List<Byte>>() {
                    @Override
                    public List<Byte> get() {
                        List<Byte> out = new ArrayList<>();

                        List<WeaponGroupSpec> weaponGroups = variant.getWeaponGroups();
                        for (WeaponGroupSpec group : weaponGroups) {
                            byte config = 0x00;
                            if (group.getType() == WeaponGroupType.ALTERNATING) config |= 0b10000000;
                            if (group.isAutofireOnByDefault()) config |= 0b01000000;

                            List<String> slots = group.getSlots();
                            config |= slots.size() & 0b00111111; // 6 bit int for group size (max size 64 weapons)
                            out.add(config);

                            for (String slotID : slots) {
                                out.add((byte) (int) slotIDs.get(slotID));
                            }
                        }

                        return out;
                    }
                },
                new DestExecute<List<Byte>>() {
                    @Override
                    public void execute(List<Byte> value, EntityData packable) {
                        List<WeaponGroupSpec> weaponGroupSpecs = new ArrayList<>();

                        for (Iterator<Byte> iterator = value.iterator(); iterator.hasNext();) {
                            WeaponGroupSpec spec = new WeaponGroupSpec();

                            Byte config = iterator.next();
                            boolean alternating = (config & 0b10000000) >>> 7 > 0;
                            boolean autofire = (config & 0b01000000) >>> 6 > 0;

                            spec.setAutofireOnByDefault(autofire);
                            spec.setType(alternating ? WeaponGroupType.ALTERNATING : WeaponGroupType.LINKED);

                            int numSlots = config & 0b00111111;
                            for (int i = 0; i < numSlots; i++) {
                                int slotID = iterator.next() & 0xFF;
                                spec.addSlot(getSlotIntIDs().get((byte) slotID));
                            }

                            weaponGroupSpecs.add(spec);
                        }

                        setWeaponGroups(weaponGroupSpecs);
                    }
                }
        ));
    }

    @Override
    public void init(MPPlugin plugin, InboundEntityManager manager) {

    }

    @Override
    public void update(float amount, BaseEntityManager manager) {

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

    public List<String> getHullmods() {
        return hullmods;
    }

    public void setHullmods(List<String> hullmods) {
        this.hullmods = hullmods;
    }

    public Map<String, Byte> getSlotIDs() {
        return slotIDs;
    }

    public void setSlotIDs(Map<String, Byte> slotIDs) {
        this.slotIDs = slotIDs;
    }

    public Map<Byte, String> getSlotIntIDs() {
        return slotIntIDs;
    }

    public void setSlotIntIDs(Map<Byte, String> slotIntIDs) {
        this.slotIntIDs = slotIntIDs;
    }

    public Map<Byte, String> getWeaponSlots() {
        return weaponSlots;
    }

    public void setWeaponSlots(Map<Byte, String> weaponSlots) {
        this.weaponSlots = weaponSlots;
    }

    public void setWeaponGroups(List<WeaponGroupSpec> weaponGroups) {
        this.weaponGroups = weaponGroups;
    }

    public List<WeaponGroupSpec> getWeaponGroups() {
        return weaponGroups;
    }

    public static Map<String, Integer> getSlotIDs(ShipVariantAPI variant) {
        final Map<String, Integer> slotIDs = new HashMap<>();
        List<WeaponSlotAPI> allWeaponSlotsCopy = variant.getHullSpec().getAllWeaponSlotsCopy();
        for (int i = 0; i < allWeaponSlotsCopy.size(); i++) {
            WeaponSlotAPI slot = allWeaponSlotsCopy.get(i);
            slotIDs.put(slot.getId(), i);
        }
        return slotIDs;
    }
}
