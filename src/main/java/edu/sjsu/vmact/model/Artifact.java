package edu.sjsu.vmact.model;

import java.util.UUID;

public class Artifact {
    private final String id;
    private final String parentArtifactId;
    private final ArtifactType type;
    private final String value;
    private final String sourceName;
    private final String encoding;
    private final String context;
    private final double confidence;
    private final long offset;

    public Artifact(
            ArtifactType type,
            String value,
            String sourceName,
            String encoding,
            long offset,
            String context,
            double confidence
    ) {
        this(
                UUID.randomUUID().toString(),
                "",
                type,
                value,
                sourceName,
                encoding,
                offset,
                context,
                confidence
        );
    }
    public Artifact(
            String parentArtifactId,
            ArtifactType type,
            String value,
            String sourceName,
            String encoding,
            long offset,
            String context,
            double confidence
    ) {
        this(
                UUID.randomUUID().toString(),
                parentArtifactId,
                type,
                value,
                sourceName,
                encoding,
                offset,
                context,
                confidence
        );
    }

    public Artifact(
            String id,
            String parentArtifactId,
            ArtifactType type,
            String value,
            String sourceName,
            String encoding,
            long offset,
            String context,
            double confidence
    ) {
        this.id = id;
        this.parentArtifactId = parentArtifactId;
        this.type = type;
        this.value = value;
        this.sourceName = sourceName;
        this.encoding = encoding;
        this.offset = offset;
        this.context = context;
        this.confidence = confidence;
    }

    public String getId() {
        return id;
    }

    public String getParentArtifactId() {
        return parentArtifactId;
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
                "id='" + id + '\'' +
                ", parentArtifactId='" + parentArtifactId + '\'' +
                ", type=" + type +
                ", value='" + value + '\'' +
                ", sourceName='" + sourceName + '\'' +
                ", encoding='" + encoding + '\'' +
                ", offset=" + offset +
                ", confidence=" + confidence +
                ", context='" + context + '\'' +
                '}';
    }
}
