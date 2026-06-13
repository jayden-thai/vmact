package edu.sjsu.vmact.detect;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.model.ArtifactType;
import edu.sjsu.vmact.pipeline.ScanConfig;

public class KeywordDetector implements Detector{
    @Override
    public List<Artifact> detect(List<Artifact> inputArtifacts, ScanConfig config) throws IOException{
        List<String> keywords = loadKeywords(config);
        List<Artifact> outputArtifacts = new ArrayList<>(inputArtifacts);

        for (Artifact artifact : inputArtifacts) {
            String value = artifact.getValue();

            for (String keyword : keywords) {
                if (value.contains(keyword)) {
                    outputArtifacts.add(new Artifact(
                        config.nextArtifactId(),
                        artifact.getId(),
                        ArtifactType.KEYWORD_HIT, 
                        keyword, 
                        "keyword-detector", 
                        artifact.getEncoding(), 
                        artifact.getOffset(), 
                        value, 
                        0.90
                    ));
                }
            }
        }
        
        return outputArtifacts;
    }

    private List<String> loadKeywords(ScanConfig config) throws IOException{
        List<String> keywords = new ArrayList<>();

        for (String line : Files.readAllLines(config.getKeywordsFile())) {
            String trimmed = line.trim();

            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                keywords.add(trimmed);
            }
        }

        return keywords;
    }
}
