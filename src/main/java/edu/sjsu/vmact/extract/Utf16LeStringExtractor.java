package edu.sjsu.vmact.extract;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.model.ArtifactType;
import edu.sjsu.vmact.model.EvidenceSource;
import edu.sjsu.vmact.model.SourceType;
import edu.sjsu.vmact.pipeline.ScanConfig;

public class Utf16LeStringExtractor implements Extractor {
    private static final int MIN_STRING_LENGTH = 4;

    @Override
    public boolean supports(EvidenceSource source) {
        return source.getType() == SourceType.RAW_MEMORY
                || source.getType() == SourceType.PROCESS_DUMP;
    }

    @Override
    public List<Artifact> extract(EvidenceSource source, ScanConfig config) throws IOException {
        List<Artifact> artifacts = new ArrayList<>();

        scanWithAlignment(source, config, artifacts, 0);
        scanWithAlignment(source, config, artifacts, 1);

        return artifacts;
    }

    private void scanWithAlignment(
        EvidenceSource source,
        ScanConfig config,
        List<Artifact> artifacts,
        int alignment
    ) throws IOException {
        try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(source.getPath()))) {
            long currentOffset = 0;

            if (alignment == 1) {
                int skippedByte = inputStream.read();

                if (skippedByte == -1) {
                    return;
                }

                currentOffset = 1;
            }

            StringBuilder currentString = new StringBuilder();
            long stringStartOffset = -1;

            int lowByte = inputStream.read();
            int highByte = inputStream.read();

            while (lowByte != -1 && highByte != -1) {
                int unsignedLowByte = lowByte & 0xFF;
                int unsignedHighByte = highByte & 0xFF;

                if(isPrintableUtf16LeAsciiPair(unsignedLowByte, unsignedHighByte)) {
                    if (currentString.length() == 0) {
                        stringStartOffset = currentOffset;
                    }

                    currentString.append((char) unsignedLowByte);
                } else {
                    flushStringIfLongEnough(source, config, artifacts, currentString, stringStartOffset);
                    currentString.setLength(0);
                    stringStartOffset = -1;
                }

                currentOffset += 2;

                lowByte = inputStream.read();
                highByte = inputStream.read();
            }

            flushStringIfLongEnough(source, config, artifacts, currentString, stringStartOffset);
        }
    }

    private boolean isPrintableUtf16LeAsciiPair(int lowByte, int highByte) {
        return highByte == 0
                && lowByte >= 32
                && lowByte <= 126;
    }

    private void flushStringIfLongEnough(
        EvidenceSource source,
        ScanConfig config,
        List<Artifact> artifacts,
        StringBuilder currentString,
        long stringStartOffset
    ) {
        if(currentString.length() >= MIN_STRING_LENGTH) {
            artifacts.add(new Artifact(
                config.nextArtifactId(),
                "",
                ArtifactType.RAW_STRING,
                currentString.toString(),
                source.getId(),
                source.getName(),
                source.getType(),
                "utf16le-extractor",
                "UTF-16LE",
                stringStartOffset,
                "",
                1.0
            ));
        } 
    }

}
