package com.infrastructure.maintenance.service;

import com.infrastructure.maintenance.model.SensorData;
import com.infrastructure.maintenance.model.InfrastructureAsset;
import com.infrastructure.maintenance.repository.InfrastructureAssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SensorDataService {

    @Autowired
    private InfrastructureAssetRepository assetRepository;

    @Autowired
    private InfluxDBService influxDBService;

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private PredictionEngineService predictionEngineService;

    public SensorData recordSensorData(String assetId, String sensorId, String sensorType, 
                                     Double value, String unit) {
        Optional<InfrastructureAsset> assetOpt = assetRepository.findByAssetId(assetId);
        
        if (assetOpt.isEmpty()) {
            throw new RuntimeException("Asset not found: " + assetId);
        }

        InfrastructureAsset asset = assetOpt.get();
        SensorData sensorData = new SensorData(sensorId, sensorType, value, unit, asset);
        sensorData.setTimestamp(LocalDateTime.now());

        // Validate sensor data quality
        validateSensorData(sensorData);

        // Store in InfluxDB for time-series analysis
        influxDBService.writeSensorData(sensorData);

        // Publish sensor data event
        eventPublisher.publishSensorDataReceived(sensorData);

        // Check for anomalies and trigger prediction if needed
        checkForAnomalies(sensorData);

        return sensorData;
    }

    public void recordSensorDataBatch(List<SensorData> sensorDataList) {
        // Validate all sensor data
        for (SensorData sensorData : sensorDataList) {
            validateSensorData(sensorData);
        }

        // Batch write to InfluxDB for better performance
        influxDBService.writeSensorDataBatch(sensorDataList);

        // Publish events for each sensor data point
        for (SensorData sensorData : sensorDataList) {
            eventPublisher.publishSensorDataReceived(sensorData);
        }
    }

    private void validateSensorData(SensorData sensorData) {
        // Basic validation
        if (sensorData.getValue() == null) {
            throw new IllegalArgumentException("Sensor value cannot be null");
        }

        // Set quality score based on data validation
        double qualityScore = calculateDataQuality(sensorData);
        sensorData.setQualityScore(qualityScore);

        // Alert if data quality is poor
        if (qualityScore < 0.5) {
            eventPublisher.publishAlert(
                    sensorData.getAsset().getAssetId(),
                    "POOR_DATA_QUALITY",
                    String.format("Poor data quality detected for sensor %s: %.2f", 
                                sensorData.getSensorId(), qualityScore),
                    "WARNING"
            );
        }
    }

    private double calculateDataQuality(SensorData sensorData) {
        double qualityScore = 1.0;

        // Check for reasonable value ranges based on sensor type
        switch (sensorData.getSensorType().toUpperCase()) {
            case "TEMPERATURE":
                if (sensorData.getValue() < -50 || sensorData.getValue() > 100) {
                    qualityScore *= 0.3; // Unrealistic temperature
                }
                break;
            case "VIBRATION":
                if (sensorData.getValue() < 0 || sensorData.getValue() > 1000) {
                    qualityScore *= 0.3; // Unrealistic vibration
                }
                break;
            case "PRESSURE":
                if (sensorData.getValue() < 0) {
                    qualityScore *= 0.1; // Negative pressure is unusual
                }
                break;
            case "HUMIDITY":
                if (sensorData.getValue() < 0 || sensorData.getValue() > 100) {
                    qualityScore *= 0.2; // Humidity should be 0-100%
                }
                break;
        }

        return Math.max(0.0, Math.min(1.0, qualityScore));
    }

    private void checkForAnomalies(SensorData sensorData) {
        // Simple anomaly detection based on thresholds
        boolean isAnomalous = false;
        String alertMessage = "";

        switch (sensorData.getSensorType().toUpperCase()) {
            case "VIBRATION":
                if (sensorData.getValue() > 50) { // High vibration threshold
                    isAnomalous = true;
                    alertMessage = "High vibration detected: " + sensorData.getValue();
                }
                break;
            case "TEMPERATURE":
                if (sensorData.getValue() > 80 || sensorData.getValue() < -20) {
                    isAnomalous = true;
                    alertMessage = "Extreme temperature detected: " + sensorData.getValue();
                }
                break;
            case "STRAIN":
                if (sensorData.getValue() > 100) { // High strain threshold
                    isAnomalous = true;
                    alertMessage = "High strain detected: " + sensorData.getValue();
                }
                break;
        }

        if (isAnomalous) {
            // Publish alert
            eventPublisher.publishAlert(
                    sensorData.getAsset().getAssetId(),
                    "ANOMALY_DETECTED",
                    alertMessage,
                    "WARNING"
            );

            // Trigger predictive analysis for this asset
            predictionEngineService.triggerPredictionForAsset(sensorData.getAsset().getAssetId());
        }
    }

    public List<com.influxdb.query.FluxRecord> getSensorDataHistory(String assetId, String sensorType, String timeRange) {
        return influxDBService.querySensorData(assetId, sensorType, timeRange);
    }

    public List<com.influxdb.query.FluxRecord> getAggregatedSensorData(String assetId, String aggregationWindow) {
        return influxDBService.getAggregatedSensorData(assetId, aggregationWindow);
    }
}