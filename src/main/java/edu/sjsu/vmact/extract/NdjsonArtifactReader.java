package edu.sjsu.vmact.extract;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class NdjsonArtifactReader implements ArtifactReader {
    private final Path inputPath;

    public NdjsonArtifactReader(Path inputPath) {
        this.inputPath = inputPath;
    }

    @Override
    public void forEach(ArtifactHandler handler) throws Exception {
        try (BufferedReader reader = Files.newBufferedReader(inputPath)) {
            String line = reader.readLine();

            while (line != null) {
                if (!line.isBlank()) {
                    handler.handle(ArtifactNdjsonCodec.fromJson(line));
                }

                line = reader.readLine();
            }
        }
    }
}