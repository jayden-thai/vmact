package edu.sjsu.vmact.model;

import java.util.Collections;
import java.util.List;

public class Hypothesis {
    private final String id;
    private final String title;
    private final String claim;
    private final ActivityType activityType;
    private final ScoreBreakdown scoreBreakdown;
    private final List<String> supportingClusterIds;
    private final List<Subclaim> subclaims;
    private final List<String> sourceNames;
    private final List<SourceType> sourceTypes;
    private final List<String> producerNames;
    private final List<String> caveats;
    private final List<String> alternativeExplanations;
    private final List<RuleId> ruleIds;

    public Hypothesis(
        String id,
        String title,
        String claim,
        ActivityType activityType,
        double confidence,
        List<String> supportingClusterIds,
        List<Subclaim> subclaims,
        List<String> sourceNames,
        List<SourceType> sourceTypes,
        List<String> producerNames,
        List<String> caveats,
        List<String> alternativeExplanations,
        List<RuleId> ruleIds
    ) {
        this.id = id;
        this.title = title;
        this.claim = claim;
        this.activityType = activityType;
        this.scoreBreakdown = ScoreBreakdown.fromScore(
            confidence, 
            OverclaimRisk.MODERATE, 
            "This initial score is a rule-based evidentiary support value assigned by the current hypothesis template."

        );
        this.supportingClusterIds = List.copyOf(supportingClusterIds);
        this.subclaims = List.copyOf(subclaims);
        this.sourceNames = List.copyOf(sourceNames);
        this.sourceTypes = List.copyOf(sourceTypes);
        this.producerNames = List.copyOf(producerNames);
        this.caveats = List.copyOf(caveats);
        this.alternativeExplanations = List.copyOf(alternativeExplanations);
        this.ruleIds = List.copyOf(ruleIds);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getClaim() {
        return claim;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public double getConfidence() {
        return scoreBreakdown.getFinalScore();
    }

    public OverclaimRisk getOverclaimRisk() {
        return scoreBreakdown.getOverclaimRisk();
    }

    public SupportLevel getSupportLevel() {
        return scoreBreakdown.getSupportLevel();
    }

    public ScoreBreakdown getScoreBreakdown() {
        return scoreBreakdown;
    }

    public List<String> getSupportingClusterIds() {
        return Collections.unmodifiableList(supportingClusterIds);
    }

    public List<Subclaim> getSubclaims() {
        return Collections.unmodifiableList(subclaims);
    }

    public List<String> getSourceNames() {
        return Collections.unmodifiableList(sourceNames);
    }

    public List<SourceType> getSourceTypes() {
        return Collections.unmodifiableList(sourceTypes);
    }

    public List<String> getProducerNames() {
        return Collections.unmodifiableList(producerNames);
    }

    public List<String> getCaveats() {
        return Collections.unmodifiableList(caveats);
    }

    public List<String> getAlternativeExplanations() {
        return Collections.unmodifiableList(alternativeExplanations);
    }
    
    public List<RuleId> getRuleIds() {
        return Collections.unmodifiableList(ruleIds);
    }

    @Override
    public String toString() {
        return "Hypothesis{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", claim='" + claim + '\'' +
                ", activityType='" + activityType + '\'' +
                ", confidence=" + getConfidence() +
                ", supportLevel=" + getSupportLevel() +
                ", overclaimRisk=" + getOverclaimRisk() +
                ", supportingClusterIds=" + supportingClusterIds +
                ", subclaimCount=" + subclaims.size() +
                ", sourceNames=" + sourceNames +
                ", sourceTypes=" + sourceTypes +
                ", producerNames=" + producerNames +
                ", caveats=" + caveats +
                ", alternativeExplanations=" + alternativeExplanations +
                ", ruleIds=" + ruleIds +
                '}';
    }
}
