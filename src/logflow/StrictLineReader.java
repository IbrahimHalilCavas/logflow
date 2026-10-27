package logflow;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Dosyayi satir satir, BAYT duzeyinde okur. Her satir ayri ayri UTF-8 olarak
 * cozulur; boylece tek bir bozuk satir tum dosyanin okunmasini engellemez.
 */
public class StrictLineReader {

    /** Bir satir: cozulebildiyse text dolu; bozuksa decodeError dolu, text ise okunabilir (lossy) kopya. */
    public record Line(String text, boolean malformed) {}

    public static List<Line> read(Path path) throws IOException {
        byte[] all = Files.readAllBytes(path);   // okunamayan dosya burada IOException -> PipelineException olur
        List<Line> lines = new ArrayList<>();
        int start = 0;
        for (int i = 0; i <= all.length; i++) {
            boolean end = (i == all.length);
            if (end || all[i] == '\n') {
                if (end && start == all.length) break;           // dosya sonu, bos kuyruk yok
                int stop = i;
                if (stop > start && all[stop - 1] == '\r') stop--; // CRLF
                lines.add(decode(all, start, stop - start));
                start = i + 1;
            }
        }
        return lines;
    }

    private static Line decode(byte[] buf, int off, int len) {
        try {
            String s = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(buf, off, len)).toString();
            return new Line(s, false);
        } catch (CharacterCodingException e) {
            return new Line(new String(buf, off, len, StandardCharsets.UTF_8), true); // bozuklar U+FFFD olur
        }
    }
}