package edu.sjsu.vmact.correlate;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.model.ArtifactType;
import edu.sjsu.vmact.model.Cluster;
import edu.sjsu.vmact.model.RuleId;
import edu.sjsu.vmact.model.SourceType;
import edu.sjsu.vmact.pipeline.ScanConfig;

public class SimpleRuleCorrelator implements Correlator{
    @Override
    public List<Cluster> correlate(List<Artifact> artifacts, ScanConfig config) {
        Map<String, Artifact> rootArtifactsById = new HashMap<>();
        Map<String, List<Artifact>> derivedArtifactsByParentId = new HashMap<>();

        for (Artifact artifact : artifacts) {
            if (artifact.isDerived()) {
                derivedArtifactsByParentId
                        .computeIfAbsent(artifact.getParentArtifactId(), key -> new ArrayList<>())
                        .add(artifact);
            } else {
                rootArtifactsById.put(artifact.getId(), artifact);
            }
        }

        List<Cluster> clusters = new ArrayList<>();

        for (Map.Entry<String, List<Artifact>> entry : derivedArtifactsByParentId.entrySet()) {
            String parentArtifactId = entry.getKey();
            List<Artifact> derivedArtifacts = entry.getValue();

            Artifact rootArtifact = rootArtifactsById.get(parentArtifactId);

            if (rootArtifact != null) {
                List<Artifact> clusterArtifacts = new ArrayList<>();
                clusterArtifacts.add(rootArtifact);
                clusterArtifacts.addAll(derivedArtifacts);

                String label = chooseLabel(derivedArtifacts);
                double confidence = calculateConfidence(derivedArtifacts);
                String explanation = buildExplanation(rootArtifact, derivedArtifacts);
                List<String> rootArtifactIds = List.of(rootArtifact.getId());

                clusters.add(new Cluster(
                    config.nextClusterId(),
                    label, 
                    rootArtifactIds,
                    rootArtifact.getId(),
                    rootArtifact.getValue(),
                    buildClusterTypes(clusterArtifacts),
                    buildSourceNames(clusterArtifacts),
                    buildSourceTypes(clusterArtifacts),
                    buildProducerNames(clusterArtifacts),
                    List.of(RuleId.CLUSTER_PARENT_CHILD),
                    clusterArtifacts,
                    confidence,
                    explanation
                ));
            }
        }

        return clusters;
    }

    private String buildClusterTypes(List<Artifact> artifacts) {
        Set<ArtifactType> types = EnumSet.noneOf(ArtifactType.class);

        for (Artifact artifact : artifacts) {
            types.add(artifact.getType());
        }

        List<String> typeNames = new ArrayList<>();

        for (ArtifactType type : types) {
            typeNames.add(type.name());
        }

        return String.join(";", typeNames);
    }

    private List<String> buildSourceNames(List<Artifact> artifacts) {
        Set<String> sourceNames = new TreeSet<>();

        for (Artifact artifact : artifacts) {
            sourceNames.add(artifact.getSourceName());
        }

        return new ArrayList<>(sourceNames);
    }

    private List<String> buildProducerNames(List<Artifact> artifacts) {
        Set<String> producerNames = new TreeSet<>();

        for (Artifact artifact : artifacts) {
            producerNames.add(artifact.getProducerName());
        }

        return new ArrayList<>(producerNames);
    }

    private List<SourceType> buildSourceTypes(List<Artifact> artifacts) {
        Set<SourceType> sourceTypes = EnumSet.noneOf(SourceType.class);

        for (Artifact artifact : artifacts) {
            sourceTypes.add(artifact.getSourceType());
        }

        return new ArrayList<>(sourceTypes);
    }

    private String chooseLabel(List<Artifact> derivedArtifacts) {
        if (containsType(derivedArtifacts, ArtifactType.DEVICE_ID)) {
            return "Possible device artifact";
        }

        if (containsType(derivedArtifacts, ArtifactType.FILE_PATH)) {
            return "Possible file path artifact";
        }

        if (containsType(derivedArtifacts, ArtifactType.URL)) {
            return "Possible web artifact";
        }

        if (containsType(derivedArtifacts, ArtifactType.EMAIL)) {
            return "Possible email/account artifact";
        }

        if (containsType(derivedArtifacts, ArtifactType.KEYWORD_HIT)) {
            return "Keyword match cluster";
        }

        return "Artifact cluster";
    }

    private double calculateConfidence(List<Artifact> derivedArtifacts) {
        double confidence = 0.50;

        if (containsType(derivedArtifacts, ArtifactType.KEYWORD_HIT)) {
            confidence += 0.10;
        }

        if (containsAnyRegexType(derivedArtifacts)) {
            confidence += 0.10;
        }

        if (countDistinctTypes(derivedArtifacts) >= 2) {
            confidence += 0.10;
        }

        return Math.min(confidence, 0.95);
    }

    private boolean containsType(List<Artifact> artifacts, ArtifactType type) {
        for (Artifact artifact : artifacts) {
            if (artifact.getType() == type) {
                return true;
            }
        }

        return false;
    }

    private boolean containsAnyRegexType(List<Artifact> artifacts) {
        return containsType(artifacts, ArtifactType.URL)
                || containsType(artifacts, ArtifactType.EMAIL)
                || containsType(artifacts, ArtifactType.FILE_PATH)
                || containsType(artifacts, ArtifactType.DEVICE_ID);
    }

    private int countDistinctTypes(List<Artifact> artifacts) {
        Set<ArtifactType> seenTypes = new HashSet<>();

        for (Artifact artifact : artifacts) {
            seenTypes.add(artifact.getType());
        }

        return seenTypes.size();
    }

    private String buildExplanation(Artifact rootArtifact, List<Artifact> derivedArtifacts) {
        return "Root artifact at offset " 
                + rootArtifact.getOffset()
                + " has "
                + derivedArtifacts.size()
                + " derived annotation(s).";
    }
}
