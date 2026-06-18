package edu.sjsu.vmact.pipeline;

import java.util.ArrayList;
import java.util.List;

import edu.sjsu.vmact.correlate.Correlator;
import edu.sjsu.vmact.detect.Detector;
import edu.sjsu.vmact.extract.Extractor;
import edu.sjsu.vmact.hypothesize.HypothesisGenerator;
import edu.sjsu.vmact.model.Artifact;
import edu.sjsu.vmact.model.Cluster;
import edu.sjsu.vmact.model.EvidenceSource;
import edu.sjsu.vmact.model.Hypothesis;
import edu.sjsu.vmact.report.Reporter;

public class ScanPipeline {
    private final ScanConfig config;
    private final List<Extractor> extractors;
    private final List<Detector> detectors;
    private final Correlator correlator;
    private final HypothesisGenerator hypothesisGenerator;
    private final List<Reporter> reporters;

    public ScanPipeline(
        ScanConfig config,
        List<Extractor> extractors,
        List<Detector> detectors,
        Correlator correlator,
        HypothesisGenerator hypothesisGenerator,
        List<Reporter> reporters
    ) {
        this.config = config;
        this.extractors = extractors;
        this.detectors = detectors;
        this.correlator = correlator;
        this.hypothesisGenerator = hypothesisGenerator;
        this.reporters = reporters;
    }

    public void run() throws Exception {
        System.out.println("Starting scan pipeline...");

        List<Artifact> artifacts = new ArrayList<>();
        List<EvidenceSource> evidenceSources = config.getEvidenceSources();

        for (EvidenceSource source : evidenceSources) {
            for(Extractor extractor : extractors) {
                if (extractor.supports(source)) {
                    List<Artifact> extractedArtifacts = extractor.extract(source, config);
                    artifacts.addAll(extractedArtifacts);
                }
            }
        }

        for (Detector detector : detectors) {
            artifacts = detector.detect(artifacts, config);
        }

        List<Cluster> clusters = correlator.correlate(artifacts, config);

        List<Hypothesis> hypotheses = hypothesisGenerator.generate(artifacts, clusters, config);

        for (Reporter reporter : reporters) {
            reporter.report(artifacts, clusters, hypotheses, config);
        }
    }
}
