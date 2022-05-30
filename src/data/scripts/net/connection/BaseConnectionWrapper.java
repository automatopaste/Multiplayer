package data.scripts.net.connection;

import data.scripts.net.data.packables.ConnectionStatusData;
import data.scripts.net.io.PacketContainer;

import java.io.IOException;

public abstract class BaseConnectionWrapper {
    public enum ConnectionState {
        INITIALISATION_READY,
        INITIALISING,
        LOADING_READY,
        LOADING,
        SIMULATING,
        CLOSED
    }
    protected ConnectionState connectionState = ConnectionState.INITIALISATION_READY;

    protected ConnectionStatusData statusData;

    protected int connectionId;

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    public abstract PacketContainer getSocketMessage() throws IOException;

    public abstract PacketContainer getDatagram() throws IOException;
}
