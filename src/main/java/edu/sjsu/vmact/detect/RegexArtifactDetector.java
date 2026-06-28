package edu.sjsu.vmact.detect;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.sjsu.vmact.extract.ArtifactReader;
import edu.sjsu.vmact.extract.ArtifactWriter;
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

    private static final Pattern WINDOWS_FILE_PATH_PATTERN = Pattern.compile(
            "\\b[A-Za-z]:\\\\[^\\s\"'<>|]+"
    );

    private static final Pattern FILE_URI_PATTERN = Pattern.compile(
            "file:///[A-Za-z0-9._~:/%+\\-=]+"
    );

    private static final Pattern LINUX_FILE_PATH_PATTERN = Pattern.compile(
            "/(?:home|media|mnt)/[^\\\\s\\\"'<>]+"
    );

    private static final Pattern DEVICE_ID_PATTERN = Pattern.compile(
            "(?:USBSTOR[\\\\#][^\\s\"'<>]+|DISK&VEN_[^\\s\"'<>]+|idVendor=[0-9A-Fa-f]{4})",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public void detect(
        ArtifactReader inputArtifacts, 
        ArtifactWriter outputArtifacts, 
        ScanConfig config
    ) throws Exception {
        
        inputArtifacts.forEach(artifact -> {
            outputArtifacts.write(artifact);

            // only checks root artifacts to avoid needless artifact inflation
            if (artifact.getType() == ArtifactType.RAW_STRING) {
                Set<String> emittedForParent = new HashSet<>();

                detectPattern(config, outputArtifacts, artifact, EMAIL_PATTERN, ArtifactType.EMAIL, 0.85, emittedForParent);
                detectPattern(config, outputArtifacts, artifact, URL_PATTERN, ArtifactType.URL, 0.85, emittedForParent);
                detectPattern(config, outputArtifacts, artifact, WINDOWS_FILE_PATH_PATTERN, ArtifactType.WINDOWS_FILE_PATH, 0.80, emittedForParent);
                detectPattern(config, outputArtifacts, artifact, LINUX_FILE_PATH_PATTERN, ArtifactType.LINUX_FILE_PATH, 0.80, emittedForParent);
                detectPattern(config, outputArtifacts, artifact, FILE_URI_PATTERN, ArtifactType.FILE_URI, 0.80, emittedForParent);
                detectPattern(config, outputArtifacts, artifact, DEVICE_ID_PATTERN, ArtifactType.DEVICE_ID, 0.90, emittedForParent);
            }
        });

        System.out.println("    RegexArtifactDetector wrote artifacts: " + outputArtifacts.getWrittenCount());
        
    }

    private void detectPattern(
            ScanConfig config,
            ArtifactWriter outputArtifacts,
            Artifact parentArtifact,
            Pattern pattern,
            ArtifactType artifactType,
            double confidence,
            Set<String> emittedForParent
    ) throws Exception {
        Matcher matcher = pattern.matcher(parentArtifact.getValue());

        while (matcher.find()) {
            String matchedValue = matcher.group();
            String dedupeKey = artifactType.name() + "\u0000" + matchedValue;

            if (!emittedForParent.add(dedupeKey)) {
                outputArtifacts.write(new Artifact(
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
                    contextWindow(parentArtifact.getValue(), matcher.start(), matcher.end(), 200), 
                    confidence
                ));
            }
        }
    }

    private String contextWindow(String value, int start, int end, int radius) {
        int contextStart = Math.max(0, start - radius);
        int contextEnd = Math.min(value.length(), end + radius);

        return value.substring(contextStart, contextEnd);
    }

    private long calculateDerivedOffset(Artifact parentArtifact, int matchStartIndex) {
        if (parentArtifact.getOffset() < 0) {
            return -1;
        }

        return parentArtifact.getOffset() + matchStartIndex;
    }
}
