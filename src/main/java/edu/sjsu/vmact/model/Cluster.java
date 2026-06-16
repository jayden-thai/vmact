package edu.sjsu.vmact.model;

import java.util.List;

public class Cluster {
    private final String id;
    private final String label;
    private final List<String> rootArtifactIds;
    private final String anchorArtifactId;
    private final String anchorValue;
    private final String clusterTypes;
    private final List<Artifact> artifacts;
    private final double confidence;
    private final String explanation;

    public Cluster(
        String id,
        String label, 
        List<String> rootArtifactIds,
        String anchorArtifactId,
        String anchorValue,
        String clusterTypes,
        List<Artifact> artifacts, 
        double confidence, 
        String explanation
    ) {
        this.id = id;
        this.label = label;
        this.rootArtifactIds = rootArtifactIds;
        this.anchorArtifactId = anchorArtifactId;
        this.anchorValue = anchorValue;
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

    public List<String> getRootArtifactIds() {
        return rootArtifactIds;
    }

    public String getAnchorArtifactId() {
        return anchorArtifactId;
    }

    public String getAnchorValue() {
        return anchorValue;
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
                ", rootArtifactIds=" + rootArtifactIds +
                ", anchorArtifactId'=" + anchorArtifactId + '\'' +
                ", anchorValue='" + anchorValue + '\'' +
                ", clusterTypes='" + clusterTypes + '\'' +
                ", artifactCount=" + artifacts.size() +
                ", confidence=" + confidence +
                ", explanation='" + explanation + '\'' +
                '}';
    }
}
