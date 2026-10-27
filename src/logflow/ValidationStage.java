package logflow;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationStage implements Stage {

    // İleride bunları YAML'dan okuyacak şekilde parametreye çevirebilirsin.
    private static final int MAX_LINE_CHARS = 8192;
    private static final Instant MIN_TS = Instant.parse("2000-01-01T00:00:00Z");

    private static final DateTimeFormatter CLF =
            DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);

    private static final Pattern IPV4 = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|1?\\d?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1?\\d?\\d)$");

    // ip ident user [tarih] "istek" status bayt ["referer" "user-agent"]
    private static final Pattern LINE = Pattern.compile(
            "^(\\S+) \\S+ \\S+ \\[([^\\]]+)\\] \"([^\"]*)\" (\\d{3}) (-?\\d+|-)"
          + "(?: \"[^\"]*\" \"[^\"]*\")?\\s*$");

    @Override
    public void process(String item, Emitter out) throws StageException {
        if (item == null || item.trim().isEmpty()) {
            throw new StageException("Bos veya null kayit.");
        }
        String line = item.trim();

        if (line.startsWith("#")) {
            throw new StageException("Yorum satiri.");
        }
        if (line.length() > MAX_LINE_CHARS) {
            throw new StageException("Satir boyutu cok buyuk (limit: " + MAX_LINE_CHARS + ").");
        }
        if (line.indexOf('\uFFFD') >= 0) {
            throw new StageException("Gecersiz UTF-8 baytlari iceriyor.");
        }

        Matcher m = LINE.matcher(line);
        if (!m.matches()) {
            throw new StageException("Kesilmis veya bicimi bozuk log satiri.");
        }

        if (!IPV4.matcher(m.group(1)).matches()) {
            throw new StageException("Gecersiz IP bicimi: " + m.group(1));
        }

        Instant ts;
        try {
            ts = OffsetDateTime.parse(m.group(2), CLF).toInstant();
        } catch (Exception e) {
            throw new StageException("Zaman damgasi cozumlenemedi: " + m.group(2));
        }
        if (ts.isBefore(MIN_TS) || ts.isAfter(Instant.now().plus(1, ChronoUnit.DAYS))) {
            throw new StageException("Zaman damgasi makul aralik disinda: " + ts);
        }

        int status = Integer.parseInt(m.group(4));
        if (status < 100 || status > 599) {
            throw new StageException("Gecersiz HTTP status: " + status);
        }

        String bytes = m.group(5);
        if (!bytes.equals("-") && Long.parseLong(bytes) < 0) {
            throw new StageException("Negatif bayt miktari: " + bytes);
        }

        out.emit(item);
    }
}