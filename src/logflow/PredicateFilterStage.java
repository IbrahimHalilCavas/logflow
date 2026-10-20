package logflow;

import java.util.function.Predicate;

public class PredicateFilterStage implements Stage {
    private final Predicate<LogRecord> predicate;

    public PredicateFilterStage(Predicate<LogRecord> predicate) {
        this.predicate = predicate;
    }

    @Override
    public void process(String item, Emitter out) throws StageException {
        // Eğer gelen öge boru hattından zaten geçmiş ve ConsoleSink düzeninde ise
        // veya ham log satırıysa süzme testi uygularız.
        // Test için geçici ayrıştırma:
        LogRecord record = parseLineQuietly(item);
        if (record != null) {
            if (predicate.test(record)) {
                out.emit(item);
            }
        } else {
            // Ayrıştırılamıyorsa varsayılan olarak iletir
            out.emit(item);
        }
    }

    private LogRecord parseLineQuietly(String line) {
        try {
            if (line == null || line.trim().isEmpty()) return null;
            String trimmed = line.trim();
            String[] parts = trimmed.split(" ");
            
            // Eğer "LogRecord[...]" biçiminde veya düz metin ise durum kodunu yakalayalım:
            int status = -1;
            for (String p : parts) {
                if (p.equals("->") || p.startsWith("status=")) {
                    continue;
                }
                try {
                    int val = Integer.parseInt(p);
                    if (val >= 100 && val <= 599) {
                        status = val;
                        break;
                    }
                } catch (NumberFormatException ignored) {}
            }

            if (status != -1) {
                return new LogRecord(null, "", "", "", status, 0L, "-", null, line);
            }
        } catch (Exception ignored) {}
        return null;
    }

    public boolean testRecord(LogRecord record) {
        return predicate.test(record);
    }
}