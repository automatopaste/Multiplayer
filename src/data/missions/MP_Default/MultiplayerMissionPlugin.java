package data.missions.MP_Default;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.MPModPlugin;
import data.scripts.net.data.packables.entities.ships.ClientPlayerData;
import data.scripts.net.data.packables.entities.ships.ShipData;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.tables.server.combat.players.PlayerShips;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.MPServerPlugin;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiplayerMissionPlugin extends BaseEveryFrameCombatPlugin {

    private static final List<String> variants = new ArrayList<>();
    static {
        variants.add("hyperion_Attack");
    }

    private final Map<Short, IntervalUtil> cooldowns = new HashMap<>();

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatEngineAPI engine = Global.getCombatEngine();

        engine.setDoNotEndCombat(true);

        if (engine.isPaused()) return;

        MPPlugin plugin = MPModPlugin.getPlugin();
        if (!(plugin instanceof MPServerPlugin)) return;
        MPServerPlugin server = (MPServerPlugin) plugin;

        PlayerShips playerShips = (PlayerShips) server.getEntityManagers().get(PlayerShips.class);
        Map<Short, ClientPlayerData> controlData = playerShips.getControlData();

        //runcode Global.getCombatEngine().applyDamage(Global.getCombatEngine().getPlayerShip(), Global.getCombatEngine().getPlayerShip().getLocation(), 9999999f, DamageType.ENERGY, 0f, true, false, null);

        // host
        short hostID = playerShips.getHostShipID();
        ShipAPI host = null;
        if (hostID != BaseEntityManager.DEFAULT_HOST_INSTANCE) {
            ShipData shipData = playerShips.getShipTable().getTable()[hostID];

            if (shipData != null) {
                host = shipData.getShip();
            }
        }

        IntervalUtil hostCooldown = cooldowns.get((short)-1);
        if (hostCooldown == null) {
            hostCooldown = new IntervalUtil(1f, 1f);
            cooldowns.put((short)-1, hostCooldown);
        }
        if (host == null || !host.isAlive() || host.isHulk()) {
            hostCooldown.advance(amount);
            if (hostCooldown.intervalElapsed()) {
                ShipAPI newShip = spawnShip(engine);

                playerShips.transferControl(newShip, true, null);
            }
        } else {
            hostCooldown.setElapsed(0f);
        }

        // clients
        for (ClientPlayerData player : controlData.values()) {
            ShipAPI ship = player.getShip();

            IntervalUtil cooldown = cooldowns.get(player.getInstanceID());
            if (cooldown == null) {
                cooldown = new IntervalUtil(1f, 1f);
                cooldowns.put(player.getInstanceID(), cooldown);
            }

            if (ship == null || !ship.isAlive() || ship.isHulk()) {
                cooldown.advance(amount);
                if (cooldown.intervalElapsed()) {
                    ShipAPI newShip = spawnShip(engine);

                    playerShips.transferControl(newShip, false, player);
                }
            } else {
                cooldown.setElapsed(0f);
            }
        }

    }

    private ShipAPI spawnShip(CombatEngineAPI engine) {
        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variants.get(0));
        member.getCrewComposition().setCrew(member.getHullSpec().getMaxCrew());

        CombatFleetManagerAPI fleetManager = engine.getFleetManager(FleetSide.PLAYER);
        fleetManager.addToReserves(member);

        Vector2f location = new Vector2f((float) (Math.random() * 2000f - 1000f), (float) (Math.random() * 2000f - 1000f));
        float facing = (float) (Math.random() * 360f);

        ShipAPI ship = fleetManager.spawnFleetMember(member, location, facing, 0f);
        ship.setCRAtDeployment(0.7f);
        ship.setControlsLocked(false);

        return ship;
    }
}
