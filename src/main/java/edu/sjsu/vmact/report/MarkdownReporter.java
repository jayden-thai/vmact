package edu.sjsu.vmact.report;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;

import edu.sjsu.vmact.extract.ArtifactReader;
import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.model.ArtifactType;
import edu.sjsu.vmact.model.Cluster;
import edu.sjsu.vmact.model.Hypothesis;
import edu.sjsu.vmact.model.RuleId;
import edu.sjsu.vmact.model.ScoreBreakdown;
import edu.sjsu.vmact.model.ScoreComponent;
import edu.sjsu.vmact.model.SourceType;
import edu.sjsu.vmact.model.Subclaim;
import edu.sjsu.vmact.pipeline.ScanConfig;
import edu.sjsu.vmact.rules.ArtifactSignalRules;

public class MarkdownReporter implements Reporter {
    private static final int MAX_VALUE_LENGTH = 300;
    private static final int MAX_CLUSTERS_PER_SUBCLAIM = 5;
    private static final int MAX_ARTIFACTS_PER_CLUSTER = 4;
    private static final int MAX_VALUE_SUMMARIES = 25;

    private static class ValueSummary {
        private final ArtifactType type;
        private final String value;
        private int count;
        private final List<String> representativeArtifactIds = new ArrayList<>();

        private ValueSummary(ArtifactType type, String value) {
            this.type = type;
            this.value = value;
        }

        private void add(String artifactId) {
            count++;

            if (representativeArtifactIds.size() < 5) {
                representativeArtifactIds.add(artifactId);
            }
        }
    }

