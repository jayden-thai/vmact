package edu.sjsu.vmact.report;

import java.util.List;

import edu.sjsu.vmact.extract.ArtifactReader;
import edu.sjsu.vmact.model.Cluster;
import edu.sjsu.vmact.model.Hypothesis;
import edu.sjsu.vmact.pipeline.ScanConfig;

public interface Reporter {
    void report(
        ArtifactReader artifactReader, 
        List<Cluster> clusters, 
        List<Hypothesis> hypotheses,
        ScanConfig config
    ) throws Exception;
}
