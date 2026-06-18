package edu.sjsu.vmact.hypothesize;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.sjsu.vmact.model.ActivityType;
import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.model.ArtifactType;
import edu.sjsu.vmact.model.Cluster;
import edu.sjsu.vmact.model.Hypothesis;
import edu.sjsu.vmact.model.RuleId;
import edu.sjsu.vmact.model.SourceType;
import edu.sjsu.vmact.model.Subclaim;
import edu.sjsu.vmact.model.SubclaimType;
import edu.sjsu.vmact.pipeline.ScanConfig;

public class SimpleRuleHypothesisGenerator implements HypothesisGenerator{
    
    @Override
    public List<Hypothesis> generate(
        List<Artifact> artifacts,
        List<Cluster> clusters,
        ScanConfig config
    ) throws Exception {
        List<Hypothesis> hypotheses = new ArrayList<>();
        ClusterBuckets buckets = bucketClusters(clusters);
        List<Subclaim> subclaims = new ArrayList<>();

        addIfPresent(subclaims, buildAccountIdentifierSubclaim(buckets.emailClusters, config));
        addIfPresent(subclaims, buildUrlRecoveredSubclaim(buckets.urlClusters, config));
        addIfPresent(subclaims, buildFilePathRecoveredSubclaim(buckets.filePathClusters, config));
        addIfPresent(subclaims, buildDeviceIdentifierSubclaim(buckets.deviceIdClusters, config));

        addIfPresent(hypotheses, buildAccountHypothesis(subclaims, config));
        addIfPresent(hypotheses, buildWebActivityHypothesis(subclaims, config));
        addIfPresent(hypotheses, buildFileActivityHypothesis(subclaims, config));
        addIfPresent(hypotheses, buildDeviceInteractionHypothesis(subclaims, config));

        return hypotheses;
    }

    private ClusterBuckets bucketClusters(List<Cluster> clusters) {
        ClusterBuckets buckets = new ClusterBuckets();

        for (Cluster cluster : clusters) {
            if (clusterContainsType(cluster, ArtifactType.EMAIL)) {
                buckets.emailClusters.add(cluster);
            }

            if (clusterContainsType(cluster, ArtifactType.URL)) {
                buckets.urlClusters.add(cluster);
            }

            if (clusterContainsType(cluster, ArtifactType.FILE_PATH)) {
                buckets.filePathClusters.add(cluster);
            }

            if (clusterContainsType(cluster, ArtifactType.DEVICE_ID)) {
                buckets.deviceIdClusters.add(cluster);
            }
        }

        return buckets;
    }

    private Subclaim buildAccountIdentifierSubclaim(List<Cluster> clusters, ScanConfig config) {
        if (clusters.isEmpty()) {
            return null;
        }

        return buildSubclaimFromClusters(
            SubclaimType.ACCOUNT_IDENTIFIER_RECOVERED,
            "Email/account-like identifiers were recovered from supporting evidence clusters.",
            0.70,
            clusters,
            "This supports account or communication trace presence, but does not prove successful login, message access, or message transmission.",
            config
        );
    }

    private Subclaim buildUrlRecoveredSubclaim(List<Cluster> clusters, ScanConfig config) {
        if (clusters.isEmpty()) {
            return null;
        }

        return buildSubclaimFromClusters(
            SubclaimType.URL_RECOVERED,
            "URL-like values were recovered from supporting evidence clusters.",
            0.70,
            clusters,
            "This supports web-related trace presence, but does not independently prove that a page was visited by the user.",
            config
        );
    }

    private Subclaim buildFilePathRecoveredSubclaim(List<Cluster> clusters, ScanConfig config) {
        if (clusters.isEmpty()) {
            return null;
        }

        return buildSubclaimFromClusters(
            SubclaimType.FILE_PATH_RECOVERED,
            "File path-like values were recovered from supporting evidence clusters.",
            0.70,
            clusters,
            "This supports file-related trace presence, but does not independently prove file creation, editing, deletion, or transfer.",
            config
        );
    }

    private Subclaim buildDeviceIdentifierSubclaim(List<Cluster> clusters, ScanConfig config) {
        if (clusters.isEmpty()) {
            return null;
        }

        return buildSubclaimFromClusters(
            SubclaimType.DEVICE_IDENTIFIER_RECOVERED,
            "Device identifier-like values were recovered from supporting evidence clusters.",
            0.70,
            clusters,
            "This supports device-related trace presence, but does not independently prove file transfer or user intent.",
            config
        );
    }

