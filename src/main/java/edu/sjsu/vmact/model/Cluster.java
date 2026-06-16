package edu.sjsu.vmact.model;

import java.util.Collections;
import java.util.List;

public class Cluster {
    private final String id;
    private final String label;
    private final List<String> rootArtifactIds;
    private final String anchorArtifactId;
    private final String anchorValue;
    private final String clusterTypes;
    private final List<Artifact> artifacts;
    private final List<String> sourceNames;
    private final List<SourceType> sourceTypes;
    private final List<String> producerNames;
    private final List<RuleId> ruleIds;
    private final double confidence;
    private final String explanation;

    public Cluster(
        String id,
        String label, 
        List<String> rootArtifactIds,
        String anchorArtifactId,
        String anchorValue,
        String clusterTypes,
        List<String> sourceNames,
        List<SourceType> sourceTypes,
        List<String> producerNames,
        List<RuleId> ruleIds,
        List<Artifact> artifacts, 
        double confidence, 
        String explanation
    ) {
        this.id = id;
        this.label = label;
        this.rootArtifactIds = List.copyOf(rootArtifactIds);
        this.anchorArtifactId = anchorArtifactId;
        this.anchorValue = anchorValue;
        this.clusterTypes = clusterTypes;
        this.artifacts = List.copyOf(artifacts);
        this.sourceNames = List.copyOf(sourceNames);
        this.sourceTypes = List.copyOf(sourceTypes);
        this.producerNames = List.copyOf(producerNames);
        this.ruleIds = List.copyOf(ruleIds);
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
        return Collections.unmodifiableList(rootArtifactIds);
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
        return Collections.unmodifiableList(artifacts);
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

    public List<RuleId> getRuleIds() {
        return Collections.unmodifiableList(ruleIds);
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
                ", anchorArtifactId='" + anchorArtifactId + '\'' +
                ", anchorValue='" + anchorValue + '\'' +
                ", clusterTypes='" + clusterTypes + '\'' +
                ", sourceNames=" + sourceNames +
                ", sourceTypes=" + sourceTypes +
                ", producerNames=" + producerNames +
                ", ruleIds=" + ruleIds +
                ", artifactCount=" + artifacts.size() +
                ", confidence=" + confidence +
                ", explanation='" + explanation + '\'' +
                '}';
    }
}
