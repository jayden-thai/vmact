package edu.sjsu.vmact.pipeline;

import java.nio.file.Path;

import edu.sjsu.vmact.model.IdGenerator;

public class ScanConfig {
    private final Path inputFile;
    private final Path keywordsFile;
    private final Path outputDir;

    private final IdGenerator idGenerator;

    private final String sourceId;
    private final String sourceName;
    private final String sourceType;

    public ScanConfig(Path inputFile, Path keywordsFile, Path outputDir) {
        this.inputFile = inputFile;
        this.keywordsFile = keywordsFile;
        this.outputDir = outputDir;

        this.idGenerator = new IdGenerator();

        this.sourceId = nextSourceId();
        this.sourceName = inputFile.getFileName().toString();
        this.sourceType = "RAW_MEMORY";
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

    public String getSourceId() {
        return sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String nextArtifactId() {
        return idGenerator.nextArtifactId();
    }

    public String nextClusterId() {
        return idGenerator.nextClusterId();
    }

    public String nextSourceId() {
        return idGenerator.nextSourceId();
    }
}
