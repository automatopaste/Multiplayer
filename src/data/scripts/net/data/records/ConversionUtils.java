package data.scripts.net.data.records;

public class ConversionUtils {

    public static byte floatToByte(float value, float range) {
        return (byte) (value * 255f / range);
    }

    public static float byteToFloat(byte value, float range) {
        return (value & 0xFF) / 255f * range;
    }

    // https://jvm-gaming.org/t/16-bit-float-conversion-java-code/55621/2
    public static short toHalfFloat(final float v) {
        //if(Float.isNaN(v)) throw new UnsupportedOperationException("NaN to half conversion not supported!");
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
            case 0x7c00 :
                return Float.POSITIVE_INFINITY;
            default :
                return Float.intBitsToFloat((( half & 0x8000 )<<16 ) | ((( half & 0x7c00 ) + 0x1C000 )<<13 ) | (( half & 0x03FF )<<13 ));
        }
    }
}
