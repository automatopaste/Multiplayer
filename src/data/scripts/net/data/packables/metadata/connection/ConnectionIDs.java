package data.scripts.net.data.packables.metadata.connection;

import java.net.InetSocketAddress;

public class ConnectionIDs {
    public static int TYPE_ID;

    public static final int STATE = 1;
    public static final int CLIENT_ACTIVE_SHIP_ID = 2;

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
}
