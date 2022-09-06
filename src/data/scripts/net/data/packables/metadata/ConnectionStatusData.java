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

    private final IntRecord state;

    private static final int STATE = 1;

    private BaseConnectionWrapper connection;
    private boolean canUpdateState = true;

    public ConnectionStatusData(int connectionID) {
        super(connectionID);

        state = new IntRecord(0);
    }

    public ConnectionStatusData(int connectionID, Map<Integer, BaseRecord<?>> records) {
        super(connectionID);

        BaseRecord<?> temp;

        temp = records.get(STATE);
        state = (temp == null) ? (IntRecord) new IntRecord(0).setUndefined(true) : (IntRecord) temp;
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
        if (d.getState().isDefined()) state.forceUpdate(d.getState().getRecord());
//        if (d.getState() != null && canUpdateState) {
//            state.forceUpdate(d.getState().getRecord());
//            canUpdateState = false;
//        }
    }

    @Override
    protected boolean write(boolean flush) {
        if (flush) {
            flushWrite();
            return false;
        }

        if (connection == null) return false;

        boolean update = false;
        if (state.checkUpdate(connection.getConnectionState().ordinal())) {
            state.write(packer, STATE);
            update = true;
        }

        return update;
    }

    private void flushWrite() {
        if (connection == null) throw new NullPointerException("Attempted flush write with null connection!");

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

    public IntRecord getState() {
        return state;
    }

    public static void setTypeId(int typeId) {
        ConnectionStatusData.TYPE_ID = typeId;
    }

    public static int getConnectionId(InetSocketAddress address) {
        byte[] ids = address.getAddress().getAddress();

        int id = 0x00;
        byte o = 0x0;
        for (int i = 0; i < 4; i++) {
            byte d = ids[i];
            id <<= (4 - i) * 4;
            id += o ^ d;
            o = d;
        }

        id =~ id;

        return id;
    }

    public void updateState() {
        canUpdateState = true;
    }
}
