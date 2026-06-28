package edu.sjsu.vmact.detect;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.sjsu.vmact.extract.ArtifactReader;
import edu.sjsu.vmact.extract.ArtifactWriter;
import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.model.ArtifactType;
import edu.sjsu.vmact.pipeline.ScanConfig;

public class KeywordDetector implements Detector{
    @Override
    public void detect(
        ArtifactReader inputArtifacts, 
        ArtifactWriter outputArtifacts,
        ScanConfig config
    ) throws Exception{
        List<String> lowercaseKeywords = loadLowercaseKeywords(config);

        inputArtifacts.forEach(artifact -> {
            outputArtifacts.write(artifact);
            
            if (artifact.getType() != ArtifactType.RAW_STRING) {
                return;
            }

            String value = artifact.getValue();
            String lowercaseValue = value.toLowerCase(Locale.ROOT);

            for (String lowercaseKeyword : lowercaseKeywords) {
                int matchStart = lowercaseValue.indexOf(lowercaseKeyword);

                if (matchStart >= 0) {
                    int matchEnd = matchStart + lowercaseKeyword.length();

                    outputArtifacts.write(new Artifact(
                        config.nextArtifactId(),
                        artifact.getId(),
                        ArtifactType.KEYWORD_HIT, 
                        lowercaseKeyword, 
                        artifact.getSourceId(),
                        artifact.getSourceName(),
                        artifact.getSourceType(),
                        "keyword-detector", 
                        artifact.getEncoding(), 
                        artifact.getOffset(), 
                        contextWindow(artifact.getValue(), matchStart, matchEnd, 200), 
                        0.90
                    ));
                }
            }
        });
        System.out.println("    KeywordDetector wrote artifacts: " + outputArtifacts.getWrittenCount());
        
    }

    private String contextWindow(String value, int start, int end, int radius) {
        int contextStart = Math.max(0, start - radius);
        int contextEnd = Math.min(value.length(), end + radius);

        return value.substring(contextStart, contextEnd);
    }

    private List<String> loadLowercaseKeywords(ScanConfig config) throws IOException{
        List<String> keywords = new ArrayList<>();

        for (String line : Files.readAllLines(config.getKeywordsFile())) {
            String trimmed = line.trim();

            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                keywords.add(trimmed.toLowerCase(Locale.ROOT));
            }
        }

        return keywords;
    }
}
