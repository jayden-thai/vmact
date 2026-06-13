package edu.sjsu.vmact.pipeline;

import java.nio.file.Path;

import edu.sjsu.vmact.model.IdGenerator;

public class ScanConfig {
    private final Path inputFile;
    private final Path keywordsFile;
    private final Path outputDir;
    private final IdGenerator idGenerator;

    public ScanConfig(Path inputFile, Path keywordsFile, Path outputDir) {
        this.inputFile = inputFile;
        this.keywordsFile = keywordsFile;
        this.outputDir = outputDir;
        this.idGenerator = new IdGenerator();
    }

    public Path getInputFile() {
        return inputFile;
    }

    public Path getKeywordsFile() {
        return keywordsFile;
    }

    public Path getOutputDir() {
        return outputDir;
    }

    public String nextArtifactId() {
        return idGenerator.nextArtifactId();
    }

    public String nextClusterId() {
        return idGenerator.nextClusterId();
    }
}