    private Subclaim buildSubclaimFromClusters(
        SubclaimType type,
        String text,
        double confidence,
        List<Cluster> supportingClusters,
        String caveat,
        ScanConfig config
    ) {
        return new Subclaim(
            config.nextSubclaimId(),
            type,
            text,
            confidence,
            collectClusterIds(supportingClusters),
            collectArtifactIds(supportingClusters),
            collectSourceNamesFromClusters(supportingClusters),
            collectSourceTypesFromClusters(supportingClusters),
            collectProducerNamesFromClusters(supportingClusters),
            caveat
        );
    }

    private Hypothesis buildAccountHypothesis(List<Subclaim> subclaims, ScanConfig config) {
        List<Subclaim> selectedSubclaims = findSubclaims(
            subclaims,
            SubclaimType.ACCOUNT_IDENTIFIER_RECOVERED
        );

        if (selectedSubclaims.isEmpty()) {
            return null;
        }

        return buildHypothesisFromSubclaims(
            "Possible account or communication trace",
            "Supporting subclaims indicate the presence of account or communication-related identifiers.",
            ActivityType.ACCOUNT_OR_COMMUNICATION,
            0.70,
            selectedSubclaims,
            List.of(
                "Recovered account-like values may reflect user input, cached content, configuration fragments, or memory residue.",
                "This hypothesis does not prove successful authentication or message access."
            ),
            List.of(
                "The identifier may be present due to cached content, prior application state, copied text, or unrelated memory residue."
            ),
            List.of(
                RuleId.HYPOTHESIS_ACCOUNT_OR_COMMUNICATION_TRACE
            ),
            config
        );
    }

    private Hypothesis buildWebActivityHypothesis(List<Subclaim> subclaims, ScanConfig config) {
        List<Subclaim> selectedSubclaims = findSubclaims(
            subclaims,
            SubclaimType.URL_RECOVERED
        );

        if (selectedSubclaims.isEmpty()) {
            return null;
        }

        return buildHypothesisFromSubclaims(
            "Possible web activity trace",
            "Supporting subclaims indicate the presence of web-related activity indicators.",
            ActivityType.WEB_ACTIVITY,
            0.70,
            selectedSubclaims,
            List.of(
                "Recovered URLs may come from browser activity, cached content, logs, bookmarks, copied text, or preload data",
                "Additional process, timestamp, or application context is needed for stronger session reconstruction."
            ),
            List.of(
                "The URL may be present due to cached page content, application strings, bookmarks, or unrelated memory residue."
            ),
            List.of(
                RuleId.HYPOTHESIS_WEB_ACTIVITY_TRACE
            ),
            config
        );
    }

    private Hypothesis buildFileActivityHypothesis(List<Subclaim> subclaims, ScanConfig config) {
        List<Subclaim> selectedSubclaims = findSubclaims(
            subclaims,
            SubclaimType.FILE_PATH_RECOVERED
        );

        if (selectedSubclaims.isEmpty()) {
            return null;
        }

        return buildHypothesisFromSubclaims(
            "Possible file activity trace",
            "Supporting subclaims indicate the presence of file-system-related activity indicators.",
            ActivityType.FILE_ACTIVITY,
            0.72,
            selectedSubclaims,
            List.of(
                "File path artifacts can reflect recent access, application state, cached metadata, bookmarks, or system-generated references.",
                "Additional timestamp, process, or content evidence is needed before claiming a specific file operation."
            ),
            List.of(
                "The path may be present because of application indexing, recent-file metadata, shell history, or unrelated memory residue."
            ),
            List.of(
                RuleId.HYPOTHESIS_FILE_ACTIVITY_TRACE
            ),
            config
        );
    }

    private Hypothesis buildDeviceInteractionHypothesis(List<Subclaim> subclaims, ScanConfig config) {
        List<Subclaim> selectedSubclaims = findSubclaims(
            subclaims,
            SubclaimType.DEVICE_IDENTIFIER_RECOVERED
        );

        if (selectedSubclaims.isEmpty()) {
            return null;
        }

        return buildHypothesisFromSubclaims(
            "Possible external device interaction trace",
            "Supporting subclaims indicate the presence of removable or external device indicators.",
            ActivityType.DEVICE_INTERACTION,
            0.75,
            selectedSubclaims,
            List.of(
                "Device identifiers may reflect enumeration, driver state, cached system metadata, or host-level artifacts.",
                "This hypothesis does not prove that files were copied to or from the device."
            ),
            List.of(
                "The device string may reflect prior host state, automatic enumeration, or unrelated cached hardware metadata."
            ),
            List.of(
                RuleId.HYPOTHESIS_DEVICE_INTERACTION_TRACE
            ),
            config
        );
    }

