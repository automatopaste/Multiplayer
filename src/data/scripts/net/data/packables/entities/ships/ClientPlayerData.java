package data.scripts.net.data.packables.entities.ships;

import cmu.drones.ai.DroneAIUtils;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import data.scripts.net.data.packables.DestExecute;
import data.scripts.net.data.packables.EntityData;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.records.ByteRecord;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.ShortRecord;
import data.scripts.net.data.records.Vector2f32Record;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.client.combat.player.PlayerShip;
import data.scripts.net.data.tables.server.combat.entities.ShipTable;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.ai.MPDefaultShipAIPlugin;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

/**
 * Sends player ship commands to the server
 */
public class ClientPlayerData extends EntityData {

    public static byte TYPE_ID;

    private int controlBitmask;
    private short playerShipID;
    private byte playerShipFlags;
    private short requestedShipID = -1;

    private ShipAPI ship;

    private final Vector2f mouseTarget = new Vector2f(0f, 0f);

    private boolean prevFighters = false;
    private byte activeGroup = 0;

    private byte autofireStates = 0x00;
    private byte selected = 0x00;
    private boolean prevSelectedGroupAutofire = false;

    private final float[] autofireToggleCooldowns = new float[7];
    private float shieldToggleCooldown = 0f;
    private float fighterToggleCooldown = 0f;
    private float holdFireToggleCooldown = 0;

    // make use of the CMUtils pd controller functionality to imitate the point-at-cursor pilot mode
    private final DroneAIUtils.PDControl control = new DroneAIUtils.PDControl() {
        @Override
        public float getKp() {
            return 0;
        }

        @Override
        public float getKd() {
            return 0;
        }

        @Override
        public float getRp() {
            return 0.1f;
        }

        @Override
        public float getRd() {
            return 0.05f;
        }
    };

