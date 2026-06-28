package edu.sjsu.vmact.pipeline;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import edu.sjsu.vmact.correlate.Correlator;
import edu.sjsu.vmact.detect.Detector;
import edu.sjsu.vmact.extract.ArtifactReader;
import edu.sjsu.vmact.extract.ArtifactWriter;
import edu.sjsu.vmact.extract.Extractor;
import edu.sjsu.vmact.extract.NdjsonArtifactReader;
import edu.sjsu.vmact.extract.NdjsonArtifactWriter;
import edu.sjsu.vmact.hypothesize.HypothesisGenerator;
import edu.sjsu.vmact.model.Cluster;
import edu.sjsu.vmact.model.EvidenceSource;
import edu.sjsu.vmact.model.Hypothesis;
import edu.sjsu.vmact.report.ReportPaths;
import edu.sjsu.vmact.report.Reporter;
import edu.sjsu.vmact.util.Stopwatch;

public class ScanPipeline {
    private final ScanConfig config;
    private final List<Extractor> extractors;
    private final List<Detector> detectors;
    private final List<Correlator> correlators;
    private final List<HypothesisGenerator> hypothesisGenerators;
    private final List<Reporter> reporters;

    public ScanPipeline(
        ScanConfig config,
        List<Extractor> extractors,
        List<Detector> detectors,
        List<Correlator> correlators,
        List<HypothesisGenerator> hypothesisGenerators,
        List<Reporter> reporters
    ) {
        this.config = config;
        this.extractors = extractors;
        this.detectors = detectors;
        this.correlators = correlators;
        this.hypothesisGenerators = hypothesisGenerators;
        this.reporters = reporters;
    }

