package edu.sjsu.vmact.pipeline;

public class ScanMetric {
    private final String stage;
    private final String component;
    private final long durationMillis;
    private final long artifactCount;
    private final String details;

    public ScanMetric(
            String stage,
            String component,
            long durationMillis,
            long artifactCount,
            String details
    ) {
        this.stage = stage;
        this.component = component;
        this.durationMillis = durationMillis;
        this.artifactCount = artifactCount;
        this.details = details;
    }

    public String getStage() {
        return stage;
    }

    public String getComponent() {
        return component;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public long getArtifactCount() {
        return artifactCount;
    }

    public String getDetails() {
        return details;
    }
}