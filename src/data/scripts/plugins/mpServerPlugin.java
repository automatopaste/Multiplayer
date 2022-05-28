package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.data.BasePackable;
import data.scripts.net.terminals.server.NettyServer;
import data.scripts.plugins.state.DataDuplex;
import data.scripts.plugins.state.ServerCombatEntityManager;
import data.scripts.plugins.state.ServerInboundEntityManager;
import org.apache.log4j.Logger;
import org.lazywizard.console.Console;
import org.lwjgl.input.Keyboard;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class mpServerPlugin extends BaseEveryFrameCombatPlugin {
    private final int port;
    private final Logger logger;

    private NettyServer server;
    private Thread serverThread;

    private final DataDuplex serverDataDuplex;
    private final ServerInboundEntityManager serverEntityManager;
    private final ServerCombatEntityManager serverCombatEntityManager;

    private int nextInstanceID = 0;
    private Set<Integer> usedIDs = new HashSet<>();

    public mpServerPlugin(int port) {
        this.port = port;
        logger = Global.getLogger(mpServerPlugin.class);

        serverDataDuplex = new DataDuplex();

        serverEntityManager = new ServerInboundEntityManager(this);
//        Map<Integer, APackable> deltas = new HashMap<>();
//        serverEntityManager.processDeltas(deltas);

        serverCombatEntityManager = new ServerCombatEntityManager(this);

        server = new NettyServer(port, serverDataDuplex);
        serverThread = new Thread(server, "mpServer");

        logger.info("Starting server");

        serverThread.start();
    }

    boolean yeah = true;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (!serverThread.isAlive() || serverThread.isInterrupted()) {
            serverThread = null;
            Global.getCombatEngine().removePlugin(this);
            Console.showMessage("Server interrupted");
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
            serverThread.interrupt();
            serverThread = null;
            Global.getCombatEngine().removePlugin(this);
            Console.showMessage("Closed server");
        }



        // outbound data update
        serverCombatEntityManager.update();
        Map<Integer, BasePackable> entities = serverCombatEntityManager.getEntities();

        //inbound data update
        serverEntityManager.processDeltas(serverDataDuplex.getDeltas());
//        serverEntityManager.delete(new ArrayList<>(serverDataDuplex.getRemovedInbound()));
        serverEntityManager.updateEntities();

        serverDataDuplex.updateOutbound(entities);

        ShipVariantAPI variant = Global.getCombatEngine().getPlayerShip().getVariant().clone();

        String wpn = (yeah) ? "pdlaser" : "pdburst";
        yeah = !yeah;
        for (int i = 0; i < variant.getNonBuiltInWeaponSlots().size(); i++) {
            String slot = variant.getNonBuiltInWeaponSlots().get(i);

            variant.addWeapon(slot, wpn);
        }
        System.out.println(variant.getWeaponId("WS 001"));

        variant.autoGenerateWeaponGroups();

        ShipAPI ship = Global.getCombatEngine().getPlayerShip();
        ship.getFleetMember().setVariant(variant, true, true);
    }

    public int getNewInstanceID(ShipAPI ship) {
        int id;
        if (ship != null) {
            id = ship.getFleetMemberId().hashCode();
        } else {
            nextInstanceID++;
            id = nextInstanceID;
        }
        while (usedIDs.contains(id)) {
            logger.warn("Attempted to provide new instance with historic ID: is ShipAPI String ID the same?");
            nextInstanceID++;
            id = nextInstanceID;
        }

        usedIDs.add(id);

        return id;
    }
}