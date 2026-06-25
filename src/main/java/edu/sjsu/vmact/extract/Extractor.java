package edu.sjsu.vmact.extract;

import edu.sjsu.vmact.model.EvidenceSource;
import edu.sjsu.vmact.pipeline.ScanConfig;

public interface Extractor {
    boolean supports(EvidenceSource source);

    void extract(
        EvidenceSource source, 
        ScanConfig config, 
        ArtifactWriter artifactWriter
    ) throws Exception;
}
