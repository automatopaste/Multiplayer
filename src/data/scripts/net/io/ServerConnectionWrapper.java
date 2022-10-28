package data.scripts.net.io;

import cmu.CMUtils;
import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.metadata.connection.ConnectionData;
import data.scripts.net.data.packables.metadata.connection.ConnectionIDs;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.records.IntRecord;
import data.scripts.plugins.MPPlugin;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerConnectionWrapper extends BaseConnectionWrapper {
    private final ServerConnectionManager connectionManager;
    private final InetSocketAddress remoteAddress;

    public ServerConnectionWrapper(ServerConnectionManager connectionManager, short connectionId, InetSocketAddress remoteAddress, MPPlugin plugin) {
        super(plugin);

        this.connectionManager = connectionManager;
        this.remoteAddress = remoteAddress;
        this.connectionID = connectionId;

        statusData = new ConnectionData(connectionId, this);
    }

    @Override
    public MessageContainer getSocketMessage() throws IOException {
        if (statusData == null) return null;

        List<BasePackable> data = new ArrayList<>();
        switch (connectionState) {
            //case INITIALISATION_READY:
            case INITIALISING:
                CMUtils.getGuiDebug().putText(ServerConnectionWrapper.class, "debug" + connectionID, connectionID + ": initialising connection...");

                connectionState = ConnectionState.LOADING_READY;
                statusData.getRecord(ConnectionIDs.STATE).updateFromDelta(new IntRecord(connectionState.ordinal(), (byte) -1));

                break;
            //case LOADING_READY:
            case LOADING:
                CMUtils.getGuiDebug().putText(ServerConnectionWrapper.class, "debug" + connectionID, connectionID + ": sending client data over socket...");

                data.addAll(connectionManager.getServerPlugin().getDataStore().getGenerated());

                connectionState = ConnectionState.SPAWNING_READY;
                statusData.getRecord(ConnectionIDs.STATE).updateFromDelta(new IntRecord(connectionState.ordinal(), (byte) -1));

                break;
            //case SPAWNING_READY:
            case SPAWNING:
                CMUtils.getGuiDebug().putText(ServerConnectionWrapper.class, "debug" + connectionID, connectionID + ": spawning ships on client...");

                data.addAll(connectionManager.getServerPlugin().getServerShipTable().getOutbound().values());

                connectionState = ConnectionState.SIMULATION_READY;
                statusData.getRecord(ConnectionIDs.STATE).updateFromDelta(new IntRecord(connectionState.ordinal(), (byte) -1));

                break;
            //case SIMULATION_READY:
            case SIMULATING:
                for (Map<Short, BasePackable> type : connectionManager.getDuplex().getOutboundSocket().values()) {
                    data.addAll(type.values());
                }

                break;
            case CLOSED:
            default:
        }

        data.add(statusData);

        return new MessageContainer(
                data, connectionManager.getTick(), true, remoteAddress, socketBuffer, connectionID
        );
    }

    @Override
    public MessageContainer getDatagram() throws IOException {
        if (statusData == null) return null;

        List<BasePackable> data = new ArrayList<>();
        switch (connectionState) {
            case INITIALISATION_READY:
            case INITIALISING:
            case LOADING_READY:
            case LOADING:
            case SPAWNING_READY:
            case SPAWNING:
            case SIMULATION_READY:
                break;
            case SIMULATING:
                for (Map<Short, BasePackable> type : connectionManager.getDuplex().getOutboundDatagram().values()) {
                    data.addAll(type.values());
                }

                break;
            case CLOSED: // *shuts briefcase*
            default:
                break;
        }

        return new MessageContainer(
                data, connectionManager.getTick(), false, remoteAddress, datagramBuffer, connectionID
        );
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public void updateInbound(Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> entities) {
        connectionManager.getDuplex().updateInbound(entities);
    }

    public void updateConnectionStatus(Map<Byte, BaseRecord<?>> data) {
        byte state = (byte) data.get(ConnectionIDs.STATE).getValue();

        clientPort = (int) data.get(ConnectionIDs.CLIENT_PORT).getValue();
        //remoteAddress = new InetSocketAddress(remoteAddress.getAddress(), clientPort);

        statusData.updateFromDelta(data);
        connectionState = BaseConnectionWrapper.ordinalToConnectionState(state);
    }

    public void close() {
        connectionManager.removeConnection(connectionID);
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }
}
