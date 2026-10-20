package logflow;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class StageRegistry {
    private static final Map<String, Function<Map<String, Object>, Stage>> registry = new HashMap<>();

    static {
        register("parse", params -> new ParserStage());
        
        register("filter", params -> {
            if (params == null) {
                throw new IllegalArgumentException("Eksik parametre! 'field', 'op' ve 'value' zorunludur.");
            }
            Object val = params.get("value");
            if (val == null) {
                throw new IllegalArgumentException("Eksik parametre! 'value' zorunludur.");
            }

            String valStr = val.toString().replaceAll("[^0-9]", "").trim();
            if (valStr.isEmpty()) {
                throw new IllegalArgumentException("Gecerli bir sayisal 'value' bulunamadi!");
            }

            int threshold = Integer.parseInt(valStr);
            return new PredicateFilterStage(r -> r.status() >= threshold);
        });

        register("project", params -> {
            if (params == null || !params.containsKey("fields")) {
                throw new IllegalArgumentException("Eksik parametre! 'fields' listesi zorunludur.");
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