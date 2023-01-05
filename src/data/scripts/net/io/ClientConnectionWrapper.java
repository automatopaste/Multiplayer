package data.scripts.net.io;

import cmu.CMUtils;
import com.fs.starfarer.api.Global;
import data.scripts.net.data.packables.metadata.ConnectionData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.net.io.tcp.client.SocketClient;
import data.scripts.net.io.udp.client.DatagramClient;
import data.scripts.plugins.MPPlugin;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages switching logic for inputting/sending data
 */
public class ClientConnectionWrapper extends BaseConnectionWrapper implements InboundEntityManager, OutboundEntityManager {
    private final DataDuplex dataDuplex;

    private DatagramClient datagramClient;
    private Thread datagram;

    private final SocketClient socketClient;
    private final Thread socket;
    private final String host;
    private final int port;

    private int tick;

    public ClientConnectionWrapper(String host, int port, MPPlugin plugin) {
        super(plugin);

        this.host = host;
        this.port = port;
        dataDuplex = new DataDuplex();

        socketClient = new SocketClient(host, port, this);
        socket = new Thread(socketClient, "SOCKET_CLIENT_THREAD");
        socket.start();

        tick = -1;
    }

    @Override
    public MessageContainer getSocketMessage() throws IOException {
        if (connectionData == null) {
            InetSocketAddress address = socketClient.getLocal();
            if (address == null) return null;

            connectionID = ConnectionData.getConnectionID(address);
            connectionData = new ConnectionData(connectionID, this);
            clientPort = socketClient.getLocalPort();
        }

        Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> outbound = dataDuplex.getOutboundSocket();

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

                if (datagramClient == null) startDatagramClient();

                break;
            case SIMULATING:
            default:
                break;
        }

        ByteBuf data = initBuffer(tick, connectionID);
        writeBuffer(outbound, data);

        return new MessageContainer(data, tick, true, null, socketBuffer, connectionID);
    }

    private void startDatagramClient() {
        datagramClient = new DatagramClient(host, port, clientPort, this);
        datagram = new Thread(datagramClient, "DATAGRAM_CLIENT_THREAD");
        datagram.start();
    }

    @Override
    public MessageContainer getDatagram() throws IOException {
        if (connectionData == null) return null;

        Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> outbound = dataDuplex.getOutboundDatagram();

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

        ByteBuf data = initBuffer(tick, connectionID);
        writeBuffer(outbound, data);

        return new MessageContainer(data, tick, false, null, datagramBuffer, connectionID);
    }

    public void updateInbound(Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> entities, int tick) {
        this.tick = tick;
        dataDuplex.updateInbound(entities);
    }

    public DataDuplex getDuplex() {
        return dataDuplex;
    }

    public synchronized ConnectionState getConnectionState() {
        return connectionState;
    }

    public synchronized void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public void stop() {
        socketClient.stop();
        if (datagramClient != null) datagramClient.stop();
        socket.interrupt();
        if (datagram != null) datagram.interrupt();

        setConnectionState(BaseConnectionWrapper.ConnectionState.CLOSED);
    }

    public int getTick() {
        return tick;
    }

    @Override
    public void processDelta(short instanceID, Map<Byte, BaseRecord<?>> toProcess, MPPlugin plugin) {
        connectionData.overwrite(toProcess);
    }

    @Override
    public void update(float amount) {

    }

    @Override
    public void execute() {
        connectionData.destExecute();
    }

    @Override
    public Map<Short, Map<Byte, BaseRecord<?>>> getOutbound() {
        Map<Short, Map<Byte, BaseRecord<?>>> out = new HashMap<>();
        out.put(connectionID, connectionData.getDeltas());
        return out;
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(ConnectionData.TYPE_ID, this);
        DataGenManager.registerOutboundEntityManager(ConnectionData.TYPE_ID, this);
    }

    @Override
    public PacketType getOutboundPacketType() {
        return PacketType.SOCKET;
    }
}
