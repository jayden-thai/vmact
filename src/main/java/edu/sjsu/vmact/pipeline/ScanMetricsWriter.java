package edu.sjsu.vmact.pipeline;

import edu.sjsu.vmact.report.ReportPaths;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ScanMetricsWriter {
    public void write(List<ScanMetric> metrics, Path outputDir) throws Exception {
        Files.createDirectories(outputDir);

        Path outputPath = outputDir.resolve(ReportPaths.SCAN_METRICS_CSV);

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write("stage,component,durationMillis,artifactCount,details");
            writer.newLine();

            for (ScanMetric metric : metrics) {
                writer.write(csv(metric.getStage()));
                writer.write(",");
                writer.write(csv(metric.getComponent()));
                writer.write(",");
                writer.write(Long.toString(metric.getDurationMillis()));
                writer.write(",");
                writer.write(Long.toString(metric.getArtifactCount()));
                writer.write(",");
                writer.write(csv(metric.getDetails()));
                writer.newLine();
            }
        }
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}