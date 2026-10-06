package logflow;

public class ConsoleSink implements Sink {
    @Override
    public void consume(String item) {
        System.out.println(item);
    }

    public void consume(LogRecord record) {
        System.out.printf("[%s] %s %s %s -> %d (%d bytes)%n",
                record.timestamp(),
                record.clientIp(),
                record.method(),
                record.path(),
                record.status(),
                record.bytes());
    }
}