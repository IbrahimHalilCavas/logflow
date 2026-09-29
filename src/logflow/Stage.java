package logflow;

public interface Stage {
    void process(String input, Emitter out) throws StageException;
    default void open() {}
    default void close() {}
}