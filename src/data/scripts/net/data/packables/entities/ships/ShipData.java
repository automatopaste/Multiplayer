package data.scripts.net.data.packables.entities.ships;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.combat.entities.Ship;
import data.scripts.net.data.packables.*;
import data.scripts.net.data.records.*;
import data.scripts.net.data.records.collections.ListenArrayRecord;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.plugins.MPClientPlugin;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.ai.MPDefaultShipAIPlugin;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShipData extends BasePackable {
    public static byte TYPE_ID;

    private final Set<WeaponAPI> knownDisabled = new HashSet<>();
    private final Set<WeaponAPI> knownActive = new HashSet<>();

    private float[][] prevArmourGrid = null;

    private ShipAPI ship;
    private String hullID;
    private String fleetMemberID;
    private int owner;

    public ShipData(short instanceID, final ShipAPI ship) {
        super(instanceID);
        this.ship = ship;

        addRecord(new RecordLambda<>(
                StringRecord.getDefault().setDebugText("fleet member id"),
                new SourceExecute<String>() {
                    @Override
                    public String get() {
                        return ship.getFleetMemberId();
                    }
                },
                new DestExecute<String>() {
                    @Override
                    public void execute(String value, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        shipData.setFleetMemberID(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                StringRecord.getDefault().setDebugText("hullspec id"),
                new SourceExecute<String>() {
                    @Override
                    public String get() {
                        return ship.getHullSpec().getHullId();
                    }
                },
                new DestExecute<String>() {
                    @Override
                    public void execute(String value, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        shipData.setHullID(value);
                    }
                }
        ));
        addInterpRecord(new InterpRecordLambda<>(
                Vector2f32Record.getDefault().setDebugText("location"),
                new SourceExecute<Vector2f>() {
                    @Override
                    public Vector2f get() {
                        return new Vector2f(ship.getLocation());
                    }
                },
                new DestExecute<Vector2f>() {
                    @Override
                    public void execute(Vector2f value, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) ship.getLocation().set(value);
                    }
                }
        ));
        addInterpRecord(new InterpRecordLambda<>(
                Vector2f16Record.getDefault().setDebugText("velocity"),
                new SourceExecute<Vector2f>() {
                    @Override
                    public Vector2f get() {
                        return new Vector2f(ship.getVelocity());
                    }
                },
                new DestExecute<Vector2f>() {
                    @Override
                    public void execute(Vector2f value, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) ship.getVelocity().set(value);
                    }
                }
        ));
        addInterpRecord(new InterpRecordLambda<>(
                Float16Record.getDefault().setDebugText("facing"),
                new SourceExecute<Float>() {
                    @Override
                    public Float get() {
                        return ship.getFacing();
                    }
                },
                new DestExecute<Float>() {
                    @Override
                    public void execute(Float value, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) ship.setFacing(value);
                    }
                }
        ).setInterpExecute(new InterpExecute<Float>() {
            @Override
            public Float interpExecute(float progressive, Float v1, Float v2) {
                return v1 + (progressive * MathUtils.getShortestRotation(v1, v2));
            }
        }));
        addInterpRecord(new InterpRecordLambda<>(
                Float16Record.getDefault().setDebugText("angular vel"),
                new SourceExecute<Float>() {
                    @Override
                    public Float get() {
                        return ship.getAngularVelocity();
                    }
                },
                new DestExecute<Float>() {
                    @Override
                    public void execute(Float value, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) ship.setAngularVelocity(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("hitpoints"),
                new SourceExecute<Byte>() {
                    @Override
                    public Byte get() {
                        return ConversionUtils.floatToByte(ship.getHullLevel(), 1f);
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(Byte value, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) ship.setHitpoints(ship.getMaxHitpoints() * ConversionUtils.byteToFloat(value, 1f));
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("flux level"),
                new SourceExecute<Byte>() {
                    @Override
                    public Byte get() {
                        return ConversionUtils.floatToByte(ship.getFluxLevel(), 1f);
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(Byte value, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) ship.getFluxTracker().setCurrFlux(ship.getMaxFlux() * ConversionUtils.byteToFloat(value, 1f));
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("cr level"),
                new SourceExecute<Byte>() {
                    @Override
                    public Byte get() {
                        return ConversionUtils.floatToByte(ship.getCurrentCR(), 1f);
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(Byte value, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) ship.setCurrentCR(ConversionUtils.byteToFloat(value, 1f));
                    }
                }
        ));
        addInterpRecord(new InterpRecordLambda<>(
                Vector2f32Record.getDefault().setDebugText("mouse target"),
                new SourceExecute<Vector2f>() {
                    @Override
                    public Vector2f get() {
                        return new Vector2f(ship.getMouseTarget());
                    }
                },
                new DestExecute<Vector2f>() {
                    @Override
                    public void execute(Vector2f value, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) ship.getMouseTarget().set(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("fleet owner"),
                new SourceExecute<Byte>() {
                    @Override
                    public Byte get() {
                        return (byte) ship.getOwner();
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(Byte value, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) ship.setOwner(value);
                        shipData.setOwner(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                new ListenArrayRecord<>(new ArrayList<String>(), StringRecord.TYPE_ID).setDebugText("disabled weapon slot ids"),
                new SourceExecute<List<String>>() {
                    @Override
                    public List<String> get() {
                        List<String> out = new ArrayList<>();

                        for (WeaponAPI weapon : ship.getAllWeapons()) {
                            if (weapon.isDisabled() && !knownDisabled.contains(weapon)) {
                                out.add(weapon.getSlot().getId());
                                knownDisabled.add(weapon);
                            } else {
                                knownDisabled.remove(weapon);
                            }
                        }

                        return out;
                    }
                },
                new DestExecute<List<String>>() {
                    @Override
                    public void execute(List<String> value, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) {
                            for (String id : value) {
                                for (WeaponAPI weapon : ship.getAllWeapons()) {
                                    if (weapon.getSlot().getId().equals(id)) {
                                        weapon.disable();
                                    }
                                }
                            }
                        }
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                new ListenArrayRecord<>(new ArrayList<String>(), StringRecord.TYPE_ID).setDebugText("active weapon slot ids"),
                new SourceExecute<List<String>>() {
                    @Override
                    public List<String> get() {
                        List<String> out = new ArrayList<>();

                        for (WeaponAPI weapon : ship.getAllWeapons()) {
                            if (!weapon.isDisabled() && !knownActive.contains(weapon)) {
                                out.add(weapon.getSlot().getId());
                                knownActive.add(weapon);
                            } else {
                                knownActive.remove(weapon);
                            }
                        }

                        return out;
                    }
                },
                new DestExecute<List<String>>() {
                    @Override
                    public void execute(List<String> value, BasePackable packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) {
                            for (String id : value) {
                                for (WeaponAPI weapon : ship.getAllWeapons()) {
                                    if (weapon.getSlot().getId().equals(id)) {
                                        weapon.repair();
                                    }
                                }
                            }
                        }
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                new ListenArrayRecord<>(new ArrayList<Byte>(), ByteRecord.TYPE_ID).setDebugText("armour grid"),
                new SourceExecute<List<Byte>>() {
                    @Override
                    public List<Byte> get() {
                        List<ArmourSyncData> data = new ArrayList<>();

                        if (prevArmourGrid == null) {
                            prevArmourGrid = ship.getArmorGrid().getGrid();
                            return new ArrayList<>();
                        }

                        float[][] g = ship.getArmorGrid().getGrid();
                        for (int i = 0; i < g.length; i++) {
                            float[] row = g[i];
                            for (int j = 0; j < row.length; j++) {
                                float v = row[j];

                                if (v != prevArmourGrid[i][j]) {
                                    data.add(new ArmourSyncData(i, j, v));
                                }
                            }
                        }

                        float[][] t = new float[g.length][g[0].length];
                        for (int i = 0; i < g.length; i++) {
                            float[] row = g[i];
                            float[] tRow = t[i];
                            System.arraycopy(row, 0, tRow, 0, tRow.length);
                        }
                        prevArmourGrid = t;

                        List<Byte> out = new ArrayList<>();
                        // 2x6 bits for coordinates, 4 bits for armour fraction (16 discrete armour levels)
                        for (ArmourSyncData a : data) {
                            int v = Math.round(ship.getArmorGrid().getArmorFraction(a.x, a.y) * 0b00001111);
                            out.add((byte) ((a.x & 0b00111111) << 2 | (a.y & 0b00110000) >>> 4));
                            out.add((byte) ((a.y & 0b00001111) << 4 | v & 0b00001111));
                        }

                        return out;
                    }
                },
                new DestExecute<List<Byte>>() {
                    @Override
                    public void execute(List<Byte> value, BasePackable packable) {
                        for (int i = 0; i < value.size(); i += 2) {
                            byte b1 = value.get(i);
                            byte b2 = value.get(i + 1);

                            int x = (b1 & 0b11111100) >>> 2;
                            int y = ((b1 & 0b00000011) << 4) | ((b2 & 0b11110000) >>> 4);
                            int v = b2 & 0b00001111;
                            float a = getShip().getArmorGrid().getMaxArmorInCell() * v / 16f;

                            getShip().getArmorGrid().setArmorValue(x, y, a);
                        }
                    }
                }
        ));
    }

    @Override
    public void init(MPPlugin plugin, InboundEntityManager manager) {
        if (plugin.getType() != MPPlugin.PluginType.CLIENT) return;
        MPClientPlugin clientPlugin = (MPClientPlugin) plugin;

        CombatEngineAPI engine = Global.getCombatEngine();

        VariantData variantData = clientPlugin.getVariantDataMap().find(fleetMemberID);
        if (variantData == null) return;

        // update variant
        ShipHullSpecAPI hullSpec = Global.getSettings().getHullSpec(hullID);

        CombatFleetManagerAPI fleetManager = engine.getFleetManager(owner);

        if (hullSpec.getHullSize() != ShipAPI.HullSize.FIGHTER) {
            String hullVariantId = hullID + "_Hull";
            ShipVariantAPI variant = Global.getSettings().createEmptyVariant(
                    hullVariantId,
                    hullSpec
            );

            int numCapacitors = variantData.getNumFluxCapacitors();
            variant.setNumFluxCapacitors(numCapacitors);
            int numVents = variantData.getNumFluxVents();
            variant.setNumFluxVents(numVents);

            for (String id : variantData.getHullmods()) {
                variant.addMod(id);
            }

            List<String> weaponSlots = variantData.getWeaponSlots();
            List<String> weaponIds = variantData.getWeaponIDs();
            for (int i = 0; i < weaponSlots.size(); i++) {
                String slot = weaponSlots.get(i);
                variant.addWeapon(slot, weaponIds.get(i));
            }

            variant.autoGenerateWeaponGroups();

            FleetMemberType fleetMemberType = FleetMemberType.SHIP;
            FleetMemberAPI fleetMember = Global.getFactory().createFleetMember(fleetMemberType, variant);

            fleetManager.addToReserves(fleetMember);

            fleetMember.getCrewComposition().setCrew(fleetMember.getHullSpec().getMaxCrew());

            ship = fleetManager.spawnFleetMember(fleetMember, new Vector2f(0f, 0f), 0f, 0f);
            ship.setCRAtDeployment(0.7f);
            ship.setControlsLocked(false);

            // set fleetmember id to sync with server
            Ship s = (Ship) ship;
            s.setFleetMemberId(fleetMemberID);
        } else {
            throw new NullPointerException("Attempted fighter init in ship data");
        }

        ship.setShipAI(new MPDefaultShipAIPlugin());
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

    public ShipAPI getShip() {
        return ship;
    }

    public void setHullID(String hullID) {
        this.hullID = hullID;
    }

    public void setFleetMemberID(String fleetMemberID) {
        this.fleetMemberID = fleetMemberID;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public static class ArmourSyncData {
        public int x;
        public int y;
        public float v;

        public ArmourSyncData(int x, int y, float v) {
            this.x = x;
            this.y = y;
            this.v = v;
        }
    }
}
