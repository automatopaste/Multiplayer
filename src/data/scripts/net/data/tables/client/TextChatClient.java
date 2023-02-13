package data.scripts.net.data.tables.client;

import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.InstanceData;
import data.scripts.net.data.packables.metadata.ChatListenData;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TextChatClient implements InboundEntityManager, OutboundEntityManager {

    private final ChatListenData send;
    private final ChatListenData receive;
    private final short instanceID;

    public TextChatClient(short instanceID) {
        send = new ChatListenData(instanceID);
        receive = new ChatListenData(instanceID);
        this.instanceID = instanceID;
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        send.update(amount, this);
        receive.update(amount, this);
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(ChatListenData.TYPE_ID, this);
        DataGenManager.registerOutboundEntityManager(ChatListenData.TYPE_ID, this);
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, Object> toProcess, MPPlugin plugin, int tick, byte connectionID) {
        receive.destExecute(toProcess, tick);
    }

    @Override
    public void processDeletion(byte typeID, short instanceID, MPPlugin plugin, int tick, byte connectionID) {

    }

    @Override
    public Map<Short, InstanceData> getOutbound(byte typeID, byte connectionID, float amount) {
        Map<Short, InstanceData> out = new HashMap<>();

        InstanceData instanceData = send.sourceExecute(amount);

        if (instanceData != null && instanceData.size > 0) {
            out.put(instanceID, instanceData);
        }

        return out;
    }

    @Override
    public Set<Short> getDeleted(byte typeID, byte connectionID) {
        return null;
    }

    @Override
    public PacketType getOutboundPacketType() {
        return PacketType.SOCKET;
    }

    public void addTextChatEntry(short id, String text) {

    }
}
