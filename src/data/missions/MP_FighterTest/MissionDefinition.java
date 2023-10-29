package data.missions.MP_FighterTest;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    private static final int NUM_PLAYER = 6;
    private static final int NUM_ENEMY = 6;

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        api.addPlanet(0f, 0f, 150f, "barren-bombarded", 0f, true);

        api.initFleet(FleetSide.PLAYER, "PCS", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "TTS", FleetGoal.ATTACK, true);

        api.setFleetTagline(FleetSide.PLAYER, "Us");
        api.setFleetTagline(FleetSide.ENEMY, "Them");

        api.addToFleet(FleetSide.PLAYER, "condor_Attack", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.ENEMY, "condor_Attack", FleetMemberType.SHIP, false);

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
    }
}