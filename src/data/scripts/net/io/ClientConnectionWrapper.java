package data.scripts.net.io;

import cmu.CMUtils;
import cmu.plugins.debug.DebugGraphContainer;
import com.fs.starfarer.api.Global;
import data.scripts.net.data.InboundData;
import data.scripts.net.data.InstanceData;
import data.scripts.net.data.OutboundData;
import data.scripts.net.data.packables.metadata.ClientConnectionData;
import data.scripts.net.data.packables.metadata.ServerConnectionData;
import data.scripts.net.io.tcp.client.SocketClient;
import data.scripts.net.io.udp.client.DatagramClient;
import data.scripts.plugins.MPClientPlugin;
import data.scripts.plugins.MPPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages switching logic for inputting/sending data
 */
public class ClientConnectionWrapper extends BaseConnectionWrapper {
    private final ClientDuplex duplex;

    private DatagramClient datagramClient;
    private Thread datagram;

    private final SocketClient socketClient;
    private final Thread socket;
    private final String host;
    private final int port;

    private int tick;
    private byte connectionID;
    private ClientConnectionData send;
    private ServerConnectionData receive;

    private DebugGraphContainer dataGraph;

    public ClientConnectionWrapper(String host, int port, MPPlugin plugin) {
        super(plugin);

        this.host = host;
        this.port = port;
        duplex = new ClientDuplex();

        tick = -1;

        dataGraph = new DebugGraphContainer("Latency", 120, 30f);

        socketClient = new SocketClient(host, port, this);
        socket = new Thread(socketClient, "SOCKET_CLIENT_THREAD");
        socket.start();
    }

    @Override
    public List<MessageContainer> getSocketMessages() throws IOException {
        if (receive == null || send == null) {
            return null;
        }

        clientPort = socketClient.getLocalPort();

        connectionState = BaseConnectionWrapper.ordinalToConnectionState(receive.getConnectionState());

        OutboundData outbound = duplex.getOutboundSocket();

        switch (connectionState) {
            case INITIALISATION_READY:
            //case INITIALISING:
                CMUtils.getGuiDebug().putText(ClientConnectionWrapper.class, "debug", "initialising connection...");
                Global.getLogger(ClientConnectionWrapper.class).info("initialising connection");

                connectionState = ConnectionState.INITIALISING;

                break;
            case LOADING_READY:
            //case LOADING:
                CMUtils.getGuiDebug().putText(ClientConnectionWrapper.class, "debug", "Receiving data over socket...");
                Global.getLogger(ClientConnectionWrapper.class).info("receiving data");

                connectionState = ConnectionState.LOADING;

                break;
            case SPAWNING_READY:
            //case SPAWNING:
                CMUtils.getGuiDebug().putText(ClientConnectionWrapper.class, "debug", "Spawning entities...");
                Global.getLogger(ClientConnectionWrapper.class).info("spawning entities");

                connectionState = ConnectionState.SPAWNING;

                break;
            case SIMULATION_READY:
                CMUtils.getGuiDebug().putText(ClientConnectionWrapper.class, "debug", "Starting simulation...");
                Global.getLogger(ClientConnectionWrapper.class).info("starting simulation");

                connectionState = ConnectionState.SIMULATING;

                if (datagramClient == null) {
                    datagramClient = new DatagramClient(host, port, clientPort, this);
                    datagram = new Thread(datagramClient, "DATAGRAM_CLIENT_THREAD");
                    datagram.start();
                }

                break;
            case SIMULATING:
                break;
            default:
                break;
        }

        Map<Short, InstanceData> instance = new HashMap<>();
        send.flush();
        send.setState(connectionState);
        instance.put((short) connectionID, send.sourceExecute(0f));
        outbound.out.put(ClientConnectionData.TYPE_ID, instance);

        CMUtils.getGuiDebug().putText(
                ServerConnectionWrapper.class,
                "latency",
                "server latency " + send.getLatency()
        );
        dataGraph.increment(send.getLatency());
        CMUtils.getGuiDebug().putContainer(ClientConnectionWrapper.class, "dataGraph", dataGraph);

        return writeBuffer(outbound, tick, null, connectionID);
    }

    @Override
    public List<MessageContainer> getDatagrams() throws IOException {
        if (send == null || connectionState != ConnectionState.SIMULATING) return null;

        OutboundData outbound = duplex.getOutboundDatagram();

        switch (connectionState) {
            case INITIALISATION_READY:
            case INITIALISING:
            case LOADING_READY:
            case LOADING:
            case SIMULATION_READY:
            case SPAWNING_READY:
            case SPAWNING:
            case SIMULATING:
            case CLOSED:
            default:
                break;
        }

        return writeBuffer(outbound, tick, null, connectionID);
    }

    public void updateInbound(InboundData entities, int tick) {
        Map<Short, Map<Byte, Object>> instance = entities.in.get(ServerConnectionData.TYPE_ID);
        if (instance != null && !instance.values().isEmpty()) {
            if (instance.values().size() > 1) throw new RuntimeException("wtf");

            for (short id : instance.keySet()) {
                Map<Byte, Object> records = instance.get(id);

                if (receive == null) {
                    receive = new ServerConnectionData((short) -10, (byte) -5, this);
                    ((MPClientPlugin) localPlugin).init();
                    receive.destExecute(records, tick);

                    send = new ClientConnectionData(receive.getConnectionID(), this);
                    send.sourceExecute(0f);
                } else {
                    receive.destExecute(records, tick);
                }

                connectionID = receive.getConnectionID();

                break;
            }
        }
        entities.in.remove(ServerConnectionData.TYPE_ID);

        if (tick != -1) this.tick = tick;
        duplex.updateInbound(entities);
    }

    public ClientDuplex getDuplex() {
        return duplex;
    }

    public synchronized ConnectionState getConnectionState() {
        return connectionState;
    }

    public synchronized void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    @Override
    public void stop() {
        socketClient.stop();
        if (datagramClient != null) datagramClient.stop();

        setConnectionState(BaseConnectionWrapper.ConnectionState.CLOSED);
    }

    public int getTick() {
        return tick;
    }

    public byte getConnectionID() {
        return connectionID;
    }
}
