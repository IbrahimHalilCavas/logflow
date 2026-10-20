package logflow;

public class Main {
    public static void main(String[] args) {
        String configPath = null;

        for (int i = 0; i < args.length; i++) {
            if ("--config".equals(args[i]) && i + 1 < args.length) {
                configPath = args[i + 1];
                break;
            }
        }

        if (configPath == null) {
            // Varsayılan olarak error-report yapılandırmasını çalıştırır
            configPath = "configs/error-report.yaml";
        }

        try {
            System.out.println("Yapilandirma yukleniyor: " + configPath);
            Pipeline pipeline = PipelineLoader.loadFromConfig(configPath);
            pipeline.run();
            System.out.println("Islem hatti basariyla calistirildi.");
        } catch (ConfigException e) {
            System.err.println("YAPILANDIRMA HATA RAPORU (Fail-Fast):");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}