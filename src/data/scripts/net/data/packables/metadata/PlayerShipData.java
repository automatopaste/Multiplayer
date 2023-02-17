package data.scripts.net.data.packables.metadata;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import data.scripts.net.data.packables.DestExecute;
import data.scripts.net.data.packables.EntityData;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.net.data.records.Vector2f32Record;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.client.PlayerShip;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.ai.MPDefaultShipAIPlugin;
import org.lazywizard.lazylib.MathUtils;
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

    private boolean shieldToggleCheck = false;

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
                        if (playerShip.getPlayerShip() != null) return playerShip.getPlayerShip().getMouseTarget();
                        return new Vector2f(0f, 0f);
                    }
                },
                new DestExecute<Vector2f>() {
                    @Override
                    public void execute(Vector2f value, EntityData packable) {
                        setMouseTarget(value);

                        ShipAPI ship = getPlayerShip();
                        if (ship != null) ship.getMouseTarget().set(value);
                    }
                }
        ));
    }

    @Override
    public void init(MPPlugin plugin, InboundEntityManager manager) {

    }

    @Override
    public void update(float amount, BaseEntityManager manager, MPPlugin.PluginType pluginType) {
        if (playerShip == null) {
            check();
        }

        if (playerShip != null) {
            if (pluginType == MPPlugin.PluginType.SERVER) {
                unmask(playerShip, controlBitmask);
            } else {
                playerShip.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
            }
        }
    }

    private void check() {
        CombatEngineAPI engine = Global.getCombatEngine();
        for (ShipAPI ship : engine.getShips()) {
            if (ship.getFleetMemberId().equals(playerShipID)) {
                ship.setShipAI(new MPDefaultShipAIPlugin());

                playerShip = ship;
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
        controls[9] = Mouse.isButtonDown(1) && !shieldToggleCheck;
        shieldToggleCheck = Mouse.isButtonDown(1);

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

    public void unmask(ShipAPI ship, int bitmask) {

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
            float target = VectorUtils.getAngle(ship.getLocation(), ship.getMouseTarget());
            float rotate = MathUtils.getShortestRotation(ship.getFacing(), target);
            rotate = MathUtils.clamp(rotate, -ship.getMaxTurnRate(), ship.getMaxTurnRate());
            ship.setFacing(ship.getFacing() + (Global.getCombatEngine().getElapsedInLastFrame() * rotate));
        }
        if (controls[6]) ship.giveCommand(ShipCommand.STRAFE_LEFT, null, 0);
        if (controls[7]) ship.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0);
        if (controls[8]) ship.giveCommand(ShipCommand.USE_SYSTEM, null, 0);
        if (controls[9]) ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);

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
        this.mouseTarget = mouseTarget;
    }

    public Vector2f getMouseTarget() {
        return mouseTarget;
    }
}
