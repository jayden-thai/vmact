package edu.sjsu.vmact.report;

import java.util.List;

import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.model.Cluster;
import edu.sjsu.vmact.pipeline.ScanConfig;

public class ConsoleReporter implements Reporter{
    @Override
    public void report(List<Artifact> artifacts, List<Cluster> clusters, ScanConfig config) {
        System.out.println("Scan complete.");
        System.out.println("Artifacts found: " + artifacts.size());
        System.out.println("Clusters created: " + clusters.size());

        System.out.println("\nArtifacts:");
        for (Artifact artifact : artifacts) {
            System.out.println("    " + artifact);
        }

        System.out.println("\nClusters:");
        for (Cluster cluster : clusters) {
            System.out.println("    " + cluster);
        }
    }
}
