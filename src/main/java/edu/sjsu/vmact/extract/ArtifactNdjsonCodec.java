package edu.sjsu.vmact.extract;

import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.model.ArtifactType;
import edu.sjsu.vmact.model.SourceType;

public final class ArtifactNdjsonCodec {
    private ArtifactNdjsonCodec() {
    }

    public static String toJson(Artifact artifact) {
        StringBuilder builder = new StringBuilder();

        builder.append("{");
        appendStringField(builder, "id", artifact.getId());
        appendStringField(builder, "parentArtifactId", artifact.getParentArtifactId());
        appendStringField(builder, "type", artifact.getType().name());
        appendStringField(builder, "value", artifact.getValue());
        appendStringField(builder, "sourceId", artifact.getSourceId());
        appendStringField(builder, "sourceName", artifact.getSourceName());
        appendStringField(builder, "sourceType", artifact.getSourceType().name());
        appendStringField(builder, "producerName", artifact.getProducerName());
        appendStringField(builder, "encoding", artifact.getEncoding());
        appendLongField(builder, "offset", artifact.getOffset());
        appendStringField(builder, "context", artifact.getContext());
        appendDoubleField(builder, "confidence", artifact.getConfidence());
        removeTrailingComma(builder);
        builder.append("}");

        return builder.toString();
    }

    public static Artifact fromJson(String json) {
        return new Artifact(
                readStringField(json, "id"),
                readStringField(json, "parentArtifactId"),
                ArtifactType.valueOf(readStringField(json, "type")),
                readStringField(json, "value"),
                readStringField(json, "sourceId"),
                readStringField(json, "sourceName"),
                SourceType.valueOf(readStringField(json, "sourceType")),
                readStringField(json, "producerName"),
                readStringField(json, "encoding"),
                readLongField(json, "offset"),
                readStringField(json, "context"),
                readDoubleField(json, "confidence")
        );
    }

    private static void appendStringField(StringBuilder builder, String name, String value) {
        builder.append("\"")
                .append(escape(name))
                .append("\":\"")
                .append(escape(value))
                .append("\",");
    }

    private static void appendLongField(StringBuilder builder, String name, long value) {
        builder.append("\"")
                .append(escape(name))
                .append("\":")
                .append(value)
                .append(",");
    }

    private static void appendDoubleField(StringBuilder builder, String name, double value) {
        builder.append("\"")
                .append(escape(name))
                .append("\":")
                .append(value)
                .append(",");
    }

    private static void removeTrailingComma(StringBuilder builder) {
        if (builder.length() > 0 && builder.charAt(builder.length() - 1) == ',') {
            builder.deleteCharAt(builder.length() - 1);
        }
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);

            if (current == '\\') {
                builder.append("\\\\");
            } else if (current == '"') {
                builder.append("\\\"");
            } else if (current == '\n') {
                builder.append("\\n");
            } else if (current == '\r') {
                builder.append("\\r");
            } else if (current == '\t') {
                builder.append("\\t");
            } else if (current < 32) {
                builder.append(String.format("\\u%04x", (int) current));
            } else {
                builder.append(current);
            }
        }

        return builder.toString();
    }

    private static String readStringField(String json, String fieldName) {
        int valueStart = findValueStart(json, fieldName);

        if (valueStart < 0 || json.charAt(valueStart) != '"') {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        int index = valueStart + 1;
        boolean escaping = false;

        while (index < json.length()) {
            char current = json.charAt(index);

            if (escaping) {
                if (current == 'n') {
                    builder.append('\n');
                } else if (current == 'r') {
                    builder.append('\r');
                } else if (current == 't') {
                    builder.append('\t');
                } else if (current == 'u' && index + 4 < json.length()) {
                    String hex = json.substring(index + 1, index + 5);
                    builder.append((char) Integer.parseInt(hex, 16));
                    index += 4;
                } else {
                    builder.append(current);
                }

                escaping = false;
            } else if (current == '\\') {
                escaping = true;
            } else if (current == '"') {
                return builder.toString();
            } else {
                builder.append(current);
            }

            index++;
        }

        return builder.toString();
    }

    private static long readLongField(String json, String fieldName) {
        String rawValue = readRawField(json, fieldName);

        if (rawValue.isBlank()) {
            return -1;
        }

        return Long.parseLong(rawValue);
    }

    private static double readDoubleField(String json, String fieldName) {
        String rawValue = readRawField(json, fieldName);

        if (rawValue.isBlank()) {
            return 0.0;
        }

        return Double.parseDouble(rawValue);
    }

    private static String readRawField(String json, String fieldName) {
        int valueStart = findValueStart(json, fieldName);

        if (valueStart < 0) {
            return "";
        }

        int index = valueStart;

        while (index < json.length() && json.charAt(index) != ',' && json.charAt(index) != '}') {
            index++;
        }

        return json.substring(valueStart, index).trim();
    }

    private static int findValueStart(String json, String fieldName) {
        String fieldToken = "\"" + fieldName + "\":";
        int fieldIndex = json.indexOf(fieldToken);

        if (fieldIndex < 0) {
            return -1;
        }

        return fieldIndex + fieldToken.length();
    }
}