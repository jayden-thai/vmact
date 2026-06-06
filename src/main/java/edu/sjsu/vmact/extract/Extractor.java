package edu.sjsu.vmact.extract;

import java.util.List;

import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.pipeline.ScanConfig;

public interface Extractor {
    List<Artifact> extract(ScanConfig config) throws Exception;
}
