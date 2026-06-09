package edu.sjsu.vmact.model;

import java.util.List;
import java.util.UUID;

public class Cluster {
    private final String id;
    private final String label;
    private final List<Artifact> artifacts;
    private final double confidence;
    private final String explanation;

    public Cluster(
        String label, 
        List<Artifact> artifacts, 
        double confidence, 
        String explanation
    ) {
        this.id = UUID.randomUUID().toString();
        this.label = label;
        this.artifacts = artifacts;
        this.confidence = confidence;
        this.explanation = explanation;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getExplanation() {
        return explanation;
    }

    @Override
    public String toString() {
        return "Cluster{" +
                "id='" + id + '\'' +
                "label='" + label + '\'' +
                ", artifactCounter=" + artifacts.size() +
                ", confidence=" + confidence +
                ", explanation='" + explanation + '\'' +
                '}';
    }
}
