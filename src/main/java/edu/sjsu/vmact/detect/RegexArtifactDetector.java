package edu.sjsu.vmact.detect;

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
            "/(?:home|media|usr|etc|var|tmp|opt|mnt|run|dev)/[^\\s\"'<>]+"
    );

    private static final Pattern DEVICE_ID_PATTERN = Pattern.compile(
            "\\bUSBSTOR\\\\[^\\s\"'<>]+"
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
                detectPattern(config, outputArtifacts, artifact, EMAIL_PATTERN, ArtifactType.EMAIL, 0.85);
                detectPattern(config, outputArtifacts, artifact, URL_PATTERN, ArtifactType.URL, 0.85);
                detectPattern(config, outputArtifacts, artifact, WINDOWS_FILE_PATH_PATTERN, ArtifactType.WINDOWS_FILE_PATH, 0.80);
                detectPattern(config, outputArtifacts, artifact, LINUX_FILE_PATH_PATTERN, ArtifactType.LINUX_FILE_PATH, 0.80);
                detectPattern(config, outputArtifacts, artifact, FILE_URI_PATTERN, ArtifactType.FILE_URI, 0.80);
                detectPattern(config, outputArtifacts, artifact, DEVICE_ID_PATTERN, ArtifactType.DEVICE_ID, 0.90);
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
            double confidence
    ) throws Exception {
        Matcher matcher = pattern.matcher(parentArtifact.getValue());

        while (matcher.find()) {
            String matchedValue = matcher.group();

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
