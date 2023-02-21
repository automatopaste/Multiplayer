package data.scripts.net.data.packables.entities.ships;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.combat.entities.Ship;
import data.scripts.net.data.packables.*;
import data.scripts.net.data.packables.metadata.PlayerShipData;
import data.scripts.net.data.records.*;
import data.scripts.net.data.records.collections.ListenArrayRecord;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.client.ClientShipTable;
import data.scripts.net.data.tables.server.PlayerShips;
import data.scripts.plugins.MPClientPlugin;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.ai.MPDefaultAutofireAIPlugin;
import data.scripts.plugins.ai.MPDefaultShipAIPlugin;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

public class ShipData extends EntityData {

    public static byte TYPE_ID;

    private final Map<Integer, Boolean> prevStates = new HashMap<>();
    private final Set<ShipEngineControllerAPI.ShipEngineAPI> knownDisabledEngines = new HashSet<>();

    private float[][] prevArmourGrid = null;
    private boolean prevFlameout = false;

    private ShipAPI ship;
    private String hullID;
    private String fleetMemberID;
    private int owner;
    private Vector2f location = new Vector2f(0f, 0f);
    private float facing = 0f;

    private Map<String, Byte> slotIDs;
    private Map<Byte, String> slotIntIDs;
    private final Map<Byte, WeaponAPI> weaponSlots = new HashMap<>();
    private final Map<WeaponAPI, Byte> weaponSlotIDs = new HashMap<>();
    private Map<Integer, MPDefaultAutofireAIPlugin> autofirePluginSlots;

    private PlayerShipData.ShipControlOverride controlOverride;

