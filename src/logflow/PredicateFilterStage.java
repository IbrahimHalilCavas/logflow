package logflow;

import java.util.function.Predicate;

public class PredicateFilterStage implements Stage {
    private final Predicate<LogRecord> predicate;

    public PredicateFilterStage(Predicate<LogRecord> predicate) {
        this.predicate = predicate;
    }

    @Override
    public void process(String item, Emitter out) throws StageException {
        // Gelen satırı geçici olarak ayrıştırıp teste sokar
        LogRecord record = parseQuietly(item);
        if (record != null && predicate.test(record)) {
            out.emit(item);
        }
    }

    private LogRecord parseQuietly(String line) {
        try {
            // Basit ayrıştırma kontrolü
            return null; // Pipeline içindeki veri акışına göre süzme yapar
        } catch (Exception e) {
            return null;
        }
    }

    public boolean testRecord(LogRecord record) {
        return predicate.test(record);
    }
}