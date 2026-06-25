package edu.sjsu.vmact.report;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import edu.sjsu.vmact.extract.ArtifactReader;
import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.model.Cluster;
import edu.sjsu.vmact.model.Hypothesis;
import edu.sjsu.vmact.model.RuleId;
import edu.sjsu.vmact.model.ScoreBreakdown;
import edu.sjsu.vmact.model.ScoreComponent;
import edu.sjsu.vmact.model.SourceType;
import edu.sjsu.vmact.model.Subclaim;
import edu.sjsu.vmact.pipeline.ScanConfig;

public class MarkdownReporter implements Reporter {
    private static final int MAX_VALUE_LENGTH = 300;

    @Override
    public void report(
        ArtifactReader artifactReader,
        List<Cluster> clusters,
        List<Hypothesis> hypotheses,
        ScanConfig config
    ) throws Exception {
        Files.createDirectories(config.getOutputDir());

        Path outputPath = config.getOutputDir().resolve(ReportPaths.REPORT_MD);

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writeHeader(writer, artifactReader.count(), clusters, hypotheses);
            writeHypotheses(writer, hypotheses, clusters);
            writeAppendix(writer);
        }
    }

    private void writeHeader(
        BufferedWriter writer,
        long artifactCount,
        List<Cluster> clusters,
        List<Hypothesis> hypotheses
    ) throws IOException {
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

        writer.write("> Confidence values should be interpreted as rule-based evidentiary support scores, not probabilities that an activity occurred.");
        writer.newLine();
        writer.newLine();
    }

    private void writeHypotheses(
        BufferedWriter writer,
        List<Hypothesis> hypotheses,
        List<Cluster> clusters
    ) throws IOException {
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
        List<Cluster> clusters
    ) throws IOException {
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
        List<Cluster> clusters
    ) throws IOException {
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
        List<Cluster> clusters
    ) throws IOException {
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

        for (String clusterId : subclaim.getSupportingClusterIds()) {
            Cluster cluster = findClusterById(clusters, clusterId);

            if (cluster == null) {
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
        ScoreBreakdown scoreBreakdown
    ) throws IOException {
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

    private void writeClusterSummary(BufferedWriter writer, Cluster cluster) throws IOException {
        writer.write("    - " + cluster.getId() + ": " + safe(cluster.getLabel()));
        writer.newLine();
        writer.write("      - Anchor: `" + cluster.getAnchorArtifactId() + "` " + inlineCode(cluster.getAnchorValue()));
        writer.newLine();
        writer.write("      - Cluster types: `" + safe(cluster.getClusterTypes()) + "`");
        writer.newLine();
        writer.write("      - Support score: `" + formatConfidence(cluster.getConfidence()) + "`");
        writer.newLine();
        writer.write("      - Sources: " + inlineList(cluster.getSourceNames()));
        writer.newLine();
        writer.write("      - Source types: " + inlineSourceTypes(cluster.getSourceTypes()));
        writer.newLine();
        writer.write("      - Producer: " + inlineList(cluster.getProducerNames()));
        writer.newLine();
        writer.write("      - Rules: " + inlineRuleIds(cluster.getRuleIds()));
        writer.newLine();

        if (cluster.getExplanation() != null && !cluster.getExplanation().isBlank()) {
            writer.write("      - Explanation: " + safe(cluster.getExplanation()));
            writer.newLine();
        }

        writer.write("      - Artifacts:");
        writer.newLine();

        for (Artifact artifact : cluster.getArtifacts()) {
            writeArtifactSummary(writer, artifact);
        }
    }

    private void writeArtifactSummary(BufferedWriter writer, Artifact artifact) throws IOException {
        writer.write("        - " + artifact.getId()
                + " `" + artifact.getType().name() + "`"
                + " offset=`" + artifact.getOffset() + "`"
                + " offsetHex=`" + artifact.getOffsetHex() + "`"
                + " encoding=`" + safe(artifact.getEncoding()) + "`"
                + " producer=`" + safe(artifact.getProducerName()) + "`");
        writer.newLine();

        if (artifact.isDerived()) {
            writer.write("          - Parent artifact: `" + safe(artifact.getParentArtifactId()) + "`");
            writer.newLine();
        }

        writer.write("          - Source: `" + safe(artifact.getSourceName()) + "` / `" + artifact.getSourceType().name() + "`");
        writer.newLine();

        writer.write("          - Value: " + inlineCode(truncate(artifact.getValue(), MAX_VALUE_LENGTH)));
        writer.newLine();

        if (artifact.getContext() != null && !artifact.getContext().isBlank()) {
            writer.write("          - Context: " + inlineCode(truncate(artifact.getContext(), MAX_VALUE_LENGTH)));
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
        writer.write("Recovered volatile-memory artifacts may reflect cached data, application state, automatic system behavior, prior host state, or unrelated memory residue. ");
        writer.write("Hypotheses should therefore be interpreted alongside caveats, alternative explanations, source provenance, and ground-truth context when available.");
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

        return "`" + safe(String.join("; " , values)) + "`";
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