    /**
     * Source constructor
     *
     * @param instanceID unique
     */
    public ClientPlayerData(short instanceID, final PlayerShip playerShip) {
        super(instanceID);

        addRecord(new RecordLambda<>(
                IntRecord.getDefault().setDebugText("control bitmask"),
                new SourceExecute<Integer>() {
                    @Override
                    public Integer get() {
                        return mask();
                    }
                },
                new DestExecute<Integer>() {
                    @Override
                    public void execute(Integer value, EntityData packable) {
                        ClientPlayerData clientPlayerData = (ClientPlayerData) packable;
                        clientPlayerData.setControlBitmask(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ShortRecord.getDefault().setDebugText("player ship id"),
                new SourceExecute<Short>() {
                    @Override
                    public Short get() {
                        return playerShip.getActiveShipID();
                    }
                },
                new DestExecute<Short>() {
                    @Override
                    public void execute(Short value, EntityData packable) {
                        setPlayerShipID(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ShortRecord.getDefault().setDebugText("requested switch ship id"),
                new SourceExecute<Short>() {
                    @Override
                    public Short get() {
                        return requestedShipID;
                    }
                },
                new DestExecute<Short>() {
                    @Override
                    public void execute(Short value, EntityData packable) {
                        setRequestedShipID(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                Vector2f32Record.getDefault().setDebugText("player mouse target"),
                new SourceExecute<Vector2f>() {
                    @Override
                    public Vector2f get() {
                        Vector2f m = new Vector2f(Mouse.getX(), Mouse.getY());
                        ViewportAPI v = Global.getCombatEngine().getViewport();
                        m.x = v.convertScreenXToWorldX(m.x);
                        m.y = v.convertScreenYToWorldY(m.y);
                        return m;
                    }
                },
                new DestExecute<Vector2f>() {
                    @Override
                    public void execute(Vector2f value, EntityData packable) {
                        setMouseTarget(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("group select commands"),
                new SourceExecute<Byte>() {
                    @Override
                    public Byte get() {
                        byte b = 0;
                        if (Keyboard.isKeyDown(Keyboard.KEY_1)) {
                            b = 1;
                        } else if (Keyboard.isKeyDown(Keyboard.KEY_2)) {
                            b = 2;
                        } else if (Keyboard.isKeyDown(Keyboard.KEY_3)) {
                            b = 3;
                        } else if (Keyboard.isKeyDown(Keyboard.KEY_4)) {
                            b = 4;
                        } else if (Keyboard.isKeyDown(Keyboard.KEY_5)) {
                            b = 5;
                        } else if (Keyboard.isKeyDown(Keyboard.KEY_6)) {
                            b = 6;
                        } else if (Keyboard.isKeyDown(Keyboard.KEY_7)) {
                            b = 7;
                        }

                        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && b != 0) {
                            b |= 0b10000000;
                        }

                        return b;
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(Byte value, EntityData packable) {
                        if (value == 0) return;

                        ClientPlayerData clientPlayerData = (ClientPlayerData) packable;
                        clientPlayerData.activeGroup = value;
                    }
                }
        ));
    }

    @Override
    public void init(MPPlugin plugin, InboundEntityManager manager) {

    }

    @Override
    public void update(float amount, BaseEntityManager manager, MPPlugin plugin) {
        if (ship == null && plugin.getType() == MPPlugin.PluginType.SERVER) {
            check((ShipTable) plugin.getEntityManagers().get(ShipTable.class));
        }

        for (int i = 0; i < autofireToggleCooldowns.length; i++) {
            autofireToggleCooldowns[i] -= amount;
        }

        if (ship != null) {
            if (plugin.getType() == MPPlugin.PluginType.SERVER) {
                unmask(ship, controlBitmask, amount);
                ship.getMouseTarget().set(getMouseTarget());

                if (activeGroup != 0) {
                    int group = (activeGroup & 0b01111111) - 1;

                    if ((activeGroup & 0b10000000) != 0) {
                        if (autofireToggleCooldowns[group] < 0f) {
                            ship.giveCommand(ShipCommand.TOGGLE_AUTOFIRE, null, group);

                            autofireToggleCooldowns[group] = 0.5f;
                        }
                    } else {
                        ship.giveCommand(ShipCommand.SELECT_GROUP, null, group);
                    }

                    activeGroup = 0;
                }

            }
        }
    }

    public void transferPlayerShip(ShipAPI dest) {
        if (ship != null) {
            ship.resetDefaultAI();
            ship.getShipAI().forceCircumstanceEvaluation();
        }

        dest.setShipAI(new MPDefaultShipAIPlugin());
        ship = dest;
    }

    private void check(ShipTable shipTable) {
        for (ShipData data : shipTable.getTable()) {
            if (data != null && data.getShip() != null) {
                ShipAPI ship = data.getShip();

                if (data.getInstanceID() == playerShipID) {
                    ship.setShipAI(new MPDefaultShipAIPlugin());
                    this.ship = ship;
                }
            }
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

    public int getControlBitmask() {
        return controlBitmask;
    }

    public void setControlBitmask(int controlBitmask) {
        this.controlBitmask = controlBitmask;
    }

    public short getPlayerShipID() {
        return playerShipID;
    }

    public void setPlayerShipID(short playerShipID) {
        this.playerShipID = playerShipID;
    }

    public short getRequestedShipID() {
        return requestedShipID;
    }

    public byte getPlayerShipFlags() {
        return playerShipFlags;
    }

    public void setPlayerShipFlags(byte playerShipFlags) {
        this.playerShipFlags = playerShipFlags;
    }

    public void setRequestedShipID(short requestedShipID) {
        this.requestedShipID = requestedShipID;
    }

    public int mask() {
        boolean[] controls = new boolean[Integer.SIZE];

        if (!Keyboard.isCreated()) return 0x00000000;

        controls[0] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_ACCELERATE")));
        controls[1] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_ACCELERATE_BACKWARDS")));
        controls[2] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_TURN_LEFT")));
        controls[3] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_TURN_RIGHT")));
        controls[4] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_DECELERATE")));

        controls[5] = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

        controls[6] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_STRAFE_LEFT_NOTURN")));
        controls[7] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_STRAFE_RIGHT_NOTURN")));
        controls[8] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_USE_SYSTEM")));

        controls[9] = Mouse.isButtonDown(1); // shield toggle

        controls[10] = Mouse.isButtonDown(0); // shoot gun pew

        controls[11] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_VENT_FLUX")));
        controls[12] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_HOLD_FIRE")));

        controls[13] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_PULL_BACK_FIGHTERS")));

        final int numGroups = 7;
        final int controls0 = 14;
        final int toggle0 = 21;
        for (int i = 0; i < numGroups; i++) {
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_" + (i + 1))))) {
                if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                    controls[i + controls0] = true;
                    autofireStates ^= 0b00000001 << i;
                } else {
                    controls[i + toggle0] = true;
                    selected = (byte) i;
                }
            }
        }

        // max length 32
        int bits = 0;
        for (int i = 0; i < controls.length; i++) {
            if (controls[i]) bits |= 1 << i;
        }

        return bits;
    }

    public void unmask(ShipAPI ship, int bitmask, float amount) {
        boolean[] controls = new boolean[Integer.SIZE];
        for (int i = 0; i < controls.length; i++) {
            if ((bitmask & 1 << i) != 0) controls[i] = true;
        }

        if (controls[0]) ship.giveCommand(ShipCommand.ACCELERATE, null, 0);
        if (controls[1]) ship.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, 0);

        if (controls[4]) ship.giveCommand(ShipCommand.DECELERATE, null, 0);

        if (controls[5]) { // strafe mode
            float target = VectorUtils.getAngle(ship.getLocation(), mouseTarget);
            DroneAIUtils.rotate(target, ship, control);

            if (controls[2]) ship.giveCommand(ShipCommand.STRAFE_LEFT, null, 0);
            if (controls[3]) ship.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0);
        } else {
            if (controls[2]) ship.giveCommand(ShipCommand.TURN_LEFT, null, 0);
            if (controls[3]) ship.giveCommand(ShipCommand.TURN_RIGHT, null, 0);
            if (controls[6]) ship.giveCommand(ShipCommand.STRAFE_LEFT, null, 0);
            if (controls[7]) ship.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0);
        }

        if (controls[8]) ship.giveCommand(ShipCommand.USE_SYSTEM, null, 0);

        shieldToggleCooldown -= amount;
        if (shieldToggleCooldown <= 0f && ship.getShield() != null && controls[9]) {
            ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
            shieldToggleCooldown = 0.2f;
        }

        int selected = 0;
        List<WeaponGroupAPI> groups = ship.getWeaponGroupsCopy();
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).equals(ship.getSelectedGroupAPI())) {
                selected = i;
                break;
            }
        }

        if (controls[10]) ship.giveCommand(ShipCommand.FIRE, ship.getMouseTarget(), selected);
        if (controls[11]) ship.giveCommand(ShipCommand.VENT_FLUX, null, 0);

        fighterToggleCooldown -= amount;
        if (fighterToggleCooldown <= 0f && controls[13]) {
            ship.giveCommand(ShipCommand.PULL_BACK_FIGHTERS, null, 0);
            fighterToggleCooldown = 0.2f;
        }

        holdFireToggleCooldown -= amount;
        if (holdFireToggleCooldown <= 0f && controls[12]) {
            ship.giveCommand(ShipCommand.HOLD_FIRE, null, 0);
            holdFireToggleCooldown = 0.2f;
        }
    }

    public void setMouseTarget(Vector2f mouseTarget) {
        this.mouseTarget.set(mouseTarget);
    }

    public Vector2f getMouseTarget() {
        return mouseTarget;
    }

    public abstract static class ShipControlOverride {
        public final ClientPlayerData data;

        public ShipControlOverride(ClientPlayerData data) {
            this.data = data;
        }

        public abstract void control(ShipAPI ship);
    }
}