    public void run() throws Exception {
        Stopwatch totalWatch = Stopwatch.startNew();
        List<ScanMetric> metrics = new ArrayList<>();
        System.out.println("\nBeginning Extraction...\n");

        // Extraction Stage
        Stopwatch stageWatch = Stopwatch.startNew();

        Path rootArtifactsPath = config.getOutputDir().resolve("root-artifacts.ndjson");
        long rootArtifactCount;

        try (ArtifactWriter artifactWriter = new NdjsonArtifactWriter(rootArtifactsPath)) {
            List<EvidenceSource> evidenceSources = config.getEvidenceSources();
            for (int i = 1; i <= evidenceSources.size(); i++) {
                EvidenceSource source = evidenceSources.get(i - 1);
                System.out.println("    Extracting " + source.getName() + " (" + i + "/" + evidenceSources.size() + ")");
                for(int k = 1; k <= extractors.size(); k++) {
                    Extractor extractor = extractors.get(k - 1);
                    if (extractor.supports(source)) {
                        Stopwatch extractionWatch = Stopwatch.startNew();
                        long beforeCount = artifactWriter.getWrittenCount();

                        extractor.extract(source, config, artifactWriter);

                        long producedCount = artifactWriter.getWrittenCount() - beforeCount;

                        metrics.add(new ScanMetric(
                                "extraction",
                                extractor.getClass().getSimpleName(),
                                extractionWatch.elapsedMillis(),
                                producedCount,
                                "source=" + source.getName() + ";sourceType=" + source.getType()
                        ));

                        System.out.println("      (" + k + "/" + extractors.size() + ") " + extractor.getClass().getSimpleName()
                                + " completed in " + extractionWatch.elapsedText()
                                + " and retained " + producedCount + " artifact(s)");
                    } else {
                        System.out.println("      (" + k + "/" + extractors.size() + ") " + extractor.getClass().getSimpleName()
                                + " skipped due to incompatibility with " + source.getType());
                    }
                }
                System.out.println();
            }

            rootArtifactCount = artifactWriter.getWrittenCount();
            System.out.println("    Retained root artifacts: " + rootArtifactCount + "\n");
        }

        metrics.add(new ScanMetric(
            "extraction_total",
            "all_extractors",
            stageWatch.elapsedMillis(),
            rootArtifactCount,
            "root artifacts retained"
        ));

        System.out.println("Total extraction time: " + stageWatch.elapsedText() + "\n");

        // Detection Stage
        stageWatch = Stopwatch.startNew();
        System.out.println("Beginning Detection... (@ " + totalWatch.elapsedText() + ")\n");

        Path currentArtifactsPath = rootArtifactsPath;

        int detectorStage = 1;
        long totalDetectorOutputCount = 0;

        for (int i = 1; i <= detectors.size(); i++) {
            Stopwatch detectionWatch = Stopwatch.startNew();
            Detector detector = detectors.get(i - 1);

            Path nextArtifactsPath = config.getOutputDir().resolve(
                    "artifacts-stage-" + detectorStage + "-"
                            + detector.getClass().getSimpleName() + ".ndjson"
            );

            long detectorOutputCount;

            try (
                ArtifactReader inputArtifacts = new NdjsonArtifactReader(currentArtifactsPath);
                ArtifactWriter outputArtifacts = new NdjsonArtifactWriter(nextArtifactsPath);
            ) {
                detector.detect(inputArtifacts, outputArtifacts, config);
                detectorOutputCount = outputArtifacts.getWrittenCount();
                totalDetectorOutputCount += detectorOutputCount;
            }

            metrics.add(new ScanMetric(
                "detection",
                detector.getClass().getSimpleName(),
                detectionWatch.elapsedMillis(),
                detectorOutputCount, 
                "output=" + nextArtifactsPath.getFileName()
            ));

            System.out.println("        (" + i + "/" + detectors.size() + ") " + detector.getClass().getSimpleName()
                    + " completed in " + detectionWatch.elapsedText() + "\n");
            currentArtifactsPath = nextArtifactsPath;
            detectorStage++;
        }

        Path finalArtifactsPath = config.getOutputDir().resolve(ReportPaths.FINAL_ARTIFACTS_NDJSON);
        Files.copy(currentArtifactsPath, finalArtifactsPath, StandardCopyOption.REPLACE_EXISTING);

        metrics.add(new ScanMetric(
                "detection_total",
                "all_detectors",
                stageWatch.elapsedMillis(),
                totalDetectorOutputCount,
                "finalArtifactFile=" + finalArtifactsPath.getFileName()
        ));

        System.out.println("Total detection time: " + stageWatch.elapsedText() + "\n");

        // Correlation Stage
        stageWatch = Stopwatch.startNew();
        System.out.println("Starting Correlation... (@ " + totalWatch.elapsedText() + ")\n");

        List<Cluster> clusters = new ArrayList<>();

        for (int i = 1; i <= correlators.size(); i++) {
            Stopwatch correlationWatch = Stopwatch.startNew();
            Correlator correlator = correlators.get(i - 1);

            int beforeClusterCount = clusters.size();

            try (ArtifactReader artifactReader = new NdjsonArtifactReader(finalArtifactsPath)) {
                clusters.addAll(correlator.correlate(artifactReader, config));
            }

            int producedClusterCount = clusters.size() - beforeClusterCount;

            metrics.add(new ScanMetric(
                    "correlation",
                    correlator.getClass().getSimpleName(),
                    correlationWatch.elapsedMillis(),
                    producedClusterCount,
                    "clusters produced"
            ));

            System.out.println("    (" + i + "/" + correlators.size() + ") " + correlator.getClass().getSimpleName() 
                        + " completed in " + correlationWatch.elapsedText());
        }

        metrics.add(new ScanMetric(
                "correlation_total",
                "all_correlators",
                stageWatch.elapsedMillis(),
                clusters.size(),
                "completed cluster production"
        ));

        System.out.println("\nTotal correlation time: " + stageWatch.elapsedText() + "\n");

        // Hypothesis Generation Stage
        stageWatch = Stopwatch.startNew();
        System.out.println("Starting Hypothesis Generation... (@ " + totalWatch.elapsedText() + ")\n");

        List<Hypothesis> hypotheses = new ArrayList<>();
        
        for (int i = 1; i <= hypothesisGenerators.size(); i++) {
            Stopwatch hypothesizeWatch = Stopwatch.startNew();
            HypothesisGenerator hypothesisGenerator = hypothesisGenerators.get(i - 1);

            int beforeHypothesisCount = hypotheses.size();

            hypotheses.addAll(hypothesisGenerator.generate(clusters, config));

            int producedHypothesisCount = hypotheses.size() - beforeHypothesisCount;

            metrics.add(new ScanMetric(
                    "hypothesis_generation", 
                    hypothesisGenerator.getClass().getSimpleName(), 
                    hypothesizeWatch.elapsedMillis(), 
                    producedHypothesisCount, 
                    "hypotheses produced"
            ));
            
            System.out.println("    (" + i + "/" + hypothesisGenerators.size() + ") " + hypothesisGenerator.getClass().getSimpleName() 
                    + " completed in " + hypothesizeWatch.elapsedText());
        }

        metrics.add(new ScanMetric(
                "hypothesis_generation_total", 
                "all_hypothesis_generators", 
                stageWatch.elapsedMillis(), 
                hypotheses.size(), 
                "completed hypotheses production"
        ));

        System.out.println("\nTotal hypothesis generation time: " + stageWatch.elapsedText());

        // Reporting Stage
        stageWatch = Stopwatch.startNew();

        for (Reporter reporter : reporters) {
            Stopwatch reporterWatch = Stopwatch.startNew();

            try (ArtifactReader artifactReader = new NdjsonArtifactReader(finalArtifactsPath)) {
                reporter.report(artifactReader, clusters, hypotheses, config);
            }

            metrics.add(new ScanMetric(
                "reporting", 
                reporter.getClass().getSimpleName(), 
                reporterWatch.elapsedMillis(), 
                -1, 
                "report generated"
            ));
        }

        metrics.add(new ScanMetric(
                "total",
                "scan",
                totalWatch.elapsedMillis(),
                -1,
                "complete scan runtime"
        ));

        new ScanMetricsWriter().write(metrics, config.getOutputDir());

        System.out.println("\nReporting completed in " + stageWatch.elapsedText());
        System.out.println("\nSuccess! Total scan completed in " + totalWatch.elapsedText() + "\n");
    }
}
