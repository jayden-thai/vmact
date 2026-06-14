package edu.sjsu.vmact.model;

import java.nio.file.Path;

public class EvidenceSource {
    private final String id;
    private final String name;
    private final SourceType type;
    private final Path path;

    public EvidenceSource(String id, Path path, SourceType type) {
        this(
            id,
            path.getFileName().toString(),
            path,
            type
        );
    }

    public EvidenceSource(String id, String name, Path path, SourceType type) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.type = type;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public Path getPath() {
        return path;
    }
    public SourceType getType() {
        return type;
    }
}
