package edu.sjsu.vmact.model;

import java.util.UUID;

public class Artifact {
    private final String id;
    private final String parentArtifactId;
    private final ArtifactType type;
    private final String value;
    private final String sourceId;
    private final String sourceName;
    private final SourceType sourceType;
    private final String producerName;
    private final String encoding;
    private final String context;
    private final double confidence;
    private final long offset;


    public Artifact(
            String id,
            String parentArtifactId,
            ArtifactType type,
            String value,
            String sourceId,
            String sourceName,
            SourceType sourceType,
            String producerName,
            String encoding,
            long offset,
            String context,
            double confidence
    ) {
        this.id = id;
        this.parentArtifactId = parentArtifactId;
        this.type = type;
        this.value = value;
        this.sourceId = sourceId;
        this.sourceName = sourceName;
        this.sourceType = sourceType;
        this.producerName = producerName;
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

    public String getSourceId() {
        return sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public String getProducerName() {
        return producerName;
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

    public boolean isDerived() {
        return parentArtifactId != null && !parentArtifactId.isBlank();
    }

    @Override
    public String toString() {
        return "Artifact{" +
                "id='" + id + '\'' +
                ", parentArtifactId='" + parentArtifactId + '\'' +
                ", type=" + type +
                ", value='" + value + '\'' +
                ", sourceName='" + sourceName + '\'' +
                ", sourceType='" + sourceType + '\'' +
                ", producerName='" + producerName + '\'' +
                ", encoding='" + encoding + '\'' +
                ", offset=" + offset +
                ", confidence=" + confidence +
                ", context='" + context + '\'' +
                '}';
    }
}
