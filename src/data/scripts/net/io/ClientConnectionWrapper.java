package data.scripts.net.io;

import cmu.CMUtils;
import com.fs.starfarer.api.Global;
import data.scripts.net.data.packables.metadata.ConnectionData;
import data.scripts.net.data.records.BaseRecord;
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
public class ClientConnectionWrapper extends BaseConnectionWrapper {
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

        tick = -1;

        socketClient = new SocketClient(host, port, this);
        socket = new Thread(socketClient, "SOCKET_CLIENT_THREAD");
        socket.start();

        datagramClient = new DatagramClient(host, port, clientPort, this);
        datagram = new Thread(datagramClient, "DATAGRAM_CLIENT_THREAD");
        datagram.start();
    }

    @Override
    public MessageContainer getSocketMessage() throws IOException {
        if (connectionData == null) {
            InetSocketAddress address = socketClient.getLocal();
            if (address == null) return null;

            connectionID = ConnectionData.getConnectionID(address);
            connectionData = new ConnectionData(connectionID, this);
            clientPort = socketClient.getLocalPort();
            return null;
        }

        connectionState = BaseConnectionWrapper.ordinalToConnectionState(connectionData.getConnectionState());

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

                break;
            case SIMULATING:
            default:
                break;
        }

        connectionData.sourceExecute();

        Map<Short, Map<Byte, BaseRecord<?>>> instance = new HashMap<>();
        instance.put(connectionID, connectionData.getDeltas());
        outbound.put(ConnectionData.TYPE_ID, instance);

        ByteBuf data = initBuffer(tick, connectionID);
        writeBuffer(outbound, data);

        return new MessageContainer(data, tick, true, null, socketBuffer, connectionID);
    }

    @Override
    public MessageContainer getDatagram() throws IOException {
        if (connectionData == null || connectionState != ConnectionState.SIMULATING) return null;

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
        Map<Short, Map<Byte, BaseRecord<?>>> instance = entities.get(ConnectionData.TYPE_ID);
        if (instance != null) connectionData.destExecute(instance.get(connectionID));
        entities.remove(ConnectionData.TYPE_ID);

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

    @Override
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
}
