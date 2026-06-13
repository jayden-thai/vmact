package edu.sjsu.vmact.model;

import java.util.List;
import java.util.UUID;

public class Cluster {
    private final String id;
    private final String label;
    private final String rootArtifactId;
    private final String rootValue;
    private final String clusterTypes;
    private final List<Artifact> artifacts;
    private final double confidence;
    private final String explanation;

    public Cluster(
        String id,
        String label, 
        String rootArtifactId,
        String rootValue,
        String clusterTypes,
        List<Artifact> artifacts, 
        double confidence, 
        String explanation
    ) {
        this.id = id;
        this.label = label;
        this.rootArtifactId = rootArtifactId;
        this.rootValue = rootValue;
        this.clusterTypes = clusterTypes;
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

    public String getRootArtifactId() {
        return rootArtifactId;
    }

    public String getRootValue() {
        return rootValue;
    }

    public String getClusterTypes() {
        return clusterTypes;
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
                ", label='" + label + '\'' +
                ", rootArtifactId='" + rootArtifactId + '\'' +
                ", rootValue='" + rootValue + '\'' +
                ", clusterTypes='" + clusterTypes + '\'' +
                ", artifactCount=" + artifacts.size() +
                ", confidence=" + confidence +
                ", explanation='" + explanation + '\'' +
                '}';
    }
}
