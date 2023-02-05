package data.scripts.net.data.packables.metadata;

import com.fs.starfarer.api.combat.ViewportAPI;
import data.scripts.net.data.packables.EntityData;
import data.scripts.net.data.packables.DestExecute;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.pregen.ProjectileDatastore;
import data.scripts.net.data.records.*;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.plugins.MPPlugin;
import org.lwjgl.util.vector.Vector2f;

/**
 * Sends player camera data to the server
 */
public class ClientData extends EntityData {

    public static byte TYPE_ID;

    private Vector2f viewportCenter;
    private float viewMult;
    private int connectionType;
    private short numSpecs;

    public ClientData(short instanceID, final ViewportAPI viewport, final MPPlugin plugin) {
        super(instanceID);

        try {
            ProjectileDatastore datastore = (ProjectileDatastore) plugin.getDatastore(ProjectileDatastore.class);
            numSpecs = (short) datastore.getGeneratedWeaponIDs().size();
        } catch (NullPointerException ignored) {}

        addRecord(new RecordLambda<>(
                Vector2f32Record.getDefault(),
                new SourceExecute<Vector2f>() {
                    @Override
                    public Vector2f get() {
                        return viewport.getCenter();
                    }
                },
                new DestExecute<Vector2f>() {
                    @Override
                    public void execute(Vector2f value, EntityData packable) {
                        ClientData clientData = (ClientData) packable;
                        clientData.setViewportCenter(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                Float16Record.getDefault(),
                new SourceExecute<Float>() {
                    @Override
                    public Float get() {
                        return viewport.getViewMult();
                    }
                },
                new DestExecute<Float>() {
                    @Override
                    public void execute(Float value, EntityData packable) {
                        ClientData clientData = (ClientData) packable;
                        clientData.setViewMult(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault(),
                new SourceExecute<Byte>() {
                    @Override
                    public Byte get() {
                        switch (plugin.getType()) {
                            case SERVER:
                                return 1;
                            case CLIENT:
                            default:
                                return 0;
                        }
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(Byte record, EntityData packable) {
                        ClientData clientData = (ClientData) packable;
                        clientData.setConnectionType(record);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ShortRecord.getDefault(),
                new SourceExecute<Short>() {
                    @Override
                    public Short get() {
                        return numSpecs;
                    }
                },
                new DestExecute<Short>() {
                    @Override
                    public void execute(Short value, EntityData packable) {
                        numSpecs = value;
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

    @Override
    public byte getTypeID() {
        return TYPE_ID;
    }

    public Vector2f getViewportCenter() {
        return viewportCenter;
    }

    public void setViewportCenter(Vector2f viewportCenter) {
        this.viewportCenter = viewportCenter;
    }

    public float getViewMult() {
        return viewMult;
    }

    public void setViewMult(float viewMult) {
        this.viewMult = viewMult;
    }

    public int getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(int connectionType) {
        this.connectionType = connectionType;
    }

    public short getNumSpecs() {
        return numSpecs;
    }
}
