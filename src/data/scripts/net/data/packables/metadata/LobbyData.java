package data.scripts.net.data.packables.metadata;

import data.scripts.net.data.packables.DestExecute;
import data.scripts.net.data.packables.EntityData;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.records.ByteRecord;
import data.scripts.net.data.records.collections.SyncingListRecord;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.server.combat.players.PlayerLobby;
import data.scripts.net.data.tables.server.combat.players.PlayerShips;
import data.scripts.plugins.MPPlugin;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Sends information about lobby data to clients
 */
public class LobbyData extends EntityData {

    public static final int MAX_USERNAME_CHARS = 12;

    public static byte TYPE_ID;

    private List<Byte> players;
    private Map<Byte, Short> playerShipIDs;
    private final Map<Byte, String> playerUsernames = new HashMap<>();

    public LobbyData(short instanceID, final PlayerLobby playerLobby, final PlayerShips playerShips) {
        super(instanceID);

        addRecord(new RecordLambda<>(
                new SyncingListRecord<>(new ArrayList<Byte>(), ByteRecord.TYPE_ID).setDebugText("player ship instances"),
                new SourceExecute<List<Byte>>() {
                    @Override
                    public List<Byte> get() {
                        List<Byte> out = new ArrayList<>();

                        // server ship
                        out.add((byte) -1);

                        for (short id : playerLobby.getPlayers().keySet()) {
                            out.add((byte) (id & 0xFF));
                        }

                        return out;
                    }
                },
                new DestExecute<List<Byte>>() {
                    @Override
                    public void execute(List<Byte> value, EntityData packable) {
                        LobbyData lobbyData = (LobbyData) packable;
                        lobbyData.setPlayers(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                new SyncingListRecord<>(new ArrayList<Byte>(), ByteRecord.TYPE_ID).setDebugText("player piloted ship ids"),
                new SourceExecute<List<Byte>>() {
                    @Override
                    public List<Byte> get() {
                        List<Byte> out = new ArrayList<>();

                        out.add(BaseEntityManager.DEFAULT_HOST_ID);
                        short id = playerShips.getHostShipID();
                        out.add((byte) ((id >>> 8) & 0xFF));
                        out.add((byte) (id & 0xFF));

                        for (PlayerShipData playerShipData : playerShips.getPlayerShips().values()) {
                            out.add((byte) (playerShipData.getInstanceID())); // hack where instance id is player id

                            id = playerShipData.getPlayerShipID();
                            out.add((byte) ((id >>> 8) & 0xFF));
                            out.add((byte) (id & 0xFF));
                        }

                        return out;
                    }
                },
                new DestExecute<List<Byte>>() {
                    @Override
                    public void execute(List<Byte> value, EntityData packable) {
                        Map<Byte, Short> in = new HashMap<>();

                        for (Iterator<Byte> iterator = value.iterator(); iterator.hasNext(); ) {
                            byte id = iterator.next();

                            byte b1 = iterator.next();
                            byte b2 = iterator.next();
                            short s = (short) ((b1 << 8) | b2);

                            in.put(id, s);
                        }

                        setPlayerShipIDs(in);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                new SyncingListRecord<>(new ArrayList<Byte>(), ByteRecord.TYPE_ID).setDebugText("player names"),
                new SourceExecute<List<Byte>>() {
                    @Override
                    public List<Byte> get() {
                        List<Byte> out = new ArrayList<>();

                        for (byte id : playerLobby.getUsernames().keySet()) {
                            String name = playerLobby.getUsernames().get(id);
                            out.add(id);

                            byte[] b = name.getBytes(StandardCharsets.UTF_8);
                            int length = Math.min(b.length, MAX_USERNAME_CHARS);
                            out.add((byte) length);
                            for (int i = 0; i < length; i++) {
                                out.add(b[i]);
                            }
                        }

                        return out;
                    }
                },
                new DestExecute<List<Byte>>() {
                    @Override
                    public void execute(List<Byte> value, EntityData packable) {
                        ListIterator<Byte> iterator = value.listIterator();

                        Map<Byte, String> out = new HashMap<>();

                        while (iterator.hasNext()) {
                            byte id = iterator.next();
                            byte length = iterator.next();
                            byte[] b = new byte[length];
                            for (int i = 0; i < length; i++) {
                                b[i] = iterator.next();
                            }
                            String username = new String(b, StandardCharsets.UTF_8);

                            out.put(id, username);
                        }

                        getPlayerUsernames().putAll(out);
                    }
                }
        ));
    }

    @Override
    public void init(MPPlugin plugin, InboundEntityManager manager) {

    }

    @Override
    public void update(float amount, BaseEntityManager manager, MPPlugin plugin) {

    }

    @Override
    public void delete() {

    }

    @Override
    public byte getTypeID() {
        return TYPE_ID;
    }

    public List<Byte> getPlayers() {
        return players;
    }

    public void setPlayers(List<Byte> players) {
        this.players = players;
    }

    public Map<Byte, Short> getPlayerShipIDs() {
        return playerShipIDs;
    }

    public void setPlayerShipIDs(Map<Byte, Short> playerShipIDs) {
        this.playerShipIDs = playerShipIDs;
    }

    public Map<Byte, String> getPlayerUsernames() {
        return playerUsernames;
    }
}
