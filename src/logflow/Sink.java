package logflow;

public interface Sink {
    void consume(String item);
}