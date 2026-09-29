package logflow;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileLineSource implements Source {
    private final String filePath;

    public FileLineSource(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void produce(Emitter out) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                out.emit(line);
            }
        } catch (IOException e) {
            System.err.println("Dosya okuma hatasi: " + e.getMessage());
        }
    }
}