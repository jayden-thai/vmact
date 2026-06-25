package edu.sjsu.vmact.extract;

import edu.sjsu.vmact.model.Artifact;

public interface ArtifactWriter extends AutoCloseable {
    void write(Artifact artifact) throws Exception;

    long getWrittenCount();

    @Override
    default void close() throws Exception {
        // Default no-op for writers that do not need cleanup.
    }
}