package logflow;

import java.util.ArrayList;
import java.util.List;

public class Pipeline {
    private final Source source;
    private final Sink sink;
    private final List stages = new ArrayList();

    public Pipeline(Source source, Sink sink) {
        this.source = source;
        this.sink = sink;
    }

    public Pipeline addStage(Stage stage) {
        stages.add(stage);
        return this;
    }

    public void run() {
        for (Object s : stages) {
            ((Stage) s).open();
        }

        source.produce(item -> feed(item, 0));

        for (Object s : stages) {
            ((Stage) s).close();
        }
    }

    private void feed(String item, int stageIndex) {
        if (stageIndex >= stages.size()) {
            sink.consume(item);
            return;
        }

        Stage currentStage = (Stage) stages.get(stageIndex);
        try {
            currentStage.process(item, output -> feed(output, stageIndex + 1));
        } catch (StageException e) {
            System.err.println("Pipeline hatasi: " + e.getMessage());
        }
    }
}