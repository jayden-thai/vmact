package edu.sjsu.vmact.report;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.model.ArtifactType;
import edu.sjsu.vmact.model.Cluster;
import edu.sjsu.vmact.model.Hypothesis;
import edu.sjsu.vmact.pipeline.ScanConfig;

public class ConsoleReporter implements Reporter{
    @Override
    public void report(
        List<Artifact> artifacts, 
        List<Cluster> clusters, 
        List<Hypothesis> hypotheses, 
        ScanConfig config
    ) {
        long rootArtifactCount = artifacts.stream()
                .filter(artifact -> !artifact.isDerived())
                .count();

        long derivedArtifactCount = artifacts.stream()
                .filter(Artifact::isDerived)
                .count();

        Map<ArtifactType, Integer> typeCounts = countArtifactTypes(artifacts);
        
        System.out.println("\nScan complete.");

        System.out.println("\nArtifacts:");
        System.out.println("    Total normalized artifacts: " + artifacts.size());
        System.out.println("    Root artifacts: " + rootArtifactCount);
        System.out.println("    Derived artifacts: " + derivedArtifactCount);

        System.out.println("\nArtifact types:");
        for (Map.Entry<ArtifactType, Integer> entry: typeCounts.entrySet()) {
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

    private Map<ArtifactType, Integer> countArtifactTypes(List<Artifact> artifacts) {
        Map<ArtifactType, Integer> counts = new EnumMap<>(ArtifactType.class);

        for (Artifact artifact : artifacts) {
            ArtifactType type = artifact.getType();
            counts.put(type, counts.getOrDefault(type, 0) + 1);
        }

        return counts;
    }

    private void printReportPath(Path path) {
        System.out.println("    " + path);
    }
}
