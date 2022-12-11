package data.scripts.net.io;

import data.scripts.net.data.packables.metadata.connection.ConnectionData;
import data.scripts.plugins.MPPlugin;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

import java.io.IOException;

public abstract class BaseConnectionWrapper {
    public static final short DEFAULT_CONNECTION_ID = -10;

    public enum ConnectionState {
        INITIALISATION_READY,
        INITIALISING,
        LOADING_READY,
        LOADING,
        SPAWNING_READY,
        SPAWNING,
        SIMULATION_READY,
        SIMULATING,
        CLOSED
    }
    protected ConnectionState connectionState = ConnectionState.INITIALISATION_READY;

    protected ConnectionData statusData;

    protected short connectionID = DEFAULT_CONNECTION_ID;
    protected int clientPort;

    protected MPPlugin localPlugin;

    protected final ByteBuf socketBuffer;
    protected final ByteBuf datagramBuffer;

    public BaseConnectionWrapper(MPPlugin localPlugin) {
        this.localPlugin = localPlugin;

        socketBuffer = PooledByteBufAllocator.DEFAULT.buffer();
        datagramBuffer = PooledByteBufAllocator.DEFAULT.buffer();
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public void setConnectionID(short connectionID) {
        this.connectionID = connectionID;
    }

    public short getConnectionID() {
        return connectionID;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    public abstract MessageContainer getSocketMessage() throws IOException;

    public abstract MessageContainer getDatagram() throws IOException;

    public static BaseConnectionWrapper.ConnectionState ordinalToConnectionState(int state) {
        switch (state) {
            case 0:
                return BaseConnectionWrapper.ConnectionState.INITIALISATION_READY;
            case 1:
                return BaseConnectionWrapper.ConnectionState.INITIALISING;
            case 2:
                return BaseConnectionWrapper.ConnectionState.LOADING_READY;
            case 3:
                return BaseConnectionWrapper.ConnectionState.LOADING;
            case 4:
                return ConnectionState.SPAWNING_READY;
            case 5:
                return ConnectionState.SPAWNING;
            case 6:
                return BaseConnectionWrapper.ConnectionState.SIMULATION_READY;
            case 7:
                return BaseConnectionWrapper.ConnectionState.SIMULATING;
            case 8:
                return BaseConnectionWrapper.ConnectionState.CLOSED;
            default:
                return null;
        }
    }

    public MPPlugin getLocalPlugin() {
        return localPlugin;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(short clientPort) {
        this.clientPort = clientPort;
    }
}
