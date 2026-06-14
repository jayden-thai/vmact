package edu.sjsu.vmact.detect;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.model.ArtifactType;
import edu.sjsu.vmact.pipeline.ScanConfig;

public class RegexArtifactDetector implements Detector{
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b"    
    );

    private static final Pattern URL_PATTERN = Pattern.compile(
            "\\bhttps?://[^\\s\"'<>]+"
    );

    private static final Pattern FILE_PATH_PATTERN = Pattern.compile(
            "\\b[A-Za-z]:\\\\[^\\s\"'<>|]+"
    );

    private static final Pattern DEVICE_ID_PATTERN = Pattern.compile(
            "\\bUSBSTOR\\\\[^\\s\"'<>]+"
    );

    @Override
    public List<Artifact> detect(List<Artifact> inputArtifacts, ScanConfig config) {
        List<Artifact> outputArtifacts = new ArrayList<>(inputArtifacts);

        for (Artifact artifact : inputArtifacts) {
            // only checks root artifacts to avoid needless artifact inflation
            if (artifact.getType() == ArtifactType.RAW_STRING) {
                detectPattern(config, outputArtifacts, artifact, EMAIL_PATTERN, ArtifactType.EMAIL, 0.85);
                detectPattern(config, outputArtifacts, artifact, URL_PATTERN, ArtifactType.URL, 0.85);
                detectPattern(config, outputArtifacts, artifact, FILE_PATH_PATTERN, ArtifactType.FILE_PATH, 0.80);
                detectPattern(config, outputArtifacts, artifact, DEVICE_ID_PATTERN, ArtifactType.DEVICE_ID, 0.90);
            }
        }

        return outputArtifacts;
    }

    private void detectPattern(
            ScanConfig config,
            List<Artifact> outputArtifacts,
            Artifact parentArtifact,
            Pattern pattern,
            ArtifactType artifactType,
            double confidence
    ) {
        Matcher matcher = pattern.matcher(parentArtifact.getValue());

        while (matcher.find()) {
            String matchedValue = matcher.group();

            outputArtifacts.add(new Artifact(
                config.nextArtifactId(),
                parentArtifact.getId(), 
                artifactType, 
                matchedValue, 
                parentArtifact.getSourceId(),
                parentArtifact.getSourceName(),
                parentArtifact.getSourceType(),
                "regex-detector", 
                parentArtifact.getEncoding(),
                calculateDerivedOffset(parentArtifact, matcher.start()), 
                parentArtifact.getValue(), 
                confidence
            ));
        }
    }

    private long calculateDerivedOffset(Artifact parentArtifact, int matchStartIndex) {
        if (parentArtifact.getOffset() < 0) {
            return -1;
        }

        return parentArtifact.getOffset() + matchStartIndex;
    }
}
