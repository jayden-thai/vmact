package edu.sjsu.vmact.extract;

import edu.sjsu.vmact.model.ArtifactType;

import java.util.EnumMap;
import java.util.Map;

public interface ArtifactReader extends AutoCloseable {
    void forEach(ArtifactHandler handler) throws Exception;

    default long count() throws Exception {
        final long[] count = {0};

        forEach(artifact -> count[0]++);

        return count[0];
    }

    default Map<ArtifactType, Integer> countArtifactTypes() throws Exception {
        Map<ArtifactType, Integer> counts = new EnumMap<>(ArtifactType.class);

        forEach(artifact -> {
            ArtifactType type = artifact.getType();
            counts.put(type, counts.getOrDefault(type, 0) + 1);
        });

        return counts;
    }

    @Override
    default void close() throws Exception {
        // Default no-op for readers that open/close resources inside forEach.
    }
}