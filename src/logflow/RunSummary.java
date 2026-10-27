package logflow;

public class RunSummary {
    public int totalRead = 0;
    public int emitted = 0;
    public int skipped = 0;
    public int deadLettered = 0;
    private long startTime;
    private long endTime;

    public void start() {
        this.startTime = System.currentTimeMillis();
    }

    public void stop() {
        this.endTime = System.currentTimeMillis();
    }

    public void printSummary() {
        long duration = endTime - startTime;
        System.out.println("\n=== RUN SUMMARY ===");
        System.out.println("Okunan Toplam Kayit   : " + totalRead);
        System.out.println("Ana Hedefe Yönlendirilen: " + emitted);
        System.out.println("Atlanan (Skipped)      : " + skipped);
        System.out.println("Hata Kanalina (DeadLetter): " + deadLettered);
        System.out.println("Gecen Süre            : " + duration + " ms");
        System.out.println("====================\n");
    }
}