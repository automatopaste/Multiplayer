package data.scripts.net.data.tables.server;

import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.InstanceData;
import data.scripts.net.data.packables.metadata.ChatListenData;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.gui.MPChatboxPlugin;

import java.util.*;

public class TextChatHost implements InboundEntityManager, OutboundEntityManager {

    private final MPChatboxPlugin chatbox;
    private final PlayerLobby playerLobby;

    private final Map<Short, ChatListenData> listeners = new HashMap<>();
    private final ChatListenData send;

    public TextChatHost(MPChatboxPlugin chatbox, PlayerLobby playerLobby) {
        this.chatbox = chatbox;
        this.playerLobby = playerLobby;

        send = new ChatListenData(DEFAULT_HOST_INSTANCE);
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        List<MPChatboxPlugin.ChatEntry> fresh = new ArrayList<>();

        String hostInput = chatbox.getInput();
        if (hostInput != null) {
            fresh.add(new MPChatboxPlugin.ChatEntry(hostInput, playerLobby.getUsernames().get(DEFAULT_HOST_ID), DEFAULT_HOST_ID));
        }

        for (short id : listeners.keySet()) {
            ChatListenData listener = listeners.get(id);

            List<MPChatboxPlugin.ChatEntry> entries = listener.getReceived();
            fresh.addAll(entries);
        }

        for (MPChatboxPlugin.ChatEntry chatEntry : fresh) {
            String username = playerLobby.getUsernames().get(chatEntry.connectionID);
            if (username != null) chatEntry.username = username;

            send.submitChatEntry(chatEntry);
            chatbox.addEntry(chatEntry);
        }
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, Object> toProcess, MPPlugin plugin, int tick, byte connectionID) {
        ChatListenData data = listeners.get(instanceID);
        if (data == null) {
            data = new ChatListenData(instanceID);
            listeners.put(instanceID, data);

            data.destExecute(toProcess, tick);
            data.init(plugin, this);
        } else {
            data.destExecute(toProcess, tick);
        }
    }

    @Override
    public void processDeletion(byte typeID, short instanceID, MPPlugin plugin, int tick, byte connectionID) {
        listeners.remove(instanceID);
    }

    @Override
    public Map<Short, InstanceData> getOutbound(byte typeID, byte connectionID, float amount) {
        Map<Short, InstanceData> out = new HashMap<>();

        InstanceData instanceData = send.sourceExecute(amount);
        if (!instanceData.records.isEmpty()) {
            out.put(DEFAULT_HOST_INSTANCE, instanceData);
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

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(ChatListenData.TYPE_ID, this);
        DataGenManager.registerOutboundEntityManager(ChatListenData.TYPE_ID, this);
    }
}
