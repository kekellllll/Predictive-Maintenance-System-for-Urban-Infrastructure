package com.infrastructure.maintenance.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_predictions")
public class MaintenancePrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private InfrastructureAsset asset;

    @NotNull
    @Column(name = "prediction_date")
    private LocalDateTime predictionDate;

    @NotNull
    @Column(name = "predicted_failure_date")
    private LocalDateTime predictedFailureDate;

    @NotNull
    @Column(name = "confidence_score")
    private Double confidenceScore; // 0.0 - 1.0

    @NotNull
    @Column(name = "risk_level")
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    @Column(name = "failure_probability")
    private Double failureProbability;

    @Column(name = "recommended_action")
    private String recommendedAction;

    @Column(name = "model_version")
    private String modelVersion;

    @Column(name = "prediction_algorithm")
    private String predictionAlgorithm = "LSTM";

    // Constructors
    public MaintenancePrediction() {}

    public MaintenancePrediction(InfrastructureAsset asset, LocalDateTime predictedFailureDate,
                               Double confidenceScore, RiskLevel riskLevel) {
        this.asset = asset;
        this.predictionDate = LocalDateTime.now();
        this.predictedFailureDate = predictedFailureDate;
        this.confidenceScore = confidenceScore;
        this.riskLevel = riskLevel;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InfrastructureAsset getAsset() { return asset; }
    public void setAsset(InfrastructureAsset asset) { this.asset = asset; }

    public LocalDateTime getPredictionDate() { return predictionDate; }
    public void setPredictionDate(LocalDateTime predictionDate) { this.predictionDate = predictionDate; }

    public LocalDateTime getPredictedFailureDate() { return predictedFailureDate; }
    public void setPredictedFailureDate(LocalDateTime predictedFailureDate) { this.predictedFailureDate = predictedFailureDate; }

    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public Double getFailureProbability() { return failureProbability; }
    public void setFailureProbability(Double failureProbability) { this.failureProbability = failureProbability; }

    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public String getPredictionAlgorithm() { return predictionAlgorithm; }
    public void setPredictionAlgorithm(String predictionAlgorithm) { this.predictionAlgorithm = predictionAlgorithm; }

    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}