package logflow;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class Increment3Test {
    public static void main(String[] args) throws IOException {
        int passed = 0;
        int failed = 0;

        // Test 1: PredicateFilterStage
        try {
            LogRecord record = new LogRecord(null, "127.0.0.1", "GET", "/api/data", 404, 100, "-", null, "");
            PredicateFilterStage stage = new PredicateFilterStage(r -> r.status() >= 400);
            if (stage.testRecord(record)) passed++; else failed++;
        } catch (Exception e) { failed++; }

        // Test 2: ProjectionStage
        try {
            ProjectionStage stage = new ProjectionStage("timestamp", "clientIp", "status");
            if (stage.getFields().size() == 3) passed++; else failed++;
        } catch (Exception e) { failed++; }

        // Test 3: FileSink Open & Close Lifecycle
        File tempFile = File.createTempFile("logflow_test", ".txt");
        try {
            FileSink sink = new FileSink(tempFile.getAbsolutePath());
            sink.open();
            sink.consume("Test Log Line");
            sink.close();

            List<String> lines = Files.readAllLines(tempFile.toPath());
            if (lines.size() == 1 && lines.get(0).equals("Test Log Line")) passed++; else failed++;
        } catch (Exception e) { failed++; } finally {
            tempFile.delete();
        }

        // Test 4: Integration Test (PipelineBuilder + FileSink)
        File intTestFile = File.createTempFile("logflow_int_test", ".txt");
        try {
            Pipeline pipeline = PipelineBuilder.from(new FileLineSource("data/access-small.log"))
                    .via(new ParserStage())
                    .to(new FileSink(intTestFile.getAbsolutePath()));
            pipeline.run();

            List<String> lines = Files.readAllLines(intTestFile.toPath());
            if (lines.size() == 4) passed++; else failed++;
        } catch (Exception e) { failed++; } finally {
            intTestFile.delete();
        }

        System.out.println("Increment 3 Test Sonuclari: " + passed + " GECTI, " + failed + " KALDI.");
    }
}