package edu.sjsu.vmact.report;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.model.Cluster;
import edu.sjsu.vmact.pipeline.ScanConfig;

public class CsvReporter implements Reporter{
    @Override
    public void report(List<Artifact> artifacts, List<Cluster> clusters, ScanConfig config) throws IOException {
        Files.createDirectories(config.getOutputDir());

        writeArtifactsCsv(artifacts, config.getOutputDir().resolve("artifacts.csv"));
        writeClustersCsv(clusters, config.getOutputDir().resolve("clusters.csv"));
    }

    private void writeArtifactsCsv(List<Artifact> artifacts, Path outputPath) throws IOException{
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write("id,parentArtifactId,type,value,sourceId,sourceName,sourceType,encoding,offset,confidence,context");
            writer.newLine();

            for (Artifact artifact : artifacts) {
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
                writer.write(csv(artifact.getSourceType()));
                writer.write(",");
                writer.write(csv(artifact.getEncoding()));
                writer.write(",");
                writer.write(Long.toString(artifact.getOffset()));
                writer.write(",");
                writer.write(formatConfidence(artifact.getConfidence()));
                writer.write(",");
                writer.write(csv(artifact.getContext()));
                writer.newLine();
            }
        }
    }

    private void writeClustersCsv(List<Cluster> clusters, Path outputpath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputpath)) {
            writer.write("id,label,confidence,artifactCount,rootArtifactId,rootValue,clusterTypes,artifactIds,explanation");
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
                writer.write(csv(cluster.getRootArtifactId()));
                writer.write(",");
                writer.write(csv(cluster.getRootValue()));
                writer.write(",");
                writer.write(csv(cluster.getClusterTypes()));
                writer.write(",");
                writer.write(csv(joinArtifactIds(cluster.getArtifacts())));
                writer.write(",");
                writer.write(csv(cluster.getExplanation()));
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
}
