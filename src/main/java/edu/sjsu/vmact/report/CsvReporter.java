package edu.sjsu.vmact.report;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
            writer.write("id,parentArtifactId,type,value,sourceName,encoding,offset,confidence,context");
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
                writer.write(csv(artifact.getSourceName()));
                writer.write(",");
                writer.write(csv(artifact.getEncoding()));
                writer.write(",");
                writer.write(Long.toString(artifact.getOffset()));
                writer.write(",");
                writer.write(Double.toString(artifact.getConfidence()));
                writer.write(",");
                writer.write(csv(artifact.getContext()));
                writer.newLine();
            }
        }
    }

    private void writeClustersCsv(List<Cluster> clusters, Path outputpath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputpath)) {
            writer.write("label,artifactCount");
            writer.newLine();

            for (Cluster cluster : clusters) {
                writer.write(csv(cluster.getLabel()));
                writer.write(",");
                writer.write((Integer.toString(cluster.getArtifacts().size())));
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
}
