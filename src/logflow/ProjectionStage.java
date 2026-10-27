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
        // Seçilen alanlara göre veriyi süzüp biçimlendirir
        if (fields.isEmpty() || item == null) {
            out.emit(item);
            return;
        }

        StringBuilder sb = new StringBuilder();
        String lowerItem = item.toLowerCase();

        for (String field : fields) {
            String f = field.trim().toLowerCase();
            if (f.equals("timestamp") && item.contains("[")) {
                int start = item.indexOf("[");
                int end = item.indexOf("]");
                if (start != -1 && end != -1) sb.append(item, start, end + 1).append(" ");
            } else if (f.equals("clientip") || f.equals("ip")) {
                String[] parts = item.split(" ");
                for (String p : parts) {
                    if (p.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                        sb.append(p).append(" ");
                        break;
                    }
                }
            } else if (f.equals("path")) {
                String[] parts = item.split(" ");
                for (String p : parts) {
                    if (p.startsWith("/")) {
                        sb.append(p).append(" ");
                        break;
                    }
                }
            } else if (f.equals("status")) {
                String[] parts = item.split(" ");
                for (String p : parts) {
                    try {
                        int val = Integer.parseInt(p);
                        if (val >= 100 && val <= 599) {
                            sb.append("status=").append(val).append(" ");
                            break;
                        }
                    } catch (Exception ignored) {}
                }
            } else if (f.equals("bytes")) {
                if (item.contains("bytes")) {
                    int idx = item.indexOf("(");
                    int end = item.indexOf("bytes");
                    if (idx != -1 && end != -1) sb.append(item, idx + 1, end).append("bytes ");
                }
            }
        }

        String result = sb.toString().trim();
        out.emit(result.isEmpty() ? item : result);
    }

    public List<String> getFields() {
        return fields;
    }
}