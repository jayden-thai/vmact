package edu.sjsu.vmact.extract;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;

import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.model.ArtifactType;
import edu.sjsu.vmact.model.EvidenceSource;
import edu.sjsu.vmact.model.SourceType;
import edu.sjsu.vmact.pipeline.ScanConfig;

public class Utf16LeStringExtractor implements Extractor {
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

        scanWithAlignment(source, config, artifactWriter, relevanceFilter, 0);
        scanWithAlignment(source, config, artifactWriter, relevanceFilter, 1);
    }

    private void scanWithAlignment(
        EvidenceSource source,
        ScanConfig config,
        ArtifactWriter artifactWriter,
        StringRelevanceFilter relevanceFilter,
        int alignment
    ) throws Exception {
        long nextProgressOffset = PROGRESS_INTERVAL_BYTES;

        try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(source.getPath()))) {
            long currentOffset = 0;

            if (alignment == 1) {
                int skippedByte = inputStream.read();

                if (skippedByte == -1) {
                    return;
                }

                currentOffset = 1;
            } else if (alignment != 0) {
                throw new Exception("Valid alignment must be passed in as 1 or 0");
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

                currentOffset += 2;

                if (currentOffset >= nextProgressOffset) {
                    System.out.println("UTF-16LE alignment " + alignment + " scan processed " + formatBytes(currentOffset));
                    nextProgressOffset += PROGRESS_INTERVAL_BYTES;
                }

                lowByte = inputStream.read();
                highByte = inputStream.read();
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

    private boolean isPrintableUtf16LeAsciiPair(int lowByte, int highByte) {
        return highByte == 0
                && lowByte >= 32
                && lowByte <= 126;
    }

    private void flushRelevantStringIfLongEnough(
        EvidenceSource source,
        ScanConfig config,
        ArtifactWriter artifactWriter,
        StringRelevanceFilter relevanceFilter,
        StringBuilder currentString,
        long stringStartOffset
    ) throws Exception {
        if(currentString.length() >= MIN_STRING_LENGTH && relevanceFilter.isRelevant(currentString)) {
            artifactWriter.write(new Artifact(
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

    private String formatBytes(long bytes) {
        long megabytes = bytes / (1024L * 1024L);
        return megabytes + " MB";
    }

}
