package logflow;

public interface Sink {
    default void open() {}
    void consume(String item);
    default void close() {}
}