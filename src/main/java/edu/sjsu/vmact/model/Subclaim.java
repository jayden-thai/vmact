package edu.sjsu.vmact.model;

import java.util.Collections;
import java.util.List;

public class Subclaim {
    private final String id;
    private final SubclaimType type;
    private final String text;
    private final double confidence;
    private final List<String> supportingClusterIds;
    private final List<String> supportingArtifactIds;
    private final List<String> sourceNames;
    private final List<SourceType> sourceTypes;
    private final List<String> producerNames;
    private final String caveat;

    public Subclaim(
        String id,
        SubclaimType type,
        String text,
        double confidence,
        List<String> supportingClusterIds,
        List<String> supportingArtifactIds,
        List<String> sourceNames,
        List<SourceType> sourceTypes,
        List<String> producerNames,
        String caveat
    ) {
        this.id = id;
        this.type = type;
        this.text = text;
        this.confidence = confidence;
        this.supportingClusterIds = List.copyOf(supportingClusterIds);
        this.supportingArtifactIds = List.copyOf(supportingArtifactIds);
        this.sourceNames = List.copyOf(sourceNames);
        this.sourceTypes = List.copyOf(sourceTypes);
        this.producerNames = List.copyOf(producerNames);
        this.caveat = caveat;
    }

    public String getId() {
        return id;
    }

    public SubclaimType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public double getConfidence() {
        return confidence;
    }

    public List<String> getSupportingClusterIds() {
        return Collections.unmodifiableList(supportingClusterIds);
    }

    public List<String> getSupportingArtifactIds() {
        return Collections.unmodifiableList(supportingArtifactIds);
    }

    public List<String> getSourceNames() {
        return Collections.unmodifiableList(sourceNames);
    }

    public List<SourceType> getSourceTypes() {
        return Collections.unmodifiableList(sourceTypes);
    }

    public List<String> getProducerNames() {
        return Collections.unmodifiableList(producerNames);
    }

    public String getCaveat() {
        return caveat;
    }

    @Override
    public String toString() {
        return "Subclaim{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", text='" + text + '\'' +
                ", confidence=" + confidence +
                ", supportingClusterIds=" + supportingClusterIds + 
                ", supportingArtifactIds=" + supportingArtifactIds +
                ", sourceNames=" + sourceNames +
                ", sourceTypes=" + sourceTypes +
                ", producerNames=" + producerNames +
                ", caveat='" + caveat + '\'' +
                '}';
    }
}
