package edu.sjsu.vmact.detect;

import java.util.List;

import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.pipeline.ScanConfig;

public interface Detector {
    List<Artifact> detect(List<Artifact> inputArtifacts, ScanConfig config) throws Exception;
}
