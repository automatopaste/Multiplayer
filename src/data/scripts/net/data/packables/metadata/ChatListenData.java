package data.scripts.net.data.packables.metadata;

import data.scripts.net.data.packables.DestExecute;
import data.scripts.net.data.packables.EntityData;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.records.ByteRecord;
import data.scripts.net.data.records.collections.ListenArrayRecord;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.gui.MPChatboxPlugin;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Facilitates one-way chat text
 */
public class ChatListenData extends EntityData {

    public static byte TYPE_ID;

    private final List<MPChatboxPlugin.ChatEntry> toWrite = new ArrayList<>();
    private final List<MPChatboxPlugin.ChatEntry> received = new ArrayList<>();

    public ChatListenData(short instanceID) {
        super(instanceID);

        addRecord(new RecordLambda<>(
                new ListenArrayRecord<>(new ArrayList<Byte>(), ByteRecord.TYPE_ID).setDebugText("chat strings"),
                new SourceExecute<List<Byte>>() {
                    @Override
                    public List<Byte> get() {
                        List<Byte> out = new ArrayList<>();

                        for (MPChatboxPlugin.ChatEntry entry : toWrite) {
                            out.add(entry.connectionID);

                            byte[] s = entry.text.getBytes(StandardCharsets.UTF_8);
                            byte l = (byte) (s.length & 0xFF);
                            out.add(l);

                            for (int i = 0; i < Math.min(s.length, 255); i++) {
                                out.add(s[i]);
                            }
                        }

                        toWrite.clear();

                        if (out.size() > 0) {
                            float f = 0f;
                        }

                        return out;
                    }
                },
                new DestExecute<List<Byte>>() {
                    @Override
                    public void execute(List<Byte> value, EntityData packable) {
                        for (Iterator<Byte> iterator = value.iterator(); iterator.hasNext(); ) {
                            byte id = iterator.next();
                            byte l = iterator.next();
                            byte[] s = new byte[l];
                            for (int i = 0; i < l; i++) {
                                s[i] = iterator.next();
                            }

                            String text = new String(s, StandardCharsets.UTF_8);

                            received.add(new MPChatboxPlugin.ChatEntry(text, "unknown", id));
                        }
                    }
                }
        ));
    }

    @Override
    public void init(MPPlugin plugin, InboundEntityManager manager) {

    }

    @Override
    public void update(float amount, BaseEntityManager manager) {

    }

    @Override
    public void delete() {

    }

    public void submitChatEntry(MPChatboxPlugin.ChatEntry entry) {
        toWrite.add(entry);
    }

    public List<MPChatboxPlugin.ChatEntry> getReceived() {
        if (received.isEmpty()) {
            return new ArrayList<>();
        }
        List<MPChatboxPlugin.ChatEntry> out = new ArrayList<>(received);
        received.clear();
        return out;
    }

    @Override
    public byte getTypeID() {
        return TYPE_ID;
    }

    public static void setTypeId(byte typeId) {
        TYPE_ID = typeId;
    }
}
