package edu.sjsu.vmact.report;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import edu.sjsu.vmact.extract.ArtifactReader;
import edu.sjsu.vmact.model.ArtifactType;
import edu.sjsu.vmact.model.Cluster;
import edu.sjsu.vmact.model.Hypothesis;
import edu.sjsu.vmact.pipeline.ScanConfig;

public class ConsoleReporter implements Reporter {
    @Override
    public void report(
        ArtifactReader artifactReader,
        List<Cluster> clusters,
        List<Hypothesis> hypotheses,
        ScanConfig config
    ) throws Exception {
        ArtifactSummary summary = summarizeArtifacts(artifactReader);

        System.out.println("\nScan summary:");

        System.out.println("\nArtifacts:");
        System.out.println("    Total normalized artifacts: " + summary.totalArtifactCount);
        System.out.println("    Root artifacts: " + summary.rootArtifactCount);
        System.out.println("    Derived artifacts: " + summary.derivedArtifactCount);

        System.out.println("\nArtifact types:");
        for (Map.Entry<ArtifactType, Integer> entry : summary.typeCounts.entrySet()) {
            System.out.println("    " + entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("\nClusters:");
        System.out.println("    Total clusters: " + clusters.size());

        System.out.println("\nHypotheses:");
        System.out.println("    Total hypotheses: " + hypotheses.size());

        System.out.println("\nReports:");
        for (String reportPath : ReportPaths.getOutputFilenames()) {
            printReportPath(config.getOutputDir().resolve(reportPath));
        }
    }

    private ArtifactSummary summarizeArtifacts(ArtifactReader artifactReader) throws Exception {
        ArtifactSummary summary = new ArtifactSummary();

        artifactReader.forEach(artifact -> {
            summary.totalArtifactCount++;

            if (artifact.isDerived()) {
                summary.derivedArtifactCount++;
            } else {
                summary.rootArtifactCount++;
            }

            ArtifactType type = artifact.getType();
            summary.typeCounts.put(type, summary.typeCounts.getOrDefault(type, 0) + 1);
        });

        return summary;
    }

    private void printReportPath(Path path) {
        System.out.println("    " + path);
    }

    private static class ArtifactSummary {
        private long totalArtifactCount;
        private long rootArtifactCount;
        private long derivedArtifactCount;
        private final Map<ArtifactType, Integer> typeCounts = new EnumMap<>(ArtifactType.class);
    }
}