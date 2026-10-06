package logflow;

import java.util.ArrayList;
import java.util.List;

public class CollectingEmitter implements Emitter {
    private final List collected = new ArrayList<>();

    @Override
    public void emit(String item) {
        collected.add(item);
    }

    public List getCollected() {
        return collected;
    }

    public int size() {
        return collected.size();
    }
}