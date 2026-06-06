package edu.sjsu.vmact.model;

import java.util.List;

public class Cluster {
    private final String label;
    private final List<Artifact> artifacts;

    public Cluster(String label, List<Artifact> artifacts) {
        this.label = label;
        this.artifacts = artifacts;
    }

    public String getLabel() {
        return label;
    }

    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    @Override
    public String toString() {
        return "Cluster{" +
                "label='" + label + '\'' +
                ", artifactCounter=" + artifacts.size() +
                '}';
    }
}
