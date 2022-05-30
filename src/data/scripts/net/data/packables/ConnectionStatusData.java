package data.scripts.net.data.packables;

import data.scripts.net.io.BaseConnectionWrapper;
import data.scripts.net.data.BasePackable;
import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.records.IntRecord;
import data.scripts.plugins.mpClientPlugin;
import data.scripts.plugins.mpServerPlugin;

import java.util.Map;

/**
 * Used to transfer data regarding connection state
 */
public class ConnectionStatusData extends BasePackable {
    private static int typeID;

    public static final int UNASSIGNED = -100;

    private final IntRecord id;
    private final IntRecord state;

    private static final int ID = 1;
    private static final int STATE = 2;

    private BaseConnectionWrapper connection;

    public ConnectionStatusData(int instanceID) {
        super(instanceID);

        id = new IntRecord(0);
        state = new IntRecord(0);
    }

    public ConnectionStatusData(int instanceID, Map<Integer, BaseRecord<?>> records) {
        super(instanceID);

        BaseRecord<?> temp;

        temp = records.get(ID);
        id = (temp == null) ? new IntRecord(0) : (IntRecord) temp;
        temp = records.get(STATE);
        state = (temp == null) ? new IntRecord(0) : (IntRecord) temp;
    }

    @Override
    public void destinationUpdate() {

    }

    @Override
    public void destinationInit(mpServerPlugin serverPlugin) {

    }

    @Override
    public void destinationInit(mpClientPlugin clientPlugin) {

    }

    @Override
    public void destinationDelete() {

    }

    @Override
    public boolean shouldDeleteOnDestination() {
        return false;
    }

    @Override
    public void updateFromDelta(BasePackable delta) {
        ConnectionStatusData d = (ConnectionStatusData) delta;
        if (d.getId() != null) id.forceUpdate(d.getId().getRecord());
        if (d.getState() != null) state.forceUpdate(d.getState().getRecord());
    }

    @Override
    protected boolean write(boolean flush) {
        if (flush) {
            flushWrite();
            return false;
        }

        if (connection == null) return false;

        boolean update = false;
        if (id.checkUpdate(connection.getConnectionId())) {
            id.write(packer, ID);
            update = true;
        }
        if (state.checkUpdate(connection.getConnectionState().ordinal())) {
            state.write(packer, STATE);
            update = true;
        }

        return update;
    }

    private void flushWrite() {
        id.forceUpdate(connection.getConnectionId());
        id.write(packer, ID);

        state.forceUpdate(connection.getConnectionState().ordinal());
        state.write(packer, STATE);
    }

    @Override
    public int getTypeId() {
        return typeID;
    }

    @Override
    public BasePackable unpack(int instanceID, Map<Integer, BaseRecord<?>> records) {
        return new ConnectionStatusData(instanceID, records);
    }

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
                return BaseConnectionWrapper.ConnectionState.SIMULATING;
            case 5:
                return BaseConnectionWrapper.ConnectionState.CLOSED;
            default:
                return null;
        }
    }

    public void setConnection(BaseConnectionWrapper connection) {
        this.connection = connection;
    }

    public IntRecord getId() {
        return id;
    }

    public IntRecord getState() {
        return state;
    }

    public static void setTypeID(int typeID) {
        ConnectionStatusData.typeID = typeID;
    }
}
