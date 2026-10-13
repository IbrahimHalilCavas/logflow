package logflow;

import java.util.Arrays;
import java.util.List;

public class ProjectionStage implements Stage {
    private final List<String> fields;

    public ProjectionStage(String... fields) {
        this.fields = Arrays.asList(fields);
    }

    @Override
    public void process(String item, Emitter out) throws StageException {
        out.emit(item);
    }

    public List<String> getFields() {
        return fields;
    }
}