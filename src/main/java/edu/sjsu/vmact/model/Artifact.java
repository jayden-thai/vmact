package edu.sjsu.vmact.model;

public class Artifact {
    private final ArtifactType type;
    private final String value;
    private final String sourceName;
    private final String encoding;
    private final String context;
    private final double confidence;
    private final long offset;

    public Artifact(ArtifactType type, String value, String sourceName, String encoding, long offset, String context, double confidence) {
        this.type = type;
        this.value = value;
        this.sourceName = sourceName;
        this.encoding = encoding;
        this.offset = offset;
        this.context = context;
        this.confidence = confidence;
    }

    public ArtifactType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getEncoding() {
        return encoding;
    }

    public long getOffset() {
        return offset;
    }

    public String getContext() {
        return context;
    }

    public double getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        return "Artifact{" +
                "type=" + type +
                ", value='" + value + '\'' +
                ", sourceName='" + sourceName + '\'' +
                ", encoding='" + encoding + '\'' +
                ", offset=" + offset +
                ", confidence=" + confidence +
                ", context='" + context + '\'' +
                '}';
    }
}
