package data.scripts.misc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapSet<T, U> {

    private final Map<T, U> a = new HashMap<>();
    private final Map<U, T> b = new HashMap<>();

    public MapSet() {

    }

    public MapSet(Map<T, U> map) {
        a.putAll(map);

        Set<U> set = new HashSet<>();
        for (T t : map.keySet()) {
            U u = map.get(t);

            if (!set.add(u)) {
                throw new IllegalArgumentException("mapset constructor received duplicate values");
            }

            b.put(u, t);
        }
    }

    public U getA(T t) {
        return a.get(t);
    }

    public T getB(U u) {
        return b.get(u);
    }

    public void putA(T t, U u) {
        a.put(t, u);
    }

    public void putB(U u, T t) {
        b.put(u, t);
    }

    public Set<T> setA() {
        return a.keySet();
    }

    public Set<U> setB() {
        return b.keySet();
    }
}
