package data.scripts.net.data.packables.metadata.connection;

import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.SourcePackable;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.net.io.BaseConnectionWrapper;
import data.scripts.plugins.MPClientPlugin;
import data.scripts.plugins.MPPlugin;

public class ConnectionSource extends SourcePackable {

    public ConnectionSource(int connectionID, final BaseConnectionWrapper connection) {
        super(connectionID);

        putRecord(new IntRecord(new BaseRecord.DeltaFunc<Integer>() {
            @Override
            public Integer get() {
                return connection.getConnectionState().ordinal();
            }
        }, ConnectionIDs.STATE));
        putRecord(new StringRecord(new BaseRecord.DeltaFunc<String>() {
            @Override
            public String get() {
                MPPlugin plugin = connection.getLocalPlugin();
                if (plugin.getType() == MPPlugin.PluginType.CLIENT) {
                    MPClientPlugin clientPlugin = (MPClientPlugin) plugin;
                    ShipAPI active = clientPlugin.getShipTable().getClientActive();

                    return active == null ? null : active.getFleetMemberId();
                }
                return null;
            }
        }, ConnectionIDs.CLIENT_ACTIVE_SHIP_ID));

    }

    @Override
    public int getTypeId() {
        return ConnectionIDs.TYPE_ID;
    }
}
