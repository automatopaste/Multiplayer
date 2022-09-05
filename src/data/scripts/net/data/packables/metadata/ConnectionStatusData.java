package data.scripts.net.data.packables.metadata;

import data.scripts.net.data.BasePackable;
import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.io.BaseConnectionWrapper;
import data.scripts.plugins.MPPlugin;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * Used to transfer data regarding connection state
 */
public class ConnectionStatusData extends BasePackable {
    public static int TYPE_ID;

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
    public void destinationInit(MPPlugin plugin) {

    }

    @Override
    public void destinationDelete() {

    }

    @Override
    public boolean isTransient() {
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
        if (connection == null) throw new NullPointerException("Attempted flush write with null connection!");

        id.forceUpdate(connection.getConnectionId());
        id.write(packer, ID);

        state.forceUpdate(connection.getConnectionState().ordinal());
        state.write(packer, STATE);
    }

    @Override
    public int getTypeId() {
        return TYPE_ID;
    }

    @Override
    public BasePackable unpack(int instanceID, Map<Integer, BaseRecord<?>> records) {
        return new ConnectionStatusData(instanceID, records);
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

    public static void setTypeId(int typeId) {
        ConnectionStatusData.TYPE_ID = typeId;
    }

    public static int getConnectionId(InetSocketAddress address) {
        byte[] ids = address.getAddress().getAddress();

        int id = 0x00;
        for (int i = 0; i < 4; i++) {
            id += ids[i];
            id <<= 0x04;
            if (i > 0) id ^= ids[i - 1];
        }

        return id;
    }
}
