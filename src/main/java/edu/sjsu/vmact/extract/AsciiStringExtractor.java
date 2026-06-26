package edu.sjsu.vmact.extract;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;

import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.model.ArtifactType;
import edu.sjsu.vmact.model.EvidenceSource;
import edu.sjsu.vmact.model.SourceType;
import edu.sjsu.vmact.pipeline.ScanConfig;

public class AsciiStringExtractor implements Extractor{
    private static final int MIN_STRING_LENGTH = 4;
    private static final int MAX_STRING_LENGTH = 8192;
    private static final long PROGRESS_INTERVAL_BYTES = 512L * 1024L * 1024L;

    @Override
    public boolean supports(EvidenceSource source) {
        return source.getType() == SourceType.RAW_MEMORY 
                || source.getType() == SourceType.PROCESS_DUMP;
    }

    @Override
    public void extract(
        EvidenceSource source, 
        ScanConfig config, 
        ArtifactWriter artifactWriter
    ) throws Exception {
        StringRelevanceFilter relevanceFilter = StringRelevanceFilter.fromConfig(config);
        long nextProgressOffset = PROGRESS_INTERVAL_BYTES;

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

                    if (currentString.length() >= MAX_STRING_LENGTH) {
                        flushRelevantStringIfLongEnough(
                            source, 
                            config, 
                            artifactWriter,
                            relevanceFilter, 
                            currentString, 
                            stringStartOffset
                        );
                        currentString.setLength(0);
                        stringStartOffset = -1;
                    }
                } else {
                    flushRelevantStringIfLongEnough(
                        source, 
                        config, 
                        artifactWriter,
                        relevanceFilter, 
                        currentString, 
                        stringStartOffset
                    );
                    currentString.setLength(0);
                    stringStartOffset = -1;
                }

                currentOffset++;
                if (currentOffset >= nextProgressOffset) {
                    System.out.println("ASCII scan processed " + formatBytes(currentOffset));
                    nextProgressOffset += PROGRESS_INTERVAL_BYTES;
                }
            }

            flushRelevantStringIfLongEnough(
                source, 
                config, 
                artifactWriter,
                relevanceFilter,
                currentString, 
                stringStartOffset
            );
        }
    }

    private boolean isPrintableAscii(byte b) {
        int unsignedByte = b & 0xFF;

        return unsignedByte >= 32 && unsignedByte <= 126;
    }

    private  void flushRelevantStringIfLongEnough(
        EvidenceSource source, 
        ScanConfig config, 
        ArtifactWriter artifactWriter, 
        StringRelevanceFilter relevanceFilter,
        StringBuilder currentString, 
        long stringStartOffset
    ) throws Exception {
        if (currentString.length() >= MIN_STRING_LENGTH && relevanceFilter.isRelevant(currentString)) {
            artifactWriter.write(new Artifact(
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

    private String formatBytes(long bytes) {
        long megabytes = bytes / (1024L * 1024L);
        return megabytes + " MB";
    }
}
