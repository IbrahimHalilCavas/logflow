package logflow;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Increment4Test {
    public static void main(String[] args) throws IOException {
        int passed = 0;
        int failed = 0;

        // Test 1: Geçerli Yapılandırma
        try {
            Pipeline pipeline = PipelineLoader.loadFromConfig("configs/error-report.yaml");
            if (pipeline != null) passed++; else failed++;
        } catch (Exception e) { failed++; }

        // Test 2: Bilinmeyen Aşama Türü (Fail Fast)
        File invalidTypeFile = createTempConfig("""
            pipeline:
              source:
                params: { path: data/access-small.log }
              stages:
                - type: unknown_stage_type
                  params: {}
              sink:
                params: { path: data/out.txt }
            """);
        try {
            PipelineLoader.loadFromConfig(invalidTypeFile.getAbsolutePath());
            failed++;
        } catch (ConfigException e) {
            if (e.getMessage().contains("Bilinmeyen asama turu")) passed++; else failed++;
        } finally { invalidTypeFile.delete(); }

        // Test 3: Eksik Parametre (Fail Fast)
        File missingParamFile = createTempConfig("""
            pipeline:
              source:
                params: { path: data/access-small.log }
              stages:
                - type: filter
                  params: { field: status }
              sink:
                params: { path: data/out.txt }
            """);
        try {
            PipelineLoader.loadFromConfig(missingParamFile.getAbsolutePath());
            failed++;
        } catch (ConfigException e) {
            if (e.getMessage().contains("Eksik parametre")) passed++; else failed++;
        } finally { missingParamFile.delete(); }

        // Test 4: Boş İşlem Hattı (Fail Fast)
        File emptyPipelineFile = createTempConfig("""
            pipeline:
              source:
                params: { path: data/access-small.log }
              stages:
              sink:
                params: { path: data/out.txt }
            """);
        try {
            PipelineLoader.loadFromConfig(emptyPipelineFile.getAbsolutePath());
            failed++;
        } catch (ConfigException e) {
            if (e.getMessage().contains("Islem hatti (pipeline) bos olamaz")) passed++; else failed++;
        } finally { emptyPipelineFile.delete(); }

        System.out.println("Increment 4 Test Sonuclari: " + passed + " GECTI, " + failed + " KALDI.");
    }

    private static File createTempConfig(String content) throws IOException {
        File temp = File.createTempFile("test_config", ".yaml");
        try (FileWriter writer = new FileWriter(temp)) {
            writer.write(content);
        }
        return temp;
    }
}