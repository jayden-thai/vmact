package edu.sjsu.vmact.model;

public enum SupportLevel {
    WEAK,
    MODERATE,
    STRONG,
    VERY_STRONG;

    public static SupportLevel fromScore(double score) {
        if (score < 0.40) {
            return WEAK;
        }

        if (score < 0.60) {
            return MODERATE;
        }

        if (score < 0.80) {
            return STRONG;
        }

        return VERY_STRONG;
    }
}
