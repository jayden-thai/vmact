package edu.sjsu.vmact.extract;

import edu.sjsu.vmact.model.Artifact;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class NdjsonArtifactWriter implements ArtifactWriter {
    private final BufferedWriter writer;
    private long writtenCount;

    public NdjsonArtifactWriter(Path outputPath) throws IOException {
        Files.createDirectories(outputPath.getParent());
        this.writer = Files.newBufferedWriter(outputPath);
        this.writtenCount = 0;
    }

    @Override
    public void write(Artifact artifact) throws IOException {
        writer.write(ArtifactNdjsonCodec.toJson(artifact));
        writer.newLine();
        writtenCount++;
    }

    @Override
    public long getWrittenCount() {
        return writtenCount;
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}