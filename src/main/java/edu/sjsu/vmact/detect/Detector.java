package edu.sjsu.vmact.detect;

import edu.sjsu.vmact.extract.ArtifactReader;
import edu.sjsu.vmact.extract.ArtifactWriter;
import edu.sjsu.vmact.pipeline.ScanConfig;

public interface Detector {
    void detect(
        ArtifactReader artifactReader, 
        ArtifactWriter outputArtifacts, 
        ScanConfig config) 
        throws Exception;
}
