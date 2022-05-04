package data.scripts.net.client;

import java.util.ArrayList;
import java.util.List;

public class ClientSendPacket {
    private final List<String> inputs;

    public ClientSendPacket() {
        inputs = new ArrayList<>();
        inputs.add("TROL");
    }

    public List<String> getInputs() {
        return inputs;
    }
}
