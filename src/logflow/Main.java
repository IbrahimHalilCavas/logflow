package logflow;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Kullanim: java -cp bin logflow.Main ");
            System.exit(1);
        }

        String filePath = args[0];

        Source source = new FileLineSource(filePath);
        Sink sink = new ConsoleSink();
        ParserStage parser = new ParserStage();

        Pipeline pipeline = new Pipeline(source, sink);
        pipeline.addStage(parser);
        pipeline.run();

        System.out.println("Toplam hatali/atlanan satir sayisi: " + parser.getSkippedCount());
    }
}