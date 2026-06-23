package edu.sjsu.vmact.model;

public class ScoreComponent {
    private final String label;
    private final double delta;
    private final String explanation;

    public ScoreComponent(String label, double delta, String explanation) {
        this.label = label;
        this.delta = delta;
        this.explanation = explanation;
    }

    public String getLabel() {
        return label;
    }

    public double getDelta() {
        return delta;
    }

    public String getExplanation() {
        return explanation;
    }

    @Override
    public String toString() {
        return "ScoreComponent{" +
                "label='" + label + '\'' +
                ", delta=" + delta +
                ", explanation='" + explanation + '\'' +
                '}';
    }
}
