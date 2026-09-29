package logflow;

public interface Source {
    void produce(Emitter out);
}