package data.scripts.net.data;

import data.scripts.net.data.packables.entities.variant.VariantSource;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

public abstract class SourcePackable extends BasePackable {

    protected boolean initialForce = true;

    /**
     * Source constructor
     * @param instanceID unique
     */
    public SourcePackable(int instanceID) {
        super(instanceID);
    }

    /**
     * Ouput data to a byte buffer
     */
    public void write(boolean force, ByteBuf dest) {
        ByteBuf temp = PooledByteBufAllocator.DEFAULT.buffer();

        boolean f = force || initialForce;
        initialForce = false;

        for (BaseRecord<?> record : records.values()) {
            record.write(f, temp);
        }

        if (this instanceof VariantSource) {
            int i = 0;
        }

        if (temp.readableBytes() > 0) {
            // so packer type can be identified
            dest.writeInt(getTypeId());
            // so packer instance can be identified
            dest.writeInt(getInstanceID());

            dest.writeBytes(temp);
        }

        temp.release();
    }
}
