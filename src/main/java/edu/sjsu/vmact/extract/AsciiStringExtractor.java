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

public class AsciiStringExtractor implements Extractor{
    private static final int MIN_STRING_LENGTH = 4;

    @Override
    public boolean supports(EvidenceSource source) {
        return source.getType() == SourceType.RAW_MEMORY 
                || source.getType() == SourceType.PROCESS_DUMP;
    }

    @Override
    public List<Artifact> extract(EvidenceSource source, ScanConfig config) throws IOException {
        List<Artifact> artifacts = new ArrayList<>();

        try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(source.getPath()))) {
            StringBuilder currentString = new StringBuilder();

            long currentOffset = 0;
            long stringStartOffset = -1;

            int nextByte;
            while ((nextByte = inputStream.read()) != -1) {
                byte b = (byte) nextByte;

                if (isPrintableAscii(b)) {
                    if (currentString.length() == 0) {
                        stringStartOffset = currentOffset;
                    }

                    currentString.append((char) b);
                } else {
                    flushStringIfLongEnough(source, config, artifacts, currentString, stringStartOffset);
                    currentString.setLength(0);
                    stringStartOffset = -1;
                }

                currentOffset++;
            }

            flushStringIfLongEnough(source, config, artifacts, currentString, stringStartOffset);
        }

        return artifacts;
    }

    private boolean isPrintableAscii(byte b) {
        int unsignedByte = b & 0xFF;

        return unsignedByte >= 32 && unsignedByte <= 126;
    }

    private  void flushStringIfLongEnough(EvidenceSource source, ScanConfig config, List<Artifact> artifacts, StringBuilder currentString, long stringStartOffset) {
        if (currentString.length() >= MIN_STRING_LENGTH) {
            artifacts.add(new Artifact(
                config.nextArtifactId(),
                "",
                ArtifactType.RAW_STRING, 
                currentString.toString(), 
                source.getId(),
                source.getName(),
                source.getType(),
                "ascii-extractor", 
                "ASCII", 
                stringStartOffset, 
                "", 
                1.0
            ));
        }
    }
}
