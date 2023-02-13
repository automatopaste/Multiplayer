package data.scripts.net.data.tables;

import data.scripts.plugins.MPPlugin;

import java.util.Map;

public interface InboundEntityManager extends BaseEntityManager {

    /**
     * Process an inbound delta
     * @param typeID the entity type ID
     * @param instanceID the unique instance ID of the entity
     * @param toProcess a map of records IDs to data values
     * @param plugin the plugin running in the local game
     * @param tick the current tick at which time the packet was sent
     * @param connectionID the source connection ID of the delta
     */
    void processDelta(byte typeID, short instanceID, Map<Byte, Object> toProcess, MPPlugin plugin, int tick, byte connectionID);

    /**
     * Indicates that the remote connection is signaling for an entity instance to be deleted
     * @param typeID the entity type ID
     * @param instanceID the unique instance ID of the entity
     * @param plugin the plugin running in the local game
     * @param tick the current tick at which time the packet was sent
     */
    void processDeletion(byte typeID, short instanceID, MPPlugin plugin, int tick, byte connectionID);
}
