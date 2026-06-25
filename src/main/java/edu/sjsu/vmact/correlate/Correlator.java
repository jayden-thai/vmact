package edu.sjsu.vmact.correlate;

import java.util.List;

import edu.sjsu.vmact.extract.ArtifactReader;
import edu.sjsu.vmact.model.Cluster;
import edu.sjsu.vmact.pipeline.ScanConfig;

public interface Correlator {
    List<Cluster> correlate(ArtifactReader artifactReader, ScanConfig config) throws Exception;
}
