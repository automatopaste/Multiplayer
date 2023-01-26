package data.scripts.net.io;

import cmu.CMUtils;
import data.scripts.net.data.packables.entities.ship.VariantData;
import data.scripts.net.data.packables.metadata.ConnectionData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.plugins.MPPlugin;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ServerConnectionWrapper extends BaseConnectionWrapper {
    private final ServerConnectionManager connectionManager;
    private final InetSocketAddress remoteAddress;

    public ServerConnectionWrapper(ServerConnectionManager connectionManager, short connectionId, InetSocketAddress remoteAddress, MPPlugin plugin) {
        super(plugin);

        this.connectionManager = connectionManager;
        this.remoteAddress = remoteAddress;
        this.connectionID = connectionId;

        connectionData = new ConnectionData(connectionId, this);
    }

    @Override
    public MessageContainer getSocketMessage() throws IOException {
        if (connectionData == null) return null;

        connectionData.destExecute();
        connectionState = BaseConnectionWrapper.ordinalToConnectionState(connectionData.getConnectionState());
        clientPort = connectionData.getClientPort();

        Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> outbound = connectionManager.getDuplex().getOutboundSocket();

        switch (connectionState) {
            //case INITIALISATION_READY:
            case INITIALISING:
                CMUtils.getGuiDebug().putText(ServerConnectionWrapper.class, "debug" + connectionID, connectionID + ": initialising connection...");

                connectionState = ConnectionState.LOADING_READY;

                break;
            //case LOADING_READY:
            case LOADING:
                CMUtils.getGuiDebug().putText(ServerConnectionWrapper.class, "debug" + connectionID, connectionID + ": sending client data over socket...");

                Map<Short, Map<Byte, BaseRecord<?>>> variants = new HashMap<>();
                for (VariantData variantData : connectionManager.getServerPlugin().getVariantStore().getGenerated()) {
                    variantData.sourceExecute();
                    variants.put(variantData.getInstanceID(), variantData.getDeltas());
                }

                outbound.put(VariantData.TYPE_ID, variants);

                connectionState = ConnectionState.SPAWNING_READY;

                break;
            //case SPAWNING_READY:
            case SPAWNING:
                CMUtils.getGuiDebug().putText(ServerConnectionWrapper.class, "debug" + connectionID, connectionID + ": spawning ships on client...");

                connectionState = ConnectionState.SIMULATION_READY;

                break;
            //case SIMULATION_READY:
            case SIMULATING:
            case CLOSED:
            default:
                break;
        }

        connectionData.sourceExecute();

        Map<Short, Map<Byte, BaseRecord<?>>> instance = new HashMap<>();
        instance.put(connectionID, connectionData.getDeltas());
        outbound.put(ConnectionData.TYPE_ID, instance);

        ByteBuf data = initBuffer(connectionManager.getTick(), connectionID);
        writeBuffer(outbound, data);

        return new MessageContainer(
                data, connectionManager.getTick(), true, remoteAddress, socketBuffer, connectionID
        );
    }

    @Override
    public MessageContainer getDatagram() throws IOException {
        if (connectionData == null) return null;

        Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> outbound = connectionManager.getDuplex().getOutboundDatagram();

        switch (connectionState) {
            case INITIALISATION_READY:
            case INITIALISING:
            case LOADING_READY:
            case LOADING:
            case SPAWNING_READY:
            case SPAWNING:
            case SIMULATION_READY:
            case SIMULATING:
            case CLOSED:
            default:
                break;
        }

        ByteBuf data = initBuffer(connectionManager.getTick(), connectionID);
        writeBuffer(outbound, data);

        return new MessageContainer(
                data, connectionManager.getTick(), false, remoteAddress, datagramBuffer, connectionID
        );
    }

    @Override
    public void stop() {
        connectionManager.removeConnection(connectionID);
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public void updateInbound(Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> entities) {
        Map<Short, Map<Byte, BaseRecord<?>>> instance = entities.get(ConnectionData.TYPE_ID);
        if (instance != null) connectionData.overwrite(instance.get(connectionID));
        entities.remove(ConnectionData.TYPE_ID);

        connectionManager.getDuplex().updateInbound(entities);
    }

    public void updateConnectionStatus(Map<Byte, BaseRecord<?>> data) {
        connectionData.overwrite(data);
    }

    public void close() {
        connectionManager.removeConnection(connectionID);
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }
}
