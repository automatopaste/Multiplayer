package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.data.LoadedDataStore;
import data.scripts.net.connection.ServerConnectionManager;
import data.scripts.net.data.BasePackable;
import data.scripts.net.data.ServerCombatEntityManager;
import data.scripts.net.data.ServerInboundEntityManager;
import org.lazywizard.console.Console;
import org.lwjgl.input.Keyboard;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class mpServerPlugin extends BaseEveryFrameCombatPlugin {

    private final ServerConnectionManager serverConnectionManager;

    private final ServerInboundEntityManager serverEntityManager;
    private final ServerCombatEntityManager serverCombatEntityManager;

    private int nextInstanceID = 1;
    private final Set<Integer> usedIDs = new HashSet<>();

    private final LoadedDataStore dataStore;

    public mpServerPlugin() {

        serverEntityManager = new ServerInboundEntityManager(this);
        serverCombatEntityManager = new ServerCombatEntityManager(this);

        serverConnectionManager = new ServerConnectionManager();

        dataStore = new LoadedDataStore();
        dataStore.generate(Global.getCombatEngine(), this);
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatEngineAPI engine = Global.getCombatEngine();

        if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
            serverConnectionManager.stop();
            Global.getCombatEngine().removePlugin(this);
            Console.showMessage("Closed server");
        }

        // outbound data update
        serverCombatEntityManager.update();
        Map<Integer, BasePackable> entities = serverCombatEntityManager.getEntities();

        serverEntityManager.processDeltas(serverConnectionManager.getDuplex().getDeltas());

        //inbound data update
        serverEntityManager.updateEntities();

        serverConnectionManager.getDuplex().updateOutbound(entities);

        // move tick forward
        serverConnectionManager.update();
    }

    public int getNewInstanceID(ShipAPI ship) {
        int id = ship.getFleetMemberId().hashCode();

        while (usedIDs.contains(id)) {
//            logger.warn("Attempted to provide new instance with historic ID: is ShipAPI String ID the same?");
            nextInstanceID++;
            id = nextInstanceID;
        }

        usedIDs.add(id);
        return id;
    }

    public int getNewInstanceID() {
        int id;
        nextInstanceID++;
        id = nextInstanceID;

        while (usedIDs.contains(id)) {
            nextInstanceID++;
            id = nextInstanceID;
        }

        usedIDs.add(id);
        return id;
    }

    public ServerConnectionManager getServerConnectionManager() {
        return serverConnectionManager;
    }

    public LoadedDataStore getDataStore() {
        return dataStore;
    }
}