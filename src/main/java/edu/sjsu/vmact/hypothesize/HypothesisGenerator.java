package edu.sjsu.vmact.hypothesize;

import java.util.List;

import edu.sjsu.vmact.model.Cluster;
import edu.sjsu.vmact.model.Hypothesis;
import edu.sjsu.vmact.pipeline.ScanConfig;

public interface HypothesisGenerator {
    List<Hypothesis> generate(
        List<Cluster> clusters,
        ScanConfig config
    ) throws Exception;
}
