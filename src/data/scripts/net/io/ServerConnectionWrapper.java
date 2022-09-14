package data.scripts.net.io;

import cmu.CMUtils;
import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.SourcePackable;
import data.scripts.net.data.packables.metadata.connection.ConnectionIDs;
import data.scripts.net.data.packables.metadata.connection.ConnectionSource;
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

    public ServerConnectionWrapper(ServerConnectionManager connectionManager, int connectionId, InetSocketAddress remoteAddress, MPPlugin plugin) {
        super(plugin);

        this.connectionManager = connectionManager;
        this.remoteAddress = remoteAddress;
        this.connectionId = connectionId;

        statusData = new ConnectionSource(connectionId, this);
    }

    @Override
    public PacketContainer getSocketMessage() throws IOException {
        if (statusData == null) return null;

        List<SourcePackable> data = new ArrayList<>();
        switch (connectionState) {
            //case INITIALISATION_READY:
            case INITIALISING:
                CMUtils.getGuiDebug().putText(ServerConnectionWrapper.class, "debug" + connectionId, connectionId + ": initialising connection...");

                connectionState = ConnectionState.LOADING_READY;
                statusData.getRecord(ConnectionIDs.STATE).updateFromDelta(new IntRecord(connectionState.ordinal(), -1));

                break;
            //case LOADING_READY:
            case LOADING:
                CMUtils.getGuiDebug().putText(ServerConnectionWrapper.class, "debug" + connectionId, connectionId + ": sending client data over socket...");

                data.addAll(connectionManager.getServerPlugin().getDataStore().getGenerated());

                connectionState = ConnectionState.SPAWNING_READY;
                statusData.getRecord(ConnectionIDs.STATE).updateFromDelta(new IntRecord(connectionState.ordinal(), -1));

                break;
            //case SPAWNING_READY:
            case SPAWNING:
                CMUtils.getGuiDebug().putText(ServerConnectionWrapper.class, "debug" + connectionId, connectionId + ": spawning ships on client...");

                data.addAll(connectionManager.getServerPlugin().getServerShipTable().getOutbound().values());

                connectionState = ConnectionState.SIMULATION_READY;
                statusData.getRecord(ConnectionIDs.STATE).updateFromDelta(new IntRecord(connectionState.ordinal(), -1));

                break;
            //case SIMULATION_READY:
            case SIMULATING:
            case CLOSED:
            default:
        }

        data.add(statusData);

        return new PacketContainer(
                data,
                connectionManager.getTick(),
                true,
                remoteAddress,
                socketBuffer
        );
    }

    @Override
    public PacketContainer getDatagram() throws IOException {
        if (statusData == null) return null;

        List<SourcePackable> data = new ArrayList<>();
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
                for (Map<Integer, SourcePackable> type : connectionManager.getDuplex().getOutboundDatagram().values()) {
                    data.addAll(type.values());
                }

                break;
            case CLOSED: // *shuts briefcase*
            default:
                break;
        }

        return new PacketContainer(
                data, connectionManager.getTick(), false, remoteAddress, datagramBuffer
        );
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public void updateInbound(Map<Integer, Map<Integer, Map<Integer, BaseRecord<?>>>> entities) {
        connectionManager.getDuplex().updateInbound(entities);
    }

    public void updateConnectionStatus(Map<Integer, BaseRecord<?>> data) {
        int state = (int) data.get(ConnectionIDs.STATE).getValue();
        if (state < connectionState.ordinal()) {
            //return;
        }

        statusData.updateFromDelta(data);
        connectionState = BaseConnectionWrapper.ordinalToConnectionState(state);
    }

    public void close() {
        connectionManager.removeConnection(connectionId);
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }
}
