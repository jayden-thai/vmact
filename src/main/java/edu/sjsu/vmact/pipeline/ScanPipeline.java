package edu.sjsu.vmact.pipeline;

import java.nio.file.Path;
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
        System.out.println("\nBeginning Extraction...\n");

        // Extraction Stage
        Stopwatch stageWatch = Stopwatch.startNew();

        Path rootArtifactsPath = config.getOutputDir().resolve("root-artifacts.ndjson");

        try (ArtifactWriter artifactWriter = new NdjsonArtifactWriter(rootArtifactsPath)) {
            List<EvidenceSource> evidenceSources = config.getEvidenceSources();
            for (int i = 1; i <= evidenceSources.size(); i++) {
                EvidenceSource source = evidenceSources.get(i - 1);
                System.out.println("    Extracting " + source.getName() + " (" + i + "/" + evidenceSources.size() + ")");
                for(int k = 1; k <= extractors.size(); k++) {
                    Extractor extractor = extractors.get(k - 1);
                    if (extractor.supports(source)) {
                        Stopwatch extractionWatch = Stopwatch.startNew();
                        extractor.extract(source, config, artifactWriter);
                        System.out.println("      (" + k + "/" + extractors.size() + ") " + extractor.getClass().getSimpleName()
                                + " completed in " + extractionWatch.elapsedText());
                    } else {
                        System.out.println("      (" + k + "/" + extractors.size() + ") " + extractor.getClass().getSimpleName()
                                + " skipped due to incompatibility with " + source.getType());
                    }
                }
                System.out.println();
            }

            System.out.println("    Retained root artifacts: " + artifactWriter.getWrittenCount() + "\n");
        }

        System.out.println("Total extraction time: " + stageWatch.elapsedText() + "\n");

        // Detection Stage
        stageWatch = Stopwatch.startNew();
        System.out.println("Beginning Detection... (@ " + totalWatch.elapsedText() + ")\n");

        Path currentArtifactsPath = rootArtifactsPath;

        int detectorStage = 1;
        for (int i = 1; i <= detectors.size(); i++) {
            Stopwatch detectionWatch = Stopwatch.startNew();
            Detector detector = detectors.get(i - 1);

            Path nextArtifactsPath = config.getOutputDir().resolve(
                    "artifacts-stage-" + detectorStage + "-"
                            + detector.getClass().getSimpleName() + ".ndjson"
            );

            try (
                ArtifactReader inputArtifacts = new NdjsonArtifactReader(currentArtifactsPath);
                ArtifactWriter outputArtifacts = new NdjsonArtifactWriter(nextArtifactsPath);
            ) {
                detector.detect(inputArtifacts, outputArtifacts, config);
            }

            System.out.println("        (" + i + "/" + detectors.size() + ") " + detector.getClass().getSimpleName()
                    + " completed in " + detectionWatch.elapsedText() + "\n");
            currentArtifactsPath = nextArtifactsPath;
            detectorStage++;
        }

        System.out.println("Total detection time: " + stageWatch.elapsedText() + "\n");

        // Correlation Stage
        stageWatch = Stopwatch.startNew();
        System.out.println("Starting Correlation... (@ " + totalWatch.elapsedText() + ")\n");

        List<Cluster> clusters = new ArrayList<>();

        try (
            ArtifactReader artifactReader = new NdjsonArtifactReader(currentArtifactsPath);
        ) {
            for (int i = 1; i <= correlators.size(); i++) {
                Stopwatch correlationWatch = Stopwatch.startNew();
                Correlator correlator = correlators.get(i - 1);

                clusters.addAll(correlator.correlate(artifactReader, config));

                System.out.println("    (" + i + "/" + correlators.size() + ") " + correlator.getClass().getSimpleName() 
                        + " completed in " + correlationWatch.elapsedText());
            }
        }

        System.out.println("\nTotal correlation time: " + stageWatch.elapsedText() + "\n");

        // Hypothesis Generation Stage
        stageWatch = Stopwatch.startNew();
        System.out.println("Starting Hypothesis Generation... (@ " + totalWatch.elapsedText() + ")\n");

        List<Hypothesis> hypotheses = new ArrayList<>();
        
        for (int i = 1; i <= hypothesisGenerators.size(); i++) {
            Stopwatch hypothesizeWatch = Stopwatch.startNew();
            HypothesisGenerator hypothesisGenerator = hypothesisGenerators.get(i - 1);

            hypotheses.addAll(hypothesisGenerator.generate(clusters, config));
            
            System.out.println("    (" + i + "/" + hypothesisGenerators.size() + ") " + hypothesisGenerator.getClass().getSimpleName() 
                    + " completed in " + hypothesizeWatch.elapsedText());
        }

        System.out.println("\nTotal hypothesis generation time: " + stageWatch.elapsedText());

        // Reporting Stage
        stageWatch = Stopwatch.startNew();

        for (Reporter reporter : reporters) {
            try (ArtifactReader artifactReader = new NdjsonArtifactReader(currentArtifactsPath)) {
                reporter.report(artifactReader, clusters, hypotheses, config);
            }
        }

        System.out.println("\nReporting completed in " + stageWatch.elapsedText());
        System.out.println("\nSuccess! Total scan completed in " + totalWatch.elapsedText() + "\n");
    }
}
