package edu.sjsu.vmact.model;

import java.util.Collections;
import java.util.List;

public class ScoreBreakdown {
    private final double finalScore;
    private final SupportLevel supportLevel;
    private final OverclaimRisk overclaimRisk;
    private final List<ScoreComponent> components;
    private final String explanation;

    public ScoreBreakdown(
            double finalScore,
            SupportLevel supportLevel,
            OverclaimRisk overclaimRisk,
            List<ScoreComponent> components,
            String explanation
    ) {
        this.finalScore = clamp(finalScore);
        this.supportLevel = supportLevel;
        this.overclaimRisk = overclaimRisk;
        this.components = List.copyOf(components);
        this.explanation = explanation;
    }

    public static ScoreBreakdown fromScore(
            double score,
            OverclaimRisk overclaimRisk,
            String explanation
    ) {
        double clampedScore = clamp(score);

        return new ScoreBreakdown(
                clampedScore,
                SupportLevel.fromScore(clampedScore),
                overclaimRisk,
                List.of(new ScoreComponent(
                        "Initial support score",
                        clampedScore,
                        explanation
                )),
                explanation
        );
    }

    public static ScoreBreakdown fromComponents(
    List<ScoreComponent> components,
    OverclaimRisk overclaimRisk,
    String explanation
) {
    double total = 0.0;

    for (ScoreComponent component : components) {
        total += component.getDelta();
    }

    double clampedScore = clamp(total);

    return new ScoreBreakdown(
        clampedScore,
        SupportLevel.fromScore(clampedScore),
        overclaimRisk,
        components,
        explanation
    );
}

    public double getFinalScore() {
        return finalScore;
    }

    public SupportLevel getSupportLevel() {
        return supportLevel;
    }

    public OverclaimRisk getOverclaimRisk() {
        return overclaimRisk;
    }

    public List<ScoreComponent> getComponents() {
        return Collections.unmodifiableList(components);
    }

    public String getExplanation() {
        return explanation;
    }

    private static double clamp(double score) {
        if (score < 0.0) {
            return 0.0;
        }

        if (score > 0.95) {
            return 0.95;
        }

        return score;
    }

    @Override
    public String toString() {
        return "ScoreBreakdown{" +
                "finalScore=" + finalScore +
                ", supportLevel=" + supportLevel +
                ", overclaimRisk=" + overclaimRisk +
                ", components=" + components +
                ", explanation='" + explanation + '\'' +
                '}';
    }
}