    @Override
    public void report(
            ArtifactReader artifactReader,
            List<Cluster> clusters,
            List<Hypothesis> hypotheses,
            ScanConfig config) throws Exception {
        Files.createDirectories(config.getOutputDir());

        Path outputPath = config.getOutputDir().resolve(ReportPaths.REPORT_MD);

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writeHeader(writer, artifactReader.count(), clusters, hypotheses);
            writeValueSummary(writer, clusters);
            writeHypotheses(writer, hypotheses, clusters);
            writeAppendix(writer);
        }
    }

    private void writeHeader(
            BufferedWriter writer,
            long artifactCount,
            List<Cluster> clusters,
            List<Hypothesis> hypotheses) throws IOException {
        writer.write("# VMACT Forensic Hypothesis Report");
        writer.newLine();
        writer.newLine();

        writer.write("## Summary");
        writer.newLine();
        writer.newLine();

        writer.write("- Total artifacts: " + artifactCount);
        writer.newLine();
        writer.write("- Total clusters: " + clusters.size());
        writer.newLine();
        writer.write("- Total hypotheses: " + hypotheses.size());
        writer.newLine();
        writer.newLine();

        writer.write(
                "> Confidence values should be interpreted as rule-based evidentiary support scores, not probabilities that an activity occurred.");
        writer.newLine();
        writer.newLine();
    }

    private void writeValueSummary(
            BufferedWriter writer,
            List<Cluster> clusters) throws IOException {
        Map<String, ValueSummary> summaries = new LinkedHashMap<>();

        for (Cluster cluster : clusters) {
            for (Artifact artifact : cluster.getArtifacts()) {
                if (!(artifact.getType() == ArtifactType.RAW_STRING)) {
                    String key = artifact.getType().name() + "\u0000" + artifact.getValue();

                    ValueSummary summary = summaries.computeIfAbsent(
                            key,
                            ignored -> new ValueSummary(artifact.getType(), artifact.getValue()));

                    summary.add(artifact.getId());
                }
            }
        }

        writer.write("## Prioritized Recovered Value Summary");
        writer.newLine();
        writer.newLine();
        writer.write(
                "Repeated values are ranked for review priority. This section is not exhaustive; see artifacts.csv for complete artifact output.");
        writer.newLine();
        writer.newLine();

        int written = 0;
        boolean hasMoreRepeatedValues = false;

        PriorityQueue<ValueSummary> summaryHeap = new PriorityQueue<>(
                (first, second) -> Integer.compare(
                        valueSummaryPriority(second),
                        valueSummaryPriority(first)));

        summaryHeap.addAll(summaries.values());

        while (!summaryHeap.isEmpty() && !hasMoreRepeatedValues) {
            ValueSummary summary = summaryHeap.remove();

            if (summary.count >= 2 && written < MAX_VALUE_SUMMARIES) {
                writeValueSummaryItem(writer, summary);
                written++;
            } else if (summary.count >= 2) {
                hasMoreRepeatedValues = true;
            }
        }

        if (hasMoreRepeatedValues) {
            writer.write("- Additional repeated values omitted from this summary. See artifacts.csv for full details.");
            writer.newLine();
        }
    }

    private void writeValueSummaryItem(BufferedWriter writer, ValueSummary summary) throws IOException {
        writer.write("- `" + escapeMarkdown(summary.value) + "`");
        writer.newLine();
        writer.write("  - Type: `" + summary.type.name() + "`");
        writer.newLine();
        writer.write("  - Occurrences in clustered artifacts: " + summary.count);
        writer.newLine();
        writer.write("  - Representative artifact IDs: `"
                + String.join("`, `", summary.representativeArtifactIds) + "`");
        writer.newLine();

        String interpretation = interpretRepeatedValue(summary);

        if (!interpretation.isBlank()) {
            writer.write("  - Interpretation note: " + interpretation);
            writer.newLine();
        }

        writer.newLine();
    }

    private int valueSummaryPriority(ValueSummary summary) {
        String lowercaseValue = safe(summary.value).toLowerCase(Locale.ROOT);
        int score = baseTypePriority(summary.type);

        score += highSignalBonus(lowercaseValue);
        score -= lowSignalPenalty(summary.type, lowercaseValue);

        // repetition is useful, but cap it so noisy repeated constants do not dominate.
        score += Math.min(summary.count, 10);

        return score;
    }

    private int baseTypePriority(ArtifactType type) {
        if (type == ArtifactType.KEYWORD_HIT) {
            return 100;
        }

        if (type == ArtifactType.DEVICE_ID) {
            return 90;
        }

        if (type == ArtifactType.URL) {
            return 75;
        }

        if (type == ArtifactType.FILE_URI) {
            return 70;
        }

        if (type == ArtifactType.WINDOWS_FILE_PATH || type == ArtifactType.LINUX_FILE_PATH) {
            return 60;
        }

        if (type == ArtifactType.EMAIL) {
            return 50;
        }

        return 10;
    }

    private int highSignalBonus(String lowercaseValue) {
        int bonus = 0;

        if (ArtifactSignalRules.isHighSignalUserPath(lowercaseValue)) {
            bonus += 30;
        }

        if (ArtifactSignalRules.hasHighSignalEmailProvider(lowercaseValue)) {
            bonus += 30;
        }

        if (ArtifactSignalRules.hasHighSignalFileExtension(lowercaseValue)) {
            bonus += 20;
        }

        return bonus;
    }

    private int lowSignalPenalty(ArtifactType type, String lowercaseValue) {
        int penalty = 0;

        if (isTriviallyShortOrGenericPath(type, lowercaseValue)) {
            penalty += 100;
        }

        if (type == ArtifactType.URL
                && ArtifactSignalRules.isLowSignalTechnicalUrl(lowercaseValue)) {
            penalty += 80;
        }

        if (type == ArtifactType.EMAIL
                && ArtifactSignalRules.isLowSignalSoftwareEmail(lowercaseValue)) {
            penalty += 80;
        }

        if ((type == ArtifactType.LINUX_FILE_PATH
                || type == ArtifactType.WINDOWS_FILE_PATH
                || type == ArtifactType.FILE_URI)
                && ArtifactSignalRules.isLowSignalSystemPath(lowercaseValue)) {
            penalty += 40;
        }

        return penalty;
    }

    private boolean isTriviallyShortOrGenericPath(ArtifactType type, String lowercaseValue) {
        if (type != ArtifactType.LINUX_FILE_PATH
                && type != ArtifactType.WINDOWS_FILE_PATH
                && type != ArtifactType.FILE_URI) {
            return false;
        }

        return lowercaseValue.equals("/media/common/")
                || lowercaseValue.equals("/media/common")
                || lowercaseValue.equals("/usr/")
                || lowercaseValue.equals("/etc/")
                || lowercaseValue.equals("/run/")
                || lowercaseValue.equals("/var/")
                || lowercaseValue.equals("/dev/")
                || lowercaseValue.equals("/tmp/");
    }

    private String interpretRepeatedValue(ValueSummary summary) {
        String lowercaseValue = safe(summary.value).toLowerCase(Locale.ROOT);

        if (summary.type == ArtifactType.URL
                && ArtifactSignalRules.isLowSignalTechnicalUrl(lowercaseValue)) {
            return "This appears to be a schema, XML namespace, ontology, or embedded technical identifier rather than direct browsing evidence.";
        }

        if (summary.type == ArtifactType.EMAIL
                && ArtifactSignalRules.isLowSignalSoftwareEmail(lowercaseValue)) {
            return "This appears to be an email-shaped software, service, or automated identifier rather than direct account evidence.";
        }

        if (summary.type == ArtifactType.URL) {
            return "Repeated URL presence indicates repeated recovery in memory, not necessarily repeated user visits.";
        }

        return "";
    }

    private String escapeMarkdown(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("`", "\\`");
    }

    private void writeHypotheses(
            BufferedWriter writer,
            List<Hypothesis> hypotheses,
            List<Cluster> clusters) throws IOException {
        writer.write("## Hypotheses");
        writer.newLine();
        writer.newLine();

        if (hypotheses.isEmpty()) {
            writer.write("No hypotheses were generated");
            writer.newLine();
            return;
        }

        for (Hypothesis hypothesis : hypotheses) {
            writeHypothesis(writer, hypothesis, clusters);
        }
    }

    private void writeHypothesis(
            BufferedWriter writer,
            Hypothesis hypothesis,
            List<Cluster> clusters) throws IOException {
        writer.write("### " + hypothesis.getId() + ": " + safe(hypothesis.getTitle()));
        writer.newLine();
        writer.newLine();

        writer.write("- Activity type: `" + hypothesis.getActivityType().name() + "`");
        writer.newLine();
        writer.write("- Support score: `" + formatConfidence(hypothesis.getConfidence()) + "`");
        writer.newLine();
        writer.write("- Support level: `" + hypothesis.getSupportLevel().name() + "`");
        writer.newLine();
        writer.write("- Overclaim risk: `" + hypothesis.getOverclaimRisk().name() + "`");
        writer.newLine();
        writeScoreBreakdown(writer, "", hypothesis.getScoreBreakdown());
        writer.write("- Sources: " + inlineList(hypothesis.getSourceNames()));
        writer.newLine();
        writer.write("- Source types: " + inlineSourceTypes(hypothesis.getSourceTypes()));
        writer.newLine();
        writer.write("- Producers: " + inlineList(hypothesis.getProducerNames()));
        writer.newLine();
        writer.write("- Rules: " + inlineRuleIds(hypothesis.getRuleIds()));
        writer.newLine();
        writer.newLine();

        writer.write("Claim: ");
        writer.newLine();
        writer.newLine();
        writer.write("> " + safe(hypothesis.getClaim()));
        writer.newLine();
        writer.newLine();

        writeSubclaims(writer, hypothesis, clusters);
        writeCaveats(writer, hypothesis);
        writeAlternativeExplanations(writer, hypothesis);

        writer.write("---");
        writer.newLine();
        writer.newLine();
    }

    private void writeSubclaims(
            BufferedWriter writer,
            Hypothesis hypothesis,
            List<Cluster> clusters) throws IOException {
        writer.write("#### Subclaims");
        writer.newLine();
        writer.newLine();

        if (hypothesis.getSubclaims().isEmpty()) {
            writer.write("No subclaims recorded.");
            writer.newLine();
            writer.newLine();
            return;
        }

        for (Subclaim subclaim : hypothesis.getSubclaims()) {
            writeSubclaim(writer, subclaim, clusters);
        }
    }

    private void writeSubclaim(
            BufferedWriter writer,
            Subclaim subclaim,
            List<Cluster> clusters) throws IOException {
        writer.write("- " + subclaim.getId() + " `" + subclaim.getType().name() + "`");
        writer.newLine();
        writer.write("  - Statement: " + safe(subclaim.getText()));
        writer.newLine();
        writer.write("  - Support score: `" + formatConfidence(subclaim.getConfidence()) + "`");
        writer.newLine();
        writer.write("  - Support level: `" + subclaim.getSupportLevel().name() + "`");
        writer.newLine();
        writer.write("  - Overclaim risk: `" + subclaim.getOverclaimRisk().name() + "`");
        writer.newLine();
        writeScoreBreakdown(writer, "  ", subclaim.getScoreBreakdown());
        writer.write("  - Sources: " + inlineList(subclaim.getSourceNames()));
        writer.newLine();
        writer.write("  - Source types: " + inlineSourceTypes(subclaim.getSourceTypes()));
        writer.newLine();
        writer.write("  - Producers: " + inlineList(subclaim.getProducerNames()));
        writer.newLine();

        if (subclaim.getCaveat() != null && !subclaim.getCaveat().isBlank()) {
            writer.write("  - Caveat: " + safe(subclaim.getCaveat()));
            writer.newLine();
        }

        writer.write("  - Supporting clusters:");
        writer.newLine();

        List<String> supportingClusterIds = subclaim.getSupportingClusterIds();

        // only display ten clusters per subclaim to preserve readability
        for (int i = 0; i <= MAX_CLUSTERS_PER_SUBCLAIM && i < supportingClusterIds.size(); i++) {
            String clusterId = supportingClusterIds.get(i);
            Cluster cluster = findClusterById(clusters, clusterId);

            if (i == MAX_CLUSTERS_PER_SUBCLAIM) {
                int omittedCount = supportingClusterIds.size() - i;
                writer.write("    - " + omittedCount
                        + " additional supporting cluster(s) omitted from this summary. See clusters.csv for full details.");
                writer.newLine();
            } else if (cluster == null) {
                writer.write("    - " + clusterId + " not found in current cluster list.");
                writer.newLine();
            } else {
                writeClusterSummary(writer, cluster);
            }
        }

        writer.newLine();
    }

    private void writeScoreBreakdown(
            BufferedWriter writer,
            String indent,
            ScoreBreakdown scoreBreakdown) throws IOException {
        writer.write(indent + "- Score breakdown:");
        writer.newLine();

        for (ScoreComponent component : scoreBreakdown.getComponents()) {
            writer.write(indent + "  - `" + formatSignedDelta(component.getDelta()) + "` "
                    + safe(component.getLabel()) + ": "
                    + safe(component.getExplanation()));
            writer.newLine();
        }
    }

    private String formatSignedDelta(double delta) {
        if (delta >= 0.0) {
            return "+" + formatConfidence(delta);
        }

        return formatConfidence(delta);
    }

    private List<Artifact> derivedArtifacts(Cluster cluster) {
        List<Artifact> artifacts = new ArrayList<>();

        for (Artifact artifact : cluster.getArtifacts()) {
            if (artifact.isDerived()) {
                artifacts.add(artifact);
            }
        }

        return artifacts;
    }

    private List<Artifact> rootArtifacts(Cluster cluster) {
        List<Artifact> artifacts = new ArrayList<>();

        for (Artifact artifact : cluster.getArtifacts()) {
            if (!artifact.isDerived()) {
                artifacts.add(artifact);
            }
        }

        return artifacts;
    }

    private List<Artifact> displayOrderedArtifacts(Cluster cluster) {
        List<Artifact> artifacts = new ArrayList<>();
        artifacts.addAll(derivedArtifacts(cluster));
        artifacts.addAll(rootArtifacts(cluster));
        return artifacts;
    }

    private void writeKeyRecoveredValues(BufferedWriter writer, Cluster cluster) throws IOException {
        List<Artifact> derivedArtifacts = derivedArtifacts(cluster);

        writer.write("      **Key recovered values**");
        writer.newLine();

        if (derivedArtifacts.isEmpty()) {
            writer.write("      - No derived values recorded.");
            writer.newLine();
            return;
        }

        int written = 0;
        boolean hasMoreValues = false;

        for (int i = 0; i < derivedArtifacts.size(); i++) {
            Artifact artifact = derivedArtifacts.get(i);

            if (written < MAX_ARTIFACTS_PER_CLUSTER) {
                writer.write("      - `" + artifact.getType().name() + "` "
                        + inlineCode(truncate(artifact.getValue(), MAX_VALUE_LENGTH)));
                writer.newLine();
                written++;
            } else {
                hasMoreValues = true;
            }
        }

        if (hasMoreValues) {
            writer.write("      - Additional recovered values omitted from this cluster summary.");
            writer.newLine();
        }
    }

    private void writeClusterSummary(BufferedWriter writer, Cluster cluster) throws IOException {
        writer.write("    - Cluster `" + cluster.getId() + "` — " + safe(cluster.getLabel()));
        writer.newLine();
        writer.newLine();

        writeKeyRecoveredValues(writer, cluster);
        writer.newLine();

        writer.write("      **Evidence details**");
        writer.newLine();
        writer.write("      - Support score: `" + formatConfidence(cluster.getConfidence()) + "`");
        writer.newLine();
        writer.write("      - Cluster types: `" + safe(cluster.getClusterTypes()) + "`");
        writer.newLine();
        writer.write("      - Sources: " + inlineList(cluster.getSourceNames()));
        writer.newLine();
        writer.write("      - Source types: " + inlineSourceTypes(cluster.getSourceTypes()));
        writer.newLine();
        writer.write("      - Producers: " + inlineList(cluster.getProducerNames()));
        writer.newLine();
        writer.write("      - Rules: " + inlineRuleIds(cluster.getRuleIds()));
        writer.newLine();

        if (cluster.getExplanation() != null && !cluster.getExplanation().isBlank()) {
            writer.write("      - Explanation: " + safe(cluster.getExplanation()));
            writer.newLine();
        }

        writer.newLine();

        writer.write("      **Supporting artifacts**");
        writer.newLine();

        List<Artifact> clusterArtifacts = displayOrderedArtifacts(cluster);

        int written = 0;
        boolean hasMoreArtifacts = false;

        for (int i = 0; i < clusterArtifacts.size(); i++) {
            Artifact artifact = clusterArtifacts.get(i);

            if (written < MAX_ARTIFACTS_PER_CLUSTER) {
                writeArtifactSummary(writer, artifact);
                written++;
            } else {
                hasMoreArtifacts = true;
            }
        }

        if (hasMoreArtifacts) {
            writer.write(
                    "      - Additional supporting artifact(s) omitted from this cluster summary. See artifacts.csv for full details.");
            writer.newLine();
        }

        writer.newLine();
    }

    private void writeArtifactSummary(BufferedWriter writer, Artifact artifact) throws IOException {
        writer.write("      - `" + artifact.getId() + "` `" + artifact.getType().name() + "`");
        writer.newLine();

        writer.write("        - Value: " + inlineCode(truncate(artifact.getValue(), MAX_VALUE_LENGTH)));
        writer.newLine();

        if (artifact.isDerived()) {
            writer.write("        - Parent artifact: `" + safe(artifact.getParentArtifactId()) + "`");
            writer.newLine();
        }

        writer.write("        - Offset: `" + artifact.getOffsetHex() + "`");
        writer.newLine();

        writer.write("        - Source: `" + safe(artifact.getSourceName()) + "` / `"
                + artifact.getSourceType().name() + "`");
        writer.newLine();

        writer.write("        - Encoding: `" + safe(artifact.getEncoding()) + "`");
        writer.newLine();

        writer.write("        - Producer: `" + safe(artifact.getProducerName()) + "`");
        writer.newLine();

        if (artifact.getContext() != null && !artifact.getContext().isBlank()) {
            writer.write("        - Context: " + inlineCode(truncate(artifact.getContext(), MAX_VALUE_LENGTH)));
            writer.newLine();
        }
    }

    private void writeCaveats(BufferedWriter writer, Hypothesis hypothesis) throws IOException {
        writer.write("#### Caveats");
        writer.newLine();
        writer.newLine();

        if (hypothesis.getCaveats().isEmpty()) {
            writer.write("- No caveats recorded.");
            writer.newLine();
            writer.newLine();
            return;
        }

        for (String caveat : hypothesis.getCaveats()) {
            writer.write("- " + safe(caveat));
            writer.newLine();
        }

        writer.newLine();
    }

    private void writeAlternativeExplanations(BufferedWriter writer, Hypothesis hypothesis) throws IOException {
        writer.write("#### Alternative explanations");
        writer.newLine();
        writer.newLine();

        if (hypothesis.getAlternativeExplanations().isEmpty()) {
            writer.write("- No alternative explanations record.");
            writer.newLine();
            writer.newLine();
            return;
        }

        for (String alternativeExplanation : hypothesis.getAlternativeExplanations()) {
            writer.write("- " + safe(alternativeExplanation));
            writer.newLine();
        }

        writer.newLine();
    }

    private void writeAppendix(BufferedWriter writer) throws IOException {
        writer.write("## Interpretation note");
        writer.newLine();
        writer.newLine();

        writer.write("This report is intended to support forensic review, not replace examiner judgment. ");
        writer.write("VMACT groups artifacts, constructs subclaims, and generates hypotheses using explicit rules. ");
        writer.write(
                "Recovered volatile-memory artifacts may reflect cached data, application state, automatic system behavior, prior host state, or unrelated memory residue. ");
        writer.write(
                "Hypotheses should therefore be interpreted alongside caveats, alternative explanations, source provenance, and ground-truth context when available.");
        writer.newLine();
    }

    private Cluster findClusterById(List<Cluster> clusters, String clusterId) {
        for (Cluster cluster : clusters) {
            if (cluster.getId().equals(clusterId)) {
                return cluster;
            }
        }

        return null;
    }

    private String inlineList(List<String> values) {
        if (values.isEmpty()) {
            return "`none`";
        }

        return "`" + safe(String.join("; ", values)) + "`";
    }

    private String inlineSourceTypes(List<SourceType> sourceTypes) {
        if (sourceTypes.isEmpty()) {
            return "`none`";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < sourceTypes.size(); i++) {
            if (i > 0) {
                builder.append("; ");
            }

            builder.append(sourceTypes.get(i).name());
        }

        return "`" + builder + "`";
    }

    private String inlineRuleIds(List<RuleId> ruleIds) {
        if (ruleIds.isEmpty()) {
            return "`none`";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < ruleIds.size(); i++) {
            if (i > 0) {
                builder.append("; ");
            }

            builder.append(ruleIds.get(i).name());
        }

        return "`" + builder + "`";
    }

    private String inlineCode(String value) {
        return "`" + safe(value).replace("`", "\\`") + "`";
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength) + "...";
    }

    private String formatConfidence(double confidence) {
        return String.format(Locale.US, "%.2f", confidence);
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }

        return value;
    }
}
