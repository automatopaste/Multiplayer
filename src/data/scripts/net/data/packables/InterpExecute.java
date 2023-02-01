package data.scripts.net.data.packables;

public interface InterpExecute<T> {

    /**
     * Interpolate between values v1 and v2
     * @param progressive the progressive 0.0 .. 1.0
     * @param v1 the older value
     * @param v2 the newer value
     * @return interpolated value
     */
    T interpExecute(float progressive, T v1, T v2);
}