    private Hypothesis buildHypothesisFromSubclaims(
        String title,
        String claim,
        ActivityType activityType,
        double confidence,
        List<Subclaim> subclaims,
        List<String> caveats,
        List<String> alternativeExplanations,
        List<RuleId> ruleIds,
        ScanConfig config
    ) {
        return new Hypothesis(
            config.nextHypothesisId(),
            title,
            claim,
            activityType,
            confidence,
            collectClusterIdsFromSubclaims(subclaims),
            subclaims,
            collectSourceNamesFromSubclaims(subclaims),
            collectSourceTypesFromSubclaims(subclaims),
            collectProducerNamesFromSubclaims(subclaims),
            caveats,
            alternativeExplanations,
            ruleIds
        );
    }

    private List<Subclaim> findSubclaims(List<Subclaim> subclaims, SubclaimType type) {
        List<Subclaim> matches = new ArrayList<>();

        for (Subclaim subclaim : subclaims) {
            if (subclaim.getType() == type) {
                matches.add(subclaim);
            }
        }

        return matches;
    }

    private boolean clusterContainsType(Cluster cluster, ArtifactType artifactType) {
        for (Artifact artifact : cluster.getArtifacts()) {
            if (artifact.getType() == artifactType) {
                return true;
            }
        }

        return false;
    }

    private List<String> collectClusterIds(List<Cluster> clusters) {
        Set<String> ids = new TreeSet<>();

        for (Cluster cluster : clusters) {
            ids.add(cluster.getId());
        }

        return new ArrayList<>(ids);
    }

    private List<String> collectArtifactIds(List<Cluster> clusters) {
        Set<String> ids = new TreeSet<>();

        for (Cluster cluster : clusters) {
            for (Artifact artifact : cluster.getArtifacts()) {
                ids.add((artifact.getId()));
            }
        }

        return new ArrayList<>(ids);
    }

    private List<String> collectSourceNamesFromClusters(List<Cluster> clusters) {
        Set<String> sourceNames = new TreeSet<>();

        for (Cluster cluster : clusters) {
            sourceNames.addAll(cluster.getSourceNames());
        }

        return new ArrayList<>(sourceNames);
    }

    private List<SourceType> collectSourceTypesFromClusters(List<Cluster> clusters) {
        Set<SourceType> sourceTypes = EnumSet.noneOf(SourceType.class);

        for (Cluster cluster : clusters) {
            sourceTypes.addAll(cluster.getSourceTypes());
        }

        return new ArrayList<>(sourceTypes);
    }

    private List<String> collectProducerNamesFromClusters(List<Cluster> clusters) {
        Set<String> producerNames = new TreeSet<>();

        for (Cluster cluster : clusters) {
            producerNames.addAll(cluster.getProducerNames());
        }

        return new ArrayList<>(producerNames);
    }

    private List<String> collectClusterIdsFromSubclaims(List<Subclaim> subclaims) {
        Set<String> ids = new TreeSet<>();

        for (Subclaim subclaim : subclaims) {
            ids.addAll(subclaim.getSupportingClusterIds());
        }

        return new ArrayList<>(ids);
    }

    private List<String> collectSourceNamesFromSubclaims(List<Subclaim> subclaims) {
        Set<String> sourceNames = new TreeSet<>();

        for (Subclaim subclaim : subclaims) {
            sourceNames.addAll(subclaim.getSourceNames());
        }

        return new ArrayList<>(sourceNames);
    }

    private List<SourceType> collectSourceTypesFromSubclaims(List<Subclaim> subclaims) {
        Set<SourceType> sourceTypes = EnumSet.noneOf(SourceType.class);

        for (Subclaim subclaim : subclaims) {
            sourceTypes.addAll(subclaim.getSourceTypes());
        }

        return new ArrayList<>(sourceTypes);
    }

    private List<String> collectProducerNamesFromSubclaims(List<Subclaim> subclaims) {
        Set<String> producerNames = new TreeSet<>();

        for (Subclaim subclaim : subclaims) {
            producerNames.addAll(subclaim.getProducerNames());
        }

        return new ArrayList<>(producerNames);
    }

    private void addIfPresent(List<Subclaim> subclaims, Subclaim subclaim) {
        if (subclaim != null) {
            subclaims.add(subclaim);
        }
    } 

    private void addIfPresent(List<Hypothesis> hypotheses, Hypothesis hypothesis) {
        if (hypothesis != null) {
            hypotheses.add(hypothesis);
        }
    } 

    private static class ClusterBuckets {
        private final List<Cluster> emailClusters = new ArrayList<>();
        private final List<Cluster> urlClusters = new ArrayList<>();
        private final List<Cluster> filePathClusters = new ArrayList<>();
        private final List<Cluster> deviceIdClusters = new ArrayList<>();
    }
}
