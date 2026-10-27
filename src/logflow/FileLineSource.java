package logflow;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class FileLineSource implements Source {
    private final String filePath;

    public FileLineSource(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void produce(Emitter out) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                out.emit(line);
            }
        } catch (IOException e) {
            System.err.println("Dosya okuma hatasi: " + e.getMessage());
        }
    }
}