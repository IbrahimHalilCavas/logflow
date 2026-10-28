package logflow;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Increment5Test {

    public static void main(String[] args) {
        System.out.println("=== INCREMENT 5 TESTLERI BASLATILIYOR ===");
        
        try {
            testValidationStageValidRecord();
            System.out.println("[PASS] Valid Record Testi Başarılı");

            testValidationStageNegativeBytes();
            System.out.println("[PASS] Negative Bytes Testi Başarılı");

            testValidationStageInvalidYear();
            System.out.println("[PASS] Invalid Year (1970) Testi Başarılı");

            testValidationStageTruncatedRecord();
            System.out.println("[PASS] Truncated Record Testi Başarılı");

            testChaosTestPipelineExecution();
            System.out.println("[PASS] Chaos Test Pipeline Execution Başarılı");

            testEmptyFileHandling();
            System.out.println("[PASS] Empty File Handling Başarılı");

            System.out.println("\n=== TUM TESTLER BASARIYLA GECTI ===");
        } catch (Exception e) {
            System.err.println("\n[FAIL] Test Hata Aldı: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testValidationStageValidRecord() throws Exception {
        ValidationStage stage = new ValidationStage();
        CollectingEmitter emitter = new CollectingEmitter();

        String validLine = "192.0.2.10 - - [15/Oct/2026:10:15:32 +0000] \"GET /index.html HTTP/1.1\" 200 1234 \"-\" \"Mozilla/5.0\"";
        stage.process(validLine, emitter);

        if (emitter.getCollected().size() != 1) {
            throw new RuntimeException("Gecerli kayit yayinlanmadi!");
        }
    }

    private static void testValidationStageNegativeBytes() {
        ValidationStage stage = new ValidationStage();
        CollectingEmitter emitter = new CollectingEmitter();

        String invalidLine = "192.0.2.55 - - [15/Oct/2026:10:20:10 +0000] \"GET /download HTTP/1.1\" 200 -45 \"-\" \"test-agent\"";
        try {
            stage.process(invalidLine, emitter);
            throw new RuntimeException("Negatif bayt hatasi firlatilmadi!");
        } catch (StageException expected) {
            // Hata fırlatılması bekleniyordu, test başarılı
        }
    }

    private static void testValidationStageInvalidYear() {
        ValidationStage stage = new ValidationStage();
        CollectingEmitter emitter = new CollectingEmitter();

        String invalidLine = "192.0.2.70 - - [01/Jan/1970:00:00:00 +0000] \"GET /old HTTP/1.1\" 200 10 \"-\" \"test-agent\"";
        try {
            stage.process(invalidLine, emitter);
            throw new RuntimeException("Geçersiz yıl (1970) hatasi fırlatılmadı!");
        } catch (StageException expected) {
            // Hata fırlatılması bekleniyordu, test başarılı
        }
    }

    private static void testValidationStageTruncatedRecord() {
        ValidationStage stage = new ValidationStage();
        CollectingEmitter emitter = new CollectingEmitter();

        String truncatedLine = "198.51.100.9 - - [15/Oct/2026:10:19:10 +0000] \"GET /broken HTTP/1.1\" 200";
        try {
            stage.process(truncatedLine, emitter);
            throw new RuntimeException("Eksik satır hatası fırlatılmadı!");
        } catch (StageException expected) {
            // Hata fırlatılması bekleniyordu, test başarılı
        }
    }

    private static void testChaosTestPipelineExecution() throws Exception {
        PipelineLoader.runPipeline("configs/chaos-test.yaml");

        File deadLetterFile = new File("data/dead-letter.txt");
        if (!deadLetterFile.exists()) {
            throw new RuntimeException("Dead letter dosyasi bulunamadi!");
        }

        List<String> deadLetterLines = Files.readAllLines(Paths.get("data/dead-letter.txt"));
        if (deadLetterLines.size() != 7) {
            throw new RuntimeException("Dead letter dosyasinda 7 kayit bekleniyordu, bulunan: " + deadLetterLines.size());
        }

        File validOutputFile = new File("data/valid-output.txt");
        if (!validOutputFile.exists()) {
            throw new RuntimeException("Valid output dosyasi bulunamadi!");
        }

        List<String> validLines = Files.readAllLines(Paths.get("data/valid-output.txt"));
        if (validLines.size() != 3) {
            throw new RuntimeException("Valid output dosyasinda 3 kayit bekleniyordu, bulunan: " + validLines.size());
        }
    }

    private static void testEmptyFileHandling() throws Exception {
        PipelineLoader.runPipeline("configs/empty-test.yaml");
    }
}