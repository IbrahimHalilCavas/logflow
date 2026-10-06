package logflow;

public class ParserStageTest {

    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;

        // Test 1: Gecerli satir
        ParserStage p1 = new ParserStage();
        CollectingEmitter e1 = new CollectingEmitter();
        p1.process("127.0.0.1 - - [29/Sep/2026:14:00:01 +0300] \"GET /index.html HTTP/1.1\" 200 1043", e1);
        if (e1.size() == 1 && p1.getSkippedCount() == 0) passed++; else failed++;

        // Test 2: Eksik alan
        ParserStage p2 = new ParserStage();
        CollectingEmitter e2 = new CollectingEmitter();
        p2.process("127.0.0.1 - - [29/Sep/2026:14:00:01 +0300] \"GET /index.html HTTP/1.1\"", e2);
        if (e2.size() == 0 && p2.getSkippedCount() == 1) passed++; else failed++;

        // Test 3: Hatali zaman damgasi
        ParserStage p3 = new ParserStage();
        CollectingEmitter e3 = new CollectingEmitter();
        p3.process("127.0.0.1 - - [GECERSIZ_TARIH] \"GET /index.html HTTP/1.1\" 200 1043", e3);
        if (e3.size() == 0 && p3.getSkippedCount() == 1) passed++; else failed++;

        // Test 4: Hatali durum kodu
        ParserStage p4 = new ParserStage();
        CollectingEmitter e4 = new CollectingEmitter();
        p4.process("127.0.0.1 - - [29/Sep/2026:14:00:01 +0300] \"GET /index.html HTTP/1.1\" ABC 1043", e4);
        if (e4.size() == 0 && p4.getSkippedCount() == 1) passed++; else failed++;

        // Test 5: Bos satir
        ParserStage p5 = new ParserStage();
        CollectingEmitter e5 = new CollectingEmitter();
        p5.process("   ", e5);
        if (e5.size() == 0 && p5.getSkippedCount() == 1) passed++; else failed++;

        // Test 6: Fazladan bas/son bosluk iceren satir
        ParserStage p6 = new ParserStage();
        CollectingEmitter e6 = new CollectingEmitter();
        p6.process("   127.0.0.1 - - [29/Sep/2026:14:00:01 +0300] \"GET /index.html HTTP/1.1\" 200 1043   ", e6);
        if (e6.size() == 1 && p6.getSkippedCount() == 0) passed++; else failed++;

        // Test 7: Bosluk iceren tirnakli user-agent
        ParserStage p7 = new ParserStage();
        CollectingEmitter e7 = new CollectingEmitter();
        p7.process("127.0.0.1 - - [29/Sep/2026:14:00:01 +0300] \"GET /index.html HTTP/1.1\" 200 1043 \"-\" \"Mozilla/5.0\"", e7);
        if (e7.size() == 1 && p7.getSkippedCount() == 0) passed++; else failed++;

        // Test 8: Sorgu dizesi iceren URL
        ParserStage p8 = new ParserStage();
        CollectingEmitter e8 = new CollectingEmitter();
        p8.process("127.0.0.1 - - [29/Sep/2026:14:00:01 +0300] \"GET /search?q=logflow HTTP/1.1\" 200 1043", e8);
        if (e8.size() == 1 && p8.getSkippedCount() == 0) passed++; else failed++;

        System.out.println("Test Sonuclari: " + passed + " GECTI, " + failed + " KALDI.");
    }
}