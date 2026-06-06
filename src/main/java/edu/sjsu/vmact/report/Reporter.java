package edu.sjsu.vmact.report;

import java.util.List;

import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.model.Cluster;
import edu.sjsu.vmact.pipeline.ScanConfig;

public interface Reporter {
    void report(List<Artifact> artifacts, List<Cluster> clusters, ScanConfig config) throws Exception;
}
