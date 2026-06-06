package edu.sjsu.vmact.correlate;

import java.util.List;

import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.model.Cluster;
import edu.sjsu.vmact.pipeline.ScanConfig;

public interface Correlator {
    List<Cluster> correlate(List<Artifact> artifacts, ScanConfig config) throws Exception;
}
