package edu.sjsu.vmact.report;

import java.util.List;

public final class ReportPaths {
    public static final String ROOT_ARTIFACTS_NDJSON = "root-artifacts.ndjson";
    public static final String FINAL_ARTIFACTS_NDJSON = "artifacts.ndjson";

    public static final String ARTIFACTS_CSV = "artifacts.csv";
    public static final String CLUSTERS_CSV = "clusters.csv";
    public static final String HYPOTHESES_CSV = "hypotheses.csv";
    public static final String REPORT_MD = "report.md";

    private ReportPaths() {  
    }

    public static List<String> getOutputFilenames() {
        return List.of(ARTIFACTS_CSV, CLUSTERS_CSV, HYPOTHESES_CSV, REPORT_MD);
    }
}
