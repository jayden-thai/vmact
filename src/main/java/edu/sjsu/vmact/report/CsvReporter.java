package edu.sjsu.vmact.report;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import edu.sjsu.vmact.extract.ArtifactReader;
import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.model.Cluster;
import edu.sjsu.vmact.model.Hypothesis;
import edu.sjsu.vmact.model.RuleId;
import edu.sjsu.vmact.model.SourceType;
import edu.sjsu.vmact.model.Subclaim;
import edu.sjsu.vmact.pipeline.ScanConfig;

public class CsvReporter implements Reporter{
    @Override
    public void report(
        ArtifactReader artifactReader, 
        List<Cluster> clusters, 
        List<Hypothesis> hypotheses, 
        ScanConfig config
    ) throws Exception {
        Files.createDirectories(config.getOutputDir());

        writeArtifactsCsv(artifactReader, config.getOutputDir().resolve(ReportPaths.ARTIFACTS_CSV));
        writeClustersCsv(clusters, config.getOutputDir().resolve(ReportPaths.CLUSTERS_CSV));
        writeHypothesesCsv(hypotheses, config.getOutputDir().resolve(ReportPaths.HYPOTHESES_CSV));
    }

    private void writeArtifactsCsv(ArtifactReader artifactReader, Path outputPath) throws Exception{
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write("id,parentArtifactId,type,value,sourceId,sourceName,sourceType,producerName,encoding,offset,confidence,context");
            writer.newLine();

            artifactReader.forEach(artifact -> {
                writer.write(csv(artifact.getId()));
                writer.write(",");
                writer.write(csv(artifact.getParentArtifactId()));
                writer.write(",");
                writer.write(csv(artifact.getType().name()));
                writer.write(",");
                writer.write(csv(artifact.getValue()));
                writer.write(",");
                writer.write(csv(artifact.getSourceId()));
                writer.write(",");
                writer.write(csv(artifact.getSourceName()));
                writer.write(",");
                writer.write(csv(artifact.getSourceType().name()));
                writer.write(",");
                writer.write(csv(artifact.getProducerName()));
                writer.write(",");
                writer.write(csv(artifact.getEncoding()));
                writer.write(",");
                writer.write(Long.toString(artifact.getOffset()));
                writer.write(",");
                writer.write(formatConfidence(artifact.getConfidence()));
                writer.write(",");
                writer.write(csv(artifact.getContext()));
                writer.newLine();
            });
        }
    }

    private void writeClustersCsv(List<Cluster> clusters, Path outputpath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputpath)) {
            writer.write("id,label,confidence,artifactCount,anchorArtifactId,anchorValue,rootArtifactIds,clusterTypes,sourceNames,sourceTypes,producerNames,ruleIds,artifactIds,explanation");
            writer.newLine();

            for (Cluster cluster : clusters) {
                writer.write(csv(cluster.getId()));
                writer.write(",");
                writer.write(csv(cluster.getLabel()));
                writer.write(",");
                writer.write(formatConfidence(cluster.getConfidence()));
                writer.write(",");
                writer.write(Integer.toString(cluster.getArtifacts().size()));
                writer.write(",");
                writer.write(csv(cluster.getAnchorArtifactId()));
                writer.write(",");
                writer.write(csv(cluster.getAnchorValue()));
                writer.write(",");
                writer.write(csv(joinStrings(cluster.getRootArtifactIds())));
                writer.write(",");
                writer.write(csv(cluster.getClusterTypes()));
                writer.write(",");
                writer.write(csv(joinStrings(cluster.getSourceNames())));
                writer.write(",");
                writer.write(csv(joinSourceTypes(cluster.getSourceTypes())));
                writer.write(",");
                writer.write(csv(joinStrings(cluster.getProducerNames())));
                writer.write(",");
                writer.write(csv(joinRuleIds(cluster.getRuleIds())));
                writer.write(",");
                writer.write(csv(joinArtifactIds(cluster.getArtifacts())));
                writer.write(",");
                writer.write(csv(cluster.getExplanation()));
                writer.newLine();
            }
        }
    }

    public void writeHypothesesCsv(List<Hypothesis> hypotheses, Path outputPath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write("id,title,activityType,confidence,claim,supportingClusterIds,supportingArtifactIds,subclaimIds,subclaimTypes,subclaims,sourceNames,sourceTypes,producerNames,caveats,alternativeExplanations,ruleIds");
            writer.newLine();
            for (Hypothesis hypothesis : hypotheses) {
                writer.write(csv(hypothesis.getId()));
                writer.write(",");
                writer.write(csv(hypothesis.getTitle()));
                writer.write(",");
                writer.write(csv(hypothesis.getActivityType().name()));
                writer.write(",");
                writer.write(formatConfidence(hypothesis.getConfidence()));
                writer.write(",");
                writer.write(csv(hypothesis.getClaim()));
                writer.write(",");
                writer.write(csv(joinStrings(hypothesis.getSupportingClusterIds())));
                writer.write(",");
                writer.write(csv(joinSupportingArtifactIds(hypothesis.getSubclaims())));
                writer.write(",");
                writer.write(csv(joinSubclaimIds(hypothesis.getSubclaims())));
                writer.write(",");
                writer.write(csv(joinSubclaimTypes(hypothesis.getSubclaims())));
                writer.write(",");
                writer.write(csv(joinSubclaimTexts(hypothesis.getSubclaims())));
                writer.write(",");
                writer.write(csv(joinStrings(hypothesis.getSourceNames())));
                writer.write(",");
                writer.write(csv(joinSourceTypes(hypothesis.getSourceTypes())));
                writer.write(",");
                writer.write(csv(joinStrings(hypothesis.getProducerNames())));
                writer.write(",");
                writer.write(csv(joinStrings(hypothesis.getCaveats())));
                writer.write(",");
                writer.write(csv(joinStrings(hypothesis.getAlternativeExplanations())));
                writer.write(",");
                writer.write(csv(joinRuleIds(hypothesis.getRuleIds())));
                writer.newLine();
            }
        }
    }

    private String csv(String value) {
        if (value == null)
            return "";

        String escaped = value.replace("\"", "\"\"");

        return "\"" + escaped + "\"";
    }

    private String formatConfidence(double confidence) {
        return String.format(Locale.US, "%.2f", confidence);
    }

    private String joinArtifactIds(List<Artifact> artifacts) {
        List<String> ids = new ArrayList<>();

        for (Artifact artifact : artifacts) {
            ids.add(artifact.getId());
        }

        return String.join(";", ids);
    }

    private String joinStrings(List<String> values) {
        return String.join(";", values);
    }

    private String joinSourceTypes(List<SourceType> sourceTypes) {
        List<String> names = new ArrayList<>();

        for (SourceType sourceType : sourceTypes) {
            names.add(sourceType.name());
        }

        return String.join(";", names);
    }

    private String joinRuleIds(List<RuleId> ruleIds) {
        List<String> names = new ArrayList<>();

        for (RuleId ruleId : ruleIds) {
            names.add(ruleId.name());
        }

        return String.join(";", names);
    }

    private String joinSubclaimIds(List<Subclaim> subclaims) {
        List<String> ids = new ArrayList<>();
        for (Subclaim subclaim : subclaims) {
            ids.add(subclaim.getId());
        }
        return String.join(";", ids);
    }

    private String joinSubclaimTypes(List<Subclaim> subclaims) {
        List<String> types = new ArrayList<>();

        for (Subclaim subclaim : subclaims) {
            types.add(subclaim.getType().name());
        }

        return String.join(";", types);
    }

    private String joinSubclaimTexts(List<Subclaim> subclaims) {
        List<String> texts = new ArrayList<>();
        for (Subclaim subclaim : subclaims) {
            texts.add(subclaim.getText());
        }
        return String.join(" | ", texts);
    }

    private String joinSupportingArtifactIds(List<Subclaim> subclaims) {
        Set<String> artifactIds = new TreeSet<>();
        for (Subclaim subclaim : subclaims) {
            artifactIds.addAll(subclaim.getSupportingArtifactIds());
        }
        return String.join(";", artifactIds);
    }
}
