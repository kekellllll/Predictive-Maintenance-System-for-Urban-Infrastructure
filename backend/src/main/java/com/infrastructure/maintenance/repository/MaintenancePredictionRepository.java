package com.infrastructure.maintenance.repository;

import com.infrastructure.maintenance.model.MaintenancePrediction;
import com.infrastructure.maintenance.model.InfrastructureAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenancePredictionRepository extends JpaRepository<MaintenancePrediction, Long> {

    List<MaintenancePrediction> findByAsset(InfrastructureAsset asset);

    List<MaintenancePrediction> findByRiskLevel(MaintenancePrediction.RiskLevel riskLevel);

    @Query("SELECT p FROM MaintenancePrediction p WHERE p.asset = :asset ORDER BY p.predictionDate DESC")
    List<MaintenancePrediction> findLatestPredictionsByAsset(@Param("asset") InfrastructureAsset asset);

    @Query("SELECT p FROM MaintenancePrediction p WHERE p.predictedFailureDate BETWEEN :startDate AND :endDate ORDER BY p.predictedFailureDate ASC")
    List<MaintenancePrediction> findPredictionsInDateRange(@Param("startDate") LocalDateTime startDate, 
                                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM MaintenancePrediction p WHERE p.confidenceScore >= :minConfidence ORDER BY p.confidenceScore DESC")
    List<MaintenancePrediction> findHighConfidencePredictions(@Param("minConfidence") Double minConfidence);

    Optional<MaintenancePrediction> findTopByAssetOrderByPredictionDateDesc(InfrastructureAsset asset);
}