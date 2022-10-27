package data.scripts.net.data.records;

import data.scripts.net.io.ByteArrayReader;
import io.netty.buffer.ByteBuf;

public class FloatRecord extends BaseRecord<Float> {
    public static int TYPE_ID;

    public enum FloatPrecision {
        FLOAT_32,
        FLOAT_16,
    }
    private FloatPrecision precision;

    private boolean useDecimalPrecision; // if the update checker cares about decimal stuff, use to reduce traffic

    public FloatRecord(Float record, int uniqueID) {
        super(record, uniqueID);

        precision = FloatPrecision.FLOAT_32;
    }

    public FloatRecord(DeltaFunc<Float> deltaFunc, int uniqueID) {
        super(deltaFunc, uniqueID);
    }

    public FloatRecord setUseDecimalPrecision(boolean useDecimalPrecision) {
        this.useDecimalPrecision = useDecimalPrecision;
        return this;
    }

    @Override
    public boolean check() {
        boolean isUpdated;
        Float delta = func.get();

        if (useDecimalPrecision) {
            isUpdated = value.intValue() != delta.intValue();
        } else {
            isUpdated = !value.equals(delta);
        }
        if (isUpdated) value = delta;

        return isUpdated;
    }

    @Override
    public void get(ByteBuf dest) {
        switch (precision) {
            default:
            case FLOAT_32:
                dest.writeFloat(value);
                break;
            case FLOAT_16:
                dest.writeShort(toHalfFloat(value));
                break;
        }
    }

    @Override
    public BaseRecord<Float> read(ByteBuf in, int uniqueID) {
        float value;
        switch (precision) {
            default:
            case FLOAT_32:
                value = in.readFloat();
                break;
            case FLOAT_16:
                value = toFloat(in.readShort());
                break;
        }
        return new FloatRecord(value, uniqueID);
    }

    @Override
    public BaseRecord<Float> read(ByteArrayReader in, int uniqueID) {
        float value = in.readFloat();
        return new FloatRecord(value, uniqueID);
    }

    public static void setTypeId(int typeId) {
        FloatRecord.TYPE_ID = typeId;
    }

    @Override
    public int getTypeId() {
        return TYPE_ID;
    }

    public static BaseRecord<Float> getDefault(int uniqueID) {
        return new FloatRecord(0f, uniqueID);
    }

    public FloatRecord setPrecision(FloatPrecision precision) {
        this.precision = precision;
        return this;
    }

    @Override
    public String toString() {
        return "FloatRecord{" +
                "record=" + value +
                '}';
    }

    // https://jvm-gaming.org/t/16-bit-float-conversion-java-code/55621/2
    public static short toHalfFloat(final float v) {
        if(Float.isNaN(v)) throw new UnsupportedOperationException("NaN to half conversion not supported!");
        if(v == Float.POSITIVE_INFINITY) return(short)0x7c00;
        if(v == Float.NEGATIVE_INFINITY) return(short)0xfc00;
        if(v == 0.0f) return(short)0x0000;
        if(v == -0.0f) return(short)0x8000;
        if(v > 65504.0f) return 0x7bff;  // max value supported by half float
        if(v < -65504.0f) return(short)( 0x7bff | 0x8000 );
        if(v > 0.0f && v < 5.96046E-8f) return 0x0001;
        if(v < 0.0f && v > -5.96046E-8f) return(short)0x8001;

        final int f = Float.floatToIntBits(v);

        return(short)((( f>>16 ) & 0x8000 ) | (((( f & 0x7f800000 ) - 0x38000000 )>>13 ) & 0x7c00 ) | (( f>>13 ) & 0x03ff ));
    }
    public static float toFloat(final short half) {
        switch((int)half)
        {
            case 0x0000 :
                return 0.0f;
            case 0x8000 :
                return -0.0f;
            case 0x7c00 :
                return Float.POSITIVE_INFINITY;
            case 0xfc00 :
                return Float.NEGATIVE_INFINITY;
            // support for NaN ?
            default :
                return Float.intBitsToFloat((( half & 0x8000 )<<16 ) | ((( half & 0x7c00 ) + 0x1C000 )<<13 ) | (( half & 0x03FF )<<13 ));
        }
    }
}
