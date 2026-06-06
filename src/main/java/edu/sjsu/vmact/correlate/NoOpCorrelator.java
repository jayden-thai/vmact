package edu.sjsu.vmact.correlate;

import java.util.ArrayList;
import java.util.List;

import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.model.Cluster;
import edu.sjsu.vmact.pipeline.ScanConfig;

public class NoOpCorrelator implements Correlator{
    @Override
    public List<Cluster> correlate(List<Artifact> artifacts, ScanConfig config) {
        List<Cluster> clusters = new ArrayList<>();

        clusters.add(new Cluster(
            "placeholder cluster", 
            artifacts
        ));

        return clusters;
    }
}
