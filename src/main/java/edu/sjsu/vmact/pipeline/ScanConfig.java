package edu.sjsu.vmact.pipeline;

import java.nio.file.Path;

public class ScanConfig {
    private final Path inputFile;
    private final Path keywordsFile;
    private final Path outputDir;

    public ScanConfig(Path inputFile, Path keywordsFile, Path outputDir) {
        this.inputFile = inputFile;
        this.keywordsFile = keywordsFile;
        this.outputDir = outputDir;
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
}
