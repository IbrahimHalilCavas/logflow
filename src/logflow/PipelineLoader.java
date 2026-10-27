package logflow;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PipelineLoader {

    public static Pipeline loadFromConfig(String configPath) {
        return null;
    }

    public static void runPipeline(String configPath) {
        RunSummary summary = new RunSummary();
        summary.start();

        try {
            List<String> lines = Files.readAllLines(Paths.get(configPath));

            String sourcePath = null;
            String sinkPath = null;
            String deadLetterPath = "data/dead-letter.txt";
            String errorPolicy = "DEAD_LETTER";

            List<String> stageTypes = new ArrayList<>();
            boolean inStagesBlock = false;

            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("error_policy:")) {
                    errorPolicy = parseSimpleValue(line);
                } else if (line.startsWith("dead_letter:")) {
                    deadLetterPath = parseSimpleValue(line);
                } else if (line.startsWith("source:")) {
                    inStagesBlock = false;
                } else if (line.startsWith("stages:")) {
                    inStagesBlock = true;
                } else if (line.startsWith("sink:")) {
                    inStagesBlock = false;
                } else if (inStagesBlock && line.startsWith("- type:")) {
                    stageTypes.add(parseSimpleValue(line));
                } else if (!inStagesBlock && line.contains("path:")) {
                    if (sourcePath == null) {
                        sourcePath = parsePathValue(line);
                    } else {
                        sinkPath = parsePathValue(line);
                    }
                }
            }

            if (sourcePath == null || sourcePath.isEmpty()) {
                throw new ConfigException("HATA: 'source.path' bulunamadi!");
            }

            // 1) Satirlari BAYT duzeyinde, tek tek ve katı UTF-8 ile oku
            List<StrictLineReader.Line> rawRecords = StrictLineReader.read(Paths.get(sourcePath));

            FileSink mainSink = (sinkPath != null && !sinkPath.isEmpty()) ? new FileSink(sinkPath) : null;
            if (mainSink != null) mainSink.open();

            FileWriter deadLetterWriter = new FileWriter(deadLetterPath, false);

            // 2) Asamalari her kayit icin degil, BIR KEZ olustur
            List<Stage> stages = new ArrayList<>();
            List<String> stageNames = new ArrayList<>();
            for (String type : stageTypes) {
                stages.add(StageRegistry.createStage(type, new HashMap<>()));
                stageNames.add(type);
            }

            for (StrictLineReader.Line rec : rawRecords) {
                String raw = rec.text();
                String trimmedRaw = raw.trim();
                if (trimmedRaw.isEmpty() || trimmedRaw.startsWith("#")) continue; // yorum/bos satir veri degil

                summary.totalRead++;

                // 3) Okuma (decode) hatasi: bozuk bayt -> hata politikasina gider
                if (rec.malformed()) {
                    handleFailure(errorPolicy, "okuyucu", "Gecersiz UTF-8 baytlari iceriyor.",
                            raw, summary, deadLetterWriter, mainSink);
                    continue;
                }

                boolean reachedSink = true;
                String currentData = raw;

                for (int i = 0; i < stages.size(); i++) {
                    CollectingEmitter emitter = new CollectingEmitter();
                    try {
                        stages.get(i).process(currentData, emitter);
                    } catch (StageException e) {
                        handleFailure(errorPolicy, stageNames.get(i), e.getMessage(),
                                raw, summary, deadLetterWriter, mainSink);
                        reachedSink = false;
                        break;
                    } catch (Exception e) {
                        handleFailure(errorPolicy, stageNames.get(i),
                                "Beklenmeyen hata: " + e, raw, summary, deadLetterWriter, mainSink);
                        reachedSink = false;
                        break;
                    }

                    // 4) Asama hicbir sey uretmediyse kayit bu asamada ELENMISTIR (filtre/atlama).
                    //    Eskiden bu durumda kayit yine de "emitted" sayiliyordu -> sayilar tutmuyordu.
                    if (emitter.getCollected().isEmpty()) {
                        summary.skipped++;
                        reachedSink = false;
                        break;
                    }
                    currentData = emitter.getCollected().get(0).toString();
                }

                if (reachedSink) {
                    summary.emitted++;
                    if (mainSink != null) mainSink.consume(currentData);
                }
            }

            deadLetterWriter.close();
            if (mainSink != null) mainSink.close();

        } catch (IOException e) {
            throw new PipelineException("Dosya okuma hatasi: " + e.getMessage(), e);
        }

        summary.stop();
        summary.printSummary();
    }

    /** Hata politikasini tek yerde uygular. FAIL_FAST ise PipelineException firlatir. */
    private static void handleFailure(String policy, String stageName, String reason, String raw,
                                      RunSummary summary, FileWriter deadLetter, FileSink mainSink)
            throws IOException {
        if ("FAIL_FAST".equalsIgnoreCase(policy)) {
            deadLetter.close();
            if (mainSink != null) mainSink.close();
            throw new PipelineException("FAIL_FAST [" + stageName + "]: " + reason);
        } else if ("SKIP".equalsIgnoreCase(policy)) {
            summary.skipped++;
        } else { // DEAD_LETTER
            summary.deadLettered++;
            deadLetter.write("[FAILED] Stage: " + stageName + " | Reason: " + reason + " | Raw: " + raw + "\n");
        }
    }

    private static String parseSimpleValue(String line) {
        int idx = line.indexOf(":");
        return idx != -1 ? line.substring(idx + 1).trim() : "";
    }

    private static String parsePathValue(String line) {
        int idx = line.indexOf("path:");
        if (idx != -1) {
            String val = line.substring(idx + 5).trim();
            return val.replace("{", "").replace("}", "").trim();
        }
        return "";
    }
}