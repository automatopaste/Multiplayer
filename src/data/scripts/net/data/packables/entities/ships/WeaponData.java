package data.scripts.net.data.packables.entities.ships;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import data.scripts.misc.MapSet;
import data.scripts.net.data.packables.DestExecute;
import data.scripts.net.data.packables.EntityData;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.records.ByteRecord;
import data.scripts.net.data.records.ConversionUtils;
import data.scripts.net.data.records.collections.ListenArrayRecord;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.util.*;

public class WeaponData extends EntityData {

    public static byte TYPE_ID;

    private final ShipAPI ship;
    private final MapSet<String, Byte> slotIDs;
    private final MapSet<Byte, WeaponAPI> weaponSlots;
    private final Map<Byte, Boolean> weaponFireStates = new HashMap<>();
    private final Map<Byte, Boolean> weaponDisableStates = new HashMap<>();

    public WeaponData(short instanceID, final ShipAPI ship, final MapSet<String, Byte> slotIDs, final MapSet<Byte, WeaponAPI> weaponSlots) {
        super(instanceID);

        this.ship = ship;
        this.slotIDs = slotIDs;
        this.weaponSlots = weaponSlots;

        for (byte b : slotIDs.setB()) {
            weaponFireStates.put(b, false);
            weaponDisableStates.put(b, false);
        }

        addRecord(new RecordLambda<>(
                new ListenArrayRecord<>(new ArrayList<Byte>(), ByteRecord.TYPE_ID).setDebugText("weapon slot id status"),
                new SourceExecute<List<Byte>>() {
                    @Override
                    public List<Byte> get() {
                        List<Byte> out = new ArrayList<>();

                        for (WeaponAPI weapon : weaponSlots.setB()) {
                            byte slotID = weaponSlots.getB(weapon);
                            byte states = 0x00;

                            boolean prevDisabled = weaponDisableStates.get(slotID);
                            boolean disabled = weapon.isDisabled();

                            if (disabled != prevDisabled) {
                                if (!disabled) states |= 0b10000000;
                            }

                            weaponDisableStates.put(slotID, disabled);

                            boolean prevFiring = weaponFireStates.get(slotID);
                            boolean firing = weapon.isFiring();

                            if (firing != prevFiring) {
                                if (!firing) states |= 0b01000000;
                            }

                            byte facing = ConversionUtils.floatToByte(weapon.getCurrAngle(), 360f);

                            out.add(slotID);
                            out.add(states);
                            out.add(facing);
                        }

                        return out;
                    }
                },
                new DestExecute<List<Byte>>() {
                    @Override
                    public void execute(List<Byte> value, EntityData packable) {
                        WeaponData weaponData = (WeaponData) packable;
                        ShipAPI ship = weaponData.ship;
                        if (ship != null) {
                            for (Iterator<Byte> iterator = value.iterator(); iterator.hasNext(); ) {
                                byte id = iterator.next();
                                byte states = iterator.next();
                                byte facing = iterator.next();

                                boolean isDisabled = (states & 0b10000000) != 0x00;
                                boolean isFiring = (states & 0b01000000) != 0x00;

                                WeaponAPI weapon = weaponSlots.getA(id);
                                if (weapon == null) {
                                    continue;
                                }

                                if (isDisabled) {
                                    weapon.disable();
                                } else {
                                    weapon.repair();
                                }

                                weapon.setForceFireOneFrame(isFiring);

                                float angle = ConversionUtils.byteToFloat(facing, 360f);
                                weapon.setCurrAngle(angle);
                            }
                        }
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("weapon group autofire status"),
                new SourceExecute<Byte>() {
                    @Override
                    public Byte get() {
                        List<WeaponGroupAPI> groups = ship.getWeaponGroupsCopy();

                        byte b = 0x00;
                        byte f = 0b00000001;
                        for (WeaponGroupAPI group : groups) {
                            if (group.isAutofiring()) b |= f;
                            f <<= 1;
                        }

                        return b;
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(Byte value, EntityData packable) {
                        WeaponData weaponData = (WeaponData) packable;
                        ShipAPI ship = weaponData.ship;
                        if (ship == null) return;

                        byte b = value;
                        byte f = 0b00000001;
                        for (WeaponGroupAPI group : ship.getWeaponGroupsCopy()) {
                            if ((b & f) == 0) {
                                group.toggleOff();
                            } else {
                                group.toggleOn();
                            }
                            f <<= 1;
                        }
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("selected group"),
                new SourceExecute<Byte>() {
                    @Override
                    public Byte get() {
                        WeaponGroupAPI selected = ship.getSelectedGroupAPI();

                        byte b = 0;
                        for (WeaponGroupAPI group : ship.getWeaponGroupsCopy()) {
                            if (group.equals(selected)) {
                                return b;
                            }

                            b++;
                        }

                        return b;
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(Byte value, EntityData packable) {
                        ship.giveCommand(ShipCommand.SELECT_GROUP, null, value);
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
        if (plugin.getType() == MPPlugin.PluginType.CLIENT) {

        }
    }

    @Override
    public void init(MPPlugin plugin, InboundEntityManager manager) {

    }

    @Override
    public void delete() {

    }
}
