package logflow;

public class Main {
    public static void main(String[] args) {
        String inputPath = "data/access-small.log";
        String outputPath = null;
        int minStatus = -1;
        String pathPrefix = null;
        String[] fields = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--") && i < args.length - 1) {
                switch (args[i]) {
                    case "--min-status" -> minStatus = Integer.parseInt(args[++i]);
                    case "--path-prefix" -> pathPrefix = args[++i];
                    case "--fields" -> fields = args[++i].split(",");
                    case "--out" -> outputPath = args[++i];
                }
            } else if (!args[i].startsWith("--") && i == args.length - 1) {
                inputPath = args[i];
            }
        }

        Source source = new FileLineSource(inputPath);
        Sink sink = (outputPath != null) ? new FileSink(outputPath) : new ConsoleSink();
        ParserStage parser = new ParserStage();

        PipelineBuilder builder = PipelineBuilder.from(source).via(parser);

        if (minStatus != -1) {
            int finalMinStatus = minStatus;
            builder.via(new PredicateFilterStage(r -> r.status() >= finalMinStatus));
        }

        if (pathPrefix != null) {
            String finalPathPrefix = pathPrefix;
            builder.via(new PredicateFilterStage(r -> r.path().startsWith(finalPathPrefix)));
        }

        if (fields != null) {
            builder.via(new ProjectionStage(fields));
        }

        Pipeline pipeline = builder.to(sink);
        pipeline.run();

        System.out.println("Toplam hatali/atlanan satir sayisi: " + parser.getSkippedCount());
    }
}