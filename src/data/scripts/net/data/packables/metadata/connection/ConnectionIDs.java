package data.scripts.net.data.packables.metadata.connection;

import java.net.InetSocketAddress;

public class ConnectionIDs {
    public static byte TYPE_ID;

    public static final byte STATE = 1;
    public static final byte CLIENT_PORT = 2;

    public static short getConnectionID(InetSocketAddress address) {
        byte[] ids = address.getAddress().getAddress();

        short id = 0;
        byte o = 0;
        for (int i = 0; i < 4; i++) {
            byte d = ids[i];
            id <<= (4 - i) * 4;
            id += o ^ d;
            o = d;
        }

        id = (short) ~ id;

        return id;
    }
}
