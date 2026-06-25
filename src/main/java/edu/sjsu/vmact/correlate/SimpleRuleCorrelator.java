package edu.sjsu.vmact.correlate;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.sjsu.vmact.extract.ArtifactReader;
import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.model.ArtifactType;
import edu.sjsu.vmact.model.Cluster;
import edu.sjsu.vmact.model.RuleId;
import edu.sjsu.vmact.model.SourceType;
import edu.sjsu.vmact.pipeline.ScanConfig;

public class SimpleRuleCorrelator implements Correlator {
    @Override
    public List<Cluster> correlate(ArtifactReader artifactReader, ScanConfig config) throws Exception {
        // Preserve root encounter order deterministically without relying on child locality.
        Map<String, Artifact> rootArtifactsById = new LinkedHashMap<>();

        // Derived artifacts are grouped by parentArtifactId, regardless of where they appear in the stream.
        Map<String, List<Artifact>> derivedArtifactsByParentId = new LinkedHashMap<>();

        artifactReader.forEach(artifact -> {
            if (artifact.isDerived()) {
                derivedArtifactsByParentId
                        .computeIfAbsent(artifact.getParentArtifactId(), ignored -> new ArrayList<>())
                        .add(artifact);
            } else {
                rootArtifactsById.put(artifact.getId(), artifact);
            }
        });

        List<Cluster> clusters = new ArrayList<>();

        for (Map.Entry<String, Artifact> entry : rootArtifactsById.entrySet()) {
            String rootArtifactId = entry.getKey();
            Artifact rootArtifact = entry.getValue();

            List<Artifact> derivedArtifacts = derivedArtifactsByParentId.get(rootArtifactId);

            if (derivedArtifacts != null && !derivedArtifacts.isEmpty()) {
                clusters.add(buildCluster(rootArtifact, derivedArtifacts, config));
            }
        }

        return clusters;
    }

    private Cluster buildCluster(
            Artifact rootArtifact,
            List<Artifact> derivedArtifacts,
            ScanConfig config
    ) {
        List<Artifact> clusterArtifacts = new ArrayList<>();
        clusterArtifacts.add(rootArtifact);
        clusterArtifacts.addAll(derivedArtifacts);

        String label = chooseLabel(derivedArtifacts);
        double confidence = calculateConfidence(derivedArtifacts);
        String explanation = buildExplanation(rootArtifact, derivedArtifacts);
        List<String> rootArtifactIds = List.of(rootArtifact.getId());

        return new Cluster(
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
        );
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

    private List<SourceType> buildSourceTypes(List<Artifact> artifacts) {
        Set<SourceType> sourceTypes = EnumSet.noneOf(SourceType.class);

        for (Artifact artifact : artifacts) {
            sourceTypes.add(artifact.getSourceType());
        }

        return new ArrayList<>(sourceTypes);
    }

    private List<String> buildProducerNames(List<Artifact> artifacts) {
        Set<String> producerNames = new TreeSet<>();

        for (Artifact artifact : artifacts) {
            producerNames.add(artifact.getProducerName());
        }

        return new ArrayList<>(producerNames);
    }

    private String chooseLabel(List<Artifact> derivedArtifacts) {
        if (containsAnyType(derivedArtifacts, ArtifactType.DEVICE_ID)) {
            return "Possible device artifact";
        }

        if (containsAnyType(
                derivedArtifacts,
                ArtifactType.WINDOWS_FILE_PATH,
                ArtifactType.LINUX_FILE_PATH,
                ArtifactType.FILE_URI
        )) {
            return "Possible file path artifact";
        }

        if (containsAnyType(derivedArtifacts, ArtifactType.URL)) {
            return "Possible web artifact";
        }

        if (containsAnyType(derivedArtifacts, ArtifactType.EMAIL)) {
            return "Possible email/account artifact";
        }

        if (containsAnyType(derivedArtifacts, ArtifactType.KEYWORD_HIT)) {
            return "Keyword match cluster";
        }

        return "Artifact cluster";
    }

    private double calculateConfidence(List<Artifact> derivedArtifacts) {
        double confidence = 0.50;

        if (containsAnyType(derivedArtifacts, ArtifactType.KEYWORD_HIT)) {
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

    private boolean containsAnyRegexType(List<Artifact> artifacts) {
        return containsAnyType(
                artifacts,
                ArtifactType.URL,
                ArtifactType.EMAIL,
                ArtifactType.WINDOWS_FILE_PATH,
                ArtifactType.LINUX_FILE_PATH,
                ArtifactType.FILE_URI,
                ArtifactType.DEVICE_ID
        );
    }

    private boolean containsAnyType(List<Artifact> artifacts, ArtifactType... artifactTypes) {
        for (Artifact artifact : artifacts) {
            for (ArtifactType artifactType : artifactTypes) {
                if (artifact.getType() == artifactType) {
                    return true;
                }
            }
        }

        return false;
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
                + " ("
                + rootArtifact.getOffsetHex()
                + ") has "
                + derivedArtifacts.size()
                + " derived annotation(s).";
    }
}