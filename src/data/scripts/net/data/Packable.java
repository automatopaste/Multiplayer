package data.scripts.net.data;

import java.io.IOException;

public interface Packable {
    byte[] pack() throws IOException;

    int getTypeId();
}
