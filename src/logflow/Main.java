package logflow;

public class Main {
    public static void main(String[] args) {
        String configPath = "configs/chaos-test.yaml";

        for (int i = 0; i < args.length; i++) {
            if ("--config".equals(args[i]) && i + 1 < args.length) {
                configPath = args[i + 1];
                break;
            }
        }

        try {
            System.out.println("Pipeline Baslatiliyor: " + configPath);
            PipelineLoader.runPipeline(configPath);
        } catch (Exception e) {
            System.err.println("Kritik Hata: " + e.getMessage());
            System.exit(1);
        }
    }
}