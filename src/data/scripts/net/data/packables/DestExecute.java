package data.scripts.net.data.packables;

public interface DestExecute<T> {
    void execute(T value, EntityData packable);
}
