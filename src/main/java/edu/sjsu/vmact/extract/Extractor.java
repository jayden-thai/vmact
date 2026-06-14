package edu.sjsu.vmact.extract;

import java.util.List;

import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.model.EvidenceSource;
import edu.sjsu.vmact.pipeline.ScanConfig;

public interface Extractor {
    boolean supports(EvidenceSource source);

    List<Artifact> extract(EvidenceSource source, ScanConfig config) throws Exception;
}
