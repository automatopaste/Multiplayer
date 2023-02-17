package data.scripts.net.data.packables.metadata;

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
import data.scripts.net.data.packables.entities.ships.ShipData;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.net.data.records.Vector2f32Record;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.client.PlayerShip;
import data.scripts.net.data.tables.server.ShipTable;
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
public class PlayerShipData extends EntityData {

    public static byte TYPE_ID;

    private int controlBitmask;
    private String playerShipID;

    private ShipAPI playerShip;
    private Vector2f mouseTarget = new Vector2f(0f, 0f);

    private boolean shieldEnable = false;
    private boolean prev = false;

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
    public PlayerShipData(short instanceID, final PlayerShip playerShip) {
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
                        PlayerShipData playerShipData = (PlayerShipData) packable;
                        playerShipData.setControlBitmask(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                StringRecord.getDefault().setDebugText("player ship id"),
                new SourceExecute<String>() {
                    @Override
                    public String get() {
                        String s = playerShip.getPlayerShipID();
                        if (s == null) return StringRecord.NULL;
                        return s;
                    }
                },
                new DestExecute<String>() {
                    @Override
                    public void execute(String value, EntityData packable) {
                        PlayerShipData playerShipData = (PlayerShipData) packable;
                        if (value == null || value.equals(StringRecord.NULL)) {
                            playerShipData.setPlayerShipID(null);
                        } else {
                            playerShipData.setPlayerShipID(value);
                        }
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
                        m.y = v.convertWorldYtoScreenY(m.y);
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
    }

    @Override
    public void init(MPPlugin plugin, InboundEntityManager manager) {

    }

    @Override
    public void update(float amount, BaseEntityManager manager, MPPlugin plugin) {
        if (playerShip == null && plugin.getType() == MPPlugin.PluginType.SERVER) {
            check((ShipTable) plugin.getEntityManagers().get(ShipTable.class));
        }

        if (playerShip != null) {
            if (plugin.getType() == MPPlugin.PluginType.SERVER) {
                unmask(playerShip, controlBitmask, amount);
            } else {
                playerShip.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
            }
        }

        boolean check = Mouse.isButtonDown(1);
        if (check && !prev) shieldEnable = !shieldEnable;
        prev = check;
    }

    private void check(ShipTable shipTable) {
        for (ShipData data : shipTable.getTable()) {
            if (data != null && data.getShip() != null) {
                ShipAPI ship = data.getShip();

                if (ship.getFleetMemberId().equals(playerShipID)) {
                    ship.setShipAI(new MPDefaultShipAIPlugin());
                    data.setControlOverride(new ShipControlOverride(this) {
                        @Override
                        public void control(ShipAPI ship) {
                            ship.getMouseTarget().set(mouseTarget);
                        }
                    });
                    playerShip = ship;
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

    public ShipAPI getPlayerShip() {
        return playerShip;
    }

    public int getControlBitmask() {
        return controlBitmask;
    }

    public void setControlBitmask(int controlBitmask) {
        this.controlBitmask = controlBitmask;
    }

    public String getPlayerShipID() {
        return playerShipID;
    }

    public void setPlayerShipID(String playerShipID) {
        this.playerShipID = playerShipID;
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

//        controls[9] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SHIELDS")));

        controls[9] = shieldEnable;

//        controls[10] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_FIRE")));
        controls[10] = Mouse.isButtonDown(0);

        controls[11] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_VENT_FLUX")));
        controls[12] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_HOLD_FIRE")));
        controls[13] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_PULL_BACK_FIGHTERS")));
        controls[14] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_1")));
        controls[15] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_2")));
        controls[16] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_3")));
        controls[17] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_4")));
        controls[18] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_5")));
        controls[19] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_6")));
        controls[20] = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_SELECT_GROUP_7")));

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
        if (controls[2]) ship.giveCommand(ShipCommand.TURN_LEFT, null, 0);
        if (controls[3]) ship.giveCommand(ShipCommand.TURN_RIGHT, null, 0);
        if (controls[4]) ship.giveCommand(ShipCommand.DECELERATE, null, 0);
        if (controls[5]) {
            float target = VectorUtils.getAngle(ship.getLocation(), mouseTarget);

            DroneAIUtils.rotate(target, ship, control);
        }
        if (controls[6]) ship.giveCommand(ShipCommand.STRAFE_LEFT, null, 0);
        if (controls[7]) ship.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0);
        if (controls[8]) ship.giveCommand(ShipCommand.USE_SYSTEM, null, 0);
        if (ship.getShield() != null) {
            if (controls[9]) {
                if (ship.getShield().isOff()) {
                    ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
                }
            } else {
                if (ship.getShield().isOn()) {
                    ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
                }
            }
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
        if (controls[12]) ship.giveCommand(ShipCommand.HOLD_FIRE, null, 0);
        if (controls[13]) ship.giveCommand(ShipCommand.PULL_BACK_FIGHTERS, null, 0);
        if (controls[14]) ship.giveCommand(ShipCommand.SELECT_GROUP, ship.getMouseTarget(), 0);
        if (controls[15]) ship.giveCommand(ShipCommand.SELECT_GROUP, ship.getMouseTarget(), 1);
        if (controls[16]) ship.giveCommand(ShipCommand.SELECT_GROUP, ship.getMouseTarget(), 2);
        if (controls[17]) ship.giveCommand(ShipCommand.SELECT_GROUP, ship.getMouseTarget(), 3);
        if (controls[18]) ship.giveCommand(ShipCommand.SELECT_GROUP, ship.getMouseTarget(), 4);
        if (controls[19]) ship.giveCommand(ShipCommand.SELECT_GROUP, ship.getMouseTarget(), 5);
        if (controls[20]) ship.giveCommand(ShipCommand.SELECT_GROUP, ship.getMouseTarget(), 6);
    }

    public void setMouseTarget(Vector2f mouseTarget) {
        this.mouseTarget.set(mouseTarget);
    }

    public Vector2f getMouseTarget() {
        return mouseTarget;
    }

    public abstract static class ShipControlOverride {
        public final PlayerShipData data;

        public ShipControlOverride(PlayerShipData data) {
            this.data = data;
        }

        public abstract void control(ShipAPI ship);
    }
}
