package edu.sjsu.vmact.model;

public class IdGenerator {
    private int nextArtifactNumber = 1;
    private int nextClusterNumber = 1;

    public String nextArtifactId() {
        return String.format("A%06d", nextArtifactNumber++);
    }

    public String nextClusterId() {
        return String.format("C%06d", nextClusterNumber++);
    }
}
