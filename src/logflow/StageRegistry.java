package logflow;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class StageRegistry {
    private static final Map<String, Function<Map<String, Object>, Stage>> registry = new HashMap<>();

    static {
        register("parse", params -> new ParserStage());
        register("validate", params -> new ValidationStage());
        
        register("filter", params -> {
            if (params == null || !params.containsKey("value")) {
                throw new IllegalArgumentException("Eksik parametre! 'value' zorunludur.");
            }
            String valStr = params.get("value").toString().replaceAll("[^0-9]", "").trim();
            int threshold = Integer.parseInt(valStr);
            return new PredicateFilterStage(r -> r.status() >= threshold);
        });

        register("project", params -> {
            if (params == null || !params.containsKey("fields")) {
                throw new IllegalArgumentException("Eksik parametre! 'fields' zorunludur.");
            }
            Object fieldsObj = params.get("fields");
            String[] fields = fieldsObj.toString().replace("[", "").replace("]", "").split(",");
            return new ProjectionStage(fields);
        });
    }

    public static void register(String type, Function<Map<String, Object>, Stage> factory) {
        registry.put(type.toLowerCase(), factory);
    }

    public static Stage createStage(String type, Map<String, Object> params) {
        if (!registry.containsKey(type.toLowerCase())) {
            throw new IllegalArgumentException("Bilinmeyen asama turu: '" + type + "'");
        }
        return registry.get(type.toLowerCase()).apply(params);
    }

    public static boolean hasStage(String type) {
        return registry.containsKey(type.toLowerCase());
    }
}