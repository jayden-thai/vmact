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

            String lowercaseValue = artifact.getValue().toLowerCase(Locale.ROOT);

            for (String keyword : lowercaseKeywords) {
                if (lowercaseValue.contains(keyword)) {
                    outputArtifacts.write(new Artifact(
                        config.nextArtifactId(),
                        artifact.getId(),
                        ArtifactType.KEYWORD_HIT, 
                        keyword, 
                        artifact.getSourceId(),
                        artifact.getSourceName(),
                        artifact.getSourceType(),
                        "keyword-detector", 
                        artifact.getEncoding(), 
                        artifact.getOffset(), 
                        artifact.getValue(), 
                        0.90
                    ));
                }
            }
        });
        System.out.println("    KeywordDetector wrote artifacts: " + outputArtifacts.getWrittenCount());
        
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