    public ShipData(short instanceID, final ShipAPI ship, final PlayerShips playerShips) {
        super(instanceID);
        this.ship = ship;

        slotIDs = new HashMap<>();
        try {
            Map<String, Integer> m = VariantData.getSlotIDs(ship.getVariant());
            for (String s : m.keySet()) {
                slotIDs.put(s, (byte) (int) m.get(s));
            }

            genSlotIDs();
        } catch (NullPointerException ignored) {}

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
                    public void execute(String value, EntityData packable) {
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
                    public void execute(String value, EntityData packable) {
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
                    public void execute(Vector2f value, EntityData packable) {
                        setLocation(new Vector2f(value));

                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) {
                            ship.getLocation().set(value);
                        }
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
                    public void execute(Vector2f value, EntityData packable) {
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
                    public void execute(Float value, EntityData packable) {
                        setFacing(value);

                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) {
                            ship.setFacing(value);
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
                Float16Record.getDefault().setDebugText("angular vel"),
                new SourceExecute<Float>() {
                    @Override
                    public Float get() {
                        return ship.getAngularVelocity();
                    }
                },
                new DestExecute<Float>() {
                    @Override
                    public void execute(Float value, EntityData packable) {
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
                    public void execute(Byte value, EntityData packable) {
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
                    public void execute(Byte value, EntityData packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) ship.getFluxTracker().setCurrFlux(ship.getMaxFlux() * ConversionUtils.byteToFloat(value, 1f));
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("flux vent, overload, engine boost flags"),
                new SourceExecute<Byte>() {
                    @Override
                    public Byte get() {
                        byte b = 0x00;
//                        if (ship.getFluxTracker().isEngineBoostActive()) b |= 0b10000000;
                        if (ship.getFluxTracker().isOverloaded()) b |= 0b01000000;
                        if (ship.getFluxTracker().isVenting()) b |= 0b00100000;
                        return b;
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(Byte value, EntityData packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) {
                            byte b = value;
                            //if ((b & 0b10000000) >>> 7 != 0) // engine boost

                            if ((b & 0b01000000) >>> 6 != 0) {
                                ship.getFluxTracker().forceOverload(999f);
                            } else if (ship.getFluxTracker().isOverloaded()) {
                                ship.getFluxTracker().stopOverload();
                            }

                            if ((b & 0b00100000) >>> 5 != 0 && !ship.getFluxTracker().isVenting()) {
                                ship.giveCommand(ShipCommand.VENT_FLUX, null, 0);
                            }
                        }
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
                    public void execute(Byte value, EntityData packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) ship.setCurrentCR(ConversionUtils.byteToFloat(value, 1f));
                    }
                }
        ));
        addInterpRecord(new InterpRecordLambda<>(
                Vector2f16Record.getDefault().setDebugText("mouse target"),
                new SourceExecute<Vector2f>() {
                    @Override
                    public Vector2f get() {
                        return new Vector2f(ship.getMouseTarget());
                    }
                },
                new DestExecute<Vector2f>() {
                    @Override
                    public void execute(Vector2f value, EntityData packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) {
                            if (playerShips != null) {
                                for (PlayerShipData d : playerShips.getPlayerShips().values()) {
                                    if (d.getPlayerShip().equals(ship)) {
                                        ship.getMouseTarget().set(d.getMouseTarget());
                                        return;
                                    }
                                }
                            }

                            ship.getMouseTarget().set(value);
                        }
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
                    public void execute(Byte value, EntityData packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) ship.setOwner(value);
                        shipData.setOwner(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                new ListenArrayRecord<>(new ArrayList<Byte>(), ByteRecord.TYPE_ID).setDebugText("weapon slot id status"),
                new SourceExecute<List<Byte>>() {
                    @Override
                    public List<Byte> get() {
                        List<Byte> out = new ArrayList<>();

                        for (WeaponAPI weapon : ship.getAllWeapons()) {
                            int slotID;
                            try {
                                String s = weapon.getSlot().getId();
                                slotID = slotIDs.get(s);
                            } catch (NullPointerException n) {
                                n.printStackTrace();
                                continue;
                            }

                            Boolean p = prevStates.get(slotID);
                            if (p == null) {
                                prevStates.put(slotID, weapon.isDisabled());
                                p = weapon.isDisabled();
                            }
                            boolean prevDisabled = p;

                            if (weapon.isDisabled() != prevDisabled) {
                                byte b = (byte) slotID;
                                b &= 0b01111111;
                                if (!weapon.isDisabled()) b |= 0b10000000;
                                out.add(b);
                            }

                            prevStates.put(slotID, weapon.isDisabled());
                        }

                        return out;
                    }
                },
                new DestExecute<List<Byte>>() {
                    @Override
                    public void execute(List<Byte> value, EntityData packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) {
                            for (byte b : value) {
                                byte id = (byte) (b & 0b01111111);

                                boolean isActive = (b & 0b10000000) >>> 7 == 1;

                                if (isActive) {
                                    weaponSlots.get(id).repair();
                                } else {
                                    weaponSlots.get(id).disable();
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
                    public void execute(List<Byte> value, EntityData packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) {
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
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("num flameouts"),
                new SourceExecute<Byte>() {
                    @Override
                    public Byte get() {
                        if (prevFlameout && !ship.getEngineController().isFlamedOut()) {
                            prevFlameout = false;
                            return (byte) 2;
                        }
                        if (ship.getEngineController().isFlamedOut() || ship.getEngineController().isFlamingOut()) {
                            prevFlameout = true;
                            return (byte) 1;
                        }
                        return (byte) 0;
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(Byte value, EntityData packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) {
                            if (value == (byte) 2) {
                                ship.getMutableStats().getCombatEngineRepairTimeMult().modifyMult("mp", 0f);
                            } else if (value == (byte) 1) {
                                if (!(ship.getEngineController().isFlamingOut() || ship.getEngineController().isFlamedOut())) ship.getEngineController().forceFlameout(false);
                            } else if (value == (byte) 0) {
                                ship.getMutableStats().getCombatEngineRepairTimeMult().unmodify("mp");
                            }
                        }
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                new ListenArrayRecord<>(new ArrayList<Byte>(), ByteRecord.TYPE_ID).setDebugText("disabled engine ids"),
                new SourceExecute<List<Byte>>() {
                    @Override
                    public List<Byte> get() {
                        List<Byte> out = new ArrayList<>();

                        List<ShipEngineControllerAPI.ShipEngineAPI> shipEngines = ship.getEngineController().getShipEngines();
                        for (int i = 0; i < shipEngines.size(); i++) {
                            ShipEngineControllerAPI.ShipEngineAPI engine = shipEngines.get(i);
                            if (engine.isDisabled() && !knownDisabledEngines.contains(engine)) {
                                out.add((byte) i);
                                knownDisabledEngines.add(engine);
                            } else {
                                knownDisabledEngines.remove(engine);
                            }
                        }

                        return out;
                    }
                },
                new DestExecute<List<Byte>>() {
                    @Override
                    public void execute(List<Byte> value, EntityData packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) {
                            for (byte b : value) {
                                int id = b & 0xFF;

                                List<ShipEngineControllerAPI.ShipEngineAPI> shipEngines = getShip().getEngineController().getShipEngines();
                                for (int i = 0; i < shipEngines.size(); i++) {
                                    if (i == id) {
                                        ShipEngineControllerAPI.ShipEngineAPI engine = shipEngines.get(i);
                                        engine.disable();
                                    }
                                }
                            }
                        }
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                new ListenArrayRecord<>(new ArrayList<Byte>(), ByteRecord.TYPE_ID).setDebugText("firing weapon ids"),
                new SourceExecute<List<Byte>>() {
                    @Override
                    public List<Byte> get() {
                        List<Byte> out = new ArrayList<>();

                        for (WeaponAPI weapon : ship.getAllWeapons()) {
                            if (weapon.isFiring()) {
                                int id = slotIDs.get(weapon.getSlot().getId());
                                out.add((byte) id);
                            }
                        }
                        return out;
                    }
                },
                new DestExecute<List<Byte>>() {
                    @Override
                    public void execute(List<Byte> value, EntityData packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) {
                            for (byte b : value) {
                                int id = b & 0xFF;
                                MPDefaultAutofireAIPlugin plugin = autofirePluginSlots.get(id);
                                if (plugin != null) plugin.trigger();
                            }
                        }
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                new ListenArrayRecord<>(new ArrayList<Byte>(), ByteRecord.TYPE_ID).setDebugText("weapon facing"),
                new SourceExecute<List<Byte>>() {
                    @Override
                    public List<Byte> get() {
                        List<Byte> out = new ArrayList<>();
                        for (WeaponAPI weapon : ship.getAllWeapons()) {
                            byte id = (byte) (int) slotIDs.get(weapon.getSlot().getId());
                            out.add(id);

                            int v = ConversionUtils.floatToByte(weapon.getCurrAngle(), 360f);
                            out.add((byte) v);
                        }
                        return out;
                    }
                },
                new DestExecute<List<Byte>>() {
                    @Override
                    public void execute(List<Byte> value, EntityData packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) {
                            for (Iterator<Byte> iterator = value.iterator(); iterator.hasNext(); ) {
                                int id = iterator.next() & 0xFF;
                                float facing = ConversionUtils.byteToFloat(iterator.next(), 360f);

                                MPDefaultAutofireAIPlugin plugin = autofirePluginSlots.get(id);
                                if (plugin != null) plugin.setTargetFacing(facing);
                            }
                        }
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                new ListenArrayRecord<>(new ArrayList<Byte>(), ByteRecord.TYPE_ID).setDebugText("non autofiring weapon groups"),
                new SourceExecute<List<Byte>>() {
                    @Override
                    public List<Byte> get() {
                        List<Byte> out = new ArrayList<>();

                        List<WeaponGroupAPI> groups = ship.getWeaponGroupsCopy();
                        for (int i = 0; i < groups.size(); i++) {
                            WeaponGroupAPI group = groups.get(i);

                            byte g = (byte) (i & 0b00111111);
                            if (group.isAutofiring()) g |= 0b10000000;
                            if (group.getActiveWeapon().isFiring()) g |= 0b01000000;

                            byte cooldown = ConversionUtils.floatToByte(group.getActiveWeapon().getCooldownRemaining(), group.getActiveWeapon().getCooldown());

                            out.add(g);
                            out.add(cooldown);
                        }

                        return out;
                    }
                },
                new DestExecute<List<Byte>>() {
                    @Override
                    public void execute(List<Byte> value, EntityData packable) {
                        ShipData shipData = (ShipData) packable;
                        ShipAPI ship = shipData.getShip();
                        if (ship != null) {
                            for (Iterator<Byte> iterator = value.iterator(); iterator.hasNext();) {
                                byte aByte = iterator.next();
                                int g = aByte & 0xFF;

                                int i = g & 0b00111111;
                                boolean autofiring = (g & 0b10000000) != 0;
                                boolean firing = (g & 0b01000000) != 0;

                                if (firing) {
                                    ship.giveCommand(ShipCommand.SELECT_GROUP, null, i);
                                    ship.giveCommand(ShipCommand.FIRE, ship.getMouseTarget(), i);
                                }

                                List<WeaponGroupAPI> weaponGroupsCopy = ship.getWeaponGroupsCopy();
                                for (int j = 0; j < weaponGroupsCopy.size(); j++) {
                                    WeaponGroupAPI group = weaponGroupsCopy.get(j);

                                    if (j == i) {
                                        float cooldown = ConversionUtils.byteToFloat(iterator.next(), group.getActiveWeapon().getCooldown());

                                        group.getActiveWeapon().setRemainingCooldownTo(cooldown);

                                        if (group.isAutofiring() != autofiring) {
                                            ship.giveCommand(ShipCommand.TOGGLE_AUTOFIRE, null, i);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        ));
    }

    @Override
    public void init(MPPlugin plugin, InboundEntityManager manager) {
        if (plugin.getType() != MPPlugin.PluginType.CLIENT) return;
        MPClientPlugin clientPlugin = (MPClientPlugin) plugin;

        ClientShipTable shipTable = (ClientShipTable) manager;

        CombatEngineAPI engine = Global.getCombatEngine();

        VariantData variantData = clientPlugin.getVariantDataMap().find(fleetMemberID);
        if (variantData == null) return;

        // update variant
        ShipHullSpecAPI hullSpec = Global.getSettings().getHullSpec(hullID);

        CombatFleetManagerAPI fleetManager = engine.getFleetManager(owner);

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

        slotIDs = new HashMap<>(variantData.getSlotIDs());
        slotIntIDs = new HashMap<>();
        Map<Byte, String> fittedWeaponSlots = new HashMap<>(variantData.getWeaponSlots());

        for (String id : slotIDs.keySet()) {
            byte s = slotIDs.get(id);

            slotIntIDs.put(s, id);

            String weaponID = fittedWeaponSlots.get(s);
            if (weaponID != null) variant.addWeapon(id, weaponID);
        }

        List<WeaponGroupSpec> groupSpecs = variantData.getWeaponGroups();
        for (WeaponGroupSpec spec : groupSpecs) {
            spec.setAutofireOnByDefault(true);
            variant.addWeaponGroup(spec);
        }

        FleetMemberType fleetMemberType = FleetMemberType.SHIP;
        FleetMemberAPI fleetMember = Global.getFactory().createFleetMember(fleetMemberType, variant);

        fleetManager.addToReserves(fleetMember);

        fleetMember.getCrewComposition().setCrew(fleetMember.getHullSpec().getMaxCrew());

        ship = fleetManager.spawnFleetMember(fleetMember, location, facing, 0f);
        ship.setCRAtDeployment(0.7f);
        ship.setControlsLocked(false);

        Map<String, MPDefaultAutofireAIPlugin> autofirePluginSlotIDs = shipTable.getTempAutofirePlugins().get(ship.getId());
        autofirePluginSlots = new HashMap<>();

        List<WeaponAPI> weapons = ship.getAllWeapons();
        outer:
        for (WeaponAPI w : weapons) {
            for (String id : slotIDs.keySet()) {
                if (id.equals(w.getSlot().getId())) {
                    byte i = (byte) (int) slotIDs.get(id);

                    MPDefaultAutofireAIPlugin autofireAIPlugin = autofirePluginSlotIDs.get(id);
                    if (autofireAIPlugin != null) autofirePluginSlots.put((int) i, autofireAIPlugin);

                    continue outer;
                }
            }
        }

        genSlotIDs();

        shipTable.getTempAutofirePlugins().remove(ship.getId());

        // set fleetmember id to sync with server
        Ship s = (Ship) ship;
        s.setFleetMemberId(fleetMemberID);

        ship.setShipAI(new MPDefaultShipAIPlugin());
    }

    private void genSlotIDs() {
        List<WeaponAPI> weapons = ship.getAllWeapons();
        outer:
        for (WeaponAPI w : weapons) {
            for (String id : slotIDs.keySet()) {
                if (id.equals(w.getSlot().getId())) {
                    byte i = (byte) (int) slotIDs.get(id);
                    weaponSlots.put(i, w);
                    weaponSlotIDs.put(w, i);

                    continue outer;
                }
            }
        }
    }

    @Override
    public void update(float amount, BaseEntityManager manager, MPPlugin plugin) {
        if (controlOverride != null) {
            controlOverride.control(ship);
        }

        if (ship == null && manager instanceof InboundEntityManager) {
            init(plugin, (InboundEntityManager) manager);
        }
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

    public String getFleetMemberID() {
        return fleetMemberID;
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

    public void setLocation(Vector2f location) {
        this.location = location;
    }

    public void setFacing(float facing) {
        this.facing = facing;
    }

    public Map<WeaponAPI, Byte> getWeaponSlotIDs() {
        return weaponSlotIDs;
    }

    public Map<Byte, WeaponAPI> getWeaponSlots() {
        return weaponSlots;
    }

    public void setControlOverride(PlayerShipData.ShipControlOverride controlOverride) {
        this.controlOverride = controlOverride;
    }

    public void removeControlOverride() {
        controlOverride = null;
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
