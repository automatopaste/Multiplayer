package data.missions.MP_Default;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import org.lazywizard.console.Console;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MissionDefinition implements MissionDefinitionPlugin {

    private static final int NUM_PLAYER = 6;
    private static final int NUM_ENEMY = 6;

    private static final List<String> enemyVariants = new ArrayList<String>();
    static {
        enemyVariants.add("aurora_Balanced");
        enemyVariants.add("onslaught_Elite");
        enemyVariants.add("hammerhead_Balanced");
        enemyVariants.add("omen_PD");
    }

    private static final List<String> playerVariants = new ArrayList<String>();
    static {
        playerVariants.add("aurora_Balanced");
        playerVariants.add("wolf_Assault");
        playerVariants.add("sunder_Assault");
        playerVariants.add("medusa_Attack");
    }

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        api.addPlanet(0f, 0f, 150f, "barren-bombarded", 0f, true);

        api.initFleet(FleetSide.PLAYER, "PCS", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "TTS", FleetGoal.ATTACK, true);

        api.setFleetTagline(FleetSide.PLAYER, "Cydonian Defence Fleet");
        api.setFleetTagline(FleetSide.ENEMY, "Tri-Tachyon Planet-Killer Escort Force");

        boolean f = true;
        for (String variant : enemyVariants) {
            api.addToFleet(FleetSide.ENEMY, variant, FleetMemberType.SHIP, f);
            f = false;
        }

        f = true;
        for (String variant : playerVariants) {
            api.addToFleet(FleetSide.PLAYER, variant, FleetMemberType.SHIP, f);
            f = false;
        }

        // Set up the map.
        float width = 15000f;
        float height = 15000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

        for (int i = 0; i < 50; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 100f + (float) Math.random() * 400f;
            api.addNebula(x, y, radius);
        }

        // Add objectives
        api.addObjective(minX + width * 0.25f, minY + height * 0.25f, "nav_buoy");
        api.addObjective(minX + width * 0.35f, minY + height * 0.65f, "comm_relay");
        api.addObjective(minX + width * 0.75f, minY + height * 0.45f, "nav_buoy");
        api.addObjective(minX + width * 0.65f, minY + height * 0.35f, "comm_relay");
        api.addObjective(minX + width * 0.5f, minY + height * 0.25f, "sensor_array");

        api.addPlugin(new EveryFrameCombatPlugin() {

            private float cooldown = 1f;

            @Override
            public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {

            }

            @Override
            public void advance(float amount, List<InputEventAPI> events) {
                CombatEngineAPI engine = Global.getCombatEngine();

                if (engine.isPaused()) return;

                if (cooldown > 0f) {
                    cooldown -= engine.getElapsedInLastFrame();
                    return;
                }

                Random r = new Random(6969);

                int p = 0, e = 0;
                for (ShipAPI ship : engine.getShips()) {
                    if (ship.isAlive()) {
                        if (ship.getOwner() == 0) p++;
                        else if (ship.getOwner() == 1) e++;
                    }
                }

                int n1 = NUM_PLAYER - p;
                if (n1 > 0 && n1 <= NUM_PLAYER) {
                    for (int i = 0; i < n1; i++) {
                        int index = (int) (Math.random() * playerVariants.size());
                        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, playerVariants.get(index));
                        member.getCrewComposition().setCrew(member.getNeededCrew());
                        member.getRepairTracker().setCR(0.7f);
                        member.setOwner(0);
                        Vector2f loc = new Vector2f((r.nextFloat() * 10000f) - 5000f, (r.nextFloat() * 10000f) - 5000f);
                        ShipAPI s = engine.getFleetManager(FleetSide.PLAYER).spawnFleetMember(member, loc, 90f, 1f);
                        s.setCRAtDeployment(0.7f);
                        s.setCurrentCR(0.7f);
                        Console.showMessage("spawning " + s.getHullSpec().getNameWithDesignationWithDashClass());
                    }
                }

                int n2 = NUM_ENEMY - e;
                if (n2 > 0 && n2 <= NUM_ENEMY) {
                    for (int i = 0; i < n2; i++) {
                        int index2 = (int) (Math.random() * enemyVariants.size());
                        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, enemyVariants.get(index2));
                        member.getCrewComposition().setCrew(member.getNeededCrew());
                        member.getRepairTracker().setCR(0.7f);
                        member.setOwner(1);
                        Vector2f loc2 = new Vector2f((r.nextFloat() * 10000f) - 5000f, (r.nextFloat() * 10000f) - 5000f);
                        ShipAPI s = engine.getFleetManager(FleetSide.ENEMY).spawnFleetMember(member, loc2, 270f, 1f);
                        s.setCRAtDeployment(0.7f);
                        s.setCurrentCR(0.7f);
                        Console.showMessage("spawning " + s.getHullSpec().getNameWithDesignationWithDashClass());
                    }
                }
                cooldown = 10f;
            }

            @Override
            public void renderInWorldCoords(ViewportAPI viewport) {

            }

            @Override
            public void renderInUICoords(ViewportAPI viewport) {

            }

            @Override
            public void init(CombatEngineAPI engine) {

            }
        });
    }
}