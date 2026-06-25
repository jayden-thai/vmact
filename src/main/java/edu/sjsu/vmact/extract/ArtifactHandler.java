package edu.sjsu.vmact.extract;

import edu.sjsu.vmact.model.Artifact;

@FunctionalInterface
public interface ArtifactHandler {
    void handle(Artifact artifact) throws Exception;
}