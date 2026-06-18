package edu.sjsu.vmact.model;

public class IdGenerator {
    private int nextArtifactNumber = 1;
    private int nextClusterNumber = 1;
    private int nextSourceNumber = 1;
    private int nextHypothesisNumber = 1;
    private int nextSubclaimNumber = 1;

    public String nextArtifactId() {
        return String.format("A%06d", nextArtifactNumber++);
    }

    public String nextClusterId() {
        return String.format("C%06d", nextClusterNumber++);
    }

    public String nextSourceId() {
        return String.format("S%06d", nextSourceNumber++);
    }

    public String nextHypothesisId() {
        return String.format("H%06d", nextHypothesisNumber++);
    }

    public String nextSubclaimId() {
        return String.format("SC%06d", nextSubclaimNumber++);
    }
}
