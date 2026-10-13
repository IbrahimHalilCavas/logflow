package logflow;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileSink implements Sink {
    private final String outputPath;
    private BufferedWriter writer;

    public FileSink(String outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public void open() {
        try {
            writer = new BufferedWriter(new FileWriter(outputPath));
        } catch (IOException e) {
            throw new RuntimeException("Dosya acilamadi: " + outputPath, e);
        }
    }

    @Override
    public void consume(String item) {
        try {
            if (writer != null) {
                writer.write(item);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Yazma hatasi: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("Kapatma hatasi: " + e.getMessage());
        }
    }
}