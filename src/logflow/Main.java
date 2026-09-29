package logflow;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Kullanim: java logflow.Main ");
            System.exit(1);
        }

        String filePath = args[0];

        Source source = new FileLineSource(filePath);
        Sink sink = new ConsoleSink();

        Pipeline pipeline = new Pipeline(source, sink);
        pipeline.run();
    }
}