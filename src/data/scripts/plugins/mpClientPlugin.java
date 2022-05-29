package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.data.LoadedDataStore;
import data.scripts.net.connection.BaseConnectionWrapper;
import data.scripts.net.connection.ClientConnectionWrapper;
import data.scripts.net.data.BasePackable;
import data.scripts.net.data.ClientEntityManager;
import data.scripts.net.data.ClientInputManager;
import data.scripts.net.data.packables.InputAggregateData;
import org.lazywizard.console.Console;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.Map;

public class mpClientPlugin extends BaseEveryFrameCombatPlugin {
    private final ClientConnectionWrapper connection;

    private final ClientEntityManager entityManager;
    private final ClientInputManager inputManager;

    private LoadedDataStore dataStore;
    private boolean loaded = true;

    public mpClientPlugin(String host, int port) {

        connection = new ClientConnectionWrapper(host, port);



        entityManager = new ClientEntityManager(this);

        // placeholder id
        int id = -10;
        inputManager = new ClientInputManager(id, new InputAggregateData(id));

        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            Global.getCombatEngine().removeEntity(ship);
        }
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (connection.getConnectionState() == BaseConnectionWrapper.ConnectionState.CLOSED) {
            Global.getCombatEngine().removePlugin(this);
            Console.showMessage("Server interrupted");
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
            connection.stop();
            Global.getCombatEngine().removePlugin(this);
            Console.showMessage("Closed client");
        }

        // do nothing until connection wrapper is ready for simulation
        if (connection.getConnectionState() != BaseConnectionWrapper.ConnectionState.SIMULATION) {
            return;
        }

        CombatEngineAPI engine = Global.getCombatEngine();

        Map<Integer, BasePackable> entities = connection.getDuplex().getDeltas();

        entityManager.processDeltas(entities);
        entityManager.updateEntities();

        Map<Integer, BasePackable> outbound = inputManager.getEntities();
        connection.getDuplex().updateOutbound(outbound);
    }

    public ClientConnectionWrapper getConnection() {
        return connection;
    }

    public void setDataStore(LoadedDataStore dataStore) {
        this.dataStore = dataStore;
    }

    public LoadedDataStore getDataStore() {
        return dataStore;
    }
}