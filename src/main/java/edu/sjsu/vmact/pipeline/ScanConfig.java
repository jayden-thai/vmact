package edu.sjsu.vmact.pipeline;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.sjsu.vmact.model.EvidenceSource;
import edu.sjsu.vmact.model.IdGenerator;
import edu.sjsu.vmact.model.SourceType;

public class ScanConfig {
    private final Path keywordsFile;
    private final Path outputDir;

    private final IdGenerator idGenerator;

    private final List<EvidenceSource> evidenceSources;

    public ScanConfig(Path inputFile, Path keywordsFile, Path outputDir) {
        this.keywordsFile = keywordsFile;
        this.outputDir = outputDir;

        this.idGenerator = new IdGenerator();
        
        evidenceSources = new ArrayList<>();
        addEvidenceSource(inputFile, SourceType.RAW_MEMORY);
    }

    public Path getKeywordsFile() {
        return keywordsFile;
    }

    public Path getOutputDir() {
        return outputDir;
    }

    public EvidenceSource addEvidenceSource(Path path, SourceType type) {
        return addEvidenceSource(
            path.getFileName().toString(), 
            path, 
            type
        );
    }

    public EvidenceSource addEvidenceSource(String name, Path path, SourceType type) {
        EvidenceSource source = new EvidenceSource(
            nextSourceId(), 
            name,
            path, 
            type
        );

        evidenceSources.add(source);
        return source;
    }

    public List<EvidenceSource> getEvidenceSources() {
        return Collections.unmodifiableList(evidenceSources);
    }

    public EvidenceSource getPrimaryEvidenceSource() {
        return evidenceSources.get(0);
    }

    public String getPrimarySourceId() {
        return getPrimaryEvidenceSource().getId();
    }

    public String getPrimarySourceName() {
        return getPrimaryEvidenceSource().getName();
    }

    public Path getPrimarySourcePath() {
        return getPrimaryEvidenceSource().getPath();
    }

    public SourceType getPrimarySourceType() {
        return getPrimaryEvidenceSource().getType();
    }

    public String nextArtifactId() {
        return idGenerator.nextArtifactId();
    }

    public String nextClusterId() {
        return idGenerator.nextClusterId();
    }

    public String nextHypothesisId() {
        return idGenerator.nextHypothesisId();
    }

    public String nextSubclaimId() {
        return idGenerator.nextSubclaimId();
    }

    private String nextSourceId() {
        return idGenerator.nextSourceId();
    }
}
