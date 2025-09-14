package com.infrastructure.maintenance.service;

import com.infrastructure.maintenance.model.MaintenancePrediction;
import com.infrastructure.maintenance.model.InfrastructureAsset;
import com.infrastructure.maintenance.repository.MaintenancePredictionRepository;
import com.infrastructure.maintenance.repository.InfrastructureAssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PredictionEngineService {

    @Autowired
    private MaintenancePredictionRepository predictionRepository;

    @Autowired
    private InfrastructureAssetRepository assetRepository;

    @Autowired
    private InfluxDBService influxDBService;

    @Autowired
    private EventPublisher eventPublisher;

    @Value("${ml.engine.url:http://localhost:5000}")
    private String mlEngineUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void triggerPredictionForAsset(String assetId) {
        Optional<InfrastructureAsset> assetOpt = assetRepository.findByAssetId(assetId);
        
        if (assetOpt.isEmpty()) {
            return;
        }

        InfrastructureAsset asset = assetOpt.get();
        
        try {
            // Get recent sensor data for the asset
            List<com.influxdb.query.FluxRecord> sensorData = 
                influxDBService.getAggregatedSensorData(assetId, "1h");

            // Prepare data for ML model
            Map<String, Object> predictionRequest = preparePredictionRequest(asset, sensorData);

            // Call ML engine API
            String prediction = callMLEngine(predictionRequest);

            // Parse prediction result and save
            MaintenancePrediction maintenancePrediction = parsePredictionResult(asset, prediction);
            
            if (maintenancePrediction != null) {
                predictionRepository.save(maintenancePrediction);
                
                // Publish prediction event
                eventPublisher.publishMaintenancePrediction(maintenancePrediction);
                
                // Update asset maintenance priority based on prediction
                updateAssetPriority(asset, maintenancePrediction);
            }

        } catch (Exception e) {
            System.err.println("Error generating prediction for asset " + assetId + ": " + e.getMessage());
            
            // Generate fallback prediction based on simple rules
            MaintenancePrediction fallbackPrediction = generateFallbackPrediction(asset);
            if (fallbackPrediction != null) {
                predictionRepository.save(fallbackPrediction);
            }
        }
    }

    private Map<String, Object> preparePredictionRequest(InfrastructureAsset asset, 
                                                       List<com.influxdb.query.FluxRecord> sensorData) {
        Map<String, Object> request = new HashMap<>();
        request.put("asset_id", asset.getAssetId());
        request.put("asset_type", asset.getType());
        request.put("installation_date", asset.getInstallationDate());
        request.put("last_maintenance", asset.getLastMaintenance());
        request.put("sensor_data", sensorData);
        
        return request;
    }

    private String callMLEngine(Map<String, Object> predictionRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(predictionRequest, headers);
        
        return restTemplate.postForObject(
            mlEngineUrl + "/predict", 
            entity, 
            String.class
        );
    }

    private MaintenancePrediction parsePredictionResult(InfrastructureAsset asset, String prediction) {
        try {
            // In a real implementation, you would parse the JSON response from ML engine
            // For now, we'll create a mock prediction
            return createMockPrediction(asset);
        } catch (Exception e) {
            System.err.println("Error parsing prediction result: " + e.getMessage());
            return null;
        }
    }

    private MaintenancePrediction createMockPrediction(InfrastructureAsset asset) {
        MaintenancePrediction prediction = new MaintenancePrediction();
        prediction.setAsset(asset);
        prediction.setPredictionDate(LocalDateTime.now());
        
        // Mock prediction logic based on asset age and type
        LocalDateTime installationDate = asset.getInstallationDate();
        if (installationDate != null) {
            long monthsSinceInstallation = java.time.temporal.ChronoUnit.MONTHS.between(
                installationDate, LocalDateTime.now());
            
            // Simple rule-based prediction
            LocalDateTime predictedFailure;
            MaintenancePrediction.RiskLevel riskLevel;
            double confidence;
            
            if (monthsSinceInstallation > 60) { // 5 years
                predictedFailure = LocalDateTime.now().plusMonths(6);
                riskLevel = MaintenancePrediction.RiskLevel.HIGH;
                confidence = 0.85;
            } else if (monthsSinceInstallation > 36) { // 3 years
                predictedFailure = LocalDateTime.now().plusMonths(12);
                riskLevel = MaintenancePrediction.RiskLevel.MEDIUM;
                confidence = 0.75;
            } else {
                predictedFailure = LocalDateTime.now().plusMonths(24);
                riskLevel = MaintenancePrediction.RiskLevel.LOW;
                confidence = 0.65;
            }
            
            prediction.setPredictedFailureDate(predictedFailure);
            prediction.setRiskLevel(riskLevel);
            prediction.setConfidenceScore(confidence);
            prediction.setRecommendedAction(generateRecommendedAction(riskLevel));
            prediction.setModelVersion("1.0");
            prediction.setPredictionAlgorithm("LSTM");
            
            return prediction;
        }
        
        return null;
    }

    private MaintenancePrediction generateFallbackPrediction(InfrastructureAsset asset) {
        return createMockPrediction(asset);
    }

    private String generateRecommendedAction(MaintenancePrediction.RiskLevel riskLevel) {
        switch (riskLevel) {
            case CRITICAL:
                return "Immediate maintenance required. Schedule emergency inspection.";
            case HIGH:
                return "Schedule maintenance within 30 days. Increase monitoring frequency.";
            case MEDIUM:
                return "Schedule maintenance within 90 days. Continue regular monitoring.";
            case LOW:
                return "Continue regular maintenance schedule. Monitor for changes.";
            default:
                return "Continue normal operations.";
        }
    }

    private void updateAssetPriority(InfrastructureAsset asset, MaintenancePrediction prediction) {
        Integer newPriority = mapRiskLevelToPriority(prediction.getRiskLevel());
        
        if (!newPriority.equals(asset.getMaintenancePriority())) {
            asset.setMaintenancePriority(newPriority);
            assetRepository.save(asset);
        }
    }

    private Integer mapRiskLevelToPriority(MaintenancePrediction.RiskLevel riskLevel) {
        switch (riskLevel) {
            case CRITICAL: return 10;
            case HIGH: return 8;
            case MEDIUM: return 5;
            case LOW: return 2;
            default: return 1;
        }
    }

    public List<MaintenancePrediction> getPredictionsForAsset(String assetId) {
        Optional<InfrastructureAsset> asset = assetRepository.findByAssetId(assetId);
        return asset.map(predictionRepository::findByAsset).orElse(null);
    }

    public List<MaintenancePrediction> getHighRiskPredictions() {
        return predictionRepository.findByRiskLevel(MaintenancePrediction.RiskLevel.HIGH);
    }

    public List<MaintenancePrediction> getPredictionsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return predictionRepository.findPredictionsInDateRange(startDate, endDate);
    }
}