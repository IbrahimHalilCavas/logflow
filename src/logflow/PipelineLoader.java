package logflow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PipelineLoader {

    public static Pipeline loadFromConfig(String configPath) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(configPath));
            
            String sourcePath = null;
            String sinkPath = null;
            boolean inSinkBlock = false;
            
            List<Map<String, Object>> stageConfigs = new ArrayList<>();
            Map<String, Object> currentStageParams = null;
            String currentStageType = null;

            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("sink:")) {
                    inSinkBlock = true;
                    if (currentStageType != null) {
                        Map<String, Object> cfg = new HashMap<>();
                        cfg.put("type", currentStageType);
                        cfg.put("params", currentStageParams);
                        stageConfigs.add(cfg);
                        currentStageType = null;
                    }
                    continue;
                }

                if (!inSinkBlock) {
                    if (line.contains("path:") && sourcePath == null) {
                        sourcePath = cleanValue(line);
                    } else if (line.startsWith("- type:")) {
                        if (currentStageType != null) {
                            Map<String, Object> cfg = new HashMap<>();
                            cfg.put("type", currentStageType);
                            cfg.put("params", currentStageParams);
                            stageConfigs.add(cfg);
                        }
                        currentStageType = cleanValue(line);
                        currentStageParams = new HashMap<>();
                    } else if (currentStageParams != null) {
                        if (line.startsWith("params:")) {
                            String inline = line.substring("params:".length()).trim();
                            if (inline.startsWith("{") && inline.endsWith("}")) {
                                parseInlineParams(inline, currentStageParams);
                            }
                        }
                        if (line.contains("field:")) currentStageParams.put("field", cleanKeyValue(line, "field"));
                        if (line.contains("op:")) currentStageParams.put("op", cleanKeyValue(line, "op"));
                        if (line.contains("value:")) currentStageParams.put("value", cleanKeyValue(line, "value"));
                        if (line.contains("fields:")) currentStageParams.put("fields", cleanKeyValue(line, "fields"));
                    }
                } else {
                    if (line.contains("path:")) {
                        sinkPath = cleanValue(line);
                    }
                }
            }

            if (currentStageType != null) {
                Map<String, Object> cfg = new HashMap<>();
                cfg.put("type", currentStageType);
                cfg.put("params", currentStageParams);
                stageConfigs.add(cfg);
            }

            if (sourcePath == null || sourcePath.isEmpty()) {
                throw new ConfigException("HATA: Zorunlu parametre 'source.path' eksik!");
            }
            if (stageConfigs.isEmpty()) {
                throw new ConfigException("HATA: Islem hatti (pipeline) bos olamaz!");
            }

            Source source = new FileLineSource(sourcePath);
            Sink sink = (sinkPath != null && !sinkPath.isEmpty()) ? new FileSink(sinkPath) : new ConsoleSink();

            PipelineBuilder builder = PipelineBuilder.from(source);

            for (Map<String, Object> stageCfg : stageConfigs) {
                String type = (String) stageCfg.get("type");
                @SuppressWarnings("unchecked")
                Map<String, Object> params = (Map<String, Object>) stageCfg.get("params");

                if (!StageRegistry.hasStage(type)) {
                    throw new ConfigException("HATA: Bilinmeyen asama turu '" + type + "' kullanildi!");
                }

                Stage stage = StageRegistry.createStage(type, params);
                builder.via(stage);
            }

            return builder.to(sink);

        } catch (IOException e) {
            throw new ConfigException("HATA: Yapilandirma dosyasi okunamadi: " + configPath);
        }
    }

    private static String cleanValue(String line) {
        int colonIdx = line.indexOf(":");
        if (colonIdx != -1) {
            String val = line.substring(colonIdx + 1).trim();
            val = val.replace("{", "").replace("}", "").replace("[", "").replace("]", "").trim();
            if (val.startsWith("path:")) {
                val = val.substring(5).trim();
            }
            return val;
        }
        return "";
    }

    private static String cleanKeyValue(String line, String key) {
        int idx = line.indexOf(key + ":");
        if (idx != -1) {
            String val = line.substring(idx + key.length() + 1).trim();
            if (val.contains(",")) val = val.substring(0, val.indexOf(","));
            return val.replace("}", "").replace("]", "").trim();
        }
        return null;
    }

    private static void parseInlineParams(String inline, Map<String, Object> target) {
        String clean = inline.substring(1, inline.length() - 1).trim();
        String[] pairs = clean.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                target.put(kv[0].trim(), kv[1].trim());
            }
        }
    }
}