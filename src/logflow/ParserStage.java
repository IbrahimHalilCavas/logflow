package logflow;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;

public class ParserStage implements Stage {
    private static final DateTimeFormatter CLF_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);

    private int skippedCount = 0;

    @Override
    public void process(String line, Emitter out) {
        if (line == null || line.trim().isEmpty()) {
            skippedCount++;
            return;
        }

        try {
            String trimmed = line.trim();
            String[] parts = trimmed.split(" ");
            if (parts.length < 7) {
                skippedCount++;
                return;
            }

            String ip = parts[0];

            int dateStart = trimmed.indexOf("[");
            int dateEnd = trimmed.indexOf("]");
            if (dateStart == -1 || dateEnd == -1) {
                skippedCount++;
                return;
            }
            String dateStr = trimmed.substring(dateStart + 1, dateEnd);
            Instant timestamp = OffsetDateTime.parse(dateStr, CLF_DATE_FORMATTER).toInstant();

            int firstQuote = trimmed.indexOf("\"");
            int secondQuote = trimmed.indexOf("\"", firstQuote + 1);
            if (firstQuote == -1 || secondQuote == -1) {
                skippedCount++;
                return;
            }
            String request = trimmed.substring(firstQuote + 1, secondQuote);
            String[] reqParts = request.split(" ");
            if (reqParts.length < 2) {
                skippedCount++;
                return;
            }
            String method = reqParts[0];
            String path = reqParts[1];

            String remainder = trimmed.substring(secondQuote + 1).trim();
            String[] remParts = remainder.split("\\s+");
            if (remParts.length < 2) {
                skippedCount++;
                return;
            }

            int status = Integer.parseInt(remParts[0]);
            long bytes = remParts[1].equals("-") ? 0L : Long.parseLong(remParts[1]);

            String userAgent = "-";
            int uaStart = remainder.indexOf("\"", remainder.indexOf("\"") + 1);
            if (uaStart != -1) {
                int uaEnd = remainder.lastIndexOf("\"");
                if (uaEnd > uaStart) {
                    userAgent = remainder.substring(uaStart + 1, uaEnd);
                }
            }

            LogRecord record = new LogRecord(
                    timestamp,
                    ip,
                    method,
                    path,
                    status,
                    bytes,
                    userAgent,
                    Collections.emptyMap(),
                    line
            );

            String formatted = String.format("[%s] %s %s %s -> %d (%d bytes)",
                    record.timestamp(),
                    record.clientIp(),
                    record.method(),
                    record.path(),
                    record.status(),
                    record.bytes());

            out.emit(formatted);
        } catch (Exception e) {
            skippedCount++;
            // Hata durumunda fırlatmayıp skipped sayısını artırıyoruz
        }
    }

    public int getSkippedCount() {
        return skippedCount;
    }
}