package edu.sjsu.vmact.detect;

import java.util.List;

import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.pipeline.ScanConfig;

public class NoOpDetector implements Detector{
    @Override
    public List<Artifact> detect(List<Artifact> inputArtifacts, ScanConfig config) {
        return inputArtifacts;
    }
}
