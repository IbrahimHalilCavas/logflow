package logflow;

public class ConsoleSink implements Sink {
    @Override
    public void consume(String item) {
        System.out.println(item);
    }
}